package cc.endmc.node.controller;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.ws.NodeConnectionPool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点连接管理控制器
 * 提供连接池状态查询和管理功能
 */
@RestController
@RequestMapping("/node/connection")
@RequiredArgsConstructor
public class NodeConnectionController extends BaseController {

    private final NodeConnectionPool connectionPool;

    /**
     * 获取所有节点连接状态
     */
    @GetMapping("/status")
    public AjaxResult getConnectionStatus() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 获取连接池统计信息
            Map<Long, NodeConnectionPool.NodeConnection> pool = connectionPool.getConnectionPool();

            List<Map<String, Object>> connections = pool.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> info = new HashMap<>();
                        NodeConnectionPool.NodeConnection conn = entry.getValue();

                        info.put("nodeId", entry.getKey());
                        info.put("connected", conn.isConnected());
                        info.put("reconnectAttempts", conn.getReconnectAttempts());
                        info.put("subscribedServers", conn.getServerSubscriptions().size());

                        return info;
                    })
                    .collect(Collectors.toList());

            result.put("totalConnections", pool.size());
            result.put("activeConnections", pool.values().stream().filter(NodeConnectionPool.NodeConnection::isConnected).count());
            result.put("connections", connections);

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取连接状态失败", e);
            return AjaxResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    /**
     * 重连指定节点
     */
    @PostMapping("/reconnect/{nodeId}")
    public AjaxResult reconnectNode(@PathVariable Long nodeId) {
        try {
            NodeConnectionPool.NodeConnection connection = connectionPool.getOrCreateConnection(nodeId);
            if (connection == null) {
                return AjaxResult.error("节点不存在");
            }

            connection.reconnect();
            return AjaxResult.success("重连指令已发送");
        } catch (Exception e) {
            logger.error("重连节点失败: nodeId={}", nodeId, e);
            return AjaxResult.error("重连失败: " + e.getMessage());
        }
    }

    /**
     * 断开指定节点连接
     */
    @DeleteMapping("/{nodeId}")
    public AjaxResult disconnectNode(@PathVariable Long nodeId) {
        try {
            connectionPool.removeConnection(nodeId);
            return AjaxResult.success("节点连接已断开");
        } catch (Exception e) {
            logger.error("断开节点连接失败: nodeId={}", nodeId, e);
            return AjaxResult.error("断开连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅信息
     */
    @GetMapping("/subscriptions")
    public AjaxResult getSubscriptions() {
        try {
            Map<String, Set<String>> subscriptions = connectionPool.getSubscriptions();

            List<Map<String, Object>> result = subscriptions.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> info = new HashMap<>();
                        String[] parts = entry.getKey().split(":");

                        info.put("nodeId", parts.length > 0 ? parts[0] : "");
                        info.put("serverId", parts.length > 1 ? parts[1] : "");
                        info.put("clientCount", entry.getValue().size());
                        info.put("clients", new ArrayList<>(entry.getValue()));

                        return info;
                    })
                    .collect(Collectors.toList());

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取订阅信息失败", e);
            return AjaxResult.error("获取订阅信息失败: " + e.getMessage());
        }
    }
}
