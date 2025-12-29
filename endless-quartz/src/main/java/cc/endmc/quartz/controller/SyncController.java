package cc.endmc.quartz.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.quartz.task.SyncTask;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 同步任务控制器
 *
 * @author Memory
 * @date 2025-12-27
 */
@RestController
@RequestMapping("/quartz/sync")
@RequiredArgsConstructor
public class SyncController extends BaseController {

    private final SyncTask syncTask;

    /**
     * 手动触发同步所有服务器数据
     */
    @PreAuthorize("@ss.hasPermi('quartz:sync:execute')")
    @Log(title = "同步任务", businessType = BusinessType.OTHER)
    @PostMapping("/all")
    public AjaxResult syncAllServerData() {
        try {
            syncTask.syncAllServerData();
            return success("同步任务执行成功");
        } catch (Exception e) {
            return error("同步任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发同步指定服务器数据
     */
    @PreAuthorize("@ss.hasPermi('quartz:sync:execute')")
    @Log(title = "同步任务", businessType = BusinessType.OTHER)
    @PostMapping("/server/{rconServerId}")
    public AjaxResult syncServerData(@PathVariable Long rconServerId) {
        try {
            syncTask.syncServerData(rconServerId);
            return success("同步任务执行成功");
        } catch (Exception e) {
            return error("同步任务执行失败: " + e.getMessage());
        }
    }
}