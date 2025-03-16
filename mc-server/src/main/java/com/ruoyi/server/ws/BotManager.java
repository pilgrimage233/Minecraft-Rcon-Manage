package com.ruoyi.server.ws;

import com.ruoyi.server.domain.bot.QqBotConfig;
import com.ruoyi.server.mapper.bot.QqBotConfigMapper;
import com.ruoyi.server.service.bot.IQqBotConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * QQ机器人管理器
 * 负责管理所有机器人客户端的生命周期
 */
@Slf4j
@Component
public class BotManager {

    /**
     * 存储所有活跃的机器人客户端
     * Key: 机器人ID
     * Value: 机器人客户端实例
     */
    public static final Map<Long, BotClient> botClients = new ConcurrentHashMap<>();

    /**
     * 存储机器人最后心跳时间
     * Key: 机器人ID
     * Value: 最后心跳时间
     */
    private final Map<Long, Date> lastHeartbeatTimes = new ConcurrentHashMap<>();
    
    @Autowired
    private IQqBotConfigService qqBotConfigService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private QqBotConfigMapper qqBotConfigMapper;

    /**
     * 初始化方法
     * 启动时加载所有启用状态的机器人
     */
    @PostConstruct
    public void init() {
        try {
            log.info("正在初始化QQ机器人管理器...");
            loadBotConfigs();
            log.info("QQ机器人管理器初始化完成，共加载 {} 个机器人", botClients.size());
        } catch (Exception e) {
            log.error("QQ机器人管理器初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 加载机器人配置
     * 检查数据库中的配置，启动新增的机器人，停止已删除的机器人
     */
    public void loadBotConfigs() {
        // 查询所有启用的机器人配置
        QqBotConfig query = new QqBotConfig();
        query.setStatus(1L); // 获取启用状态的机器人
        List<QqBotConfig> configs = qqBotConfigService.selectQqBotConfigList(query);

        // 获取当前活跃的机器人ID集合
        Set<Long> currentBotIds = botClients.keySet();

        // 获取配置中的机器人ID集合
        Set<Long> configBotIds = configs.stream()
                .map(QqBotConfig::getId)
                .collect(Collectors.toSet());

        // 停止已删除或禁用的机器人
        currentBotIds.stream()
                .filter(id -> !configBotIds.contains(id))
                .forEach(id -> {
                    log.info("停止已删除或禁用的机器人: {}", id);
                    stopBot(id);
                });

        // 处理所有配置中的机器人
        configs.forEach(config -> {
            Long botId = config.getId();
            BotClient client = botClients.get(botId);

            if (client == null) {
                // 如果是新机器人，启动它
                log.info("启动新增的机器人: {}", botId);
                startBot(config);
            } else {
                // 如果是现有机器人，只更新配置
                log.info("更新机器人配置: {}", botId);
                client.init(config);
            }
        });
    }

    /**
     * 销毁方法
     * 关闭所有机器人客户端
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭QQ机器人管理器...");
        botClients.values().forEach(BotClient::destroy);
        botClients.clear();
        lastHeartbeatTimes.clear();
        log.info("QQ机器人管理器已关闭");
    }

    /**
     * 启动一个新的机器人客户端
     *
     * @param config 机器人配置
     */
    public void startBot(QqBotConfig config) {
        try {
            if (botClients.containsKey(config.getId())) {
                log.warn("机器人 {} 已经在运行中", config.getId());
                return;
            }

            // 从Spring容器获取BotClient实例，确保依赖注入正确
            BotClient client = applicationContext.getBean(BotClient.class);
            log.info("成功从Spring容器获取BotClient实例");

            // 初始化客户端
            client.init(config);
            log.info("机器人客户端初始化完成，配置ID: {}", config.getId());

            // 更新最后登录时间
            updateLastLoginTime(config.getId());

            // 初始化心跳时间
            updateHeartbeat(config.getId());

            // 存储客户端实例
            botClients.put(config.getId(), client);
            log.info("机器人 {} 启动成功", config.getId());
        } catch (Exception e) {
            log.error("启动机器人 {} 失败: {}", config.getId(), e.getMessage(), e);
        }
    }

    /**
     * 更新机器人最后登录时间
     *
     * @param botId 机器人ID
     */
    private void updateLastLoginTime(Long botId) {
        try {
            QqBotConfig config = new QqBotConfig();
            config.setId(botId);
            config.setLastLoginTime(new Date());
            int result = qqBotConfigMapper.updateQqBotConfig(config);
            if (result > 0) {
                log.info("机器人 {} 最后登录时间更新成功", botId);
            } else {
                log.warn("机器人 {} 最后登录时间更新失败", botId);
            }
        } catch (Exception e) {
            log.error("更新机器人 {} 最后登录时间失败: {}", botId, e.getMessage());
        }
    }

    /**
     * 更新机器人心跳时间
     *
     * @param botId 机器人ID
     */
    public void updateHeartbeat(Long botId) {
        Date now = new Date();
        lastHeartbeatTimes.put(botId, now);
        log.debug("机器人 {} 心跳更新: {}", botId, now);
    }

    /**
     * 更新机器人最后心跳时间到数据库
     *
     * @param botId         机器人ID
     * @param lastHeartbeat 最后心跳时间
     */
    private void updateLastHeartbeatTime(Long botId, Date lastHeartbeat) {
        try {
            QqBotConfig config = new QqBotConfig();
            config.setId(botId);
            config.setLastHeartbeatTime(lastHeartbeat);
            int result = qqBotConfigMapper.updateQqBotConfig(config);
            if (result > 0) {
                log.info("机器人 {} 最后心跳时间更新成功: {}", botId, lastHeartbeat);
            } else {
                log.warn("机器人 {} 最后心跳时间更新失败", botId);
            }
        } catch (Exception e) {
            log.error("更新机器人 {} 最后心跳时间失败: {}", botId, e.getMessage());
        }
    }

    /**
     * 定时检查机器人心跳
     * 每30秒执行一次，检查所有机器人的WebSocket连接状态
     * 如果连接断开，则更新最后心跳时间到数据库
     */
    @Scheduled(fixedRate = 30000)
    public void checkHeartbeats() {
        log.debug("开始检查机器人心跳...");

        for (Map.Entry<Long, BotClient> entry : botClients.entrySet()) {
            Long botId = entry.getKey();
            BotClient client = entry.getValue();

            try {
                // 检查WebSocket连接是否打开
                boolean isConnected = client.isOpen();

                if (isConnected) {
                    // 如果连接正常，更新内存中的心跳时间
                    updateHeartbeat(botId);
                } else {
                    // 如果连接断开，记录最后心跳时间到数据库
                    Date lastHeartbeat = lastHeartbeatTimes.get(botId);
                    if (lastHeartbeat != null) {
                        updateLastHeartbeatTime(botId, lastHeartbeat);
                        log.warn("机器人 {} WebSocket连接已断开，最后心跳时间: {}", botId, lastHeartbeat);
                    }

                    // 尝试重新连接
                    log.info("尝试重新连接机器人 {}", botId);
                    client.reconnect();
                }
            } catch (Exception e) {
                log.error("检查机器人 {} 心跳时发生错误: {}", botId, e.getMessage());
            }
        }
    }

    /**
     * 停止一个机器人客户端
     *
     * @param botId 机器人ID
     */
    public void stopBot(Long botId) {
        BotClient client = botClients.remove(botId);
        if (client != null) {
            // 记录最后心跳时间到数据库
            Date lastHeartbeat = lastHeartbeatTimes.remove(botId);
            if (lastHeartbeat != null) {
                updateLastHeartbeatTime(botId, lastHeartbeat);
            }
            
            client.destroy();
            log.info("机器人 {} 已停止", botId);
        }
    }

    /**
     * 重启一个机器人客户端
     *
     * @param config 机器人配置
     */
    public void restartBot(QqBotConfig config) {
        stopBot(config.getId());
        startBot(config);
    }

    /**
     * 获取一个机器人客户端
     *
     * @param botId 机器人ID
     * @return 机器人客户端实例
     */
    public BotClient getBot(Long botId) {
        return botClients.get(botId);
    }

    /**
     * 获取所有机器人客户端
     *
     * @return 机器人客户端映射
     */
    public Map<Long, BotClient> getAllBots() {
        return botClients;
    }

    /**
     * 获取机器人最后心跳时间
     *
     * @param botId 机器人ID
     * @return 最后心跳时间
     */
    public Date getLastHeartbeatTime(Long botId) {
        return lastHeartbeatTimes.get(botId);
    }
} 