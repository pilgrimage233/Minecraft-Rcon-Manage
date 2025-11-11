package cc.endmc.node.ws;

import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.mapper.NodeServerMapper;
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
public class NodeConsoleProxyController {

    private final NodeServerMapper nodeServerMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, StompSession> clientToDownstream = new ConcurrentHashMap<>();

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
                    // 1) 认证
                    Map<String, Object> authBody = new HashMap<>();
                    authBody.put("token", node.getToken());
                    downstream.send("/app/auth", JSONObject.toJSONString(authBody));
                    // 2) 订阅控制台
                    Map<String, Object> subBody = new HashMap<>();
                    subBody.put("serverId", serverId);
                    subBody.put("token", node.getToken());
                    downstream.send("/app/console/subscribe", JSONObject.toJSONString(subBody));

                    // 3) 监听控制台topic并转发到主控端topic
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
}


