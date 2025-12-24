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
import java.util.List;

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
        List<String> jarConfigLines;
        try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            jarConfigLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                jarConfigLines.add(line);
            }
        }

        // 如果本地配置文件不存在，创建不包含endless节点的配置文件
        if (!Files.exists(targetPath)) {
            List<String> filteredLines = filterOutEndlessSection(jarConfigLines);
            Files.write(targetPath, filteredLines);
            log.info("创建新配置文件(不包含endless节点): {}", targetPath.toAbsolutePath());
            return;
        }

        // 如果本地配置文件存在，保持原样，不做任何修改
        log.info("配置文件已存在，保持不变: {}", targetPath.toAbsolutePath());
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