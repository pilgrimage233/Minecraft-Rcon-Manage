package cc.endmc.framework.database.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库备份服务
 * 在数据库迁移前自动备份重要数据
 *
 * @author Memory
 * @since 2024-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    /**
     * 排除备份的表（日志表等）
     */
    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "qq_bot_log",
            "sys_job_log",
            "sys_oper_log"
    );
    /**
     * 备份文件名时间格式
     */
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final JdbcTemplate jdbcTemplate;
    /**
     * 备份文件存储路径
     */
    @Value("${endless.database.backup.path:./backup}")
    private String backupPath;
    /**
     * 数据库URL - 支持多种配置方式
     */
    @Value("${spring.datasource.url:#{null}}")
    private String datasourceUrl;

    @Value("${spring.datasource.druid.master.url:#{null}}")
    private String druidMasterUrl;

    /**
     * 执行数据库备份
     *
     * @param appVersion 应用版本号
     * @return 备份结果
     */
    public BackupResult backup(String appVersion) {
        log.info("🗄️ 开始数据库备份 - 应用版本: {}", appVersion);

        BackupResult result = new BackupResult();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 创建备份目录
            String backupDir = createBackupDirectory(appVersion);
            result.setBackupPath(backupDir);

            // 2. 获取需要备份的表列表
            List<String> tablesToBackup = getTablesToBakcup();
            if (tablesToBackup.isEmpty()) {
                log.warn("⚠️ 未找到需要备份的表");
                return result.setSuccess(true).setMessage("未找到需要备份的表");
            }

            log.info("📋 需要备份 {} 个表: {}", tablesToBackup.size(),
                    String.join(", ", tablesToBackup));

            // 3. 备份每个表
            for (String tableName : tablesToBackup) {
                backupTable(tableName, backupDir, result);
            }

            // 4. 生成备份信息文件
            generateBackupInfo(backupDir, appVersion, tablesToBackup, result);

            long elapsedTime = System.currentTimeMillis() - startTime;
            String message = String.format("数据库备份完成，备份了 %d 个表，耗时 %d ms",
                    result.getBackedupTables().size(), elapsedTime);

            log.info("✅ {}", message);
            return result.setSuccess(true).setMessage(message);

        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("数据库备份失败: %s (耗时 %d ms)", e.getMessage(), elapsedTime);

            log.error("❌ {}", errorMsg, e);
            return result.setSuccess(false).setMessage(errorMsg).setError(e);
        }
    }

    /**
     * 创建备份目录
     *
     * @param appVersion 应用版本
     * @return 备份目录路径
     */
    private String createBackupDirectory(String appVersion) throws IOException {
        String timestamp = LocalDateTime.now().format(BACKUP_TIME_FORMAT);
        String dirName = String.format("backup_%s_%s", appVersion, timestamp);

        File backupDir = new File(backupPath, dirName);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
        }

        log.info("📁 创建备份目录: {}", backupDir.getAbsolutePath());
        return backupDir.getAbsolutePath();
    }

    /**
     * 获取需要备份的表列表
     *
     * @return 表名列表
     */
    private List<String> getTablesToBakcup() {
        try {
            String databaseName = extractDatabaseName();

            String sql = "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = ? AND table_type = 'BASE TABLE'";

            List<String> allTables = jdbcTemplate.queryForList(sql, String.class, databaseName);

            // 过滤掉排除的表
            return allTables.stream()
                    .filter(tableName -> !EXCLUDED_TABLES.contains(tableName.toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ 获取表列表失败", e);
            throw new RuntimeException("获取表列表失败", e);
        }
    }

    /**
     * 从数据源URL中提取数据库名称
     *
     * @return 数据库名称
     */
    private String extractDatabaseName() {
        // 优先使用 Druid 主库配置，然后是标准配置
        String url = druidMasterUrl != null && !druidMasterUrl.isEmpty() ? druidMasterUrl : datasourceUrl;

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("数据源URL未配置，请检查 spring.datasource.url 或 spring.datasource.druid.master.url 配置");
        }

        // 从 jdbc:mysql://localhost:3306/database_name 中提取 database_name
        String[] parts = url.split("/");
        if (parts.length < 4) {
            throw new IllegalArgumentException("无效的数据源URL格式: " + url);
        }

        String dbNameWithParams = parts[parts.length - 1];
        // 移除URL参数 (如 ?useSSL=false)
        return dbNameWithParams.split("\\?")[0];
    }

    /**
     * 备份单个表
     *
     * @param tableName 表名
     * @param backupDir 备份目录
     * @param result    备份结果
     */
    private void backupTable(String tableName, String backupDir, BackupResult result) {
        log.debug("📄 备份表: {}", tableName);

        try {
            // 1. 获取表结构
            String createTableSql = getCreateTableSql(tableName);

            // 2. 获取表数据
            List<Map<String, Object>> tableData = getTableData(tableName);

            // 3. 生成备份文件
            String backupFile = generateTableBackupFile(tableName, backupDir, createTableSql, tableData);

            result.addBackedupTable(tableName, backupFile, tableData.size());
            log.debug("✅ 表 {} 备份完成，共 {} 条记录", tableName, tableData.size());

        } catch (Exception e) {
            log.error("❌ 备份表 {} 失败: {}", tableName, e.getMessage(), e);
            result.addFailedTable(tableName, e);
        }
    }

    /**
     * 获取表的创建语句
     *
     * @param tableName 表名
     * @return 创建表的SQL语句
     */
    private String getCreateTableSql(String tableName) {
        try {
            String sql = "SHOW CREATE TABLE `" + tableName + "`";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return (String) result.get("Create Table");
        } catch (Exception e) {
            log.warn("⚠️ 获取表 {} 的创建语句失败，使用默认格式", tableName);
            return "-- 无法获取表 " + tableName + " 的创建语句";
        }
    }

    /**
     * 获取表数据
     *
     * @param tableName 表名
     * @return 表数据
     */
    private List<Map<String, Object>> getTableData(String tableName) {
        try {
            String sql = "SELECT * FROM `" + tableName + "`";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.warn("⚠️ 获取表 {} 数据失败: {}", tableName, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 生成表备份文件
     *
     * @param tableName      表名
     * @param backupDir      备份目录
     * @param createTableSql 创建表SQL
     * @param tableData      表数据
     * @return 备份文件路径
     */
    private String generateTableBackupFile(String tableName, String backupDir,
                                           String createTableSql, List<Map<String, Object>> tableData) throws IOException {

        String fileName = tableName + ".sql";
        File backupFile = new File(backupDir, fileName);

        try (FileWriter writer = new FileWriter(backupFile, StandardCharsets.UTF_8)) {
            // 写入文件头
            writer.write("-- =====================================================\n");
            writer.write("-- 表备份文件: " + tableName + "\n");
            writer.write("-- 备份时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("-- 记录数量: " + tableData.size() + "\n");
            writer.write("-- =====================================================\n\n");

            writer.write("SET NAMES utf8mb4;\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            // 写入表结构
            writer.write("-- ----------------------------\n");
            writer.write("-- Table structure for " + tableName + "\n");
            writer.write("-- ----------------------------\n");
            writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
            writer.write(createTableSql + ";\n\n");

            // 写入表数据
            if (!tableData.isEmpty()) {
                writer.write("-- ----------------------------\n");
                writer.write("-- Records of " + tableName + "\n");
                writer.write("-- ----------------------------\n");

                // 生成INSERT语句
                generateInsertStatements(writer, tableName, tableData);
            }

            writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
        }

        return backupFile.getAbsolutePath();
    }

    /**
     * 生成INSERT语句
     *
     * @param writer    文件写入器
     * @param tableName 表名
     * @param tableData 表数据
     */
    private void generateInsertStatements(FileWriter writer, String tableName,
                                          List<Map<String, Object>> tableData) throws IOException {

        if (tableData.isEmpty()) {
            return;
        }

        // 获取列名
        Set<String> columnNames = tableData.get(0).keySet();
        String columns = columnNames.stream()
                .map(col -> "`" + col + "`")
                .collect(Collectors.joining(", "));

        // 批量插入，每100条记录一个INSERT语句
        int batchSize = 100;
        for (int i = 0; i < tableData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, tableData.size());
            List<Map<String, Object>> batch = tableData.subList(i, endIndex);

            writer.write("INSERT INTO `" + tableName + "` (" + columns + ") VALUES\n");

            for (int j = 0; j < batch.size(); j++) {
                Map<String, Object> row = batch.get(j);
                String values = columnNames.stream()
                        .map(col -> formatValue(row.get(col)))
                        .collect(Collectors.joining(", "));

                writer.write("(" + values + ")");
                if (j < batch.size() - 1) {
                    writer.write(",\n");
                } else {
                    writer.write(";\n\n");
                }
            }
        }
    }

    /**
     * 格式化字段值
     *
     * @param value 字段值
     * @return 格式化后的值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            // 转义单引号和反斜杠
            String escaped = value.toString()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            return "'" + escaped + "'";
        }

        if (value instanceof Date || value instanceof LocalDateTime) {
            return "'" + value.toString() + "'";
        }

        return value.toString();
    }

    /**
     * 生成备份信息文件
     *
     * @param backupDir      备份目录
     * @param appVersion     应用版本
     * @param tablesToBackup 备份的表列表
     * @param result         备份结果
     */
    private void generateBackupInfo(String backupDir, String appVersion,
                                    List<String> tablesToBackup, BackupResult result) throws IOException {

        File infoFile = new File(backupDir, "backup_info.txt");

        try (FileWriter writer = new FileWriter(infoFile, StandardCharsets.UTF_8)) {
            writer.write("数据库备份信息\n");
            writer.write("=====================================\n");
            writer.write("备份时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("应用版本: " + appVersion + "\n");
            writer.write("数据库: " + extractDatabaseName() + "\n");
            writer.write("备份目录: " + backupDir + "\n");
            writer.write("总表数量: " + tablesToBackup.size() + "\n");
            writer.write("成功备份: " + result.getBackedupTables().size() + "\n");
            writer.write("备份失败: " + result.getFailedTables().size() + "\n");
            writer.write("\n");

            writer.write("排除的表:\n");
            for (String excludedTable : EXCLUDED_TABLES) {
                writer.write("  - " + excludedTable + "\n");
            }
            writer.write("\n");

            writer.write("备份成功的表:\n");
            for (BackupResult.TableBackupInfo info : result.getBackedupTables()) {
                writer.write(String.format("  - %s (%d 条记录) -> %s\n",
                        info.tableName(), info.recordCount(), info.backupFile()));
            }

            if (!result.getFailedTables().isEmpty()) {
                writer.write("\n备份失败的表:\n");
                for (Map.Entry<String, Exception> entry : result.getFailedTables().entrySet()) {
                    writer.write(String.format("  - %s: %s\n",
                            entry.getKey(), entry.getValue().getMessage()));
                }
            }
        }

        log.info("📋 生成备份信息文件: {}", infoFile.getAbsolutePath());
    }

    /**
     * 备份结果类
     */
    @Getter
    public static class BackupResult {
        private final List<TableBackupInfo> backedupTables = new ArrayList<>();
        private final Map<String, Exception> failedTables = new HashMap<>();
        private boolean success;
        private String message;
        private Exception error;
        private String backupPath;

        public BackupResult setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public BackupResult setMessage(String message) {
            this.message = message;
            return this;
        }

        public BackupResult setError(Exception error) {
            this.error = error;
            return this;
        }

        public BackupResult setBackupPath(String backupPath) {
            this.backupPath = backupPath;
            return this;
        }

        public void addBackedupTable(String tableName, String backupFile, int recordCount) {
            backedupTables.add(new TableBackupInfo(tableName, backupFile, recordCount));
        }

        public void addFailedTable(String tableName, Exception error) {
            failedTables.put(tableName, error);
        }

        /**
         * 单个表的备份信息
         */
        public record TableBackupInfo(String tableName, String backupFile, int recordCount) {

        }
    }
}