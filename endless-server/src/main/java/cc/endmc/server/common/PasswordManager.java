package cc.endmc.server.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordManager {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Autowired
    private Environment env;

    /**
     * 加密（返回格式：BASE64(IV + 密文)）
     */
    public String encrypt(String plaintext) throws Exception {
        byte[] key = loadKeyFromEnv();
        byte[] iv = generateSecureIV();

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(combineIVAndCipher(iv, cipherText));
    }

    /**
     * 解密
     */
    public String decrypt(String encrypted) throws Exception {
        byte[] key = loadKeyFromEnv();
        byte[] combined = Base64.getDecoder().decode(encrypted);

        ByteSplitter splitter = new ByteSplitter(combined)
                .split(GCM_IV_LENGTH); // 分离IV和密文

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH, splitter.getFirstPart()));

        return new String(cipher.doFinal(splitter.getSecondPart()), StandardCharsets.UTF_8);
    }

    // 安全密钥加载（通过环境变量）
    private byte[] loadKeyFromEnv() {
        String keyBase64 = env.getProperty("encrypt.key");
        if (keyBase64 == null) {
            throw new SecurityException("加密密钥未配置");
        }
        return Base64.getDecoder().decode(keyBase64);
    }

    // 辅助方法
    private byte[] generateSecureIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] combineIVAndCipher(byte[] iv, byte[] cipherText) {
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        return combined;
    }

    // 字节分割工具类
    private class ByteSplitter {
        private final byte[] data;
        private final int splitPosition;

        public ByteSplitter(byte[] data) {
            this.data = data;
            this.splitPosition = -1;
        }

        private ByteSplitter(byte[] data, int splitAt) {
            this.data = data;
            this.splitPosition = splitAt;
        }

        public ByteSplitter split(int splitAt) {
            return new ByteSplitter(data, splitAt);
        }

        public byte[] getFirstPart() {
            if (splitPosition <= 0) throw new IllegalStateException();
            byte[] part = new byte[splitPosition];
            System.arraycopy(data, 0, part, 0, splitPosition);
            return part;
        }

        public byte[] getSecondPart() {
            if (splitPosition <= 0) throw new IllegalStateException();
            byte[] part = new byte[data.length - splitPosition];
            System.arraycopy(data, splitPosition, part, 0, part.length);
            return part;
        }
    }
}