package cc.endmc.server.controller.bot;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.service.bot.IQqBotConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * QQ机器人配置Controller
 *
 * @author ruoyi
 * @date 2025-03-12
 */
@RestController
@RequestMapping("/bot/config")
public class QqBotConfigController extends BaseController {
    @Autowired
    private IQqBotConfigService qqBotConfigService;

    /**
     * 查询QQ机器人配置列表
     */
    @PreAuthorize("@ss.hasPermi('bot:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(QqBotConfig qqBotConfig) {
        startPage();
        List<QqBotConfig> list = qqBotConfigService.selectQqBotConfigList(qqBotConfig);
        return getDataTable(list);
    }

    /**
     * 导出QQ机器人配置列表
     */
    @PreAuthorize("@ss.hasPermi('bot:config:export')")
    @Log(title = "QQ机器人配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QqBotConfig qqBotConfig) {
        List<QqBotConfig> list = qqBotConfigService.selectQqBotConfigList(qqBotConfig);
        ExcelUtil<QqBotConfig> util = new ExcelUtil<QqBotConfig>(QqBotConfig.class);
        util.exportExcel(response, list, "QQ机器人配置数据");
    }

    /**
     * 获取QQ机器人配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('bot:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(qqBotConfigService.selectQqBotConfigById(id));
    }

    /**
     * 新增QQ机器人配置
     */
    @PreAuthorize("@ss.hasPermi('bot:config:add')")
    @Log(title = "QQ机器人配置", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody QqBotConfig qqBotConfig) {
        return toAjax(qqBotConfigService.insertQqBotConfig(qqBotConfig));
    }

    /**
     * 修改QQ机器人配置
     */
    @PreAuthorize("@ss.hasPermi('bot:config:edit')")
    @Log(title = "QQ机器人配置", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody QqBotConfig qqBotConfig) {
        return toAjax(qqBotConfigService.updateQqBotConfig(qqBotConfig));
    }

    /**
     * 删除QQ机器人配置
     */
    @PreAuthorize("@ss.hasPermi('bot:config:remove')")
    @Log(title = "QQ机器人配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(qqBotConfigService.deleteQqBotConfigByIds(ids));
    }
}
