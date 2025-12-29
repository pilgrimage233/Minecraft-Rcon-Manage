package cc.endmc.framework.database.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库版本管理实体类
 *
 * @author Memory
 * @since 2024-12-28
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class DatabaseVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 应用程序版本号
     */
    private String appVersion;

    /**
     * 脚本类型(migration)
     */
    private String scriptType;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 脚本文件名
     */
    private String fileName;

    /**
     * 版本描述
     */
    private String description;

    /**
     * 脚本内容校验和
     */
    private String checksum;

    /**
     * 执行耗时(毫秒)
     */
    private Integer executionTime;

    /**
     * 执行是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 构造函数
     */
    public DatabaseVersion() {
    }

    /**
     * 构造函数
     *
     * @param appVersion  应用版本号
     * @param scriptType  脚本类型
     * @param scriptName  脚本名称
     * @param fileName    文件名
     * @param description 描述
     */
    public DatabaseVersion(String appVersion, String scriptType, String scriptName, String fileName, String description) {
        this.appVersion = appVersion;
        this.scriptType = scriptType;
        this.scriptName = scriptName;
        this.fileName = fileName;
        this.description = description;
        this.success = true;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 设置执行失败
     *
     * @param errorMessage 错误信息
     * @return this
     */
    public DatabaseVersion setFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * 设置执行成功
     *
     * @param executionTime 执行耗时
     * @return this
     */
    public DatabaseVersion Success(int executionTime) {
        this.success = true;
        this.executionTime = executionTime;
        this.errorMessage = null;
        return this;
    }

    /**
     * 获取脚本唯一标识
     *
     * @return 唯一标识
     */
    public String getScriptKey() {
        return String.format("%s:%s:%s", appVersion, scriptType, scriptName);
    }
}