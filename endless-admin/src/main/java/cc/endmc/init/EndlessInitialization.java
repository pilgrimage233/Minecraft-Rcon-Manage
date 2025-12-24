package cc.endmc.init;

import cc.endmc.common.config.EndlessConfig;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.node.utils.ApiUtil;
import cc.endmc.server.cache.EmailTempCache;
import cc.endmc.server.cache.ObjectCache;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.constant.RconMsg;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.config.RconConfig;
import cc.endmc.server.domain.email.CustomEmailTemplates;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.sdk.SearchHttpAK;
import cc.endmc.server.service.email.ICustomEmailTemplatesService;
import cc.endmc.server.service.server.IServerCommandInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Endless 系统初始化类
 * 在 Spring Bean 初始化后执行系统初始化操作
 * 包括：服务器信息缓存、Rcon连接、Node初始化、邮件模板加载等
 *
 * @author Memory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EndlessInitialization implements InitializingBean {

    private final RedisCache redisCache;
    private final IServerInfoService serverInfoService;
    private final INodeServerService nodeServerService;
    private final IServerCommandInfoService commandInfoService;
    private final ICustomEmailTemplatesService customEmailTemplatesService;
    private final RconService rconService;
    private final RconConfig rconConfig;
    private final Environment env;
    private final EndlessConfig endlessConfig;

    // 线程池用于并发初始化任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("endless-init-" + thread.getId());
                thread.setDaemon(true);
                return thread;
            }
    );

    @Override
    public void afterPropertiesSet() {
        log.info("🔧 ENDLESS INIT: 初始化开始...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证必要配置
            validateRequiredConfigs();

            // 2. 初始化 Rcon 配置
            rconConfig.init();

            // 3. 并发执行各项初始化任务
            CompletableFuture<Void> serverInfoFuture = CompletableFuture.runAsync(this::initServerInfo, executorService);
            CompletableFuture<Void> commandFuture = CompletableFuture.runAsync(this::initCommandInfo, executorService);
            CompletableFuture<Void> nodeFuture = CompletableFuture.runAsync(this::initNodeServers, executorService);
            CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(this::initEmailTemplates, executorService);

            // 等待所有任务完成
            CompletableFuture.allOf(serverInfoFuture, commandFuture, nodeFuture, emailFuture)
                    .get(30, TimeUnit.SECONDS);

            // 4. 初始化 Rcon 连接（依赖服务器信息）
            initRconConnections();

            // 5. 更新节点控制端信息
            updateNodeMasterInfo();

            // 6. 发送初始化完成广播
            sendInitBroadcast();

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("✅ ENDLESS INIT: 初始化完成，耗时 {} ms", elapsedTime);

        } catch (TimeoutException e) {
            log.error("❌ ENDLESS INIT: 初始化超时", e);
            throw new RuntimeException("系统初始化超时", e);
        } catch (Exception e) {
            log.error("❌ ENDLESS INIT: 初始化失败", e);
            throw new RuntimeException("系统初始化失败", e);
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * 验证必要的配置项
     */
    private void validateRequiredConfigs() {
        log.debug("📋 验证配置项...");

        // 百度 IP 定位 API
        String baiduKey = env.getProperty("baidu.key");
        if (baiduKey != null && !baiduKey.isEmpty()) {
            SearchHttpAK.AK = baiduKey;
            log.info("✓ 百度 IP 定位 API 密钥已配置");
        } else {
            log.warn("⚠️ 百度 IP 定位 API 密钥未配置");
        }

        // 应用 URL
        if (env.getProperty("app-url") == null) {
            log.error("❌ 白名单申请网站地址未配置");
        }

        // 管理员邮件
        if (env.getProperty("whitelist.email") == null) {
            log.error("❌ 白名单管理员邮件通知地址未配置");
            throw new IllegalStateException("白名单管理员邮件未配置");
        }

        // 应用密钥
        String secretKey = env.getProperty("app.secret-key");
        if (secretKey == null || secretKey.equalsIgnoreCase("EndmcAppSecretKey")) {
            log.error("❌ 警告: 为了您的程序安全，请务必修改默认的 app.secret-key 配置项！");
            throw new IllegalStateException("必须修改默认的安全密钥");
        }

        log.debug("✓ 配置项验证完成");
    }

    /**
     * 初始化服务器信息缓存
     */
    private void initServerInfo() {
        try {
            log.debug("📊 初始化服务器信息缓存...");

            List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(new ServerInfo());

            if (serverInfos == null || serverInfos.isEmpty()) {
                log.warn("⚠️ " + RconMsg.SERVER_EMPTY);
                return;
            }

            // 使用 Stream API 构建 Map
            Map<String, ServerInfo> serverMap = serverInfos.stream()
                    .collect(Collectors.toMap(
                            info -> info.getId().toString(),
                            info -> info,
                            (existing, replacement) -> replacement,
                            HashMap::new
                    ));

            // 缓存服务器信息
            redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, serverMap);
            redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);
            redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());

            log.info("✓ 缓存服务器数量: {}", serverInfos.size());

        } catch (Exception e) {
            log.error("❌ 初始化服务器信息缓存失败", e);
            throw new RuntimeException("服务器信息缓存初始化失败", e);
        }
    }

    /**
     * 初始化服务器指令缓存
     */
    private void initCommandInfo() {
        try {
            log.debug("🔨 初始化服务器指令缓存...");

            commandInfoService.initServerCommandInfo();
            RconService.COMMAND_INFO = ObjectCache.getCommandInfo();

            if (RconService.COMMAND_INFO != null && !RconService.COMMAND_INFO.isEmpty()) {
                log.info("✓ 缓存指令数量: {}", RconService.COMMAND_INFO.size());
            } else {
                log.warn("⚠️ 未找到可用的服务器指令");
            }

        } catch (Exception e) {
            log.error("❌ 初始化服务器指令缓存失败", e);
            throw new RuntimeException("指令缓存初始化失败", e);
        }
    }

    /**
     * 初始化 Rcon 连接
     */
    private void initRconConnections() {
        try {
            log.debug("🔌 初始化 Rcon 连接...");

            ServerInfo query = new ServerInfo();
            query.setStatus(1L);
            List<ServerInfo> activeServers = serverInfoService.selectServerInfoList(query);

            if (activeServers == null || activeServers.isEmpty()) {
                log.warn("⚠️ 没有活跃的服务器需要建立 Rcon 连接");
                return;
            }

            // 并发初始化 Rcon 连接

            // 等待所有 Rcon 连接完成
            CompletableFuture.allOf(activeServers.stream()
                            .map(serverInfo -> CompletableFuture.runAsync(
                                    () -> {
                                        try {
                                            rconService.init(serverInfo);
                                        } catch (Exception e) {
                                            log.error("❌ 服务器 [{}] Rcon 连接初始化失败: {}",
                                                    serverInfo.getId(), e.getMessage());
                                        }
                                    },
                                    executorService
                            )).toArray(CompletableFuture[]::new))
                    .get(20, TimeUnit.SECONDS);

            log.info("✓ Rcon 连接初始化完成，共 {} 个服务器", RconCache.size());

        } catch (Exception e) {
            log.error("❌ Rcon 连接初始化失败", e);
            // Rcon 连接失败不应该导致整个初始化失败
        }
    }

    /**
     * 初始化 Node 节点服务器
     */
    private void initNodeServers() {
        try {
            log.debug("🖥️ 初始化节点服务器缓存...");

            NodeServer query = new NodeServer();
            query.setStatus("0");
            List<NodeServer> nodeServers = nodeServerService.selectNodeServerList(query);

            if (nodeServers != null && !nodeServers.isEmpty()) {
                nodeServers.forEach(server -> NodeCache.put(server.getId(), server));
                log.info("✓ 缓存节点服务器数量: {}", NodeCache.size());
            } else {
                log.warn("⚠️ 未找到可用的节点服务器");
            }

        } catch (Exception e) {
            log.error("❌ 初始化节点服务器缓存失败", e);
            throw new RuntimeException("节点服务器缓存初始化失败", e);
        }
    }

    /**
     * 初始化自定义邮件模板
     */
    private void initEmailTemplates() {
        try {
            log.debug("📧 初始化邮件模板缓存...");

            CustomEmailTemplates query = new CustomEmailTemplates();
            query.setStatus(1L); // 只加载启用的模板
            List<CustomEmailTemplates> templates = customEmailTemplatesService.selectCustomEmailTemplatesList(query);

            if (templates != null && !templates.isEmpty()) {
                for (CustomEmailTemplates template : templates) {
                    if (template.getServerId() != null) {
                        EmailTempCache.put(template.getId().toString(), template);
                    } else {
                        EmailTempCache.put("default", template); // 默认模板
                    }
                }
                log.info("✓ 缓存邮件模板数量: {}", EmailTempCache.size());
            } else {
                log.warn("⚠️ 未找到可用的邮件模板");
            }

        } catch (Exception e) {
            log.error("❌ 初始化邮件模板缓存失败", e);
            // 邮件模板失败不应该导致整个初始化失败
        }
    }

    /**
     * 更新节点控制端服务器信息
     */
    private void updateNodeMasterInfo() {
        if (NodeCache.isEmpty()) {
            log.debug("⚠️ 没有节点服务器需要更新主控信息");
            return;
        }

        log.debug("🔄 更新节点控制端信息...");

        String hostIp = IpUtils.getHostIp();

        NodeCache.getMap().forEach((key, node) -> {
            try {
                JSONObject request = buildMasterInfoRequest(node, hostIp);
                HttpResponse response = sendMasterInfoUpdate(node, request);
                handleMasterInfoResponse(node, response);

                // 避免请求过于频繁
                TimeUnit.MILLISECONDS.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("⚠️ 节点服务器 [{}] 主控信息更新被中断", node.getName());
            } catch (Exception e) {
                log.error("❌ 节点服务器 [{}] 主控信息更新异常: {}", node.getName(), e.getMessage());
            }
        });

        log.info("✓ 节点控制端信息更新完成");
    }

    /**
     * 构建主控信息请求体
     */
    private JSONObject buildMasterInfoRequest(NodeServer node, String hostIp) {
        JSONObject request = new JSONObject();
        request.put("masterUuid", node.getUuid());
        request.put("version", endlessConfig.getVersion());
        request.put("protocolVersion", node.getProtocol());
        request.put("ipAddress", hostIp);
        return request;
    }

    /**
     * 发送主控信息更新请求
     */
    private HttpResponse sendMasterInfoUpdate(NodeServer node, JSONObject request) {
        return HttpUtil.createPost(ApiUtil.getUpdateMasterInfoApi(node))
                .header(ApiUtil.X_ENDLESS_TOKEN, node.getToken())
                .timeout(5000)
                .body(request.toJSONString())
                .execute();
    }

    /**
     * 处理主控信息更新响应
     */
    private void handleMasterInfoResponse(NodeServer node, HttpResponse response) {
        if (!response.isOk()) {
            log.warn("❌ 节点服务器 [{}] 主控信息更新请求失败，HTTP状态码: {}",
                    node.getName(), response.getStatus());
            return;
        }

        JSONObject body = JSONObject.parseObject(response.body(), JSONObject.class);
        if (body.getBoolean("success")) {
            log.debug("✓ 节点服务器 [{}] 主控信息更新成功", node.getName());
        } else {
            log.warn("❌ 节点服务器 [{}] 主控信息更新失败: {}",
                    node.getName(), body.getString("message"));
        }
    }

    /**
     * 发送初始化完成广播
     */
    private void sendInitBroadcast() {
        try {
            String message = String.format("Rcon ready! Time: %s", DateUtils.getNowDate());
            rconService.sendCommand("all", "say " + message, false);
            log.debug("✓ 初始化完成广播已发送");
        } catch (Exception e) {
            log.warn("⚠️ 发送初始化广播失败: {}", e.getMessage());
        }
    }
}