package cc.endmc.framework.database.mapper;

import cc.endmc.framework.database.domain.DatabaseVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数据库版本管理Mapper接口
 *
 * @author Memory
 * @since 2024-12-28
 */
@Mapper
public interface DatabaseVersionMapper {

    /**
     * 检查数据库版本表是否存在
     *
     * @return 表是否存在
     */
    @Select("SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'database_version'")
    int checkVersionTableExists();

    /**
     * 创建数据库版本表
     */
    @Update("CREATE TABLE IF NOT EXISTS `database_version` (" +
            "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID'," +
            "`app_version` varchar(50) NOT NULL COMMENT '应用程序版本号'," +
            "`script_type` varchar(20) NOT NULL COMMENT '脚本类型(migration)'," +
            "`script_name` varchar(200) NOT NULL COMMENT '脚本名称'," +
            "`file_name` varchar(200) NOT NULL COMMENT '脚本文件名'," +
            "`description` varchar(500) DEFAULT NULL COMMENT '版本描述'," +
            "`checksum` varchar(64) DEFAULT NULL COMMENT '脚本内容校验和'," +
            "`execution_time` int DEFAULT NULL COMMENT '执行耗时(毫秒)'," +
            "`success` tinyint(1) NOT NULL DEFAULT 1 COMMENT '执行是否成功'," +
            "`error_message` text DEFAULT NULL COMMENT '错误信息'," +
            "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间'," +
            "PRIMARY KEY (`id`)," +
            "UNIQUE KEY `uk_app_script` (`app_version`, `script_type`, `script_name`)," +
            "KEY `idx_app_version` (`app_version`)," +
            "KEY `idx_script_type` (`script_type`)," +
            "KEY `idx_success` (`success`)," +
            "KEY `idx_create_time` (`create_time`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据库版本管理表'")
    void createVersionTable();

    /**
     * 插入初始版本记录
     */
    @Insert("INSERT IGNORE INTO `database_version` " +
            "(`app_version`, `script_type`, `script_name`, `file_name`, `description`, `checksum`, `execution_time`, `success`) " +
            "VALUES ('1.0.0', 'migration', 'init', 'init.sql', '初始化数据库版本管理', 'init', 0, 1)")
    void insertInitialVersion();

    /**
     * 查询所有已执行的版本记录
     *
     * @return 版本记录列表
     */
    @Select("SELECT * FROM database_version ORDER BY create_time DESC")
    List<DatabaseVersion> selectAllVersions();

    /**
     * 查询已执行的成功版本记录
     *
     * @return 成功的版本记录列表
     */
    @Select("SELECT * FROM database_version WHERE success = 1 ORDER BY create_time DESC")
    List<DatabaseVersion> selectSuccessVersions();

    /**
     * 查询指定应用版本的已执行脚本
     *
     * @param appVersion 应用版本号
     * @return 已执行脚本列表
     */
    @Select("SELECT * FROM database_version WHERE app_version = #{appVersion} AND success = 1")
    List<DatabaseVersion> selectVersionsByAppVersion(@Param("appVersion") String appVersion);

    /**
     * 检查指定脚本是否已执行
     *
     * @param appVersion 应用版本号
     * @param scriptType 脚本类型
     * @param scriptName 脚本名称
     * @return 是否已执行
     */
    @Select("SELECT COUNT(*) FROM database_version " +
            "WHERE app_version = #{appVersion} AND script_type = #{scriptType} " +
            "AND script_name = #{scriptName} AND success = 1")
    int checkScriptExecuted(@Param("appVersion") String appVersion,
                            @Param("scriptType") String scriptType,
                            @Param("scriptName") String scriptName);

    /**
     * 获取当前数据库的最新应用版本
     *
     * @return 最新应用版本
     */
    @Select("SELECT app_version FROM database_version WHERE success = 1 " +
            "ORDER BY create_time DESC LIMIT 1")
    String selectLatestAppVersion();

    /**
     * 插入版本记录
     *
     * @param version 版本记录
     * @return 影响行数
     */
    @Insert("INSERT INTO database_version " +
            "(app_version, script_type, script_name, file_name, description, checksum, " +
            "execution_time, success, error_message, create_time) " +
            "VALUES (#{appVersion}, #{scriptType}, #{scriptName}, #{fileName}, #{description}, " +
            "#{checksum}, #{executionTime}, #{success}, #{errorMessage}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertVersion(DatabaseVersion version);

    /**
     * 更新版本记录
     *
     * @param version 版本记录
     * @return 影响行数
     */
    @Update("UPDATE database_version SET " +
            "execution_time = #{executionTime}, " +
            "success = #{success}, " +
            "error_message = #{errorMessage} " +
            "WHERE id = #{id}")
    int updateVersion(DatabaseVersion version);

    /**
     * 获取当前最新版本记录
     *
     * @return 最新版本记录
     */
    @Select("SELECT * FROM database_version WHERE success = 1 " +
            "ORDER BY create_time DESC LIMIT 1")
    DatabaseVersion selectLatestVersion();

    /**
     * 执行SQL脚本
     * 注意：这个方法用于执行动态SQL，需要特别小心SQL注入
     *
     * @param sql SQL脚本内容
     */
    void executeSql(@Param("sql") String sql);
}