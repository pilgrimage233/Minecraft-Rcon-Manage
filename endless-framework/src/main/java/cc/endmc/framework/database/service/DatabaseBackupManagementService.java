package cc.endmc.framework.database.service;

import cc.endmc.framework.database.domain.BackupInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库备份管理服务
 * 提供备份列表查询、手动备份、数据恢复等功能
 *
 * @author Memory
 * @since 2026-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupManagementService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseBackupExecutor backupExecutor;
    @Value("${endless.database.backup.path:./backup}")
    private String backupPath;

    /**
     * 获取所有备份列表
     *
     * @return 备份信息列表
     */
    public List<BackupInfo> listBackups() {
        List<BackupInfo> backupList = new ArrayList<>();

        try {
            File backupDir = new File(backupPath);
            if (!backupDir.exists() || !backupDir.isDirectory()) {
                log.warn("备份目录不存在: {}", backupPath);
                return backupList;
            }

            // 扫描所有备份子目录
            File[] subDirs = backupDir.listFiles(File::isDirectory);
            if (subDirs == null || subDirs.length == 0) {
                return backupList;
            }

            for (File typeDir : subDirs) {
                String backupType = typeDir.getName(); // full_backup 或 scheduled_backup

                File[] backupDirs = typeDir.listFiles(File::isDirectory);
                if (backupDirs == null) {
                    continue;
                }

                for (File dir : backupDirs) {
                    try {
                        BackupInfo info = parseBackupDirectory(dir, backupType);
                        if (info != null) {
                            backupList.add(info);
                        }
                    } catch (Exception e) {
                        log.error("解析备份目录失败: {}", dir.getName(), e);
                    }
                }
            }

            // 按创建时间倒序排序
            backupList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        } catch (Exception e) {
            log.error("获取备份列表失败", e);
        }

        return backupList;
    }

    /**
     * 解析备份目录信息
     *
     * @param dir        备份目录
     * @param backupType 备份类型
     * @return 备份信息
     */
    private BackupInfo parseBackupDirectory(File dir, String backupType) throws IOException {
        BackupInfo info = new BackupInfo();
        info.setBackupId(dir.getName());
        info.setBackupPath(dir.getAbsolutePath());
        info.setBackupType(backupType);

        // 获取目录创建时间
        Path path = Paths.get(dir.getAbsolutePath());
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        LocalDateTime createTime = LocalDateTime.ofInstant(
                attrs.creationTime().toInstant(),
                ZoneId.systemDefault()
        );
        info.setCreateTime(createTime);

        // 读取备份信息文件
        File infoFile = new File(dir, "backup_info.txt");
        if (infoFile.exists()) {
            parseBackupInfoFile(infoFile, info);
        }

        // 统计备份文件
        File[] sqlFiles = dir.listFiles((d, name) -> name.endsWith(".sql"));
        info.setTableCount(sqlFiles != null ? sqlFiles.length : 0);

        // 计算目录大小
        long size = calculateDirectorySize(dir);
        info.setSize(size);
        info.setSizeFormatted(formatFileSize(size));

        return info;
    }

    /**
     * 解析备份信息文件
     *
     * @param infoFile 信息文件
     * @param info     备份信息对象
     */
    private void parseBackupInfoFile(File infoFile, BackupInfo info) {
        try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
            String line;
            List<String> tables = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.contains("应用版本:") || line.contains("版本号:")) {
                    String version = line.split(":")[1].trim();
                    info.setVersion(version);
                } else if (line.contains("数据库:")) {
                    String database = line.split(":")[1].trim();
                    info.setDatabase(database);
                } else if (line.startsWith("  - ") || line.startsWith("  ✅ ")) {
                    String tableName = line.replace("  - ", "")
                            .replace("  ✅ ", "")
                            .split(" ")[0]
                            .trim();
                    if (!tableName.isEmpty()) {
                        tables.add(tableName);
                    }
                }
            }

            info.setTables(tables);

        } catch (Exception e) {
            log.warn("读取备份信息文件失败: {}", infoFile.getName(), e);
        }
    }

    /**
     * 计算目录大小
     *
     * @param directory 目录
     * @return 大小（字节）
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                }
            }
        }
        return size;
    }

    /**
     * 格式化文件大小
     *
     * @param size 大小（字节）
     * @return 格式化后的大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取指定备份的详细信息
     *
     * @param backupId 备份ID
     * @return 备份详细信息
     */
    public BackupInfo getBackupDetail(String backupId) {
        List<BackupInfo> backups = listBackups();
        return backups.stream()
                .filter(b -> b.getBackupId().equals(backupId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 恢复指定表的数据
     *
     * @param backupId  备份ID
     * @param tableName 表名
     * @return 是否成功
     */
    public boolean restoreTable(String backupId, String tableName) {
        log.info("开始恢复表数据: backupId={}, tableName={}", backupId, tableName);

        try {
            // 1. 恢复前先备份当前数据
            log.info("恢复前先备份当前表数据: {}", tableName);
            String rollbackBackupDir = backupExecutor.createRollbackBackup(tableName);
            if (rollbackBackupDir != null) {
                log.info("回滚前备份成功: {}", rollbackBackupDir);
            } else {
                log.warn("回滚前备份失败，但继续执行恢复操作");
            }

            // 2. 查找备份文件
            BackupInfo backup = getBackupDetail(backupId);
            if (backup == null) {
                log.error("备份不存在: {}", backupId);
                return false;
            }

            File sqlFile = new File(backup.getBackupPath(), tableName + ".sql");
            if (!sqlFile.exists()) {
                log.error("备份文件不存在: {}", sqlFile.getAbsolutePath());
                return false;
            }

            // 3. 读取SQL文件内容
            String sqlContent = Files.readString(sqlFile.toPath());

            // 4. 执行SQL恢复
            executeSqlScript(sqlContent);

            log.info("表 {} 恢复成功", tableName);
            return true;

        } catch (Exception e) {
            log.error("恢复表 {} 失败", tableName, e);
            return false;
        }
    }

    /**
     * 恢复所有表的数据（全量回滚）
     *
     * @param backupId 备份ID
     * @return 恢复成功的表数量
     */
    public int restoreAllTables(String backupId) {
        log.info("开始全量回滚: backupId={}", backupId);

        try {
            // 1. 全量回滚前先备份当前所有数据
            log.info("全量回滚前先备份当前所有表数据");
            String rollbackBackupDir = backupExecutor.createRollbackBackup(null);
            if (rollbackBackupDir != null) {
                log.info("回滚前全量备份成功: {}", rollbackBackupDir);
            } else {
                log.warn("回滚前全量备份失败，但继续执行恢复操作");
            }

            // 2. 获取备份详情
            BackupInfo backup = getBackupDetail(backupId);
            if (backup == null) {
                log.error("备份不存在: {}", backupId);
                return 0;
            }

            // 3. 恢复所有表
            int successCount = 0;
            List<String> tables = backup.getTables();

            for (String tableName : tables) {
                try {
                    File sqlFile = new File(backup.getBackupPath(), tableName + ".sql");
                    if (!sqlFile.exists()) {
                        log.warn("备份文件不存在，跳过: {}", sqlFile.getAbsolutePath());
                        continue;
                    }

                    // 读取并执行SQL
                    String sqlContent = Files.readString(sqlFile.toPath());
                    executeSqlScript(sqlContent);

                    successCount++;
                    log.info("表 {} 恢复成功 ({}/{})", tableName, successCount, tables.size());

                } catch (Exception e) {
                    log.error("恢复表 {} 失败: {}", tableName, e.getMessage());
                }
            }

            log.info("全量回滚完成，成功恢复 {} 个表", successCount);
            return successCount;

        } catch (Exception e) {
            log.error("全量回滚失败", e);
            return 0;
        }
    }

    /**
     * 执行SQL脚本
     *
     * @param sqlScript SQL脚本内容
     */
    private void executeSqlScript(String sqlScript) {
        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        // 使用更简单的方式分割SQL语句
        // 先按行分割，然后累积完整的SQL语句
        String[] lines = sqlScript.split("\n");
        StringBuilder currentStatement = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            // 跳过注释行和空行
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                continue;
            }

            // 累积当前语句
            currentStatement.append(line).append("\n");

            // 如果行以分号结尾，说明是一个完整的语句
            if (trimmedLine.endsWith(";")) {
                String statement = currentStatement.toString().trim();

                // 移除末尾的分号
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1).trim();
                }

                if (!statement.isEmpty()) {
                    // 更新计数器（通过数组传递引用）
                    int[] counters = executeStatement(statement);
                    successCount += counters[0];
                    skipCount += counters[1];
                    errorCount += counters[2];
                }

                // 重置当前语句
                currentStatement = new StringBuilder();
            }
        }

        // 处理最后一个可能没有分号的语句
        if (currentStatement.length() > 0) {
            String statement = currentStatement.toString().trim();
            if (!statement.isEmpty() && !statement.startsWith("--")) {
                int[] counters = executeStatement(statement);
                successCount += counters[0];
                skipCount += counters[1];
                errorCount += counters[2];
            }
        }

        log.info("SQL脚本执行完成 - 成功: {}, 跳过: {}, 失败: {}", successCount, skipCount, errorCount);
    }

    /**
     * 执行单条SQL语句
     *
     * @param statement SQL语句
     * @return 计数器数组 [成功数, 跳过数, 失败数]
     */
    private int[] executeStatement(String statement) {
        int[] counters = new int[3]; // [success, skip, error]

        try {
            // 判断语句类型
            String upperStatement = statement.toUpperCase();
            boolean isInsertStatement = upperStatement.startsWith("INSERT ");
            boolean isDropStatement = upperStatement.startsWith("DROP ");
            boolean isCreateStatement = upperStatement.startsWith("CREATE ");

            jdbcTemplate.execute(statement);
            counters[0] = 1; // success

            // 记录重要语句的执行
            if (isInsertStatement) {
                log.debug("✅ INSERT 语句执行成功");
            } else if (isCreateStatement) {
                log.debug("✅ CREATE 语句执行成功");
            } else if (isDropStatement) {
                log.debug("✅ DROP 语句执行成功");
            }

        } catch (Exception e) {
            String upperStatement = statement.toUpperCase();
            boolean isSetStatement = upperStatement.startsWith("SET ");

            // SET 语句失败是正常的，其他语句失败需要警告
            if (isSetStatement) {
                log.debug("SET 语句执行失败（正常）: {}", e.getMessage());
                counters[1] = 1; // skip
            } else {
                counters[2] = 1; // error
                log.error("❌ SQL 语句执行失败: {}", e.getMessage());
                log.error("失败的SQL: {}", statement.length() > 200 ? statement.substring(0, 200) + "..." : statement);

                // 如果是 INSERT、CREATE、DROP 等重要语句失败，抛出异常
                if (upperStatement.startsWith("INSERT ") ||
                        upperStatement.startsWith("CREATE ") ||
                        upperStatement.startsWith("DROP ")) {
                    throw new RuntimeException("关键SQL语句执行失败: " + e.getMessage(), e);
                }
            }
        }

        return counters;
    }

    /**
     * 删除指定备份
     *
     * @param backupId 备份ID
     * @return 是否成功
     */
    public boolean deleteBackup(String backupId) {
        log.info("开始删除备份: {}", backupId);

        try {
            BackupInfo backup = getBackupDetail(backupId);
            if (backup == null) {
                log.error("备份不存在: {}", backupId);
                return false;
            }

            File backupDir = new File(backup.getBackupPath());
            if (!backupDir.exists()) {
                log.error("备份目录不存在: {}", backup.getBackupPath());
                return false;
            }

            // 递归删除目录
            deleteDirectory(backupDir);

            log.info("备份删除成功: {}", backupId);
            return true;

        } catch (Exception e) {
            log.error("删除备份失败: {}", backupId, e);
            return false;
        }
    }

    /**
     * 递归删除目录
     *
     * @param directory 目录
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * 获取备份统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getBackupStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<BackupInfo> backups = listBackups();

        stats.put("totalBackups", backups.size());
        stats.put("totalSize", backups.stream().mapToLong(BackupInfo::getSize).sum());
        stats.put("totalSizeFormatted", formatFileSize(
                backups.stream().mapToLong(BackupInfo::getSize).sum()
        ));

        // 按类型统计
        Map<String, Long> typeCount = backups.stream()
                .collect(Collectors.groupingBy(BackupInfo::getBackupType, Collectors.counting()));
        stats.put("typeCount", typeCount);

        // 最新备份时间
        if (!backups.isEmpty()) {
            stats.put("latestBackupTime", backups.getFirst().getCreateTime().format(DATETIME_FORMATTER));
        }

        return stats;
    }
}
