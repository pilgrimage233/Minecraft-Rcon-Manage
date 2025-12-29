package cc.endmc.quartz.task;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.relation.IRconNodeInstanceRelationService;
import cc.endmc.server.service.server.IServerInfoService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步任务
 * 同步RCON服务器关联的节点实例中的OP和封禁名单
 *
 * @author Memory
 * @date 2025-12-27
 */
@Slf4j
@Component("syncTask")
@RequiredArgsConstructor
public class SyncTask {

    private final IRconNodeInstanceRelationService relationService;
    private final IServerInfoService serverInfoService;
    private final INodeMinecraftServerService nodeMinecraftServerService;
    private final IOperatorListService operatorListService;
    private final IBanlistInfoService banlistInfoService;
    private final INodeServerService nodeServerService;

    /**
     * 同步所有RCON服务器关联实例的OP和封禁名单
     */
    public void syncAllServerData() {
        log.info("开始同步所有RCON服务器关联实例的OP和封禁名单");

        try {
            // 获取所有RCON服务器
            List<ServerInfo> serverList = serverInfoService.selectServerInfoList(new ServerInfo());

            for (ServerInfo server : serverList) {
                try {
                    syncServerData(server.getId());
                } catch (Exception e) {
                    log.error("同步服务器[{}]数据失败: {}", server.getNameTag(), e.getMessage(), e);
                }
            }

            log.info("完成同步所有RCON服务器关联实例的OP和封禁名单");
        } catch (Exception e) {
            log.error("同步任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步指定RCON服务器关联实例的OP和封禁名单
     */
    public void syncServerData(Long rconServerId) {
        log.info("开始同步RCON服务器[{}]关联实例的数据", rconServerId);

        try {
            // 查找RCON服务器关联的实例
            RconNodeInstanceRelation relation = relationService.selectByRconServerId(rconServerId);
            if (relation == null || !"0".equals(relation.getStatus())) {
                log.warn("RCON服务器[{}]未关联实例或关联已停用", rconServerId);
                return;
            }

            // 获取实例信息
            NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(relation.getInstanceId());
            if (instance == null) {
                log.warn("未找到实例[{}]", relation.getInstanceId());
                return;
            }

            // 同步OP名单
            syncOperatorList(rconServerId, relation, instance);

            // 同步封禁名单
            syncBanList(rconServerId, relation, instance);

            log.info("完成同步RCON服务器[{}]关联实例的数据", rconServerId);
        } catch (Exception e) {
            log.error("同步RCON服务器[{}]数据失败: {}", rconServerId, e.getMessage(), e);
        }
    }

    /**
     * 同步OP名单
     */
    private void syncOperatorList(Long rconServerId, RconNodeInstanceRelation relation, NodeMinecraftServer instance) {
        try {
            log.debug("开始同步OP名单 - RCON服务器[{}], 实例[{}]", rconServerId, instance.getName());

            // 读取实例目录下的ops.json文件
            String opsJsonContent = readInstanceFile(relation.getNodeId(), instance.getId().intValue(), "ops.json");
            if (StringUtils.isEmpty(opsJsonContent)) {
                log.warn("未能读取到ops.json文件内容");
                return;
            }

            // 解析ops.json
            List<OpEntry> fileOpList = parseOpsJson(opsJsonContent);

            // 获取数据库中的OP名单
            OperatorList query = new OperatorList();
            List<OperatorList> dbOpList = operatorListService.selectOperatorListList(query);

            // 对比并同步
            syncOperatorData(fileOpList, dbOpList, rconServerId);

            log.debug("完成同步OP名单 - RCON服务器[{}]", rconServerId);
        } catch (Exception e) {
            log.error("同步OP名单失败 - RCON服务器[{}]: {}", rconServerId, e.getMessage(), e);
        }
    }

    /**
     * 同步封禁名单
     */
    private void syncBanList(Long rconServerId, RconNodeInstanceRelation relation, NodeMinecraftServer instance) {
        try {
            log.debug("开始同步封禁名单 - RCON服务器[{}], 实例[{}]", rconServerId, instance.getName());

            // 读取实例目录下的banned-players.json文件
            String bannedJsonContent = readInstanceFile(relation.getNodeId(), instance.getId().intValue(), "banned-players.json");
            if (StringUtils.isEmpty(bannedJsonContent)) {
                log.warn("未能读取到banned-players.json文件内容");
                return;
            }

            // 解析banned-players.json
            List<BanEntry> fileBanList = parseBannedPlayersJson(bannedJsonContent);

            // 获取数据库中的封禁名单
            BanlistInfo query = new BanlistInfo();
            List<BanlistInfo> dbBanList = banlistInfoService.selectBanlistInfoList(query);

            // 对比并同步
            syncBanData(fileBanList, dbBanList, rconServerId);

            log.debug("完成同步封禁名单 - RCON服务器[{}]", rconServerId);
        } catch (Exception e) {
            log.error("同步封禁名单失败 - RCON服务器[{}]: {}", rconServerId, e.getMessage(), e);
        }
    }

    /**
     * 读取实例文件内容
     */
    private String readInstanceFile(Long nodeId, Integer instanceId, String fileName) {
        try {
            // 获取实例信息以构建文件路径
            NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId.longValue());
            if (instance == null) {
                log.warn("实例不存在: {}", instanceId);
                return null;
            }

            // 构建文件路径：实例目录 + 文件名
            String filePath = instance.getServerPath() + "/" + fileName;

            Map<String, Object> params = new HashMap<>();
            params.put("id", nodeId.intValue());
            params.put("path", filePath);

            // 使用NodeServerService的getFileContent方法读取文件
            String content = nodeServerService.getFileContent(params);

            if (StringUtils.isEmpty(content)) {
                log.warn("文件内容为空 - 节点[{}], 实例[{}], 文件[{}]", nodeId, instanceId, fileName);
            }

            return content;
        } catch (Exception e) {
            log.error("读取文件异常 - 节点[{}], 实例[{}], 文件[{}]: {}", nodeId, instanceId, fileName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析ops.json文件
     */
    private List<OpEntry> parseOpsJson(String jsonContent) {
        try {
            JSONArray jsonArray = JSON.parseArray(jsonContent);
            List<OpEntry> opList = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject opObj = jsonArray.getJSONObject(i);
                OpEntry entry = new OpEntry();
                entry.setUuid(opObj.getString("uuid"));
                entry.setName(opObj.getString("name"));
                entry.setLevel(opObj.getInteger("level"));
                entry.setBypassesPlayerLimit(opObj.getBoolean("bypassesPlayerLimit"));
                opList.add(entry);
            }

            return opList;
        } catch (Exception e) {
            log.error("解析ops.json失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析banned-players.json文件
     */
    private List<BanEntry> parseBannedPlayersJson(String jsonContent) {
        try {
            JSONArray jsonArray = JSON.parseArray(jsonContent);
            List<BanEntry> banList = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject banObj = jsonArray.getJSONObject(i);
                BanEntry entry = new BanEntry();
                entry.setUuid(banObj.getString("uuid"));
                entry.setName(banObj.getString("name"));
                entry.setReason(banObj.getString("reason"));
                entry.setSource(banObj.getString("source"));
                entry.setCreated(banObj.getString("created"));
                entry.setExpires(banObj.getString("expires"));
                banList.add(entry);
            }

            return banList;
        } catch (Exception e) {
            log.error("解析banned-players.json失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 同步OP数据
     */
    private void syncOperatorData(List<OpEntry> fileOpList, List<OperatorList> dbOpList, Long rconServerId) {
        try {
            // 将数据库OP名单转换为Map，便于查找
            Map<String, OperatorList> dbOpMap = dbOpList.stream()
                    .collect(Collectors.toMap(OperatorList::getUserName, op -> op, (existing, replacement) -> existing));

            // 处理文件中的OP
            for (OpEntry fileOp : fileOpList) {
                if (StringUtils.isEmpty(fileOp.getName())) {
                    continue;
                }

                OperatorList dbOp = dbOpMap.get(fileOp.getName());
                if (dbOp == null) {
                    // 数据库中不存在，新增
                    OperatorList newOp = new OperatorList();
                    newOp.setUserName(fileOp.getName());
                    newOp.setStatus(1L); // 1表示有效
                    newOp.setParameter(JSON.toJSONString(fileOp));
                    newOp.setCreateBy("syncTask");
                    newOp.setCreateTime(DateUtils.getNowDate());
                    newOp.setRemark("从实例文件同步");

                    operatorListService.insertOperatorList(newOp);
                    log.debug("新增OP: {}", fileOp.getName());
                } else {
                    // 数据库中存在，检查是否需要更新
                    String newParameter = JSON.toJSONString(fileOp);
                    if (!newParameter.equals(dbOp.getParameter()) || !Long.valueOf(1L).equals(dbOp.getStatus())) {
                        dbOp.setStatus(1L);
                        dbOp.setParameter(newParameter);
                        dbOp.setUpdateBy("syncTask");
                        dbOp.setUpdateTime(DateUtils.getNowDate());

                        operatorListService.updateOperatorList(dbOp);
                        log.debug("更新OP: {}", fileOp.getName());
                    }
                }
            }

            // 处理数据库中存在但文件中不存在的OP（标记为无效）
            Set<String> fileOpNames = fileOpList.stream()
                    .map(OpEntry::getName)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            for (OperatorList dbOp : dbOpList) {
                if (Long.valueOf(1L).equals(dbOp.getStatus()) && !fileOpNames.contains(dbOp.getUserName())) {
                    dbOp.setStatus(0L); // 0表示无效
                    dbOp.setUpdateBy("syncTask");
                    dbOp.setUpdateTime(DateUtils.getNowDate());

                    operatorListService.updateOperatorList(dbOp);
                    log.debug("标记OP为无效: {}", dbOp.getUserName());
                }
            }

        } catch (Exception e) {
            log.error("同步OP数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步封禁数据
     */
    private void syncBanData(List<BanEntry> fileBanList, List<BanlistInfo> dbBanList, Long rconServerId) {
        try {
            // 将数据库封禁名单转换为Map，便于查找
            Map<String, BanlistInfo> dbBanMap = dbBanList.stream()
                    .collect(Collectors.toMap(BanlistInfo::getUserName, ban -> ban, (existing, replacement) -> existing));

            // 处理文件中的封禁
            for (BanEntry fileBan : fileBanList) {
                if (StringUtils.isEmpty(fileBan.getName())) {
                    continue;
                }

                BanlistInfo dbBan = dbBanMap.get(fileBan.getName());
                if (dbBan == null) {
                    // 数据库中不存在，新增
                    BanlistInfo newBan = new BanlistInfo();
                    newBan.setUserName(fileBan.getName());
                    newBan.setState(1L); // 1表示封禁中
                    newBan.setReason(fileBan.getReason());
                    newBan.setCreateBy("syncTask");
                    newBan.setCreateTime(DateUtils.getNowDate());
                    newBan.setRemark("从实例文件同步 - " + JSON.toJSONString(fileBan));

                    banlistInfoService.insertBanlistInfo(newBan);
                    log.debug("新增封禁: {}", fileBan.getName());
                } else {
                    // 数据库中存在，检查是否需要更新
                    if (!Long.valueOf(1L).equals(dbBan.getState()) || !Objects.equals(fileBan.getReason(), dbBan.getReason())) {
                        dbBan.setState(1L);
                        dbBan.setReason(fileBan.getReason());
                        dbBan.setUpdateBy("syncTask");
                        dbBan.setUpdateTime(DateUtils.getNowDate());

                        banlistInfoService.updateBanlistInfo(dbBan, false);
                        log.debug("更新封禁: {}", fileBan.getName());
                    }
                }
            }

            // 处理数据库中存在但文件中不存在的封禁（标记为解封）
            Set<String> fileBanNames = fileBanList.stream()
                    .map(BanEntry::getName)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());

            for (BanlistInfo dbBan : dbBanList) {
                if (Long.valueOf(1L).equals(dbBan.getState()) && !fileBanNames.contains(dbBan.getUserName())) {
                    dbBan.setState(0L); // 0表示已解封
                    dbBan.setUpdateBy("syncTask");
                    dbBan.setUpdateTime(DateUtils.getNowDate());

                    banlistInfoService.updateBanlistInfo(dbBan, false);
                    log.debug("标记为已解封: {}", dbBan.getUserName());
                }
            }

        } catch (Exception e) {
            log.error("同步封禁数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * OP条目
     */
    @Data
    private static class OpEntry {
        private String uuid;
        private String name;
        private Integer level;
        private Boolean bypassesPlayerLimit;
    }

    /**
     * 封禁条目
     */
    @Data
    private static class BanEntry {
        private String uuid;
        private String name;
        private String reason;
        private String source;
        private String created;
        private String expires;
    }
}