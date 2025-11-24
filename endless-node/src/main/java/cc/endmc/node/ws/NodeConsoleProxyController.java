package cc.endmc.node.ws;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.utils.ApiUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/node/console/proxy")
public class NodeConsoleProxyController {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, StompSession> clientToDownstream = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> wsInfoCache = new ConcurrentHashMap<>();

    @MessageMapping("/node/console/subscribe")
    public void subscribeConsole(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        try {
            Number nodeIdNum = (Number) payload.get("nodeId");
            Number serverIdNum = (Number) payload.get("serverId");
            if (nodeIdNum == null || serverIdNum == null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "缺少参数");
                messagingTemplate.convertAndSendToUser(Objects.requireNonNull(sessionId), "/queue/node-console", resp);
                return;
            }
            long nodeId = nodeIdNum.longValue();
            int serverId = serverIdNum.intValue();

            NodeServer node = NodeCache.get(nodeId);
            if (node == null || !StringUtils.hasText(node.getToken())) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("error", "节点不存在或token缺失");
                messagingTemplate.convertAndSendToUser(Objects.requireNonNull(sessionId), "/queue/node-console", resp);
                return;
            }

            // 建立到节点端的 STOMP 连接
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

            String wsUrl = ApiUtil.getBaseUrl(node) + "/ws";
            StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession downstream, StompHeaders connectedHeaders) {
                    // 订阅控制台
                    Map<String, Object> subBody = new HashMap<>();
                    subBody.put("serverId", serverId);
                    subBody.put("token", node.getToken());
                    downstream.send("/app/console/subscribe", JSONObject.toJSONString(subBody));

                    // 监听控制台topic并转发到主控端topic
                    downstream.subscribe("/topic/console/" + serverId, new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return byte[].class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            try {
                                String text = new String((byte[]) payload);
                                messagingTemplate.convertAndSend("/topic/node-console/" + nodeId + "/" + serverId, JSONObject.parse(text));
                            } catch (Exception e) {
                                Map<String, Object> msg = new HashMap<>();
                                msg.put("line", payload.toString());
                                messagingTemplate.convertAndSend("/topic/node-console/" + nodeId + "/" + serverId, msg);
                            }
                        }
                    });

                    clientToDownstream.put(sessionIdKey(sessionId, nodeId, serverId), downstream);
                    Map<String, Object> ok = new HashMap<>();
                    ok.put("message", "已代理订阅控制台");
                    messagingTemplate.convertAndSendToUser(Objects.requireNonNull(sessionId), "/queue/node-console", ok);
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "下游连接异常: " + exception.getMessage());
                    messagingTemplate.convertAndSendToUser(Objects.requireNonNull(sessionId), "/queue/node-console", err);
                }
            };

            stompClient.connect(URI.create(wsUrl).toString(), sessionHandler);
        } catch (Exception e) {
            log.error("代理订阅失败", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(headers.getSessionId()), "/queue/node-console", err);
        }
    }

    private String sessionIdKey(String sessionId, long nodeId, int serverId) {
        return sessionId + "::" + nodeId + "::" + serverId;
    }

    private String wsInfoCacheKey(long nodeId, int serverId) {
        return nodeId + "::" + serverId;
    }

    /**
     * 获取WebSocket连接信息 - 代理模式下直接使用/ws
     */
    @GetMapping
    @ResponseBody
    public AjaxResult getWsInfo(@RequestParam long nodeId, @RequestParam int serverId) {
        try {
            NodeServer node = NodeCache.get(nodeId);
            if (node == null || !StringUtils.hasText(node.getToken())) {
                return AjaxResult.error("节点不存在或token缺失");
            }

            // 构建WebSocket连接信息
            String wsInfoKey = wsInfoCacheKey(nodeId, serverId);
            Map<String, Object> wsInfo = wsInfoCache.get(wsInfoKey);

            if (wsInfo == null) {
                wsInfo = new HashMap<>();
                // 代理模式下直接使用/ws连接
                wsInfo.put("wsUrl", "/ws");
                // 订阅路径
                wsInfo.put("console", "/topic/node-console/");
                // 订阅指令路径
                wsInfo.put("subscribe", "/app/node/console/subscribe");
                // 使用节点的token
                wsInfo.put("token", node.getToken());

                // 缓存WebSocket信息，有效期10分钟
                wsInfoCache.put(wsInfoKey, wsInfo);

                // 定时清理缓存
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        wsInfoCache.remove(wsInfoKey);
                    }
                }, 10 * 60 * 1000);
            }

            return AjaxResult.success(wsInfo);
        } catch (Exception e) {
            log.error("获取WebSocket连接信息失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    @MessageMapping("/node/console/disconnect")
    public void disconnect(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        if (sessionId == null) return;

        try {
            Number nodeIdNum = (Number) payload.get("nodeId");
            Number serverIdNum = (Number) payload.get("serverId");
            if (nodeIdNum == null || serverIdNum == null) return;

            long nodeId = nodeIdNum.longValue();
            int serverId = serverIdNum.intValue();

            String key = sessionIdKey(sessionId, nodeId, serverId);
            StompSession session = clientToDownstream.remove(key);
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception e) {
            log.error("断开连接失败", e);
        }
    }
}


