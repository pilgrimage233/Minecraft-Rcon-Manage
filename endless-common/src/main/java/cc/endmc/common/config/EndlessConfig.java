package cc.endmc.common.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "ruoyi")
public class EndlessConfig {
    /**
     * 上传路径
     */
    @Getter
    private static String profile;
    /**
     * 获取地址开关
     */
    @Getter
    private static boolean addressEnabled;
    /**
     * 验证码类型
     */
    @Getter
    private static String captchaType;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 版本
     */
    private String version;
    /**
     * 版权年份
     */
    private String copyrightYear;

    public void setProfile(String profile) {
        EndlessConfig.profile = profile;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        EndlessConfig.addressEnabled = addressEnabled;
    }

    public void setCaptchaType(String captchaType) {
        EndlessConfig.captchaType = captchaType;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }
}
