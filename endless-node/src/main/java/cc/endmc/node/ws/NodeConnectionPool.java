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

import javax.annotation.PreDestroy;
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
     * 启动心跳检测
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            connectionPool.forEach((nodeId, connection) -> {
                if (!connection.isConnected()) {
                    log.warn("检测到节点连接断开，尝试重连: nodeId={}", nodeId);
                    connection.reconnect();
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
        private static final int MAX_RECONNECT_ATTEMPTS = 5;
        private final long nodeId;
        private final NodeServer nodeServer;
        private final Map<Integer, StompSession.Subscription> serverSubscriptions = new ConcurrentHashMap<>();
        private WebSocketStompClient stompClient;
        private StompSession stompSession;
        private ThreadPoolTaskScheduler taskScheduler;
        private volatile boolean connected = false;
        private volatile boolean connecting = false;
        private int reconnectAttempts = 0;

        public NodeConnection(long nodeId, NodeServer nodeServer) {
            this.nodeId = nodeId;
            this.nodeServer = nodeServer;
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

                        // 重新订阅所有服务器
                        resubscribeAll();
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

                stompClient.connect(wsUrl, sessionHandler);
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
            if (!connected || stompSession == null) {
                log.warn("节点未连接，无法订阅: nodeId={}, serverId={}", nodeId, serverId);
                return;
            }

            if (serverSubscriptions.containsKey(serverId)) {
                log.debug("服务器已订阅: nodeId={}, serverId={}", nodeId, serverId);
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
         * 重新订阅所有服务器
         */
        private void resubscribeAll() {
            Set<Integer> serverIds = new HashSet<>(serverSubscriptions.keySet());
            serverSubscriptions.clear();

            for (Integer serverId : serverIds) {
                subscribeServer(serverId);
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
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                log.error("节点重连次数超过限制，放弃重连: nodeId={}", nodeId);
                return;
            }

            reconnectAttempts++;
            disconnect();
            connect();
        }

        /**
         * 调度重连
         */
        private void scheduleReconnect() {
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                log.error("节点重连次数超过限制: nodeId={}", nodeId);
                return;
            }

            long delay = Math.min(30, (long) Math.pow(2, reconnectAttempts));
            log.info("将在{}秒后重连节点: nodeId={}", delay, nodeId);

            reconnectExecutor.schedule(this::reconnect, delay, TimeUnit.SECONDS);
        }

        public boolean isConnected() {
            return connected && stompSession != null && stompSession.isConnected();
        }
    }
}
