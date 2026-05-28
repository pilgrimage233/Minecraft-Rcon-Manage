package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.email.EmailService;
import cc.endmc.node.domain.NodeEmailTemplate;
import cc.endmc.node.service.INodeEmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/node/email/template")
@RequiredArgsConstructor
public class NodeEmailTemplateController extends BaseController {

    private final INodeEmailTemplateService templateService;
    private final EmailService emailService;

    @PreAuthorize("@ss.hasPermi('node:email:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeEmailTemplate nodeEmailTemplate) {
        startPage();
        List<NodeEmailTemplate> list = templateService.selectNodeEmailTemplateList(nodeEmailTemplate);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('node:email:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(templateService.selectNodeEmailTemplateById(id));
    }

    @PreAuthorize("@ss.hasPermi('node:email:add')")
    @Log(title = "邮件模板", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeEmailTemplate nodeEmailTemplate) {
        return toAjax(templateService.insertNodeEmailTemplate(nodeEmailTemplate));
    }

    @PreAuthorize("@ss.hasPermi('node:email:edit')")
    @Log(title = "邮件模板", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeEmailTemplate nodeEmailTemplate) {
        return toAjax(templateService.updateNodeEmailTemplate(nodeEmailTemplate));
    }

    @PreAuthorize("@ss.hasPermi('node:email:remove')")
    @Log(title = "邮件模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(templateService.deleteNodeEmailTemplateByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('node:email:edit')")
    @Log(title = "邮件测试", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public AjaxResult sendTestEmail(@RequestParam("email") String email) {
        try {
            emailService.push(email, "Endless 邮件测试",
                    "<h3>邮件配置测试成功</h3><p>如果您收到此邮件，说明邮件服务配置正确。</p>");
            return success("测试邮件发送成功");
        } catch (Exception e) {
            return error("测试邮件发送失败：" + e.getMessage());
        }
    }
}
