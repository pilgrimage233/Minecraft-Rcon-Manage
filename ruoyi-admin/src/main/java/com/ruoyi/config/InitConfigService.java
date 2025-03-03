package com.ruoyi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                copyDefaultConfig(configFile);
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

    private void copyDefaultConfig(String configFile) throws IOException {
        Path targetPath = Paths.get(CONFIG_DIR, configFile);
        if (!Files.exists(targetPath)) {
            // 从classpath复制默认配置文件
            try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
                 OutputStream os = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            log.info("生成默认配置文件: {}", targetPath.toAbsolutePath());
        }
    }
}