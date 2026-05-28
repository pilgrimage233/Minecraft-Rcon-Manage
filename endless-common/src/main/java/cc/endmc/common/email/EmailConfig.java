package cc.endmc.common.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "email")
public class EmailConfig {

    private final Map<String, SmtpConfig> smtpConfigs = new HashMap<>();
    private boolean enable = false;
    private String type = "aliyun";
    private String account;
    private String password;
    private String senderName = "Endless";
    private String smtpHost;
    private Integer smtpPort;
    private boolean ssl = true;

    public EmailConfig() {
        SmtpConfig aliyun = new SmtpConfig();
        aliyun.setSmtpHost("smtp.qiye.aliyun.com");
        aliyun.setSmtpPort(465);
        aliyun.setSsl(true);
        smtpConfigs.put("aliyun", aliyun);

        SmtpConfig qq = new SmtpConfig();
        qq.setSmtpHost("smtp.qq.com");
        qq.setSmtpPort(465);
        qq.setSsl(true);
        smtpConfigs.put("qq", qq);

        SmtpConfig netease = new SmtpConfig();
        netease.setSmtpHost("smtp.163.com");
        netease.setSmtpPort(465);
        netease.setSsl(true);
        smtpConfigs.put("163", netease);

        SmtpConfig gmail = new SmtpConfig();
        gmail.setSmtpHost("smtp.gmail.com");
        gmail.setSmtpPort(465);
        gmail.setSsl(true);
        smtpConfigs.put("gmail", gmail);

        SmtpConfig outlook = new SmtpConfig();
        outlook.setSmtpHost("smtp.office365.com");
        outlook.setSmtpPort(587);
        outlook.setSsl(true);
        smtpConfigs.put("outlook", outlook);
    }

    public SmtpConfig getCurrentSmtpConfig() {
        if (smtpHost != null && !smtpHost.isEmpty()) {
            SmtpConfig config = new SmtpConfig();
            config.setSmtpHost(smtpHost);
            config.setSmtpPort(smtpPort);
            config.setSsl(ssl);
            return config;
        }
        return smtpConfigs.getOrDefault(type, smtpConfigs.get("aliyun"));
    }

    @Data
    public static class SmtpConfig {
        private String smtpHost;
        private Integer smtpPort;
        private boolean ssl = true;
    }
}
