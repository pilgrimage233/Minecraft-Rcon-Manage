package cc.endmc.server.common.service;

import cc.endmc.server.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: PushEmail <br>
 * Description:
 * date: 2024/1/13 15:49 <br>
 */
@Slf4j
@Component
public class EmailService {
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    @Autowired
    private EmailConfig emailConfig;

    private volatile Session cachedSession;
    private volatile boolean initialized = false;

    public void push(String email, String title, String content) throws ExecutionException, InterruptedException {
        if (!emailConfig.isEnable()) {
            return;
        }

        try {
            Session session = getOrCreateSession();
            MimeMessage message = new MimeMessage(session);
            InternetAddress from = new InternetAddress(emailConfig.getAccount(), emailConfig.getSenderName());
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();
            Transport.send(message);
            log.info("发送成功！");
        } catch (Exception e) {
            log.error("邮件发送失败！异常信息：{}", String.valueOf(e));
        }
    }

    private Session getOrCreateSession() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    cachedSession = createSession();
                    initialized = true;
                }
            }
        }
        return cachedSession;
    }

    private Session createSession() {
        Properties props = buildProperties();
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        props.getProperty("mail.user"),
                        props.getProperty("mail.password")
                );
            }
        });
    }

    private @NotNull Properties buildProperties() {
        Properties props = new Properties();
        EmailConfig.SmtpConfig smtpConfig = emailConfig.getCurrentSmtpConfig();

        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", smtpConfig.getSmtpHost());
        props.setProperty("mail.smtp.port", String.valueOf(smtpConfig.getSmtpPort()));

        if (smtpConfig.isSsl()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.socketFactory.port", String.valueOf(smtpConfig.getSmtpPort()));
        }

        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.from", emailConfig.getAccount());
        props.setProperty("mail.user", emailConfig.getAccount());
        props.setProperty("mail.password", emailConfig.getPassword());

        return props;
    }
}
