package cc.endmc.server.controller.quiz;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 题库配置Controller
 *
 * @author ruoyi
 * @date 2025-03-21
 */
@RestController
@RequestMapping("/quiz/config")
public class WhitelistQuizConfigController extends BaseController {
    @Autowired
    private IWhitelistQuizConfigService whitelistQuizConfigService;

    /**
     * 查询题库配置列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(WhitelistQuizConfig whitelistQuizConfig) {
        startPage();
        List<WhitelistQuizConfig> list = whitelistQuizConfigService.selectWhitelistQuizConfigList(whitelistQuizConfig);
        return getDataTable(list);
    }

    /**
     * 导出题库配置列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:export')")
    @Log(title = "题库配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WhitelistQuizConfig whitelistQuizConfig) {
        List<WhitelistQuizConfig> list = whitelistQuizConfigService.selectWhitelistQuizConfigList(whitelistQuizConfig);
        ExcelUtil<WhitelistQuizConfig> util = new ExcelUtil<WhitelistQuizConfig>(WhitelistQuizConfig.class);
        util.exportExcel(response, list, "题库配置数据");
    }

    /**
     * 获取题库配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(whitelistQuizConfigService.selectWhitelistQuizConfigById(id));
    }

    /**
     * 新增题库配置
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:add')")
    @Log(title = "题库配置", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody WhitelistQuizConfig whitelistQuizConfig) {
        return toAjax(whitelistQuizConfigService.insertWhitelistQuizConfig(whitelistQuizConfig));
    }

    /**
     * 修改题库配置
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:edit')")
    @Log(title = "题库配置", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody WhitelistQuizConfig whitelistQuizConfig) {
        return toAjax(whitelistQuizConfigService.updateWhitelistQuizConfig(whitelistQuizConfig));
    }

    /**
     * 删除题库配置
     */
    @PreAuthorize("@ss.hasPermi('quiz:config:remove')")
    @Log(title = "题库配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(whitelistQuizConfigService.deleteWhitelistQuizConfigByIds(ids));
    }
}
