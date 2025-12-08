package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.player.vo.PlayerDetailsVo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页数据接口
 * 用于提供首页所需的各类统计数据
 */
@RestController
@RequestMapping("/api/v1/home")
public class HomeController extends BaseController {

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private IOperatorListService operatorListService;

    @Autowired
    private IBanlistInfoService banlistInfoService;

    @Autowired
    private INodeServerService nodeServerService;

    @Autowired
    private PlayerDetailsMapper playerDetailsMapper;

    /**
     * 获取基础统计数据（不包含在线玩家信息）
     * 用于首页异步加载
     *
     * @return AjaxResult
     */
    @GetMapping("/basicStats")
    public AjaxResult getBasicStats() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 申请数量和白名单数量
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(new WhitelistInfo());
            result.put("applyCount", whitelistInfos.size());
            int whiteListCount = (int) whitelistInfos.stream().filter(info -> "1".equals(info.getStatus())).count();
            result.put("whiteListCount", whiteListCount);

            // OP数量
            final OperatorList op = new OperatorList();
            op.setStatus(1L);
            result.put("opCount", operatorListService.selectOperatorListList(op).size());

            // 封禁数量
            final BanlistInfo banlistInfo = new BanlistInfo();
            banlistInfo.setState(1L);
            result.put("banCount", banlistInfoService.selectBanlistInfoList(banlistInfo).size());

            // 服务器数量
            List<ServerInfo> serverInfo = serverInfoService.selectServerInfoList(new ServerInfo());
            result.put("serverCount", serverInfo.size());

            return success(result);
        } catch (Exception e) {
            logger.error("获取基础统计数据失败", e);
            return error("获取基础统计数据失败");
        }
    }

    /**
     * 获取节点统计信息
     *
     * @return AjaxResult
     */
    @GetMapping("/nodeStats")
    public AjaxResult getNodeStats() {
        try {
            Map<String, Object> result = new HashMap<>();
            List<NodeServer> nodeServers = nodeServerService.selectNodeServerList(new NodeServer());

            result.put("nodeCount", nodeServers.size());

            // 在线节点数量
            long onlineNodeCount = nodeServers.stream()
                    .filter(node -> "0".equals(node.getStatus()))
                    .count();
            result.put("onlineNodeCount", onlineNodeCount);

            // 节点列表简要信息
            List<Map<String, Object>> nodeList = new ArrayList<>();
            for (NodeServer node : nodeServers) {
                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("id", node.getId());
                nodeInfo.put("name", node.getName());
                nodeInfo.put("status", node.getStatus());
                nodeInfo.put("version", node.getVersion());
                nodeInfo.put("osType", node.getOsType());
                nodeInfo.put("lastHeartbeat", node.getLastHeartbeat());
                nodeList.add(nodeInfo);
            }
            result.put("nodeList", nodeList);

            return success(result);
        } catch (Exception e) {
            logger.error("获取节点统计信息失败", e);
            return error("获取节点统计信息失败");
        }
    }

    /**
     * 获取游戏时长排行榜
     *
     * @return AjaxResult
     */
    @GetMapping("/topPlayers")
    public AjaxResult getTopPlayers() {
        try {
            final List<PlayerDetails> playerDetails = playerDetailsMapper.selectTopTenByGameTime();
            final List<PlayerDetailsVo> playerDetailsVos = new ArrayList<>();
            playerDetails.forEach(o -> {
                final PlayerDetailsVo vo = new PlayerDetailsVo();
                BeanUtils.copyProperties(o, vo);
                playerDetailsVos.add(vo);
            });
            return success(playerDetailsVos);
        } catch (Exception e) {
            logger.error("获取游戏时长排行榜失败", e);
            return error("获取游戏时长排行榜失败");
        }
    }

    /**
     * 获取在线玩家信息
     *
     * @return AjaxResult
     */
    @GetMapping("/onlinePlayerInfo")
    public AjaxResult getOnlinePlayerInfo() {
        try {
            Map<String, Object> onlinePlayer = serverInfoService.getOnlinePlayer(false);
            return success(onlinePlayer);
        } catch (Exception e) {
            logger.error("获取在线玩家信息失败", e);
            return error("获取在线玩家信息失败");
        }
    }
}
