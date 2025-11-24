package cc.endmc.node.task;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器状态同步定时任务
 * 每隔5分钟检查所有实例的运行状态并更新到数据库
 *
 * @author Memory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerStatusSyncTask {

    private final INodeMinecraftServerService nodeMinecraftServerService;

    /**
     * 同步服务器状态
     * 每5分钟执行一次
     * 可通过配置文件修改:
     * 执行间隔，单位：毫秒，默认300000即5分钟
     * 初始延迟，单位：毫秒，默认60000即1分钟
     */
    @Scheduled(
            fixedRate = 300000,
            initialDelay = 60000
    )
    public void syncServerStatus() {
        log.debug("开始执行服务器状态同步任务");

        try {
            // 查询所有实例
            NodeMinecraftServer queryParam = new NodeMinecraftServer();
            List<NodeMinecraftServer> serverList = nodeMinecraftServerService.selectNodeMinecraftServerList(queryParam);

            if (serverList == null || serverList.isEmpty()) {
                log.debug("没有需要同步状态的服务器实例");
                return;
            }

            int successCount = 0;
            int failCount = 0;
            int updateCount = 0;

            for (NodeMinecraftServer server : serverList) {
                try {
                    // 跳过没有关联节点实例的服务器
                    if (server.getNodeInstancesId() == null || server.getNodeId() == null) {
                        log.debug("服务器 {} 没有关联节点实例，跳过状态同步", server.getName());
                        continue;
                    }

                    // 调用 getStatus 方法获取状态
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", server.getNodeId().intValue());
                    params.put("serverId", server.getId().intValue());

                    AjaxResult result = nodeMinecraftServerService.getStatus(params);

                    if (result != null && result.get("code") != null && (Integer) result.get("code") == 200) {
                        // 解析返回的状态信息
                        Object data = result.get("data");
                        if (data instanceof JSONObject) {
                            JSONObject statusData = (JSONObject) data;
                            Boolean isRunning = statusData.getBoolean("isRunning");
                            String statusStr = statusData.getString("status");

                            // 确定新状态
                            String newStatus;
                            if (Boolean.TRUE.equals(isRunning)) {
                                newStatus = "1"; // 运行中
                            } else if ("STOPPED".equals(statusStr)) {
                                newStatus = "2"; // 已停止
                            } else {
                                newStatus = "0"; // 未启动
                            }

                            // 如果状态发生变化，更新数据库（仅更新状态，不触发节点API）
                            if (!newStatus.equals(server.getStatus())) {
                                String oldStatus = server.getStatus();
                                nodeMinecraftServerService.updateServerStatusOnly(server.getId(), newStatus);
                                updateCount++;
                                log.info("服务器 {} 状态已更新: {} -> {}",
                                        server.getName(),
                                        getStatusName(oldStatus),
                                        getStatusName(newStatus));
                            }

                            successCount++;
                        } else {
                            log.warn("服务器 {} 状态数据格式异常", server.getName());
                            failCount++;
                        }
                    } else {
                        // 获取状态失败，可能是节点离线或服务器不存在
                        log.warn("获取服务器 {} 状态失败: {}",
                                server.getName(),
                                result != null ? result.get("msg") : "未知错误");

                        // 如果当前状态是运行中，标记为异常（仅更新状态，不触发节点API）
                        if ("1".equals(server.getStatus())) {
                            nodeMinecraftServerService.updateServerStatusOnly(server.getId(), "3");
                            updateCount++;
                            log.info("服务器 {} 状态已更新为异常", server.getName());
                        }

                        failCount++;
                    }

                } catch (Exception e) {
                    log.error("同步服务器 {} 状态时发生错误", server.getName(), e);
                    failCount++;
                }
            }

            log.info("服务器状态同步任务完成 - 总数: {}, 成功: {}, 失败: {}, 更新: {}",
                    serverList.size(), successCount, failCount, updateCount);

        } catch (Exception e) {
            log.error("执行服务器状态同步任务时发生错误", e);
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "0":
                return "未启动";
            case "1":
                return "运行中";
            case "2":
                return "已停止";
            case "3":
                return "异常";
            default:
                return "未知";
        }
    }
}
