package cc.endmc.server.controller.quiz;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 答题记录Controller
 *
 * @author Memory
 * @date 2025-03-20
 */
@RestController
@RequestMapping("/quiz/submission")
public class WhitelistQuizSubmissionController extends BaseController {
    @Autowired
    private IWhitelistQuizSubmissionService whitelistQuizSubmissionService;

    /**
     * 查询答题记录列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:list')")
    @GetMapping("/list")
    public TableDataInfo list(WhitelistQuizSubmission whitelistQuizSubmission) {
        startPage();
        List<WhitelistQuizSubmission> list = whitelistQuizSubmissionService.selectWhitelistQuizSubmissionList(whitelistQuizSubmission);
        return getDataTable(list);
    }

    /**
     * 导出答题记录列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:export')")
    @Log(title = "答题记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WhitelistQuizSubmission whitelistQuizSubmission) {
        List<WhitelistQuizSubmission> list = whitelistQuizSubmissionService.selectWhitelistQuizSubmissionList(whitelistQuizSubmission);
        ExcelUtil<WhitelistQuizSubmission> util = new ExcelUtil<WhitelistQuizSubmission>(WhitelistQuizSubmission.class);
        util.exportExcel(response, list, "答题记录数据");
    }

    /**
     * 获取答题记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(whitelistQuizSubmissionService.selectWhitelistQuizSubmissionById(id));
    }

    /**
     * 新增答题记录
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:add')")
    @Log(title = "答题记录", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody WhitelistQuizSubmission whitelistQuizSubmission) {
        return toAjax(whitelistQuizSubmissionService.insertWhitelistQuizSubmission(whitelistQuizSubmission));
    }

    /**
     * 修改答题记录
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:edit')")
    @Log(title = "答题记录", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody WhitelistQuizSubmission whitelistQuizSubmission) {
        return toAjax(whitelistQuizSubmissionService.updateWhitelistQuizSubmission(whitelistQuizSubmission));
    }

    /**
     * 删除答题记录
     */
    @PreAuthorize("@ss.hasPermi('quiz:submission:remove')")
    @Log(title = "答题记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(whitelistQuizSubmissionService.deleteWhitelistQuizSubmissionByIds(ids));
    }
}
