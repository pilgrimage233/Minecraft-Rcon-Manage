package cc.endmc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class InitConfigService {
    private static final Logger log = LoggerFactory.getLogger(InitConfigService.class);
    private static final String CONFIG_DIR = "config";
    private static final String[] CONFIG_FILES = {
            "application.yml",
            "application-druid.yml"
    };

    public void initializeConfigs() {
        try {
            createConfigDirectory();
            for (String configFile : CONFIG_FILES) {
                if ("application.yml".equals(configFile)) {
                    handleApplicationYml(configFile);
                } else {
                    handleOtherConfig(configFile);
                }
            }
        } catch (Exception e) {
            log.error("配置文件初始化失败", e);
            throw new RuntimeException("配置文件初始化失败", e);
        }
    }

    private void createConfigDirectory() throws IOException {
        Path configPath = Paths.get(CONFIG_DIR);
        if (!Files.exists(configPath)) {
            Files.createDirectory(configPath);
            log.info("创建配置目录: {}", configPath.toAbsolutePath());
        }
    }

    private void handleApplicationYml(String configFile) throws IOException {
        Path targetPath = Paths.get(CONFIG_DIR, configFile);

        // 读取jar包中的配置
        List<String> jarConfigLines = readConfigFromJar(configFile);

        if (!Files.exists(targetPath)) {
            // 如果本地配置文件不存在，创建不包含endless节点的配置文件
            List<String> filteredLines = filterOutEndlessSection(jarConfigLines);
            Files.write(targetPath, filteredLines);
            log.info("创建新配置文件(不包含endless节点): {}", targetPath.toAbsolutePath());
        } else {
            // 如果本地配置文件存在，合并新配置
            mergeConfigurations(targetPath, jarConfigLines);
        }
    }

    /**
     * 读取jar包中的配置文件
     */
    private List<String> readConfigFromJar(String configFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 合并配置文件，将jar包中的新配置项添加到外部配置文件中
     */
    private void mergeConfigurations(Path targetPath, List<String> jarConfigLines) throws IOException {
        // 读取现有的外部配置文件
        List<String> existingLines = Files.readAllLines(targetPath);

        // 解析现有配置的结构
        Set<String> existingKeys = parseYamlKeys(existingLines);

        // 从jar配置中提取新的配置项（排除endless节点）
        List<String> jarFilteredLines = filterOutEndlessSection(jarConfigLines);
        Set<String> jarKeys = parseYamlKeys(jarFilteredLines);

        // 找出需要添加的新配置项
        Set<String> newKeys = new HashSet<>(jarKeys);
        newKeys.removeAll(existingKeys);

        if (!newKeys.isEmpty()) {
            // 提取新配置项的内容
            List<String> newConfigLines = extractConfigSections(jarFilteredLines, newKeys);

            if (!newConfigLines.isEmpty()) {
                // 合并配置
                List<String> mergedLines = new ArrayList<>(existingLines);

                // 添加分隔注释
                if (!existingLines.isEmpty() && !existingLines.get(existingLines.size() - 1).trim().isEmpty()) {
                    mergedLines.add("");
                }
                mergedLines.add("# 以下配置项由系统自动添加");
                mergedLines.addAll(newConfigLines);

                // 写回文件
                Files.write(targetPath, mergedLines);
                log.info("配置文件已更新，新增 {} 个配置项: {}", newKeys.size(), targetPath.toAbsolutePath());
                log.debug("新增配置项: {}", newKeys);
            }
        } else {
            log.info("配置文件无需更新: {}", targetPath.toAbsolutePath());
        }
    }

    /**
     * 解析YAML文件的顶级键
     */
    private Set<String> parseYamlKeys(List<String> lines) {
        Set<String> keys = new HashSet<>();
        Pattern keyPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9_-]*):.*");

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                var matcher = keyPattern.matcher(line);
                if (matcher.matches()) {
                    keys.add(matcher.group(1));
                }
            }
        }
        return keys;
    }

    /**
     * 提取指定键的配置段落
     */
    private List<String> extractConfigSections(List<String> lines, Set<String> targetKeys) {
        List<String> result = new ArrayList<>();
        boolean inTargetSection = false;
        String currentKey = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 跳过注释和空行
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                if (inTargetSection) {
                    result.add(line);
                }
                continue;
            }

            // 检查是否是顶级键
            if (!line.startsWith(" ") && !line.startsWith("\t") && line.contains(":")) {
                String key = line.split(":")[0].trim();

                if (targetKeys.contains(key)) {
                    inTargetSection = true;
                    currentKey = key;
                    result.add(line);
                } else {
                    inTargetSection = false;
                    currentKey = null;
                }
            } else if (inTargetSection) {
                // 在目标段落内的缩进行
                result.add(line);
            }
        }

        return result;
    }

    /**
     * 过滤掉endless节点，保留其他配置
     */
    private List<String> filterOutEndlessSection(List<String> lines) {
        List<String> filteredLines = new ArrayList<>();
        boolean inEndlessSection = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // 遇到endless节点，开始跳过
            if (line.trim().startsWith("endless:")) {
                inEndlessSection = true;
                continue;
            }

            // 在endless节点内部，跳过所有缩进的行
            if (inEndlessSection) {
                if (line.startsWith(" ") || line.startsWith("\t") || line.trim().isEmpty()) {
                    // 检查是否是endless节点的最后一行
                    if (i + 1 < lines.size()) {
                        String nextLine = lines.get(i + 1);
                        if (!nextLine.startsWith(" ") && !nextLine.startsWith("\t") && !nextLine.trim().isEmpty()) {
                            inEndlessSection = false;
                        }
                    } else {
                        inEndlessSection = false;
                    }
                    continue;
                } else {
                    inEndlessSection = false;
                }
            }

            // 保留非endless节点的内容
            filteredLines.add(line);
        }

        return filteredLines;
    }

    private void handleOtherConfig(String configFile) throws IOException {
        Path targetPath = Paths.get(CONFIG_DIR, configFile);
        // 只在配置文件不存在时创建
        if (!Files.exists(targetPath)) {
            try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
                 OutputStream os = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            log.info("创建配置文件: {}", targetPath.toAbsolutePath());
        }
    }
}