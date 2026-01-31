package cc.endmc.framework.database.service;

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
 * 数据库备份执行器
 * 统一处理所有类型的数据库备份操作
 *
 * @author Memory
 * @since 2026-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupExecutor {

    /**
     * 备份文件名时间格式
     */
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 需要备份的表列表
     */
    private static final List<String> BACKUP_TABLES = Arrays.asList(
            "banlist_info",
            "custom_email_templates",
            "history_command",
            "ip_limit_info",
            "node_env",
            "node_minecraft_server",
            "node_server",
            "operator_list",
            "bot_group_command_config",
            "qq_bot_config",
            "qq_bot_manager",
            "qq_bot_manager_group",
            "server_info",
            "sys_user",
            "regular_cmd",
            "server_command_info",
            "whitelist_deadline_info",
            "whitelist_info",
            "whitelist_quiz_answer",
            "whitelist_quiz_config",
            "whitelist_quiz_question",
            "whitelist_quiz_submission",
            "whitelist_quiz_submission_detail"
    );
    private final JdbcTemplate jdbcTemplate;
    @Value("${endless.database.backup.path:./backup}")
    private String backupPath;
    @Value("${spring.datasource.url:#{null}}")
    private String datasourceUrl;
    @Value("${spring.datasource.druid.master.url:#{null}}")
    private String druidMasterUrl;

    /**
     * 执行定时备份任务
     */
    public void executeScheduledBackup() {
        log.info("🗄️ 开始执行数据库定时备份任务");
        executeBackup("scheduled_backup", "定时备份");
    }

    /**
     * 执行手动备份任务
     */
    public void executeManualBackup() {
        log.info("🗄️ 开始执行数据库手动备份任务");
        executeBackup("full_backup", "手动备份");
    }

    /**
     * 创建回滚前备份
     *
     * @param tableName 要备份的表名（如果为null则备份所有表）
     * @return 备份目录路径
     */
    public String createRollbackBackup(String tableName) {
        log.info("🗄️ 开始创建回滚前备份: tableName={}", tableName);

        List<String> tablesToBackup;
        if (tableName != null && !tableName.isEmpty()) {
            tablesToBackup = Collections.singletonList(tableName);
        } else {
            tablesToBackup = new ArrayList<>(BACKUP_TABLES);
        }

        return executeBackup("rollback_backup", "回滚前备份", tablesToBackup, tableName);
    }

    /**
     * 执行备份
     *
     * @param backupType 备份类型目录名
     * @param backupDesc 备份描述
     */
    private void executeBackup(String backupType, String backupDesc) {
        executeBackup(backupType, backupDesc, new ArrayList<>(BACKUP_TABLES), null);
    }

    /**
     * 执行备份（核心方法）
     *
     * @param backupType     备份类型目录名
     * @param backupDesc     备份描述
     * @param tablesToBackup 要备份的表列表
     * @param scopeDesc      备份范围描述
     * @return 备份目录路径
     */
    private String executeBackup(String backupType, String backupDesc, List<String> tablesToBackup, String scopeDesc) {
        long startTime = System.currentTimeMillis();

        try {
            // 创建备份目录
            String backupDir = createBackupDirectory(backupType);
            log.info("📁 备份目录: {}", backupDir);

            int successCount = 0;
            int failCount = 0;
            Map<String, BackupResult> backupResults = new LinkedHashMap<>();
            Map<String, Exception> failedTables = new HashMap<>();

            // 备份每个表
            for (String tableName : tablesToBackup) {
                try {
                    log.debug("📄 开始备份表: {}", tableName);

                    String createTableSql = getCreateTableSql(tableName);
                    List<Map<String, Object>> tableData = getTableData(tableName);
                    String backupFile = generateTableBackupFile(tableName, backupDir, createTableSql, tableData, backupDesc);

                    backupResults.put(tableName, new BackupResult(backupFile, tableData.size()));
                    successCount++;

                    log.info("✅ 表 {} 备份完成，共 {} 条记录", tableName, tableData.size());

                } catch (Exception e) {
                    failCount++;
                    failedTables.put(tableName, e);
                    log.error("❌ 备份表 {} 失败: {}", tableName, e.getMessage());
                }
            }

            // 生成备份信息文件
            generateBackupInfo(backupDir, backupDesc, backupResults, failedTables, scopeDesc);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("✅ {}完成！成功: {} 个表，失败: {} 个表，耗时: {} ms",
                    backupDesc, successCount, failCount, elapsedTime);

            return backupDir;

        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("❌ {}失败: {} (耗时 {} ms)", backupDesc, e.getMessage(), elapsedTime, e);
            return null;
        }
    }

    /**
     * 创建备份目录
     */
    private String createBackupDirectory(String backupType) throws IOException {
        String timestamp = LocalDateTime.now().format(BACKUP_TIME_FORMAT);
        String dirName = String.format("%s_%s", backupType.replace("_backup", "_backup"), timestamp);

        File typeDir = new File(backupPath, backupType);
        if (!typeDir.exists() && !typeDir.mkdirs()) {
            throw new IOException("无法创建备份类型目录: " + typeDir.getAbsolutePath());
        }

        File backupDir = new File(typeDir, dirName);
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
        }

        return backupDir.getAbsolutePath();
    }

    /**
     * 获取表的创建语句
     */
    private String getCreateTableSql(String tableName) {
        try {
            String sql = "SHOW CREATE TABLE `" + tableName + "`";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return (String) result.get("Create Table");
        } catch (Exception e) {
            log.warn("⚠️ 获取表 {} 的创建语句失败", tableName);
            return "-- 无法获取表 " + tableName + " 的创建语句";
        }
    }

    /**
     * 获取表数据
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
     */
    private String generateTableBackupFile(String tableName, String backupDir,
                                           String createTableSql, List<Map<String, Object>> tableData,
                                           String backupDesc) throws IOException {

        String fileName = tableName + ".sql";
        File backupFile = new File(backupDir, fileName);

        try (FileWriter writer = new FileWriter(backupFile, StandardCharsets.UTF_8)) {
            writer.write("-- =====================================================\n");
            writer.write("-- 表备份文件: " + tableName + "\n");
            writer.write("-- 备份时间: " + LocalDateTime.now().format(DATETIME_FORMATTER) + "\n");
            writer.write("-- 记录数量: " + tableData.size() + "\n");
            writer.write("-- 备份类型: " + backupDesc + "\n");
            writer.write("-- =====================================================\n\n");

            writer.write("SET NAMES utf8mb4;\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            writer.write("-- ----------------------------\n");
            writer.write("-- Table structure for " + tableName + "\n");
            writer.write("-- ----------------------------\n");
            writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
            writer.write(createTableSql + ";\n\n");

            if (!tableData.isEmpty()) {
                writer.write("-- ----------------------------\n");
                writer.write("-- Records of " + tableName + "\n");
                writer.write("-- ----------------------------\n");
                generateInsertStatements(writer, tableName, tableData);
            }

            writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
        }

        return backupFile.getAbsolutePath();
    }

    /**
     * 生成INSERT语句
     */
    private void generateInsertStatements(FileWriter writer, String tableName,
                                          List<Map<String, Object>> tableData) throws IOException {
        if (tableData.isEmpty()) {
            return;
        }

        Set<String> columnNames = tableData.getFirst().keySet();
        String columns = columnNames.stream()
                .map(col -> "`" + col + "`")
                .collect(Collectors.joining(", "));

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
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            String escaped = value.toString()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            return "'" + escaped + "'";
        }

        if (value instanceof Date || value instanceof LocalDateTime) {
            return "'" + value + "'";
        }

        return value.toString();
    }

    /**
     * 生成备份信息文件
     */
    private void generateBackupInfo(String backupDir, String backupDesc,
                                    Map<String, BackupResult> backupResults,
                                    Map<String, Exception> failedTables,
                                    String scopeDesc) throws IOException {

        File infoFile = new File(backupDir, "backup_info.txt");

        try (FileWriter writer = new FileWriter(infoFile, StandardCharsets.UTF_8)) {
            writer.write("数据库备份信息\n");
            writer.write("=====================================\n");
            writer.write("备份时间: " + LocalDateTime.now().format(DATETIME_FORMATTER) + "\n");
            writer.write("备份类型: " + backupDesc + "\n");
            writer.write("数据库: " + extractDatabaseName() + "\n");
            writer.write("备份目录: " + backupDir + "\n");

            if (scopeDesc != null && !scopeDesc.isEmpty()) {
                writer.write("备份范围: 单表备份 (" + scopeDesc + ")\n");
            } else {
                writer.write("备份范围: 全量备份\n");
            }

            writer.write("成功备份: " + backupResults.size() + "\n");
            writer.write("备份失败: " + failedTables.size() + "\n");
            writer.write("\n");

            if (!backupResults.isEmpty()) {
                writer.write("备份成功的表:\n");
                for (Map.Entry<String, BackupResult> entry : backupResults.entrySet()) {
                    writer.write(String.format("  ✅ %s (%d 条记录) -> %s\n",
                            entry.getKey(), entry.getValue().recordCount, entry.getValue().filePath));
                }
                writer.write("\n");
            }

            if (!failedTables.isEmpty()) {
                writer.write("备份失败的表:\n");
                for (Map.Entry<String, Exception> entry : failedTables.entrySet()) {
                    writer.write(String.format("  ❌ %s: %s\n", entry.getKey(), entry.getValue().getMessage()));
                }
            }
        }

        log.info("📋 生成备份信息文件: {}", infoFile.getAbsolutePath());
    }

    /**
     * 从数据源URL中提取数据库名称
     */
    private String extractDatabaseName() {
        String url = druidMasterUrl != null && !druidMasterUrl.isEmpty() ? druidMasterUrl : datasourceUrl;

        if (url == null || url.isEmpty()) {
            return "unknown";
        }

        try {
            String urlWithoutParams = url.split("\\?")[0];
            String[] parts = urlWithoutParams.split("/");
            if (parts.length >= 4) {
                return parts[parts.length - 1];
            }
        } catch (Exception e) {
            log.warn("提取数据库名称失败: {}", e.getMessage());
        }

        return "unknown";
    }

    /**
     * 获取备份表列表
     */
    public List<String> getBackupTables() {
        return new ArrayList<>(BACKUP_TABLES);
    }

    /**
     * 备份结果内部类
     */
    private static class BackupResult {
        String filePath;
        int recordCount;

        BackupResult(String filePath, int recordCount) {
            this.filePath = filePath;
            this.recordCount = recordCount;
        }
    }
}