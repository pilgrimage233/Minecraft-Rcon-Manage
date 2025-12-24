package cc.endmc.web.controller.system;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.SecurityUtils;

import cc.endmc.system.domain.SysFeedbackRecord;
import cc.endmc.system.service.ISysFeedbackRecordService;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 反馈代理控制器
 * 将用户反馈转发到反馈系统，同时在本地数据库记录
 */
@Slf4j
@RestController
@RequestMapping("/system/feedback")
@RequiredArgsConstructor
public class SysFeedbackController {
    // 最大文件大小 1MB
    private static final long MAX_FILE_SIZE = 1024 * 1024;

    @Value("${feedback.api.url:https://feed.endmc.cc/api/feedback}")
    private String feedbackApiUrl;
    @Value("${endless.version}")
    private String appVersion;

    private final ISysFeedbackRecordService feedbackRecordService;

    /**
     * 提交反馈
     */
    @PostMapping
    public AjaxResult submitFeedback(@RequestBody Map<String, Object> feedbackData) {
        try {
            // 添加应用版本
            feedbackData.put("appVersion", appVersion);

            Integer feedbackType = (Integer) feedbackData.get("feedbackType");
            String title = (String) feedbackData.get("title");

            log.info("收到用户反馈: type={}, title={}", feedbackType, title);

            // 转发到反馈系统
            String response = HttpUtil.createPost(feedbackApiUrl)
                    .header("Content-Type", "application/json")
                    .body(JSONObject.toJSONString(feedbackData))
                    .execute()
                    .body();

            JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("code") == 200) {
                JSONObject data = result.getJSONObject("data");
                String uuid = data.getString("uuid");

                // 保存到本地数据库
                SysFeedbackRecord record = new SysFeedbackRecord();
                record.setUuid(uuid);
                record.setFeedbackType(feedbackType);
                record.setTitle(title);
                record.setStatus(0);
                try {
                    record.setUserId(SecurityUtils.getUserId());
                    record.setUserName(SecurityUtils.getUsername());
                } catch (Exception e) {
                    record.setUserId(0L);
                    record.setUserName("anonymous");
                }
                feedbackRecordService.insert(record);

                log.info("反馈提交成功, uuid={}", uuid);
                return AjaxResult.success("反馈提交成功").put("uuid", uuid);
            } else {
                log.warn("反馈提交失败: {}", result.getString("message"));
                return AjaxResult.error(result.getString("message"));
            }

        } catch (Exception e) {
            log.error("反馈提交异常", e);
            return AjaxResult.error("反馈提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的反馈历史（从本地数据库）
     */
    @GetMapping("/my")
    public AjaxResult getMyFeedbackList() {
        try {
            Long userId = SecurityUtils.getUserId();
            List<SysFeedbackRecord> list = feedbackRecordService.selectByUserId(userId);
            return AjaxResult.success(list);
        } catch (Exception e) {
            log.error("获取反馈历史失败", e);
            return AjaxResult.error("获取反馈历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取反馈详情（通过UUID从远程系统查询）
     */
    @GetMapping("/{uuid}")
    public AjaxResult getFeedbackDetail(@PathVariable String uuid) {
        try {
            String response = HttpUtil.createGet(feedbackApiUrl + "/uuid/" + uuid)
                    .execute()
                    .body();

            final JSONObject result = JSONObject.parseObject(response);

            if (result.getInteger("code") == 200) {
                JSONObject data = result.getJSONObject("data");

                // 同步更新本地状态
                Integer status = data.getInteger("status");
                if (status != null) {
                    feedbackRecordService.updateStatus(uuid, status);
                }

                return AjaxResult.success(data);
            } else {
                return AjaxResult.error(result.getString("message"));
            }

        } catch (Exception e) {
            log.error("获取反馈详情失败", e);
            return AjaxResult.error("获取反馈详情失败: " + e.getMessage());
        }
    }

    /**
     * 上传反馈附件
     */
    @PostMapping("/upload")
    public AjaxResult uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return AjaxResult.error("文件大小不能超过1MB");
            }

            // 检查文件类型
            String originalFilename = file.getOriginalFilename();
            if (!isAllowedFileType(originalFilename)) {
                return AjaxResult.error("不支持的文件类型");
            }

            // 将文件转为 Base64 存储
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            String dataUrl = "data:" + file.getContentType() + ";base64," + base64Content;

            log.info("附件上传成功: {}, size: {} bytes", originalFilename, file.getSize());

            return AjaxResult.success("上传成功", dataUrl);

        } catch (Exception e) {
            log.error("附件上传失败", e);
            return AjaxResult.error("附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否允许的文件类型
     */
    private boolean isAllowedFileType(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".gif") ||
                lower.endsWith(".pdf") || lower.endsWith(".txt") ||
                lower.endsWith(".log");
    }
}
