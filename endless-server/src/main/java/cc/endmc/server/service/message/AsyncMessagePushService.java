package cc.endmc.server.service.message;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.message.PushMessage;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.utils.BotUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 异步消息推送服务
 * 提供高并发、非阻塞的消息推送功能
 *
 * @author Memory
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMessagePushService {

    private final RedisCache redisCache;
    private final IWhitelistInfoService whitelistInfoService;
    private final IQqBotConfigService qqBotConfigService;
    @Qualifier("threadPoolTaskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    /**
     * 消费者任务句柄（提交到统一线程池）
     */
    private final CopyOnWriteArrayList<Future<?>> consumerTasks = new CopyOnWriteArrayList<>();
    /**
     * 服务运行状态
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * 队列统计
     */
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger skippedCount = new AtomicInteger(0);
    /**
     * 消息队列（有界阻塞队列，避免无限增长）
     */
    private BlockingQueue<PushMessage> messageQueue;
    /**
     * 消费者线程数量配置
     */
    @Value("${message.push.consumer.threads:2}")
    private int consumerThreads;

    /**
     * 队列最大容量
     */
    @Value("${message.push.queue.maxSize:1000}")
    private int maxQueueSize;

    /**
     * 初始化消费者池
     */
    @PostConstruct
    public void init() {
        log.info("初始化异步消息推送服务，消费者线程数: {}, 队列最大容量: {}", consumerThreads, maxQueueSize);

        this.messageQueue = new LinkedBlockingQueue<>(maxQueueSize);

        // 启动消费者
        running.set(true);
        for (int i = 0; i < consumerThreads; i++) {
            Future<?> task = taskExecutor.submit(this::consumeMessages);
            consumerTasks.add(task);
        }

        log.info("异步消息推送服务启动完成");
    }

    /**
     * 销毁服务
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭异步消息推送服务...");

        running.set(false);

        // 等待消费者处理完队列中的消息（最多等待10秒）
        try {
            int waitCount = 0;
            while (!messageQueue.isEmpty() && waitCount < 100) {
                Thread.sleep(100);
                waitCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 取消消费者任务
        for (Future<?> task : consumerTasks) {
            task.cancel(true);
        }
        consumerTasks.clear();

        log.info("异步消息推送服务已关闭，处理消息总数: {}, 跳过数: {}, 失败数: {}, 队列剩余: {}",
                processedCount.get(), skippedCount.get(), failedCount.get(), messageQueue.size());
    }

    /**
     * 异步推送消息到队列
     *
     * @param playerName   玩家名称
     * @param message      消息内容
     * @param serverId     服务器ID
     * @param targetGroups 目标群组（多个群组用逗号分隔，为空则推送到所有群）
     * @return CompletableFuture<Boolean> 异步结果
     */
    public CompletableFuture<Boolean> pushMessageAsync(String playerName, String message, String serverId, String targetGroups) {
        try {
            // 加入有界队列，满了直接拒绝
            String serverName = getServerNameFromCache(serverId);
            PushMessage pushMessage = new PushMessage(playerName, message, serverId, serverName, targetGroups);

            if (!messageQueue.offer(pushMessage)) {
                log.warn("消息队列已满，丢弃消息: player={}, message={}", playerName, message);
                return CompletableFuture.completedFuture(false);
            }

            log.debug("消息已加入队列: {}, targetGroups: {}", pushMessage.getMessageId(), targetGroups);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("推送消息到队列失败", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 消费消息的工作线程
     */
    private void consumeMessages() {
        log.info("消息消费者线程启动: {}", Thread.currentThread().getName());

        while (running.get() || !messageQueue.isEmpty()) {
            try {
                PushMessage message = messageQueue.poll(1, TimeUnit.SECONDS);
                if (message == null) {
                    continue;
                }

                processMessage(message);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("消费消息异常", e);
            }
        }

        log.info("消息消费者线程退出: {}", Thread.currentThread().getName());
    }

    /**
     * 处理单个消息
     *
     * @param message 推送消息
     */
    private void processMessage(PushMessage message) {
        try {
            log.debug("开始处理消息: {}", message.getMessageId());

            // 1. 检查玩家是否在白名单中
            if (!isPlayerInWhitelist(message.getPlayerName())) {
                log.debug("玩家 {} 不在白名单中，跳过消息推送", message.getPlayerName());
                skippedCount.incrementAndGet();
                return;
            }

            // 2. 发送消息到QQ群（支持指定目标群组）
            boolean success = sendMessageToQQGroups(message.getFormattedMessage(), message.getTargetGroups());

            if (success) {
                processedCount.incrementAndGet();
                log.debug("消息处理成功: {}", message.getMessageId());
            } else {
                // 处理失败，尝试重试
                handleMessageFailure(message);
            }

        } catch (Exception e) {
            log.error("处理消息异常: {}", message.getMessageId(), e);
            handleMessageFailure(message);
        }
    }

    /**
     * 处理消息失败
     *
     * @param message 失败的消息
     */
    private void handleMessageFailure(PushMessage message) {
        if (message.canRetry()) {
            message.incrementRetry();
            log.warn("消息处理失败，重新加入队列重试: {}, 重试次数: {}",
                    message.getMessageId(), message.getRetryCount());

            // 重新入队，队列满则按失败处理
            if (!messageQueue.offer(message)) {
                failedCount.incrementAndGet();
                log.error("重试入队失败，消息队列已满: {}", message.getMessageId());
            }
        } else {
            failedCount.incrementAndGet();
            log.error("消息处理最终失败，已达最大重试次数: {}", message.getMessageId());
        }
    }

    /**
     * 检查玩家是否在白名单中（使用缓存）
     *
     * @param playerName 玩家名称
     * @return 是否在白名单中
     */
    private boolean isPlayerInWhitelist(String playerName) {
        try {
            // 先从缓存获取白名单信息
            Set<String> whitelistPlayers = redisCache.getCacheObject(CacheKey.WHITELIST_CACHE_KEY);

            if (whitelistPlayers == null) {
                // 缓存不存在，重建缓存
                whitelistPlayers = rebuildWhitelistCache();
            }

            return whitelistPlayers.contains(playerName);

        } catch (Exception e) {
            log.error("检查白名单缓存失败，回退到数据库查询", e);
            // 缓存失败时回退到数据库查询
            WhitelistInfo query = new WhitelistInfo();
            query.setUserName(playerName);
            query.setStatus("1"); // 已审核通过
            query.setAddState("1"); // 已添加
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);
            return !whitelistInfos.isEmpty();
        }
    }

    /**
     * 重建白名单缓存
     *
     * @return 白名单玩家集合
     */
    private Set<String> rebuildWhitelistCache() {
        log.info("重建白名单缓存");

        WhitelistInfo query = new WhitelistInfo();
        query.setStatus("1"); // 已审核通过
        query.setAddState("1"); // 已添加
        List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);

        Set<String> whitelistPlayers = whitelistInfos.stream()
                .map(WhitelistInfo::getUserName)
                .collect(Collectors.toSet());

        // 缓存30分钟
        redisCache.setCacheObject(CacheKey.WHITELIST_CACHE_KEY, whitelistPlayers, 30, TimeUnit.MINUTES);

        log.info("白名单缓存重建完成，共{}个玩家", whitelistPlayers.size());
        return whitelistPlayers;
    }

    /**
     * 发送消息到QQ群
     *
     * @param message      要发送的消息
     * @param targetGroups 目标群组（多个群组用逗号分隔，为空则推送到所有群）
     * @return 是否发送成功
     */
    private boolean sendMessageToQQGroups(String message, String targetGroups) {
        try {
            // 先从缓存获取机器人配置
            List<QqBotConfig> botConfigs = redisCache.getCacheObject(CacheKey.BOT_CONFIG_CACHE_KEY);

            if (botConfigs == null) {
                // 缓存不存在，重建缓存
                botConfigs = rebuildBotConfigCache();
            }

            if (botConfigs.isEmpty()) {
                log.warn("未找到可用的机器人配置");
                return false;
            }

            boolean sent = false;
            for (QqBotConfig config : botConfigs) {
                if (config.getGroupIds() != null && !config.getGroupIds().isEmpty()) {
                    try {
                        // 如果指定了目标群组，则只发送到指定群组
                        String groupsToSend = determineTargetGroups(config.getGroupIds(), targetGroups);

                        if (groupsToSend != null && !groupsToSend.isEmpty()) {
                            BotUtil.sendMessage(message, groupsToSend, config);
                            sent = true;
                            log.debug("消息已发送到QQ群: {} -> {}", groupsToSend, message);
                        } else {
                            log.debug("机器人 {} 没有匹配的目标群组，跳过发送", config.getName());
                        }
                    } catch (Exception e) {
                        log.error("发送消息到QQ群失败, 机器人: {}, 群组: {}", config.getName(), config.getGroupIds(), e);
                    }
                }
            }

            return sent;

        } catch (Exception e) {
            log.error("发送消息到QQ群异常", e);
            return false;
        }
    }

    /**
     * 确定实际要发送的目标群组
     *
     * @param configGroups 机器人配置的群组列表
     * @param targetGroups 指定的目标群组（为空则使用配置的所有群组）
     * @return 实际要发送的群组列表（逗号分隔）
     */
    private String determineTargetGroups(String configGroups, String targetGroups) {
        // 如果没有指定目标群组，则使用配置的所有群组
        if (targetGroups == null || targetGroups.trim().isEmpty()) {
            return configGroups;
        }

        Set<String> configGroupSet = Set.of(configGroups.split(","));
        Set<String> targetGroupSet = Set.of(targetGroups.split(","));

        // 只发送到既在配置中又在目标列表中的群组
        return configGroupSet.stream()
                .filter(group -> targetGroupSet.contains(group.trim()))
                .collect(Collectors.joining(","));
    }

    /**
     * 重建机器人配置缓存
     *
     * @return 机器人配置列表
     */
    private List<QqBotConfig> rebuildBotConfigCache() {
        log.info("重建机器人配置缓存");

        QqBotConfig query = new QqBotConfig();
        query.setStatus(1L); // 启用状态
        List<QqBotConfig> botConfigs = qqBotConfigService.selectQqBotConfigList(query);

        // 缓存30分钟
        redisCache.setCacheObject(CacheKey.BOT_CONFIG_CACHE_KEY, botConfigs, 30, TimeUnit.MINUTES);

        log.info("机器人配置缓存重建完成，共{}个配置", botConfigs.size());
        return botConfigs;
    }

    /**
     * 从缓存获取服务器名称
     *
     * @param serverId 服务器ID
     * @return 服务器名称
     */
    private String getServerNameFromCache(String serverId) {
        if (serverId == null || serverId.trim().isEmpty()) {
            return "未知服务器";
        }

        try {
            // 先从缓存获取服务器信息Map
            Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);

            if (serverInfoMap != null && serverInfoMap.containsKey(serverId)) {
                Object serverObj = serverInfoMap.get(serverId);
                if (serverObj != null) {
                    ServerInfo serverInfo = null;

                    if (serverObj instanceof ServerInfo) {
                        serverInfo = (ServerInfo) serverObj;
                    } else {
                        try {
                            serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                        } catch (Exception e) {
                            log.warn("服务器信息转换失败，serverId: {}, 错误: {}", serverId, e.getMessage());
                        }
                    }

                    if (serverInfo != null && serverInfo.getNameTag() != null) {
                        return serverInfo.getNameTag();
                    }
                }

                log.debug("服务器ID {} 对应的服务器信息为空或无效", serverId);
                return "未知服务器";
            } else {
                log.debug("服务器ID {} 在缓存中不存在，使用默认名称", serverId);
                return "未知服务器";
            }

        } catch (Exception e) {
            log.error("从缓存获取服务器信息失败，serverId: {}", serverId, e);
            return "未知服务器";
        }
    }

    /**
     * 获取队列统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getQueueStats() {
        int currentQueueSize = messageQueue == null ? 0 : messageQueue.size();
        return Map.of(
                "queueSize", currentQueueSize,
                "processedCount", processedCount.get(),
                "skippedCount", skippedCount.get(),
                "failedCount", failedCount.get(),
                "running", running.get(),
                "consumerThreads", consumerThreads,
                "maxQueueSize", maxQueueSize
        );
    }
}