package cc.endmc.server.controller.email;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.email.CustomEmailTemplates;
import cc.endmc.server.service.email.ICustomEmailTemplatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 自定义邮件通知模板Controller
 *
 * @author memory
 * @date 2025-10-03
 */
@RestController
@RequestMapping("/email/templates")
public class CustomEmailTemplatesController extends BaseController {
    @Autowired
    private ICustomEmailTemplatesService customEmailTemplatesService;

    /**
     * 查询自定义邮件通知模板列表
     */
    @PreAuthorize("@ss.hasPermi('email:templates:list')")
    @GetMapping("/list")
    public TableDataInfo list(CustomEmailTemplates customEmailTemplates) {
        startPage();
        List<CustomEmailTemplates> list = customEmailTemplatesService.selectCustomEmailTemplatesList(customEmailTemplates);
        return getDataTable(list);
    }

    /**
     * 导出自定义邮件通知模板列表
     */
    @PreAuthorize("@ss.hasPermi('email:templates:export')")
    @Log(title = "自定义邮件通知模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CustomEmailTemplates customEmailTemplates) {
        List<CustomEmailTemplates> list = customEmailTemplatesService.selectCustomEmailTemplatesList(customEmailTemplates);
        ExcelUtil<CustomEmailTemplates> util = new ExcelUtil<CustomEmailTemplates>(CustomEmailTemplates.class);
        util.exportExcel(response, list, "自定义邮件通知模板数据");
    }

    /**
     * 获取自定义邮件通知模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('email:templates:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(customEmailTemplatesService.selectCustomEmailTemplatesById(id));
    }

    /**
     * 新增自定义邮件通知模板
     */
    @PreAuthorize("@ss.hasPermi('email:templates:add')")
    @Log(title = "自定义邮件通知模板", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody CustomEmailTemplates customEmailTemplates) {
        return toAjax(customEmailTemplatesService.insertCustomEmailTemplates(customEmailTemplates));
    }

    /**
     * 修改自定义邮件通知模板
     */
    @PreAuthorize("@ss.hasPermi('email:templates:edit')")
    @Log(title = "自定义邮件通知模板", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody CustomEmailTemplates customEmailTemplates) {
        return toAjax(customEmailTemplatesService.updateCustomEmailTemplates(customEmailTemplates));
    }

    /**
     * 删除自定义邮件通知模板
     */
    @PreAuthorize("@ss.hasPermi('email:templates:remove')")
    @Log(title = "自定义邮件通知模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(customEmailTemplatesService.deleteCustomEmailTemplatesByIds(ids));
    }

    /**
     * 下载白名单Excel模板
     */
    @GetMapping("/downloadDocument")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 获取模板文件路径
            String templatePath = "other/email_templates.docx";
            // 获取模板文件
            Resource resource = new ClassPathResource(templatePath);
            // 获取文件名
            String fileName = "自定义邮件模板说明.md";

            // 设置响应头
            response.setContentType("application/octet-stream; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 将文件写入响应流
            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            logger.error("下载模板失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"下载模板失败\"}");
            } catch (IOException ex) {
                logger.error("写入错误响应失败", ex);
            }
        }
    }

}
