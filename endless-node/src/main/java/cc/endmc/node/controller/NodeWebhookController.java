package cc.endmc.node.controller;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.email.EmailService;
import cc.endmc.node.domain.NodeServerSettings;
import cc.endmc.node.service.INodeServerSettingsService;
import cc.endmc.node.service.INodeEmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/node/webhook")
@RequiredArgsConstructor
public class NodeWebhookController extends BaseController {

    private final INodeServerSettingsService settingsService;
    private final INodeEmailTemplateService templateService;
    private final EmailService emailService;

    @PostMapping("/crash")
    public AjaxResult crash(@RequestBody Map<String, Object> body) {
        Long nodeServerId = Long.valueOf(body.get("nodeServerId").toString());
        String serverName = (String) body.getOrDefault("serverName", "Unknown");
        String exitCode = body.getOrDefault("exitCode", "unknown").toString();
        String crashCount = body.getOrDefault("crashCount", "0").toString();
        String timestamp = body.getOrDefault("timestamp",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).toString();

        try {
            NodeServerSettings settings = settingsService.selectNodeServerSettingsByServerId(nodeServerId);
            if (settings == null || settings.getCrashNotifyEnabled() == null || settings.getCrashNotifyEnabled() != 1) {
                return success("通知未启用");
            }
            String notifyEmail = settings.getNotifyEmail();
            if (notifyEmail == null || notifyEmail.isEmpty()) {
                return success("通知邮箱未配置");
            }

            Map<String, String> vars = new HashMap<>();
            vars.put("serverName", serverName);
            vars.put("exitCode", exitCode);
            vars.put("crashCount", crashCount);
            vars.put("timestamp", timestamp);
            vars.put("nodeServerId", String.valueOf(nodeServerId));

            String subject = templateService.renderSubject("crash_notify", vars);
            String content = templateService.renderTemplate("crash_notify", vars);
            if (content == null) return error("通知模板不存在");
            if (subject == null) subject = "服务器崩溃通知 - " + serverName;

            emailService.push(notifyEmail, subject, content);
            log.info("崩溃通知已发送，实例：{}，收件人：{}", serverName, notifyEmail);
            return success("通知发送成功");
        } catch (Exception e) {
            log.error("崩溃通知发送失败，实例：{}", serverName, e);
            return error("通知发送失败：" + e.getMessage());
        }
    }

    @PostMapping("/backup")
    public AjaxResult backup(@RequestBody Map<String, Object> body) {
        Long nodeServerId = Long.valueOf(body.get("nodeServerId").toString());
        String serverName = (String) body.getOrDefault("serverName", "Unknown");
        String backupFile = body.getOrDefault("backupFile", "").toString();
        String backupSize = body.getOrDefault("backupSize", "0").toString();
        String timestamp = body.getOrDefault("timestamp",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).toString();

        try {
            NodeServerSettings settings = settingsService.selectNodeServerSettingsByServerId(nodeServerId);
            if (settings == null || settings.getBackupNotifyEnabled() == null || settings.getBackupNotifyEnabled() != 1) {
                return success("通知未启用");
            }
            String notifyEmail = settings.getNotifyEmail();
            if (notifyEmail == null || notifyEmail.isEmpty()) {
                return success("通知邮箱未配置");
            }

            Map<String, String> vars = new HashMap<>();
            vars.put("serverName", serverName);
            vars.put("backupFile", backupFile);
            vars.put("backupSize", backupSize);
            vars.put("timestamp", timestamp);
            vars.put("nodeServerId", String.valueOf(nodeServerId));

            String subject = templateService.renderSubject("backup_notify", vars);
            String content = templateService.renderTemplate("backup_notify", vars);
            if (content == null) return error("通知模板不存在");
            if (subject == null) subject = "备份通知 - " + serverName;

            emailService.push(notifyEmail, subject, content);
            log.info("备份通知已发送，实例：{}，收件人：{}", serverName, notifyEmail);
            return success("通知发送成功");
        } catch (Exception e) {
            log.error("备份通知发送失败，实例：{}", serverName, e);
            return error("通知发送失败：" + e.getMessage());
        }
    }
}
