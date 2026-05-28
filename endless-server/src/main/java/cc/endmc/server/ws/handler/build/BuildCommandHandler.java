package cc.endmc.server.ws.handler.build;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.server.service.github.GitHubActionsService;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import cc.endmc.server.ws.handler.BaseCommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.helper.BotMessageHelper;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import cc.endmc.framework.manager.AsyncManager;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * 构建命令处理器
 * 处理GitHub Actions相关的构建命令
 */
@Slf4j
public class BuildCommandHandler extends BaseCommandHandler {

    private final GitHubActionsService gitHubActionsService;
    private final AsyncManager asyncExecutor = AsyncManager.me();

    public BuildCommandHandler(BotClient botClient, RedisCache redisCache,
                               GitHubActionsService gitHubActionsService) {
        super(botClient, redisCache);
        this.gitHubActionsService = gitHubActionsService;
    }

    /**
     * 注册构建命令到命令注册器
     */
    public void registerCommands(CommandRegistry registry) {
        registry.register("正式构建", this::handleProductionBuild, "build-prod", "构建正式");
        registry.register("测试构建", this::handleTestBuild, "build-test", "构建测试");
        registry.register("构建状态", this::handleBuildStatus, "build-status", "bs");
    }

    /**
     * 处理正式构建命令
     */
    public void handleProductionBuild(QQMessage message) {
        handleBuildCommand(message, false);
    }

    /**
     * 处理测试构建命令
     */
    public void handleTestBuild(QQMessage message) {
        handleBuildCommand(message, true);
    }

    /**
     * 处理构建命令
     */
    private void handleBuildCommand(QQMessage message, boolean isPrerelease) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);

            String[] parts = message.getMessage().split("\\s+");
            if (parts.length < 2) {
                String buildType = isPrerelease ? "测试" : "正式";
                sendMessage(message, base + " 格式错误，正确格式：" + buildType + "构建 <分支名>\n" +
                        "可用分支：development, springboot3, master");
                return;
            }

            String branch = parts[1];

            // 验证分支名
            if (!branch.equals("development") && !branch.equals("springboot3") && !branch.equals("master")) {
                sendMessage(message, base + " 无效的分支名：" + branch + "\n" +
                        "可用分支：development, springboot3, master");
                return;
            }

            // 发送开始构建消息
            String buildType = isPrerelease ? "测试版" : "正式版";
            StringBuilder startMsg = new StringBuilder();
            startMsg.append(base).append("\n");
            startMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
            startMsg.append("🚀 开始触发构建\n");
            startMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
            startMsg.append("📦 构建类型: ").append(buildType).append("\n");
            startMsg.append("🌿 分支: ").append(branch).append("\n");
            startMsg.append("⏳ 正在触发工作流...\n");
            sendMessage(message, startMsg.toString());

            // 异步触发构建
            asyncExecutor.execute(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Map<String, Object> result = gitHubActionsService.triggerWorkflow(branch, isPrerelease);

                        StringBuilder resultMsg = new StringBuilder();
                        resultMsg.append(base).append("\n");
                        resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");

                        if ((Boolean) result.get("success")) {
                            resultMsg.append("✅ 构建触发成功\n");
                            resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                            resultMsg.append("📦 构建类型: ").append(buildType).append("\n");
                            resultMsg.append("🌿 分支: ").append(branch).append("\n");
                            resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                            resultMsg.append("📝 工作流程:\n");
                            resultMsg.append("1️⃣ 检出代码\n");
                            resultMsg.append("2️⃣ 设置 JDK 21 环境\n");
                            resultMsg.append("3️⃣ 构建后端项目 (Maven)\n");
                            resultMsg.append("4️⃣ 设置 Node.js 环境\n");
                            resultMsg.append("5️⃣ 构建前端项目 (npm)\n");
                            resultMsg.append("6️⃣ 压缩前端产物\n");
                            resultMsg.append("7️⃣ 创建 GitHub Release\n");
                            resultMsg.append("8️⃣ 上传构建产物\n");
                            resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                            resultMsg.append("💡 提示:\n");
                            resultMsg.append("• 使用 /构建状态 查看构建进度\n");
                            resultMsg.append("• 构建完成后可在 GitHub Releases 下载\n");
                            resultMsg.append("• 构建时间约 5-10 分钟\n");
                            resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                            resultMsg.append("🔗 仓库: https://github.com/pilgrimage233/Minecraft-Rcon-Manage\n");
                        } else {
                            resultMsg.append("❌ 构建触发失败\n");
                            resultMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                            resultMsg.append("错误信息: ").append(result.get("message")).append("\n");
                            if (result.containsKey("statusCode")) {
                                resultMsg.append("状态码: ").append(result.get("statusCode")).append("\n");
                            }
                        }

                        sendMessage(message, resultMsg.toString());

                    } catch (Exception e) {
                        log.error("触发构建失败", e);
                        sendMessage(message, base + " 触发构建时发生异常: " + e.getMessage());
                    }
                }
            });
        });
    }

    /**
     * 处理构建状态查询命令
     */
    public void handleBuildStatus(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);

            String[] parts = message.getMessage().split("\\s+");
            int limit = 3; // 默认显示最近3条

            if (parts.length > 1) {
                try {
                    limit = Integer.parseInt(parts[1]);
                    if (limit < 1 || limit > 10) {
                        sendMessage(message, base + " 数量必须在 1-10 之间");
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendMessage(message, base + " 无效的数量参数");
                    return;
                }
            }

            // 发送查询中消息
            sendMessage(message, base + " 正在查询构建状态...");

            final int finalLimit = limit;
            // 异步查询构建状态
            asyncExecutor.execute(new TimerTask() {
                @Override
                public void run() {
                    try {
                        JSONArray runs = gitHubActionsService.getRecentWorkflowRuns(finalLimit);

                        if (runs == null || runs.isEmpty()) {
                            sendMessage(message, base + " 未找到构建记录");
                            return;
                        }

                        StringBuilder statusMsg = new StringBuilder();
                        statusMsg.append(base).append("\n");
                        statusMsg.append("━━━━━━━━━━━━━━━━━━━━\n");
                        statusMsg.append("📊 最近的构建状态\n");
                        statusMsg.append("━━━━━━━━━━━━━━━━━━━━\n\n");

                        for (int i = 0; i < runs.size(); i++) {
                            JSONObject run = runs.getJSONObject(i);

                            String status = run.getString("status");
                            String conclusion = run.getString("conclusion");
                            String headBranch = run.getString("head_branch");
                            String createdAt = run.getString("created_at");
                            String htmlUrl = run.getString("html_url");
                            Long runNumber = run.getLong("run_number");

                            String statusEmoji = gitHubActionsService.formatWorkflowStatus(status, conclusion);

                            statusMsg.append("🔹 构建 #").append(runNumber).append("\n");
                            statusMsg.append("状态: ").append(statusEmoji).append("\n");
                            statusMsg.append("分支: ").append(headBranch).append("\n");
                            statusMsg.append("时间: ").append(createdAt.replace("T", " ").replace("Z", "")).append("\n");
                            statusMsg.append("链接: ").append(htmlUrl).append("\n");

                            if (i < runs.size() - 1) {
                                statusMsg.append("\n");
                            }
                        }

                        statusMsg.append("\n━━━━━━━━━━━━━━━━━━━━\n");
                        statusMsg.append("💡 点击链接查看详细日志");

                        sendMessage(message, statusMsg.toString());

                    } catch (Exception e) {
                        log.error("查询构建状态失败", e);
                        sendMessage(message, base + " 查询构建状态时发生异常: " + e.getMessage());
                    }
                }
            });
        });
    }
}
