package cc.endmc.framework.database.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库备份信息
 *
 * @author Memory
 * @since 2026-01-31
 */
@Data
public class BackupInfo {

    /**
     * 备份ID（目录名）
     */
    private String backupId;

    /**
     * 备份路径
     */
    private String backupPath;

    /**
     * 备份类型（full_backup/scheduled_backup）
     */
    private String backupType;

    /**
     * 备份类型描述
     */
    private String backupTypeDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 备份的表数量
     */
    private Integer tableCount;

    /**
     * 备份的表列表
     */
    private List<String> tables;

    /**
     * 备份大小（字节）
     */
    private Long size;

    /**
     * 格式化的大小
     */
    private String sizeFormatted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取备份类型描述
     */
    public String getBackupTypeDesc() {
        if ("full_backup".equals(backupType)) {
            return "全量备份";
        } else if ("scheduled_backup".equals(backupType)) {
            return "定时备份";
        } else if ("rollback_backup".equals(backupType)) {
            return "回滚备份";
        }
        return "未知类型";
    }
}
