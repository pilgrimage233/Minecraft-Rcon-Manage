package cc.endmc.framework.database.service;

import cc.endmc.framework.database.domain.DatabaseVersion;
import cc.endmc.framework.database.domain.MigrationScript;
import cc.endmc.framework.database.mapper.DatabaseVersionMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库迁移服务
 * 基于应用程序版本的数据库升级管理，包含自动备份功能
 *
 * @author Memory
 * @since 2024-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMigrationService {

    /**
     * 迁移脚本路径模式
     */
    private static final String MIGRATION_PATH_PATTERN = "classpath:db/migration/**/*.sql";
    private final DatabaseVersionMapper versionMapper;
    private final DatabaseBackupService backupService;
    private final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    /**
     * 当前应用版本
     */
    @Value("${endless.version}")
    private String currentAppVersion;
    /**
     * 是否启用自动备份
     */
    @Value("${endless.database.backup.enabled:true}")
    private boolean backupEnabled;

    /**
     * 执行数据库迁移
     *
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    public MigrationResult migrate() {
        log.info("🔄 开始数据库迁移检查 - 当前应用版本: {}", currentAppVersion);

        MigrationResult result = new MigrationResult();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 初始化版本管理表
            initializeVersionTable();

            // 2. 获取数据库中的最新应用版本
            String dbLatestVersion = getLatestDatabaseVersion();
            log.info("📋 数据库最新版本: {}, 应用当前版本: {}", dbLatestVersion, currentAppVersion);

            // 3. 加载所有迁移脚本
            List<MigrationScript> allScripts = loadAllMigrationScripts();
            if (allScripts.isEmpty()) {
                log.info("📋 未找到迁移脚本文件");
                return result.setSuccess(true).setMessage("未找到迁移脚本");
            }

            log.info("📋 发现 {} 个迁移脚本", allScripts.size());

            // 4. 确定需要执行的脚本
            List<MigrationScript> pendingScripts = determinePendingScripts(allScripts, dbLatestVersion);

            if (pendingScripts.isEmpty()) {
                log.info("✅ 数据库已是最新版本，无需迁移");
                return result.setSuccess(true).setMessage("数据库已是最新版本");
            }

            log.info("🔧 需要执行 {} 个迁移脚本", pendingScripts.size());

            // 5. 执行数据库备份（如果启用且有脚本需要执行）
            if (backupEnabled) {
                executeBackup(result);
            }

            // 6. 执行迁移脚本
            for (MigrationScript script : pendingScripts) {
                executeScript(script, result);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            String message = String.format("数据库迁移完成，执行了 %d 个脚本，耗时 %d ms",
                    result.getExecutedCount(), elapsedTime);

            log.info("✅ {}", message);
            return result.setSuccess(true).setMessage(message);

        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("数据库迁移失败: %s (耗时 %d ms)", e.getMessage(), elapsedTime);

            log.error("❌ {}", errorMsg, e);
            return result.setSuccess(false).setMessage(errorMsg).setError(e);
        }
    }

    /**
     * 执行数据库备份
     *
     * @param result 迁移结果
     */
    private void executeBackup(MigrationResult result) {
        try {
            log.info("🗄️ 开始执行数据库备份...");

            DatabaseBackupService.BackupResult backupResult = backupService.backup(currentAppVersion);
            result.setBackupResult(backupResult);

            if (backupResult.isSuccess()) {
                log.info("✅ 数据库备份完成: {}", backupResult.getMessage());
                log.info("📁 备份路径: {}", backupResult.getBackupPath());
            } else {
                log.error("❌ 数据库备份失败: {}", backupResult.getMessage());
                // 备份失败不应该阻止迁移，但要记录警告
                log.warn("⚠️ 备份失败，但迁移将继续执行。建议手动备份数据库！");
            }

        } catch (Exception e) {
            log.error("❌ 数据库备份异常", e);
            log.warn("⚠️ 备份异常，但迁移将继续执行。建议手动备份数据库！");
        }
    }

    /**
     * 初始化版本管理表
     */
    private void initializeVersionTable() {
        try {
            // 检查版本表是否存在
            int tableExists = versionMapper.checkVersionTableExists();

            if (tableExists == 0) {
                log.info("📋 创建数据库版本管理表...");
                versionMapper.createVersionTable();
                versionMapper.insertInitialVersion();
                log.info("✅ 数据库版本管理表创建成功");
            } else {
                log.debug("📋 数据库版本管理表已存在");
            }
        } catch (Exception e) {
            log.error("❌ 初始化版本管理表失败", e);
            throw new RuntimeException("初始化版本管理表失败", e);
        }
    }

    /**
     * 获取数据库中的最新应用版本
     *
     * @return 最新应用版本，如果没有记录返回null
     */
    private String getLatestDatabaseVersion() {
        try {
            String latestVersion = versionMapper.selectLatestAppVersion();
            return "1.0.0".equals(latestVersion) ? null : latestVersion; // 排除初始化版本
        } catch (Exception e) {
            log.warn("⚠️ 获取数据库最新版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 加载所有迁移脚本
     *
     * @return 迁移脚本列表
     */
    private List<MigrationScript> loadAllMigrationScripts() {
        List<MigrationScript> scripts = new ArrayList<>();

        try {
            // 加载migration脚本
            scripts.addAll(loadScriptsByPattern(MIGRATION_PATH_PATTERN));

            // 排序：按版本、执行顺序
            scripts.sort(MigrationScript::compareTo);

        } catch (Exception e) {
            log.error("❌ 加载迁移脚本失败", e);
            throw new RuntimeException("加载迁移脚本失败", e);
        }

        return scripts;
    }

    /**
     * 按路径模式加载脚本
     *
     * @param pathPattern 路径模式
     * @return 脚本列表
     */
    private List<MigrationScript> loadScriptsByPattern(String pathPattern) throws Exception {
        List<MigrationScript> scripts = new ArrayList<>();
        Resource[] resources = resourceResolver.getResources(pathPattern);

        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable()) {
                try {
                    String resourcePath = resource.getURI().toString();
                    // 提取相对路径
                    String relativePath = extractRelativePath(resourcePath);
                    String content = readResourceContent(resource);

                    MigrationScript script = new MigrationScript(relativePath, content);
                    scripts.add(script);
                    log.debug("📄 加载迁移脚本: {}", script);
                } catch (Exception e) {
                    log.warn("⚠️ 跳过无效的迁移脚本文件: {} - {}", resource.getFilename(), e.getMessage());
                }
            }
        }

        return scripts;
    }

    /**
     * 提取资源的相对路径
     *
     * @param resourcePath 资源完整路径
     * @return 相对路径
     */
    private String extractRelativePath(String resourcePath) {
        // 从完整路径中提取 db/migration/... 部分
        int dbIndex = resourcePath.indexOf("/db/");
        if (dbIndex != -1) {
            return resourcePath.substring(dbIndex + 1); // 去掉开头的 "/"
        }

        // 如果找不到标准路径，尝试其他方式
        if (resourcePath.contains("migration/")) {
            String[] parts = resourcePath.split("/");
            StringBuilder relativePath = new StringBuilder("db/");
            boolean foundMigration = false;

            for (String part : parts) {
                if ("migration".equals(part)) {
                    foundMigration = true;
                }
                if (foundMigration) {
                    relativePath.append(part).append("/");
                }
            }

            String result = relativePath.toString();
            return result.endsWith("/") ? result.substring(0, result.length() - 1) : result;
        }

        throw new IllegalArgumentException("无法解析资源路径: " + resourcePath);
    }

    /**
     * 读取资源文件内容
     *
     * @param resource 资源文件
     * @return 文件内容
     */
    private String readResourceContent(Resource resource) throws Exception {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    /**
     * 确定需要执行的脚本
     *
     * @param allScripts      所有脚本
     * @param dbLatestVersion 数据库最新版本
     * @return 待执行脚本列表
     */
    private List<MigrationScript> determinePendingScripts(List<MigrationScript> allScripts, String dbLatestVersion) {
        if (dbLatestVersion == null) {
            // 首次安装：执行当前版本及之前的所有脚本
            log.info("🆕 首次安装，执行当前版本 {} 及之前的所有脚本", currentAppVersion);
            return allScripts.stream()
                    .filter(script -> compareVersions(script.getAppVersion(), currentAppVersion) <= 0)
                    .filter(script -> !isScriptExecuted(script))
                    .collect(Collectors.toList());
        } else {
            // 增量升级：只执行新版本的脚本
            log.info("🔄 增量升级，从版本 {} 升级到 {}", dbLatestVersion, currentAppVersion);
            return allScripts.stream()
                    .filter(script -> compareVersions(script.getAppVersion(), dbLatestVersion) > 0)
                    .filter(script -> compareVersions(script.getAppVersion(), currentAppVersion) <= 0)
                    .filter(script -> !isScriptExecuted(script))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 检查脚本是否已执行
     *
     * @param script 脚本
     * @return 是否已执行
     */
    private boolean isScriptExecuted(MigrationScript script) {
        try {
            int count = versionMapper.checkScriptExecuted(
                    script.getAppVersion(),
                    script.getScriptType(),
                    script.getScriptName()
            );
            return count > 0;
        } catch (Exception e) {
            log.warn("⚠️ 检查脚本执行状态失败: {} - {}", script.getScriptKey(), e.getMessage());
            return false;
        }
    }

    /**
     * 比较版本号
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果：负数表示version1 < version2，0表示相等，正数表示version1 > version2
     */
    private int compareVersions(String version1, String version2) {
        if (version1 == null && version2 == null) return 0;
        if (version1 == null) return -1;
        if (version2 == null) return 1;

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        return 0;
    }

    /**
     * 解析版本号部分
     *
     * @param part 版本号部分
     * @return 数字值
     */
    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 执行单个迁移脚本
     *
     * @param script 迁移脚本
     * @param result 执行结果
     */
    private void executeScript(MigrationScript script, MigrationResult result) {
        log.info("🔧 执行迁移脚本: {} ({})", script.getFileName(), script.getDescription());

        long startTime = System.currentTimeMillis();
        DatabaseVersion version = new DatabaseVersion(
                script.getAppVersion(),
                script.getScriptType(),
                script.getScriptName(),
                script.getFileName(),
                script.getDescription()
        );
        version.setChecksum(script.getChecksum());

        try {
            // 插入执行记录（标记为执行中）
            version.setSuccess(false);  // 设置为执行中状态（false表示未成功）
            version.setExecutionTime(null);  // 执行时间暂时为空
            versionMapper.insertVersion(version);

            // 执行SQL脚本
            executeSqlScript(script.getContent());

            // 更新为执行成功
            long executionTime = System.currentTimeMillis() - startTime;
            version.Success((int) executionTime);
            versionMapper.updateVersion(version);

            result.addExecutedScript(script);
            log.info("✅ 脚本执行成功: {} (耗时 {} ms)", script.getFileName(), executionTime);

        } catch (Exception e) {
            // 更新为执行失败
            long executionTime = System.currentTimeMillis() - startTime;
            version.setSuccess(false);  // 设置为失败
            version.setExecutionTime((int) executionTime);  // 设置执行时间
            version.setErrorMessage(e.getMessage());  // 设置错误信息

            try {
                versionMapper.updateVersion(version);
            } catch (Exception updateEx) {
                log.error("❌ 更新版本记录失败", updateEx);
            }

            result.addFailedScript(script, e);
            String errorMsg = String.format("脚本执行失败: %s - %s", script.getFileName(), e.getMessage());
            log.error("❌ {}", errorMsg, e);

            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 执行SQL脚本内容
     *
     * @param sqlContent SQL脚本内容
     */
    private void executeSqlScript(String sqlContent) {
        if (sqlContent == null || sqlContent.trim().isEmpty()) {
            log.warn("⚠️ SQL脚本内容为空，跳过执行");
            return;
        }

        // 分割SQL语句（以分号分隔，但要考虑注释和字符串中的分号）
        List<String> sqlStatements = splitSqlStatements(sqlContent);

        for (String sql : sqlStatements) {
            String trimmedSql = sql.trim();
            if (trimmedSql.isEmpty() || trimmedSql.startsWith("--") || trimmedSql.startsWith("/*")) {
                continue; // 跳过空行和注释
            }

            try {
                versionMapper.executeSql(trimmedSql);
            } catch (Exception e) {
                log.error("❌ SQL语句执行失败: {}", trimmedSql);
                throw e;
            }
        }
    }

    /**
     * 分割SQL语句
     * 简单实现：按分号分割，忽略注释行
     *
     * @param sqlContent SQL内容
     * @return SQL语句列表
     */
    private List<String> splitSqlStatements(String sqlContent) {
        List<String> statements = new ArrayList<>();
        String[] lines = sqlContent.split("\n");
        StringBuilder currentStatement = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            // 跳过注释行
            if (trimmedLine.startsWith("--") || trimmedLine.startsWith("/*") ||
                    trimmedLine.startsWith("SET") || trimmedLine.isEmpty()) {
                continue;
            }

            currentStatement.append(line).append("\n");

            // 如果行以分号结尾，认为是一个完整的语句
            if (trimmedLine.endsWith(";")) {
                String statement = currentStatement.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                currentStatement = new StringBuilder();
            }
        }

        // 处理最后一个语句（可能没有分号结尾）
        String lastStatement = currentStatement.toString().trim();
        if (!lastStatement.isEmpty()) {
            statements.add(lastStatement);
        }

        return statements;
    }

    /**
     * 获取当前数据库版本
     *
     * @return 当前版本信息
     */
    public DatabaseVersion getCurrentVersion() {
        try {
            return versionMapper.selectLatestVersion();
        } catch (Exception e) {
            log.warn("⚠️ 获取当前数据库版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有版本历史
     *
     * @return 版本历史列表
     */
    public List<DatabaseVersion> getVersionHistory() {
        try {
            return versionMapper.selectAllVersions();
        } catch (Exception e) {
            log.error("❌ 获取版本历史失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 迁移结果类
     */
    @Getter
    public static class MigrationResult {
        private final List<MigrationScript> executedScripts = new ArrayList<>();
        private final Map<MigrationScript, Exception> failedScripts = new HashMap<>();
        private boolean success;
        private String message;
        private Exception error;
        private DatabaseBackupService.BackupResult backupResult;

        public int getExecutedCount() {
            return executedScripts.size();
        }

        public int getFailedCount() {
            return failedScripts.size();
        }

        public MigrationResult setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public MigrationResult setMessage(String message) {
            this.message = message;
            return this;
        }

        public MigrationResult setError(Exception error) {
            this.error = error;
            return this;
        }

        public MigrationResult setBackupResult(DatabaseBackupService.BackupResult backupResult) {
            this.backupResult = backupResult;
            return this;
        }

        public void addExecutedScript(MigrationScript script) {
            executedScripts.add(script);
        }

        public void addFailedScript(MigrationScript script, Exception error) {
            failedScripts.put(script, error);
        }
    }
}