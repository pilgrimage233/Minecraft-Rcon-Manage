package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.node.domain.NodeServerSettings;
import cc.endmc.node.service.INodeServerSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/node/settings")
@RequiredArgsConstructor
public class NodeServerSettingsController extends BaseController {

    private final INodeServerSettingsService settingsService;

    @PreAuthorize("@ss.hasPermi('node:settings:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeServerSettings nodeServerSettings) {
        startPage();
        List<NodeServerSettings> list = settingsService.selectNodeServerSettingsList(nodeServerSettings);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('node:settings:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(settingsService.selectNodeServerSettingsById(id));
    }

    @PreAuthorize("@ss.hasPermi('node:settings:query')")
    @GetMapping("/byServer/{serverId}")
    public AjaxResult getByServerId(@PathVariable("serverId") Long serverId) {
        return success(settingsService.selectNodeServerSettingsByServerId(serverId));
    }

    @PreAuthorize("@ss.hasPermi('node:settings:add')")
    @Log(title = "实例运维策略", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeServerSettings nodeServerSettings) {
        return toAjax(settingsService.insertNodeServerSettings(nodeServerSettings));
    }

    @PreAuthorize("@ss.hasPermi('node:settings:edit')")
    @Log(title = "实例运维策略", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeServerSettings nodeServerSettings) {
        return toAjax(settingsService.updateNodeServerSettings(nodeServerSettings));
    }

    @PreAuthorize("@ss.hasPermi('node:settings:remove')")
    @Log(title = "实例运维策略", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(settingsService.deleteNodeServerSettingsByIds(ids));
    }
}
