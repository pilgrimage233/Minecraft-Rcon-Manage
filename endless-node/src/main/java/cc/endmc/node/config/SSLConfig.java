package cc.endmc.node.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * SSL 配置类
 * 提供可配置的证书验证策略
 *
 * @author Memory
 */
@Slf4j
@Configuration
public class SSLConfig {

    @Value("${endless.node.ssl.trust-all:false}")
    private boolean trustAll;

    @Value("${endless.node.ssl.trust-store-path:}")
    private String trustStorePath;

    @Value("${endless.node.ssl.trust-store-password:}")
    private String trustStorePassword;

    @Value("${endless.node.ssl.enabled:true}")
    private boolean sslEnabled;

    /**
     * 获取 SSL 上下文
     * 根据配置决定使用信任所有证书还是标准证书验证
     */
    public SSLContext getSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        if (trustAll) {
            log.warn("SSL 配置为信任所有证书，仅建议在开发环境使用");
            sslContext.init(null, getTrustAllManagers(), new SecureRandom());
        } else if (trustStorePath != null && !trustStorePath.isEmpty()) {
            log.info("使用自定义信任库: {}", trustStorePath);
            sslContext.init(null, getCustomTrustManagers(), new SecureRandom());
        } else {
            log.info("使用系统默认 SSL 配置");
            sslContext.init(null, null, null);
        }

        return sslContext;
    }

    /**
     * 获取信任所有证书的 TrustManager
     * 仅用于开发环境
     */
    private TrustManager[] getTrustAllManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        log.debug("跳过客户端证书验证");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        log.debug("跳过服务端证书验证");
                    }
                }
        };
    }

    /**
     * 获取自定义信任库的 TrustManager
     */
    private TrustManager[] getCustomTrustManagers() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = new FileInputStream(trustStorePath)) {
            trustStore.load(is, trustStorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    /**
     * 获取 HostnameVerifier
     */
    public HostnameVerifier getHostnameVerifier() {
        if (trustAll) {
            return (hostname, session) -> {
                log.debug("跳过主机名验证: {}", hostname);
                return true;
            };
        }
        return HttpsURLConnection.getDefaultHostnameVerifier();
    }

    /**
     * 配置 WebSocket 客户端的 SSL
     */
    public void configureWebSocketSSL(org.springframework.web.socket.client.standard.StandardWebSocketClient client) {
        if (!sslEnabled) {
            return;
        }

        try {
            SSLContext sslContext = getSSLContext();
            client.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);
            log.debug("WebSocket SSL 配置完成");
        } catch (Exception e) {
            log.error("配置 WebSocket SSL 失败", e);
        }
    }
}
