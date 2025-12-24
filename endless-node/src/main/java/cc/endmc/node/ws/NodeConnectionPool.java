package cc.endmc.node.ws;

import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.utils.ApiUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import jakarta.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 节点WebSocket连接池管理器
 * 负责管理主控端到各个节点端的WebSocket连接
 */
@Slf4j
@Component
public class NodeConnectionPool {

    private final SimpMessagingTemplate messagingTemplate;

    // 节点连接池：nodeId -> NodeConnection
    private final Map<Long, NodeConnection> connectionPool = new ConcurrentHashMap<>();

    // 订阅管理：subscriptionKey(nodeId:serverId) -> Set<clientSessionId>
    private final Map<String, Set<String>> subscriptions = new ConcurrentHashMap<>();

    // 心跳检测定时器
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    // 重连定时器
    private final ScheduledExecutorService reconnectExecutor = Executors.newScheduledThreadPool(2);

    public NodeConnectionPool(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        // 启动心跳检测
        startHeartbeat();
    }

    /**
     * 获取或创建到节点的连接
     */
    public NodeConnection getOrCreateConnection(long nodeId) {
        return connectionPool.computeIfAbsent(nodeId, id -> {
            NodeServer node = NodeCache.get(id);
            if (node == null || !StringUtils.hasText(node.getToken())) {
                log.error("节点不存在或token缺失: nodeId={}", id);
                return null;
            }

            NodeConnection connection = new NodeConnection(id, node);
            connection.connect();
            return connection;
        });
    }

    /**
     * 订阅服务器控制台
     */
    public void subscribe(long nodeId, int serverId, String clientSessionId) {
        NodeConnection connection = getOrCreateConnection(nodeId);
        if (connection == null) {
            log.error("无法获取节点连接: nodeId={}", nodeId);
            return;
        }

        String subKey = subscriptionKey(nodeId, serverId);
        subscriptions.computeIfAbsent(subKey, k -> ConcurrentHashMap.newKeySet()).add(clientSessionId);

        connection.subscribeServer(serverId);
    }

    /**
     * 取消订阅
     */
    public void unsubscribe(long nodeId, int serverId, String clientSessionId) {
        String subKey = subscriptionKey(nodeId, serverId);
        Set<String> clients = subscriptions.get(subKey);

        if (clients != null) {
            clients.remove(clientSessionId);

            // 如果没有客户端订阅了，取消节点端订阅
            if (clients.isEmpty()) {
                subscriptions.remove(subKey);
                NodeConnection connection = connectionPool.get(nodeId);
                if (connection != null) {
                    connection.unsubscribeServer(serverId);
                }
            }
        }
    }

    /**
     * 断开客户端的所有订阅
     */
    public void disconnectClient(String clientSessionId) {
        subscriptions.forEach((subKey, clients) -> {
            if (clients.remove(clientSessionId) && clients.isEmpty()) {
                subscriptions.remove(subKey);
                // 解析nodeId和serverId
                String[] parts = subKey.split(":");
                if (parts.length == 2) {
                    try {
                        long nodeId = Long.parseLong(parts[0]);
                        int serverId = Integer.parseInt(parts[1]);
                        NodeConnection connection = connectionPool.get(nodeId);
                        if (connection != null) {
                            connection.unsubscribeServer(serverId);
                        }
                    } catch (NumberFormatException e) {
                        log.error("解析订阅key失败: {}", subKey, e);
                    }
                }
            }
        });
    }

    /**
     * 移除节点连接
     */
    public void removeConnection(long nodeId) {
        NodeConnection connection = connectionPool.remove(nodeId);
        if (connection != null) {
            connection.disconnect();
        }

        // 清理相关订阅
        subscriptions.keySet().removeIf(key -> key.startsWith(nodeId + ":"));
    }

    /**
     * 检查节点是否还有活跃的客户端订阅
     */
    private boolean hasActiveSubscriptions(long nodeId) {
        String prefix = nodeId + ":";
        return subscriptions.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .anyMatch(key -> {
                    Set<String> clients = subscriptions.get(key);
                    return clients != null && !clients.isEmpty();
                });
    }

    /**
     * 启动心跳检测
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            connectionPool.forEach((nodeId, connection) -> {
                // 只有还有客户端订阅时才尝试重连
                if (!connection.isConnected() && hasActiveSubscriptions(nodeId)) {
                    log.warn("检测到节点连接断开，尝试重连: nodeId={}", nodeId);
                    // 心跳触发的重连，重置重连计数器，允许无限重连
                    connection.resetReconnectAttempts();
                    connection.reconnect();
                } else if (!connection.isConnected() && !hasActiveSubscriptions(nodeId)) {
                    // 没有客户端订阅了，移除连接
                    log.info("节点无活跃订阅，移除连接: nodeId={}", nodeId);
                    connectionPool.remove(nodeId);
                    connection.disconnect();
                }
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    private String subscriptionKey(long nodeId, int serverId) {
        return nodeId + ":" + serverId;
    }

    /**
     * 获取连接池（只读）
     */
    public Map<Long, NodeConnection> getConnectionPool() {
        return Collections.unmodifiableMap(connectionPool);
    }

    /**
     * 获取订阅信息（只读）
     */
    public Map<String, Set<String>> getSubscriptions() {
        return Collections.unmodifiableMap(subscriptions);
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭节点连接池...");
        heartbeatExecutor.shutdown();
        reconnectExecutor.shutdown();
        connectionPool.values().forEach(NodeConnection::disconnect);
        connectionPool.clear();
        subscriptions.clear();
    }

    /**
     * 节点连接包装类
     */
    @Data
    public class NodeConnection {
        // 单次重连周期内的最大尝试次数（指数退避后会重置）
        private static final int MAX_RECONNECT_ATTEMPTS_PER_CYCLE = 5;
        // 最大重连延迟（秒）
        private static final int MAX_RECONNECT_DELAY_SECONDS = 60;
        private static final int CONNECT_TIMEOUT_SECONDS = 15;
        private final long nodeId;
        private final NodeServer nodeServer;
        private final Map<Integer, StompSession.Subscription> serverSubscriptions = new ConcurrentHashMap<>();
        // 待订阅队列：连接建立前的订阅请求会被缓存到这里
        private final Set<Integer> pendingSubscriptions = ConcurrentHashMap.newKeySet();
        private WebSocketStompClient stompClient;
        private StompSession stompSession;
        private ThreadPoolTaskScheduler taskScheduler;
        private volatile boolean connected = false;
        private volatile boolean connecting = false;
        // 当前重连周期内的尝试次数
        private int reconnectAttempts = 0;
        // 上次成功连接的时间
        private long lastConnectedTime = 0;

        public NodeConnection(long nodeId, NodeServer nodeServer) {
            this.nodeId = nodeId;
            this.nodeServer = nodeServer;
        }

        /**
         * 重置重连计数器（由心跳检测调用，允许持续重连）
         */
        public void resetReconnectAttempts() {
            this.reconnectAttempts = 0;
        }

        /**
         * 建立到节点的连接
         */
        public synchronized void connect() {
            if (connected || connecting) {
                return;
            }

            connecting = true;
            try {
                String wsUrl = ApiUtil.getBaseUrl(nodeServer) + "/ws";
                log.info("连接到节点: nodeId={}, url={}", nodeId, wsUrl);

                // 创建WebSocket客户端，支持HTTPS
                StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

                // 如果是HTTPS，配置SSL信任
                if (wsUrl.startsWith("https://")) {
                    try {
                        // 创建信任所有证书的SSL上下文
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        TrustManager[] trustAllCerts = new TrustManager[]{
                                new X509TrustManager() {
                                    public X509Certificate[] getAcceptedIssuers() {
                                        return new X509Certificate[0];
                                    }

                                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                                    }

                                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                                    }
                                }
                        };
                        sslContext.init(null, trustAllCerts, new SecureRandom());

                        // 配置WebSocket客户端使用自定义SSL上下文
                        webSocketClient.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);
                        log.debug("已配置SSL信任: nodeId={}", nodeId);
                    } catch (Exception e) {
                        log.warn("配置SSL信任失败，将使用默认配置: nodeId={}", nodeId, e);
                    }
                }

                List<Transport> transports = new ArrayList<>();
                transports.add(new WebSocketTransport(webSocketClient));
                SockJsClient sockJsClient = new SockJsClient(transports);
                stompClient = new WebSocketStompClient(sockJsClient);

                // 配置 TaskScheduler 用于心跳
                taskScheduler = new ThreadPoolTaskScheduler();
                taskScheduler.setPoolSize(1);
                taskScheduler.setThreadNamePrefix("stomp-heartbeat-node-" + nodeId + "-");
                taskScheduler.initialize();
                stompClient.setTaskScheduler(taskScheduler);

                // 设置心跳间隔（10秒）
                stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

                StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        log.info("节点连接成功: nodeId={}", nodeId);
                        stompSession = session;
                        connected = true;
                        connecting = false;
                        reconnectAttempts = 0;
                        lastConnectedTime = System.currentTimeMillis();

                        // 处理待订阅队列和重新订阅
                        processPendingAndResubscribe();
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command,
                                                StompHeaders headers, byte[] payload, Throwable exception) {
                        log.error("节点连接异常: nodeId={}, command={}", nodeId, command, exception);
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        log.error("节点传输错误: nodeId={}", nodeId, exception);
                        connected = false;
                        connecting = false;
                        scheduleReconnect();
                    }
                };

                log.debug("开始异步连接节点: nodeId={}", nodeId);

                stompClient.connectAsync(wsUrl, sessionHandler);

                // 设置连接超时检测
                reconnectExecutor.schedule(() -> {
                    if (connecting && !connected) {
                        log.error("连接节点超时: nodeId={}", nodeId);
                        connecting = false;
                        scheduleReconnect();
                    }
                }, CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("连接节点失败: nodeId={}", nodeId, e);
                connected = false;
                connecting = false;
                scheduleReconnect();
            }
        }

        /**
         * 订阅服务器控制台
         */
        public void subscribeServer(int serverId) {
            if (serverSubscriptions.containsKey(serverId)) {
                log.debug("服务器已订阅: nodeId={}, serverId={}", nodeId, serverId);
                return;
            }

            if (!connected || stompSession == null) {
                log.info("节点连接中，将订阅加入待处理队列: nodeId={}, serverId={}", nodeId, serverId);
                pendingSubscriptions.add(serverId);
                if (!connecting) {
                    connect();
                }
                return;
            }

            doSubscribeServer(serverId);
        }

        /**
         * 订阅
         */
        private void doSubscribeServer(int serverId) {
            if (serverSubscriptions.containsKey(serverId)) {
                return;
            }

            try {
                // 发送订阅指令到节点端
                Map<String, Object> subBody = new HashMap<>();
                subBody.put("serverId", serverId);
                subBody.put("token", nodeServer.getToken());
                String jsonPayload = JSONObject.toJSONString(subBody);
                stompSession.send("/app/console/subscribe", jsonPayload.getBytes(StandardCharsets.UTF_8));

                // 订阅节点端的控制台topic
                StompSession.Subscription subscription = stompSession.subscribe(
                        "/topic/console/" + serverId,
                        new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return byte[].class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                try {
                                    String text = new String((byte[]) payload);
                                    Object message = JSONObject.parse(text);
                                    // 转发到主控端topic
                                    messagingTemplate.convertAndSend(
                                            "/topic/node-console/" + nodeId + "/" + serverId,
                                            message
                                    );
                                } catch (Exception e) {
                                    log.error("处理控制台消息失败: nodeId={}, serverId={}", nodeId, serverId, e);
                                }
                            }
                        }
                );

                serverSubscriptions.put(serverId, subscription);
                log.info("订阅服务器控制台成功: nodeId={}, serverId={}", nodeId, serverId);
            } catch (Exception e) {
                log.error("订阅服务器控制台失败: nodeId={}, serverId={}", nodeId, serverId, e);
                // 订阅失败，重新加入待订阅队列，稍后重试
                pendingSubscriptions.add(serverId);
            }
        }

        /**
         * 取消订阅服务器
         */
        public void unsubscribeServer(int serverId) {
            StompSession.Subscription subscription = serverSubscriptions.remove(serverId);
            if (subscription != null) {
                try {
                    subscription.unsubscribe();
                    log.info("取消订阅服务器: nodeId={}, serverId={}", nodeId, serverId);
                } catch (Exception e) {
                    log.error("取消订阅失败: nodeId={}, serverId={}", nodeId, serverId, e);
                }
            }
        }

        /**
         * 处理待订阅队列和重新订阅已有的服务器
         */
        private void processPendingAndResubscribe() {
            Set<Integer> allServerIds = new HashSet<>();

            // 1. 添加待订阅队列中的
            allServerIds.addAll(pendingSubscriptions);
            pendingSubscriptions.clear();

            // 2. 添加之前已订阅的（重连场景）
            allServerIds.addAll(serverSubscriptions.keySet());
            serverSubscriptions.clear();

            // 3. 从外层subscriptions中恢复该节点的所有订阅
            String prefix = nodeId + ":";
            subscriptions.keySet().stream()
                    .filter(key -> key.startsWith(prefix))
                    .forEach(key -> {
                        try {
                            int serverId = Integer.parseInt(key.substring(prefix.length()));
                            allServerIds.add(serverId);
                        } catch (NumberFormatException e) {
                            log.warn("解析serverId失败: {}", key);
                        }
                    });

            log.info("节点连接成功，开始订阅 {} 个服务器: nodeId={}", allServerIds.size(), nodeId);

            for (Integer serverId : allServerIds) {
                doSubscribeServer(serverId);
            }
        }

        /**
         * 断开连接
         */
        public synchronized void disconnect() {
            connected = false;
            connecting = false;

            // 取消所有订阅
            serverSubscriptions.values().forEach(sub -> {
                try {
                    sub.unsubscribe();
                } catch (Exception e) {
                    log.error("取消订阅失败", e);
                }
            });
            serverSubscriptions.clear();

            // 断开STOMP会话
            if (stompSession != null && stompSession.isConnected()) {
                try {
                    stompSession.disconnect();
                } catch (Exception e) {
                    log.error("断开STOMP会话失败", e);
                }
            }

            // 关闭 TaskScheduler
            if (taskScheduler != null) {
                try {
                    taskScheduler.shutdown();
                    log.debug("TaskScheduler已关闭: nodeId={}", nodeId);
                } catch (Exception e) {
                    log.error("关闭TaskScheduler失败: nodeId={}", nodeId, e);
                }
            }

            stompSession = null;
            stompClient = null;
            taskScheduler = null;
            log.info("节点连接已断开: nodeId={}", nodeId);
        }

        /**
         * 重连
         */
        public void reconnect() {
            // 检查是否还有活跃订阅，没有的话不需要重连
            if (!hasActiveSubscriptions(nodeId)) {
                log.info("节点无活跃订阅，跳过重连: nodeId={}", nodeId);
                return;
            }

            // 单个周期内的重连次数限制，超过后等待心跳检测重置
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS_PER_CYCLE) {
                log.warn("节点重连次数达到周期限制，等待下次心跳检测: nodeId={}, attempts={}", 
                        nodeId, reconnectAttempts);
                return;
            }

            reconnectAttempts++;
            log.info("开始第{}次重连: nodeId={}", reconnectAttempts, nodeId);
            disconnect();
            connect();
        }

        /**
         * 调度重连（带指数退避）
         */
        private void scheduleReconnect() {
            // 检查是否还有活跃订阅
            if (!hasActiveSubscriptions(nodeId)) {
                log.info("节点无活跃订阅，跳过调度重连: nodeId={}", nodeId);
                return;
            }

            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS_PER_CYCLE) {
                log.warn("节点重连次数达到周期限制，等待心跳检测: nodeId={}", nodeId);
                return;
            }

            // 指数退避，最大60秒
            long delay = Math.min(MAX_RECONNECT_DELAY_SECONDS, (long) Math.pow(2, reconnectAttempts));
            log.info("将在{}秒后重连节点: nodeId={}, attempt={}", delay, nodeId, reconnectAttempts + 1);

            reconnectExecutor.schedule(this::reconnect, delay, TimeUnit.SECONDS);
        }

        public boolean isConnected() {
            return connected && stompSession != null && stompSession.isConnected();
        }
    }
}
