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

        // 如果本地配置文件不存在，直接复制jar包中的配置
        if (!Files.exists(targetPath)) {
            Files.write(targetPath, jarConfigLines);
            log.info("创建新配置文件: {}", targetPath.toAbsolutePath());
            return;
        }

        // 如果本地配置文件存在，读取并更新ruoyi节点
        List<String> localLines = Files.readAllLines(targetPath);
        List<String> mergedLines = new ArrayList<>();
        boolean inRuoyiSection = false;
        boolean hasAddedRuoyiSection = false;

        // 从jar包中提取ruoyi节点
        List<String> ruoyiSection = new ArrayList<>();
        boolean isRuoyiSection = false;
        for (String line : jarConfigLines) {
            if (line.trim().startsWith("ruoyi:")) {
                isRuoyiSection = true;
                ruoyiSection.add(line);
            } else if (isRuoyiSection) {
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    ruoyiSection.add(line);
                } else {
                    isRuoyiSection = false;
                }
            }
        }

        // 处理本地配置文件
        for (int i = 0; i < localLines.size(); i++) {
            String line = localLines.get(i);

            // 遇到ruoyi节点，插入jar包中的新ruoyi节点
            if (line.trim().startsWith("ruoyi:")) {
                inRuoyiSection = true;
                if (!hasAddedRuoyiSection) {
                    mergedLines.addAll(ruoyiSection);
                    hasAddedRuoyiSection = true;
                }
                continue;
            }

            // 跳过原有ruoyi节点的内容
            if (inRuoyiSection) {
                if (i + 1 < localLines.size()) {
                    String nextLine = localLines.get(i + 1);
                    if (!nextLine.startsWith(" ") && !nextLine.startsWith("\t") && !nextLine.trim().isEmpty()) {
                        inRuoyiSection = false;
                    }
                } else {
                    inRuoyiSection = false;
                }
                continue;
            }

            // 保留非ruoyi节点的内容
            mergedLines.add(line);

            // 在适当位置插入ruoyi节点（如果还没插入）
            if (!hasAddedRuoyiSection && line.trim().isEmpty()) {
                mergedLines.addAll(ruoyiSection);
                hasAddedRuoyiSection = true;
            }
        }

        // 如果还没有添加ruoyi节点，在文件末尾添加
        if (!hasAddedRuoyiSection) {
            if (!mergedLines.isEmpty() && !mergedLines.get(mergedLines.size() - 1).trim().isEmpty()) {
                mergedLines.add("");
            }
            mergedLines.addAll(ruoyiSection);
        }

        // 写回文件
        Files.write(targetPath, mergedLines);
        log.info("更新配置文件ruoyi节点: {}", targetPath.toAbsolutePath());
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