package cc.endmc.quartz.task;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.utils.BotUtil;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.BotManager;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 机器人定时任务
 * 主要用于监控白名单用户是否退群
 */
@Slf4j
@Component("botTask")
@RequiredArgsConstructor
public class BotTask {

    private final IWhitelistInfoService whitelistInfoService;
    private final BotManager botManager;
    private final IQqBotConfigService qqBotConfigService;
    private final RedisCache redisCache;
    private final Environment env;

    /**
     * 监控白名单用户是否退群
     */
    public void monitorWhiteList() {
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setStatus("1");
        final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);

        if (whitelistInfos.isEmpty()) {
            return;
        }

        // 获取所有活跃的机器人客户端
        Map<Long, BotClient> activeBots = botManager.getAllBots();
        if (activeBots.isEmpty()) {
            log.warn("没有活跃的机器人客户端");
            return;
        }

        // 用于存储每个群的退群用户列表
        Map<Long, StringBuilder> groupMessages = new HashMap<>();
        // 用于记录用户在任意群中的存在状态
        Map<Long, Boolean> userExistsInAnyGroup = new HashMap<>();
        // 用于记录用户退出的群
        Map<Long, List<Long>> userLeftGroups = new HashMap<>();
        // 所有需要监控的群ID列表
        Set<Long> allGroupIds = new HashSet<>();

        // 获取所有机器人配置的群ID
        for (BotClient bot : activeBots.values()) {
            try {
                if (bot == null || bot.getConfig() == null) {
                    continue;
                }
            } catch (Exception e) {
                // 处理异常，可能是因为机器人未正确初始化
                log.error("获取机器人配置失败: {}", e.getMessage());
                continue;
            }

            QqBotConfig config = bot.getConfig();
            if (config.getGroupIds() != null) {
                allGroupIds.addAll(Arrays.stream(config.getGroupIds().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet()));
            }
        }

        boolean isFail = false;
        // 获取启用机器人的群员列表
        JSONObject request = new JSONObject();
        request.put("no_cache", false);

        // 遍历所有群，检查每个用户的存在状态
        for (Long groupId : allGroupIds) {
            request.put("group_id", String.valueOf(groupId));
            groupMessages.put(groupId, new StringBuilder());

            // 找到负责该群的机器人
            BotClient responsibleBot = findResponsibleBot(activeBots, groupId);
            if (responsibleBot == null) {
                log.warn("群 {} 没有对应的机器人客户端", groupId);
                isFail = true;
                continue;
            }

            // 使用机器人的配置发送请求
            String botUrl = responsibleBot.getConfig().getHttpUrl();
            HttpResponse response = null;
            try {
                response = HttpUtil
                        .createPost(botUrl + BotApi.GET_GROUP_MEMBER_LIST)
                        .header("Authorization", "Bearer " + responsibleBot.getConfig().getToken())
                        .body(request.toJSONString())
                        .timeout(8000)
                        .execute();
            } catch (Exception e) {
                log.error("群 {} 获取成员列表失败: {}", groupId, e.getMessage());
                isFail = true;
                continue;
            }
            if (response == null || !response.isOk()) {
                log.warn("群 {} 获取成员列表失败", groupId);
                isFail = true;
                continue;
            }

            final JSONObject jsonObject = JSONObject.parseObject(response.body());
            if ((jsonObject.containsKey("retcode") && jsonObject.getInteger("retcode") != 0) || jsonObject.getJSONArray("data") == null) {
                log.warn("群 {} 获取成员列表失败: {}", groupId, jsonObject);
                isFail = true;
                continue;
            } else {
                log.debug("群 {} 成员列表获取成功,数量: {}", groupId, jsonObject.size());
            }

            final List<JSONObject> members = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);
            isFail = members.isEmpty();
            // 检查每个白名单用户在当前群中的状态
            whitelistInfos.forEach(whitelist -> {
                Long userId = Long.parseLong(whitelist.getQqNum());

                // 检查用户是否在当前群中
                boolean existsInCurrentGroup = members.stream()
                        .anyMatch(member -> userId.equals(member.getLong("user_id")));

                // 更新用户在任意群中的存在状态
                if (existsInCurrentGroup) {
                    userExistsInAnyGroup.put(userId, true);
                }

                // 如果用户不在当前群中，记录到退群列表
                if (!existsInCurrentGroup) {
                    userLeftGroups.computeIfAbsent(userId, k -> new ArrayList<>()).add(groupId);
                }
            });
        }

        // 避免出现空数据移除全部数据
        if (isFail) {
            log.error("获取群成员列表过程中出现错误，跳过本次白名单检查");
            return;
        }

        // 处理所有不在任何群中的用户
        whitelistInfos.forEach(whitelist -> {
            Long userId = Long.parseLong(whitelist.getQqNum());

            // 如果用户不在任何群中
            if (!userExistsInAnyGroup.containsKey(userId)) {
                // 移除白名单
                whitelist.setAddState("true");
                whitelist.setRemoveReason("用户退群-同步");
                whitelistInfoService.updateWhitelistInfo(whitelist, "system");

                // 在所有相关群中添加通知消息
                List<Long> leftGroups = userLeftGroups.getOrDefault(userId, new ArrayList<>(allGroupIds));
                leftGroups.forEach(groupId -> {
                    groupMessages.get(groupId)
                            .append("\n- 用户：")
                            .append(whitelist.getUserName())
                            .append("(")
                            .append(userId)
                            .append(")");
                });

                log.info("用户 {} ({}) 已不在任何群中，已移除白名单", whitelist.getUserName(), userId);
            }
        });

        // 发送群通知
        groupMessages.forEach((groupId, messageBuilder) -> {
            String groupMessage = messageBuilder.toString();
            if (!groupMessage.isEmpty()) {
                // 找到负责该群的机器人
                BotClient responsibleBot = findResponsibleBot(activeBots, groupId);
                if (responsibleBot == null) {
                    log.error("群 {} 没有对应的机器人客户端，无法发送通知", groupId);
                    return;
                }

                // 构建消息对象
                JSONObject msgRequest = new JSONObject();
                msgRequest.put("group_id", groupId.toString());
                msgRequest.put("message", "⚠️退群白名单移除通知：\n以下用户已退群并移除白名单：" + groupMessage);

                // 发送群消息
                String response = HttpUtil.createPost(responsibleBot.getConfig().getHttpUrl() + BotApi.SEND_GROUP_MSG)
                        .header("Authorization", "Bearer " + responsibleBot.getConfig().getToken())
                        .body(msgRequest.toJSONString())
                        .execute()
                        .body();
                if (response != null) {
                    JSONObject result = JSONObject.parseObject(response);
                    if (result.getInteger("retcode") != 0) {
                        log.error("群 {} 发送退群通知失败: {}", groupId, result.getString("msg"));
                    } else {
                        log.info("群 {} 退群移除白名单通知已发送: {}", groupId, groupMessage);
                    }
                }
            }
        });
    }

    /**
     * 查找负责特定群的机器人客户端
     *
     * @param activeBots 活跃的机器人客户端
     * @param groupId    群号
     * @return 负责该群的机器人客户端，如果没有找到则返回null
     */
    private BotClient findResponsibleBot(Map<Long, BotClient> activeBots, Long groupId) {
        for (BotClient bot : activeBots.values()) {
            QqBotConfig config = bot.getConfig();
            if (config != null && config.getGroupIds() != null) {
                boolean isResponsible = Arrays.stream(config.getGroupIds().split(","))
                        .map(Long::parseLong)
                        .anyMatch(id -> id.equals(groupId));
                if (isResponsible) {
                    return bot;
                }
            }
        }
        return null;
    }

    /**
     * 定时检查GitHub项目更新
     * 每天执行一次
     */
    @Scheduled(cron = "0 0 14 * * ?")
    public void checkUpdate() {
        QqBotConfig botConfig = new QqBotConfig();
        botConfig.setStatus(1L);
        try {
            final List<QqBotConfig> qqBotConfigs = qqBotConfigService.selectQqBotConfigList(botConfig);
            for (QqBotConfig config : qqBotConfigs) {
                if (config == null || config.getId() == null) {
                    return;
                }

                // 检查是否需要执行更新检测（避免频繁执行）
                String lastCheckKey = CacheKey.UPDATE_CHECK_KEY + "last_check";
                Long lastCheckTime = redisCache.getCacheObject(lastCheckKey);
                long currentTime = System.currentTimeMillis();

                // 如果距离上次检查不足1天，跳过本次检查
                if (lastCheckTime != null && (currentTime - lastCheckTime) < TimeUnit.DAYS.toMillis(1)) {
                    log.info("距离上次检查不足1天，跳过本次更新检查");
                    return;
                }

                // 更新最后检查时间
                redisCache.setCacheObject(lastCheckKey, currentTime, 1, TimeUnit.DAYS);

                log.info("开始检查GitHub项目更新...");

                // 获取最新发行版信息
                Map<String, Object> latestRelease = getLatestRelease();

                // 获取最新工作流构建状态
                Map<String, Object> latestWorkflow = getLatestWorkflow();

                // 检查是否有新版本
                if (latestRelease != null && !latestRelease.isEmpty()) {
                    String currentVersion = env.getProperty("endless.version", "unknown");
                    String latestVersion = latestRelease.get("tag_name")
                            .toString()
                            .replace("v", "")
                            .trim();

                    if (!currentVersion.equals(latestVersion)) {
                        // 有新版本，发送通知
                        sendUpdateNotification(latestRelease, latestWorkflow, config);

                        // 缓存最新版本信息，避免重复通知
                        String versionCacheKey = CacheKey.UPDATE_CHECK_KEY + "latest_version";
                        redisCache.setCacheObject(versionCacheKey, latestVersion, 24, TimeUnit.HOURS);
                    }
                }
                log.info("GitHub项目更新检查完成");
            }
        } catch (Exception e) {
            log.error("检查更新失败: {}", e.getMessage());
        }

    }

    /**
     * 获取GitHub最新发行版信息
     */
    private Map<String, Object> getLatestRelease() {
        try {
            String apiUrl = "https://api.github.com/repos/pilgrimage233/Minecraft-Rcon-Manage/releases/latest";

            HttpResponse response = HttpUtil.createGet(apiUrl)
                    .header("User-Agent", "Endless-Manager-Update-Checker")
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                JSONObject releaseData = JSON.parseObject(response.body());
                Map<String, Object> releaseInfo = new HashMap<>();

                releaseInfo.put("tag_name", releaseData.getString("tag_name"));
                releaseInfo.put("name", releaseData.getString("name"));
                releaseInfo.put("body", releaseData.getString("body"));
                releaseInfo.put("html_url", releaseData.getString("html_url"));
                releaseInfo.put("published_at", releaseData.getString("published_at"));
                releaseInfo.put("author", releaseData.getJSONObject("author").getString("login"));

                // 获取下载信息
                if (releaseData.containsKey("assets") && releaseData.get("assets") instanceof List) {
                    List<Object> assets = (List<Object>) releaseData.get("assets");
                    if (!assets.isEmpty()) {
                        JSONObject asset = (JSONObject) assets.get(0);
                        releaseInfo.put("download_count", asset.getInteger("download_count"));
                        releaseInfo.put("size", asset.getInteger("size"));
                        releaseInfo.put("download_url", asset.getString("browser_download_url"));
                    }
                }

                return releaseInfo;
            }
        } catch (Exception e) {
            log.error("获取GitHub最新发行版失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取GitHub最新工作流构建状态
     */
    private Map<String, Object> getLatestWorkflow() {
        try {
            String apiUrl = "https://api.github.com/repos/pilgrimage233/Minecraft-Rcon-Manage/actions/runs";

            HttpResponse response = HttpUtil.createGet(apiUrl)
                    .header("User-Agent", "Endless-Manager-Update-Checker")
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                JSONObject workflowData = JSON.parseObject(response.body());
                if (workflowData.containsKey("workflow_runs") && workflowData.get("workflow_runs") instanceof List) {
                    List<Object> runs = (List<Object>) workflowData.get("workflow_runs");
                    if (!runs.isEmpty()) {
                        JSONObject latestRun = (JSONObject) runs.get(0);
                        Map<String, Object> workflowInfo = new HashMap<>();

                        workflowInfo.put("id", latestRun.getLong("id"));
                        workflowInfo.put("name", latestRun.getString("name"));
                        workflowInfo.put("status", latestRun.getString("status"));
                        workflowInfo.put("conclusion", latestRun.getString("conclusion"));
                        workflowInfo.put("created_at", latestRun.getString("created_at"));
                        workflowInfo.put("updated_at", latestRun.getString("updated_at"));
                        workflowInfo.put("html_url", latestRun.getString("html_url"));
                        workflowInfo.put("branch", latestRun.getString("head_branch"));
                        workflowInfo.put("commit_sha", latestRun.getString("head_sha"));
                        workflowInfo.put("commit_message", latestRun.getString("head_commit").contains("message") ?
                                ((JSONObject) latestRun.get("head_commit")).getString("message") : "N/A");

                        return workflowInfo;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取GitHub工作流状态失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送更新通知到所有配置的QQ群
     */
    private void sendUpdateNotification(Map<String, Object> latestRelease, Map<String, Object> latestWorkflow, QqBotConfig config) {
        try {
            if (config == null || config.getGroupIds() == null || config.getGroupIds().trim().isEmpty()) {
                log.warn("没有配置任何QQ群，跳过发送更新通知");
                return;
            }

            // 构建通知消息
            StringBuilder notification = new StringBuilder();
            notification.append("🚀 新版本发布通知 🚀\n");
            notification.append("━━━━━━━━━━━━━━━━━━━━\n\n");

            // 发行版信息
            if (latestRelease != null && !latestRelease.isEmpty()) {
                notification.append("📦 最新版本信息：\n");
                notification.append("版本号：").append(latestRelease.get("tag_name")).append("\n");
                notification.append("版本名称：").append(latestRelease.get("name")).append("\n");
                notification.append("发布时间：").append(formatGitHubDate((String) latestRelease.get("published_at"))).append("\n");
                notification.append("发布者：").append(latestRelease.get("author")).append("\n");

                if (latestRelease.containsKey("download_count")) {
                    notification.append("下载次数：").append(latestRelease.get("download_count")).append("\n");
                }

                notification.append("下载地址：").append(latestRelease.get("html_url")).append("\n\n");

                // 版本说明（限制长度）
                String body = (String) latestRelease.get("body");
                if (body != null && !body.trim().isEmpty()) {
                    String truncatedBody = body.length() > 200 ? body.substring(0, 200) + "..." : body;
                    notification.append("📝 版本说明：\n").append(truncatedBody).append("\n\n");
                }
            }

            // 工作流构建状态
            if (latestWorkflow != null && !latestWorkflow.isEmpty()) {
                notification.append("🔧 最新构建状态：\n");
                notification.append("工作流：").append(latestWorkflow.get("name")).append("\n");
                notification.append("状态：").append(getStatusEmoji((String) latestWorkflow.get("status"))).append(" ")
                        .append(latestWorkflow.get("status")).append("\n");

                if (latestWorkflow.get("conclusion") != null) {
                    notification.append("结果：").append(getConclusionEmoji((String) latestWorkflow.get("conclusion"))).append(" ")
                            .append(latestWorkflow.get("conclusion")).append("\n");
                }

                notification.append("分支：").append(latestWorkflow.get("branch")).append("\n");
                notification.append("提交：").append(((String) latestWorkflow.get("commit_sha")).substring(0, 7)).append("\n");
                notification.append("提交信息：").append(truncateString((String) latestWorkflow.get("commit_message"), 50)).append("\n");
                notification.append("构建时间：").append(formatGitHubDate((String) latestWorkflow.get("created_at"))).append("\n");
                notification.append("构建详情：").append(latestWorkflow.get("html_url")).append("\n\n");
            }

            notification.append("━━━━━━━━━━━━━━━━━━━━\n");
            notification.append("💡 提示：请及时更新到最新版本以获得最佳体验！");

            // 发送到所有配置的群组
            if (config.getGroupIds() != null && !config.getGroupIds().trim().isEmpty()) {
                String[] groupIdArray = config.getGroupIds().split(",");
                for (String groupId : groupIdArray) {
                    try {
                        String trimmedGroupId = groupId.trim();
                        if (trimmedGroupId.isEmpty()) {
                            continue;
                        }

                        // 发送通知消息
                        BotUtil.sendMessage(notification.toString(), groupId, config);

                        log.info("已发送更新通知到群组: {}", trimmedGroupId);

                        // 避免发送过快，添加延迟
                        Thread.sleep(1000);

                    } catch (Exception e) {
                        log.error("发送更新通知到群组 {} 失败: {}", groupId, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("发送更新通知失败: {}", e.getMessage());
        }
    }

    /**
     * 格式化GitHub日期
     */
    private String formatGitHubDate(String githubDate) {
        try {
            if (githubDate == null || githubDate.isEmpty()) {
                return "未知";
            }

            // GitHub日期格式：2024-01-01T12:00:00Z
            return githubDate.replace("T", " ").replace("Z", "");
        } catch (Exception e) {
            return githubDate;
        }
    }

    /**
     * 获取状态对应的表情符号
     */
    private String getStatusEmoji(String status) {
        if (status == null) return "❓";

        switch (status.toLowerCase()) {
            case "completed":
                return "✅";
            case "in_progress":
                return "🔄";
            case "queued":
                return "⏳";
            case "waiting":
                return "⏸️";
            case "pending":
                return "⏳";
            default:
                return "❓";
        }
    }

    /**
     * 获取构建结果对应的表情符号
     */
    private String getConclusionEmoji(String conclusion) {
        if (conclusion == null) return "❓";

        switch (conclusion.toLowerCase()) {
            case "success":
                return "✅";
            case "failure":
                return "❌";
            case "cancelled":
                return "🚫";
            case "skipped":
                return "⏭️";
            case "timed_out":
                return "⏰";
            default:
                return "❓";
        }
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 同步群成员ID
     * 将通过白名单的用户群名片修改为【id】+ 原有昵称
     */
    public void synchronizeGroupMembersId() {
        log.info("开始同步群成员ID...");
        
        try {
            // 获取所有通过审核且未被移除的白名单用户
            WhitelistInfo queryCondition = new WhitelistInfo();
            queryCondition.setStatus("1"); // 审核通过
            final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(queryCondition);

            if (whitelistInfos.isEmpty()) {
                log.info("没有需要同步的白名单用户");
                return;
            }

            // 获取所有活跃的机器人客户端
            Map<Long, BotClient> activeBots = botManager.getAllBots();
            if (activeBots.isEmpty()) {
                log.warn("没有活跃的机器人客户端，无法同步群成员ID");
                return;
            }

            // 获取所有需要监控的群ID列表
            Set<Long> allGroupIds = new HashSet<>();
            for (BotClient bot : activeBots.values()) {
                try {
                    if (bot == null || bot.getConfig() == null) {
                        continue;
                    }
                } catch (Exception e) {
                    log.error("获取机器人配置失败: {}", e.getMessage());
                    continue;
                }

                QqBotConfig config = bot.getConfig();
                if (config.getGroupIds() != null) {
                    allGroupIds.addAll(Arrays.stream(config.getGroupIds().split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toSet()));
                }
            }

            if (allGroupIds.isEmpty()) {
                log.warn("没有配置任何群组，无法同步群成员ID");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            // 遍历所有群组
            for (Long groupId : allGroupIds) {
                // 找到负责该群的机器人
                BotClient responsibleBot = findResponsibleBot(activeBots, groupId);
                if (responsibleBot == null) {
                    log.warn("群 {} 没有对应的机器人客户端", groupId);
                    continue;
                }

                // 获取群成员列表
                JSONObject request = new JSONObject();
                request.put("group_id", String.valueOf(groupId));
                request.put("no_cache", false);

                String botUrl = responsibleBot.getConfig().getHttpUrl();
                HttpResponse response = null;
                try {
                    response = HttpUtil
                            .createPost(botUrl + BotApi.GET_GROUP_MEMBER_LIST)
                            .header("Authorization", "Bearer " + responsibleBot.getConfig().getToken())
                            .body(request.toJSONString())
                            .timeout(8000)
                            .execute();
                } catch (Exception e) {
                    log.error("群 {} 获取成员列表失败: {}", groupId, e.getMessage());
                    continue;
                }

                if (response == null || !response.isOk()) {
                    log.warn("群 {} 获取成员列表失败", groupId);
                    continue;
                }

                final JSONObject jsonObject = JSONObject.parseObject(response.body());
                if ((jsonObject.containsKey("retcode") && jsonObject.getInteger("retcode") != 0) || jsonObject.getJSONArray("data") == null) {
                    log.warn("群 {} 获取成员列表失败: {}", groupId, jsonObject);
                    continue;
                }

                final List<JSONObject> members = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);
                if (members.isEmpty()) {
                    log.warn("群 {} 成员列表为空", groupId);
                    continue;
                }

                log.debug("群 {} 成员列表获取成功，数量: {}", groupId, members.size());

                // 遍历白名单用户，检查是否在当前群中
                for (WhitelistInfo whitelist : whitelistInfos) {
                    try {
                        Long userId = Long.parseLong(whitelist.getQqNum());
                        String gameName = whitelist.getUserName();

                        // 查找该用户是否在当前群中
                        JSONObject targetMember = members.stream()
                                .filter(member -> userId.equals(member.getLong("user_id")))
                                .findFirst()
                                .orElse(null);

                        if (targetMember != null) {
                            // 用户在群中，检查并更新群名片
                            String currentCard = targetMember.getString("card");
                            String currentNickname = targetMember.getString("nickname");
                            
                            // 获取用户原本的昵称（优先使用群名片，如果没有则使用QQ昵称）
                            String originalName = (currentCard != null && !currentCard.trim().isEmpty()) 
                                    ? currentCard : currentNickname;
                            
                            // 检查当前群名片是否已经包含白名单标识
                            String whitelistTag = "【" + gameName + "】";
                            String newCard;
                            
                            if (originalName.startsWith("【") && originalName.contains("】")) {
                                // 如果已经有标识，替换为新的白名单标识
                                int endIndex = originalName.indexOf("】") + 1;
                                String nameWithoutTag = originalName.substring(endIndex).trim();
                                newCard = whitelistTag + nameWithoutTag;
                            } else {
                                // 如果没有标识，直接添加白名单标识
                                newCard = whitelistTag + originalName;
                            }
                            
                            // 如果当前群名片已经是目标格式，跳过更新
                            if (newCard.equals(currentCard)) {
                                log.debug("用户 {} 在群 {} 的群名片已经是正确格式，跳过更新", userId, groupId);
                                continue;
                            }

                            // 更新群名片
                            boolean updateSuccess = updateGroupCard(responsibleBot, groupId, userId, newCard);
                            if (updateSuccess) {
                                successCount++;
                                log.info("成功更新用户 {} 在群 {} 的群名片: {} -> {}", 
                                        userId, groupId, originalName, newCard);
                                
                                // 避免更新过快，添加延迟
                                Thread.sleep(500);
                            } else {
                                failCount++;
                                log.error("更新用户 {} 在群 {} 的群名片失败", userId, groupId);
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.error("白名单用户 {} 的QQ号格式错误: {}", whitelist.getUserName(), whitelist.getQqNum());
                    } catch (Exception e) {
                        log.error("处理白名单用户 {} 时发生错误: {}", whitelist.getUserName(), e.getMessage());
                        failCount++;
                    }
                }
            }

            log.info("群成员ID同步完成，成功: {} 个，失败: {} 个", successCount, failCount);

        } catch (Exception e) {
            log.error("同步群成员ID失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新群成员名片
     *
     * @param bot     机器人客户端
     * @param groupId 群号
     * @param userId  用户QQ号
     * @param newCard 新的群名片
     * @return 是否更新成功
     */
    private boolean updateGroupCard(BotClient bot, Long groupId, Long userId, String newCard) {
        try {
            // 构建请求参数
            JSONObject request = new JSONObject();
            request.put("group_id", String.valueOf(groupId));
            request.put("user_id", String.valueOf(userId));
            request.put("card", newCard);

            // 发送更新群名片请求
            HttpResponse response = HttpUtil
                    .createPost(bot.getConfig().getHttpUrl() + BotApi.SET_GROUP_CARD)
                    .header("Authorization", "Bearer " + bot.getConfig().getToken())
                    .body(request.toJSONString())
                    .timeout(8000)
                    .execute();

            if (response == null || !response.isOk()) {
                log.error("更新群名片请求失败，HTTP状态: {}", response != null ? response.getStatus() : "null");
                return false;
            }

            JSONObject result = JSONObject.parseObject(response.body());
            if (result.containsKey("retcode") && result.getInteger("retcode") == 0) {
                return true;
            } else {
                log.error("更新群名片失败，返回码: {}, 消息: {}", 
                        result.getInteger("retcode"), result.getString("msg"));
                return false;
            }

        } catch (Exception e) {
            log.error("更新群名片时发生异常: {}", e.getMessage());
            return false;
        }
    }
}
