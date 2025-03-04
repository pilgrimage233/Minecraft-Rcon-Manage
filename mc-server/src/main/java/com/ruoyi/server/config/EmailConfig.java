package com.ruoyi.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "email")
public class EmailConfig {

    /**
     * 各邮箱提供商的SMTP配置
     */
    private final Map<String, SmtpConfig> smtpConfigs = new HashMap<>();
    /**
     * 是否启用邮件服务
     */
    private boolean enable = false;
    /**
     * 邮件服务提供商类型：aliyun, qq, 163, gmail等
     */
    private String type = "aliyun";
    /**
     * 发件人邮箱账号
     */
    private String account;
    /**
     * 发件人邮箱密码或授权码
     */
    private String password;
    /**
     * 发件人显示名称
     */
    private String senderName = "Minecraft Server";
    /**
     * SMTP服务器地址
     */
    private String smtpHost;
    /**
     * SMTP服务器端口
     */
    private Integer smtpPort;
    /**
     * 是否启用SSL
     */
    private boolean ssl = true;

    public EmailConfig() {
        // 初始化默认的SMTP配置

        // 阿里云企业邮箱
        SmtpConfig aliyun = new SmtpConfig();
        aliyun.setSmtpHost("smtp.qiye.aliyun.com");
        aliyun.setSmtpPort(465);
        aliyun.setSsl(true);
        smtpConfigs.put("aliyun", aliyun);

        // QQ邮箱
        SmtpConfig qq = new SmtpConfig();
        qq.setSmtpHost("smtp.qq.com");
        qq.setSmtpPort(465);
        qq.setSsl(true);
        smtpConfigs.put("qq", qq);

        // 163邮箱
        SmtpConfig netease = new SmtpConfig();
        netease.setSmtpHost("smtp.163.com");
        netease.setSmtpPort(465);
        netease.setSsl(true);
        smtpConfigs.put("163", netease);

        // Gmail
        SmtpConfig gmail = new SmtpConfig();
        gmail.setSmtpHost("smtp.gmail.com");
        gmail.setSmtpPort(465);
        gmail.setSsl(true);
        smtpConfigs.put("gmail", gmail);

        // Outlook/Hotmail
        SmtpConfig outlook = new SmtpConfig();
        outlook.setSmtpHost("smtp.office365.com");
        outlook.setSmtpPort(587);
        outlook.setSsl(true);
        smtpConfigs.put("outlook", outlook);
    }

    /**
     * 获取当前配置的SMTP配置
     */
    public SmtpConfig getCurrentSmtpConfig() {
        // 如果用户配置了SMTP服务器，则使用用户配置
        if (smtpHost != null && !smtpHost.isEmpty()) {
            SmtpConfig config = new SmtpConfig();
            config.setSmtpHost(smtpHost);
            config.setSmtpPort(smtpPort);
            config.setSsl(ssl);
            return config;
        }

        // 否则使用预设的配置
        return smtpConfigs.getOrDefault(type, smtpConfigs.get("aliyun"));
    }

    /**
     * SMTP配置类
     */
    public static class SmtpConfig {
        private String smtpHost;
        private Integer smtpPort;
        private boolean ssl = true;

        public String getSmtpHost() {
            return smtpHost;
        }

        public void setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public Integer getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(Integer smtpPort) {
            this.smtpPort = smtpPort;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }
    }
}