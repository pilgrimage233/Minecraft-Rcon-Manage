package cc.endmc.server.controller.quiz;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.quiz.WhitelistQuizQuestion;
import cc.endmc.server.service.quiz.IWhitelistQuizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 白名单申请题库问题Controller
 *
 * @author Memory
 * @date 2025-03-19
 */
@RestController
@RequestMapping("/quiz/question")
public class WhitelistQuizQuestionController extends BaseController {
    @Autowired
    private IWhitelistQuizQuestionService whitelistQuizQuestionService;

    /**
     * 查询白名单申请题库问题列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(WhitelistQuizQuestion whitelistQuizQuestion) {
        startPage();
        List<WhitelistQuizQuestion> list = whitelistQuizQuestionService.selectWhitelistQuizQuestionList(whitelistQuizQuestion);
        return getDataTable(list);
    }

    /**
     * 导出白名单申请题库问题列表
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:export')")
    @Log(title = "白名单申请题库问题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WhitelistQuizQuestion whitelistQuizQuestion) {
        List<WhitelistQuizQuestion> list = whitelistQuizQuestionService.selectWhitelistQuizQuestionList(whitelistQuizQuestion);
        ExcelUtil<WhitelistQuizQuestion> util = new ExcelUtil<WhitelistQuizQuestion>(WhitelistQuizQuestion.class);
        util.exportExcel(response, list, "白名单申请题库问题数据");
    }

    /**
     * 获取白名单申请题库问题详细信息
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(whitelistQuizQuestionService.selectWhitelistQuizQuestionById(id));
    }

    /**
     * 新增白名单申请题库问题
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:add')")
    @Log(title = "白名单申请题库问题", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody WhitelistQuizQuestion whitelistQuizQuestion) {
        return toAjax(whitelistQuizQuestionService.insertWhitelistQuizQuestion(whitelistQuizQuestion));
    }

    /**
     * 修改白名单申请题库问题
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:edit')")
    @Log(title = "白名单申请题库问题", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody WhitelistQuizQuestion whitelistQuizQuestion) {
        return toAjax(whitelistQuizQuestionService.updateWhitelistQuizQuestion(whitelistQuizQuestion));
    }

    /**
     * 删除白名单申请题库问题
     */
    @PreAuthorize("@ss.hasPermi('quiz:question:remove')")
    @Log(title = "白名单申请题库问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(whitelistQuizQuestionService.deleteWhitelistQuizQuestionByIds(ids));
    }
}
