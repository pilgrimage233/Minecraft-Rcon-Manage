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
    @Autowired
    private EmailConfig emailConfig;

    public void push(String email, String title, String content) throws ExecutionException, InterruptedException {
        // 是否开启邮件推送
        if (!emailConfig.isEnable()) {
            return;
        }

        try {
            //设置SSL连接、邮件环境
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            final Properties props = getProperties();
            //建立邮件会话
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                //身份认证
                protected PasswordAuthentication getPasswordAuthentication() {
                    //发件人的账号、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            });
            //建立邮件对象
            MimeMessage message = new MimeMessage(session);
            //设置邮件的发件人
            InternetAddress from = new InternetAddress(emailConfig.getAccount(), emailConfig.getSenderName()); //from 参数,可实现代发，注意：代发容易被收信方拒信或进入垃圾箱。
            message.setFrom(from);
            //设置邮件的收件人
            String[] to = {email};//收件人列表
            InternetAddress[] sendTo = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                //System.out.println("发送到：" + to[i]);
                sendTo[i] = new InternetAddress(to[i]);
            }

            //传入收件人
            message.setRecipients(Message.RecipientType.TO, sendTo);
            //设置邮件的主题
            message.setSubject(title);
            //设置邮件的文本内容
            message.setContent(content, "text/html;charset=UTF-8");
            //设置时间
            message.setSentDate(new Date());
            message.saveChanges();
            //发送邮件
            Transport.send(message);
            log.info("发送成功！");
        } catch (Exception e) {
            log.error("邮件发送失败！异常信息：{}", String.valueOf(e));
        }
        //  log.debug("发送邮件给{}，标题：{}，内容：{}", email, title, content);
    }

    private @NotNull Properties getProperties() {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Properties props = System.getProperties();
        EmailConfig.SmtpConfig smtpConfig = emailConfig.getCurrentSmtpConfig();
        
        //协议
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", smtpConfig.getSmtpHost());
        props.setProperty("mail.smtp.port", String.valueOf(smtpConfig.getSmtpPort()));

        if (smtpConfig.isSsl()) {
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
