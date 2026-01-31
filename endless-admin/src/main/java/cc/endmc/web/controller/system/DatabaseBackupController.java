package cc.endmc.web.controller.system;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.framework.database.domain.BackupInfo;
import cc.endmc.framework.database.service.DatabaseBackupExecutor;
import cc.endmc.framework.database.service.DatabaseBackupManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库备份管理控制器
 *
 * @author Memory
 * @since 2026-01-31
 */
@Slf4j
@RestController
@RequestMapping("/system/backup")
@RequiredArgsConstructor
public class DatabaseBackupController extends BaseController {

    private final DatabaseBackupManagementService backupManagementService;
    private final DatabaseBackupExecutor backupExecutor;

    /**
     * 获取备份列表
     */
    @PreAuthorize("@ss.hasPermi('system:backup:list')")
    @GetMapping("/list")
    public TableDataInfo list() {
        List<BackupInfo> list = backupManagementService.listBackups();
        return getDataTable(list);
    }

    /**
     * 获取备份详情
     */
    @PreAuthorize("@ss.hasPermi('system:backup:query')")
    @GetMapping("/{backupId}")
    public AjaxResult getInfo(@PathVariable String backupId) {
        BackupInfo info = backupManagementService.getBackupDetail(backupId);
        if (info == null) {
            return AjaxResult.error("备份不存在");
        }
        return AjaxResult.success(info);
    }

    /**
     * 手动执行备份
     */
    @PreAuthorize("@ss.hasPermi('system:backup:add')")
    @PostMapping("/manual")
    public AjaxResult manualBackup() {
        try {
            log.info("手动触发数据库备份");
            backupExecutor.executeManualBackup();
            return AjaxResult.success("备份任务已启动");
        } catch (Exception e) {
            log.error("手动备份失败", e);
            return AjaxResult.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 恢复指定表的数据
     */
    @PreAuthorize("@ss.hasPermi('system:backup:restore')")
    @PostMapping("/restore")
    public AjaxResult restoreTable(@RequestParam String backupId, @RequestParam String tableName) {
        try {
            log.info("开始恢复表数据: backupId={}, tableName={}", backupId, tableName);
            boolean success = backupManagementService.restoreTable(backupId, tableName);
            if (success) {
                return AjaxResult.success("表 " + tableName + " 恢复成功");
            } else {
                return AjaxResult.error("表 " + tableName + " 恢复失败");
            }
        } catch (Exception e) {
            log.error("恢复表数据失败", e);
            return AjaxResult.error("恢复失败: " + e.getMessage());
        }
    }

    /**
     * 全量回滚（恢复所有表）
     */
    @PreAuthorize("@ss.hasPermi('system:backup:restore')")
    @PostMapping("/restoreAll")
    public AjaxResult restoreAllTables(@RequestParam String backupId) {
        try {
            log.info("开始全量回滚: backupId={}", backupId);
            int successCount = backupManagementService.restoreAllTables(backupId);
            if (successCount > 0) {
                return AjaxResult.success("全量回滚成功，共恢复 " + successCount + " 个表");
            } else {
                return AjaxResult.error("全量回滚失败");
            }
        } catch (Exception e) {
            log.error("全量回滚失败", e);
            return AjaxResult.error("回滚失败: " + e.getMessage());
        }
    }

    /**
     * 删除备份
     */
    @PreAuthorize("@ss.hasPermi('system:backup:remove')")
    @DeleteMapping("/{backupId}")
    public AjaxResult remove(@PathVariable String backupId) {
        try {
            log.info("删除备份: {}", backupId);
            boolean success = backupManagementService.deleteBackup(backupId);
            if (success) {
                return AjaxResult.success("备份删除成功");
            } else {
                return AjaxResult.error("备份删除失败");
            }
        } catch (Exception e) {
            log.error("删除备份失败", e);
            return AjaxResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份统计信息
     */
    @PreAuthorize("@ss.hasPermi('system:backup:list')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = backupManagementService.getBackupStatistics();
        return AjaxResult.success(stats);
    }

    /**
     * 获取可备份的表列表
     */
    @PreAuthorize("@ss.hasPermi('system:backup:list')")
    @GetMapping("/tables")
    public AjaxResult getBackupTables() {
        List<String> tables = backupExecutor.getBackupTables();
        return AjaxResult.success(tables);
    }
}
