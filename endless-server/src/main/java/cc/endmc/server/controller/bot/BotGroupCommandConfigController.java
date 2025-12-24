package cc.endmc.server.controller.bot;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.bot.BotGroupCommandConfig;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组指令功能配置Controller
 *
 * @author Memory
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/bot/cmdconfig")
public class BotGroupCommandConfigController extends BaseController {
    @Autowired
    private IBotGroupCommandConfigService botGroupCommandConfigService;

    /**
     * 查询群组指令功能配置列表
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(BotGroupCommandConfig botGroupCommandConfig) {
        startPage();
        List<BotGroupCommandConfig> list = botGroupCommandConfigService.selectBotGroupCommandConfigList(botGroupCommandConfig);
        return getDataTable(list);
    }

    /**
     * 导出群组指令功能配置列表
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:export')")
    @Log(title = "群组指令功能配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BotGroupCommandConfig botGroupCommandConfig) {
        List<BotGroupCommandConfig> list = botGroupCommandConfigService.selectBotGroupCommandConfigList(botGroupCommandConfig);
        ExcelUtil<BotGroupCommandConfig> util = new ExcelUtil<BotGroupCommandConfig>(BotGroupCommandConfig.class);
        util.exportExcel(response, list, "群组指令功能配置数据");
    }

    /**
     * 获取群组指令功能配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(botGroupCommandConfigService.selectBotGroupCommandConfigById(id));
    }

    /**
     * 新增群组指令功能配置
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:add')")
    @Log(title = "群组指令功能配置", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody BotGroupCommandConfig botGroupCommandConfig) {
        return toAjax(botGroupCommandConfigService.insertBotGroupCommandConfig(botGroupCommandConfig));
    }

    /**
     * 修改群组指令功能配置
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:edit')")
    @Log(title = "群组指令功能配置", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody BotGroupCommandConfig botGroupCommandConfig) {
        return toAjax(botGroupCommandConfigService.updateBotGroupCommandConfig(botGroupCommandConfig));
    }

    /**
     * 删除群组指令功能配置
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:remove')")
    @Log(title = "群组指令功能配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(botGroupCommandConfigService.deleteBotGroupCommandConfigByIds(ids));
    }

    /**
     * 清除所有指令配置缓存
     */
    @PreAuthorize("@ss.hasPermi('bot:cmdconfig:edit')")
    @Log(title = "清除指令配置缓存", businessType = BusinessType.DELETE)
    @DeleteMapping("/clearCache")
    public AjaxResult clearCache() {
        botGroupCommandConfigService.clearAllCache();
        return success("缓存清除成功");
    }
}
