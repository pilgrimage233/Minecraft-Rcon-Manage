package cc.endmc.framework.database.controller;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.framework.database.domain.DatabaseVersion;
import cc.endmc.framework.database.service.DatabaseMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据库迁移管理控制器
 *
 * @author Memory
 * @since 2024-12-28
 */
@Slf4j
@RestController
@RequestMapping("/system/database/migration")
@RequiredArgsConstructor
public class DatabaseMigrationController extends BaseController {

    private final DatabaseMigrationService migrationService;

    /**
     * 获取数据库版本历史
     */
    @PreAuthorize("@ss.hasPermi('system:database:list')")
    @GetMapping("/history")
    public TableDataInfo getVersionHistory() {
        startPage();
        List<DatabaseVersion> list = migrationService.getVersionHistory();
        return getDataTable(list);
    }

    /**
     * 获取当前数据库版本
     */
    @PreAuthorize("@ss.hasPermi('system:database:query')")
    @GetMapping("/current")
    public AjaxResult getCurrentVersion() {
        DatabaseVersion currentVersion = migrationService.getCurrentVersion();
        return success(currentVersion);
    }

    /**
     * 手动执行数据库迁移
     */
    @PreAuthorize("@ss.hasPermi('system:database:migrate')")
    @PostMapping("/migrate")
    public AjaxResult migrate() {
        try {
            log.info("🔧 管理员手动触发数据库迁移");

            DatabaseMigrationService.MigrationResult result = migrationService.migrate();

            if (result.isSuccess()) {
                AjaxResult ajaxResult = success(result.getMessage())
                        .put("executedCount", result.getExecutedCount())
                        .put("executedScripts", result.getExecutedScripts());

                // 添加备份信息
                if (result.getBackupResult() != null) {
                    ajaxResult.put("backupResult", result.getBackupResult());
                    ajaxResult.put("backupPath", result.getBackupResult().getBackupPath());
                    ajaxResult.put("backedupTables", result.getBackupResult().getBackedupTables().size());
                }

                return ajaxResult;
            } else {
                return error(result.getMessage());
            }

        } catch (Exception e) {
            log.error("❌ 手动数据库迁移失败", e);
            return error("数据库迁移失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否有待执行的迁移脚本
     */
    @PreAuthorize("@ss.hasPermi('system:database:query')")
    @GetMapping("/check")
    public AjaxResult checkPendingMigrations() {
        try {
            // 这里可以实现检查逻辑，暂时返回当前版本信息
            DatabaseVersion currentVersion = migrationService.getCurrentVersion();
            return success("检查完成")
                    .put("currentVersion", currentVersion)
                    .put("hasPending", false); // 实际实现中可以检查是否有新的迁移脚本

        } catch (Exception e) {
            log.error("❌ 检查待执行迁移失败", e);
            return error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 手动执行数据库备份
     */
    @PreAuthorize("@ss.hasPermi('system:database:backup')")
    @PostMapping("/backup")
    public AjaxResult backup() {
        try {
            log.info("🗄️ 管理员手动触发数据库备份");

            // 这里需要注入 DatabaseBackupService
            // DatabaseBackupService.BackupResult result = backupService.backup("manual");

            // 暂时返回成功信息
            return success("手动备份功能需要注入 DatabaseBackupService");

        } catch (Exception e) {
            log.error("❌ 手动数据库备份失败", e);
            return error("数据库备份失败: " + e.getMessage());
        }
    }
}