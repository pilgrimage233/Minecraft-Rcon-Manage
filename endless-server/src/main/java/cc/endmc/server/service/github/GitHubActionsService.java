package cc.endmc.server.service.github;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * GitHub Actions 服务
 * 用于触发和监控 GitHub Actions 工作流
 */
@Slf4j
@Service
public class GitHubActionsService {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    @Value("${github.token:}")
    private String githubToken;
    @Value("${github.repository:pilgrimage233/Minecraft-Rcon-Manage}")
    private String repository;
    @Value("${github.workflow-file:release.yml}")
    private String workflowFile;

    /**
     * 触发工作流构建
     *
     * @param branch       分支名称
     * @param isPrerelease 是否为预发布版本
     * @return 触发结果
     */
    public Map<String, Object> triggerWorkflow(String branch, boolean isPrerelease) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (githubToken == null || githubToken.isEmpty()) {
                result.put("success", false);
                result.put("message", "GitHub Token 未配置，请在配置文件中设置 github.token");
                return result;
            }

            // 构建请求 URL
            String url = String.format("%s/repos/%s/actions/workflows/%s/dispatches",
                    GITHUB_API_BASE, repository, workflowFile);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("ref", branch);

            JSONObject inputs = new JSONObject();
            inputs.put("branch", branch);
            inputs.put("is_prerelease", isPrerelease);
            requestBody.put("inputs", inputs);

            log.info("触发 GitHub Actions 工作流: {}, 分支: {}, 预发布: {}", workflowFile, branch, isPrerelease);

            // 发送请求
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .body(requestBody.toString())
                    .timeout(10000)
                    .execute();

            if (response.getStatus() == 204) {
                result.put("success", true);
                result.put("message", "工作流触发成功");
                result.put("branch", branch);
                result.put("isPrerelease", isPrerelease);
                log.info("GitHub Actions 工作流触发成功");
            } else {
                result.put("success", false);
                result.put("message", "触发失败: " + response.body());
                result.put("statusCode", response.getStatus());
                log.error("触发 GitHub Actions 失败: {}, 响应: {}", response.getStatus(), response.body());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "触发异常: " + e.getMessage());
            log.error("触发 GitHub Actions 异常", e);
        }

        return result;
    }

    /**
     * 获取最近的工作流运行记录
     *
     * @param limit 返回数量限制
     * @return 工作流运行记录列表
     */
    public JSONArray getRecentWorkflowRuns(int limit) {
        try {
            if (githubToken == null || githubToken.isEmpty()) {
                log.warn("GitHub Token 未配置");
                return new JSONArray();
            }

            String url = String.format("%s/repos/%s/actions/workflows/%s/runs?per_page=%d",
                    GITHUB_API_BASE, repository, workflowFile, limit);

            HttpResponse response = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .timeout(10000)
                    .execute();

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSON.parseObject(response.body());
                return jsonResponse.getJSONArray("workflow_runs");
            } else {
                log.error("获取工作流运行记录失败: {}", response.getStatus());
                return new JSONArray();
            }

        } catch (Exception e) {
            log.error("获取工作流运行记录异常", e);
            return new JSONArray();
        }
    }

    /**
     * 获取工作流运行详情
     *
     * @param runId 运行ID
     * @return 运行详情
     */
    public JSONObject getWorkflowRunDetails(Long runId) {
        try {
            if (githubToken == null || githubToken.isEmpty()) {
                log.warn("GitHub Token 未配置");
                return null;
            }

            String url = String.format("%s/repos/%s/actions/runs/%d",
                    GITHUB_API_BASE, repository, runId);

            HttpResponse response = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .timeout(10000)
                    .execute();

            if (response.getStatus() == 200) {
                return JSON.parseObject(response.body());
            } else {
                log.error("获取工作流运行详情失败: {}", response.getStatus());
                return null;
            }

        } catch (Exception e) {
            log.error("获取工作流运行详情异常", e);
            return null;
        }
    }

    /**
     * 格式化工作流状态
     *
     * @param status     状态
     * @param conclusion 结论
     * @return 格式化后的状态
     */
    public String formatWorkflowStatus(String status, String conclusion) {
        if ("completed".equals(status)) {
            if ("success".equals(conclusion)) {
                return "✅ 成功";
            } else if ("failure".equals(conclusion)) {
                return "❌ 失败";
            } else if ("cancelled".equals(conclusion)) {
                return "⚠️ 已取消";
            } else {
                return "❓ " + conclusion;
            }
        } else if ("in_progress".equals(status)) {
            return "🔄 进行中";
        } else if ("queued".equals(status)) {
            return "⏳ 排队中";
        } else {
            return "❓ " + status;
        }
    }
}
