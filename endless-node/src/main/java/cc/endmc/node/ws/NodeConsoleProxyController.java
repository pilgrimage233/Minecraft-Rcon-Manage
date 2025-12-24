package cc.endmc.node.ws;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点控制台WebSocket代理控制器
 * 使用连接池管理到各个节点的持久化连接
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/node/console/proxy")
public class NodeConsoleProxyController {

    private final NodeConnectionPool connectionPool;

    // WebSocket信息缓存
    private final Map<String, Map<String, Object>> wsInfoCache = new ConcurrentHashMap<>();

    /**
     * 订阅节点服务器控制台
     */
    @MessageMapping("/node/console/subscribe")
    public void subscribeConsole(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        if (sessionId == null) {
            log.warn("客户端会话ID为空");
            return;
        }

        try {
            // 解析参数
            Number nodeIdNum = (Number) payload.get("nodeId");
            Number serverIdNum = (Number) payload.get("serverId");

            if (nodeIdNum == null || serverIdNum == null) {
                log.warn("订阅参数不完整: nodeId={}, serverId={}", nodeIdNum, serverIdNum);
                return;
            }

            long nodeId = nodeIdNum.longValue();
            int serverId = serverIdNum.intValue();

            // 验证节点是否存在
            NodeServer node = NodeCache.get(nodeId);
            if (node == null || !StringUtils.hasText(node.getToken())) {
                log.error("节点不存在或token缺失: nodeId={}", nodeId);
                return;
            }

            // 使用连接池订阅
            connectionPool.subscribe(nodeId, serverId, sessionId);
            log.info("客户端订阅成功: sessionId={}, nodeId={}, serverId={}", sessionId, nodeId, serverId);

        } catch (Exception e) {
            log.error("订阅控制台失败: sessionId={}", sessionId, e);
        }
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
     * 取消订阅
     */
    @MessageMapping("/node/console/unsubscribe")
    public void unsubscribe(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        if (sessionId == null) return;

        try {
            Number nodeIdNum = (Number) payload.get("nodeId");
            Number serverIdNum = (Number) payload.get("serverId");
            if (nodeIdNum == null || serverIdNum == null) return;

            long nodeId = nodeIdNum.longValue();
            int serverId = serverIdNum.intValue();

            connectionPool.unsubscribe(nodeId, serverId, sessionId);
            log.info("客户端取消订阅: sessionId={}, nodeId={}, serverId={}", sessionId, nodeId, serverId);
        } catch (Exception e) {
            log.error("取消订阅失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 监听WebSocket断开事件，清理订阅
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            log.info("客户端断开连接，清理订阅: sessionId={}", sessionId);
            connectionPool.disconnectClient(sessionId);
        }
    }
}


