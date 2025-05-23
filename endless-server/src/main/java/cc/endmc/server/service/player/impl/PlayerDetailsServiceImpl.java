package cc.endmc.server.service.player.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 玩家详情Service业务层处理
 *
 * @author Memory
 * @date 2024-12-31
 */
@Service
public class PlayerDetailsServiceImpl implements IPlayerDetailsService {
    @Autowired
    private PlayerDetailsMapper playerDetailsMapper;

    @Autowired
    private IOperatorListService operatorListService;

    @Autowired
    private RconService rconService;

    /**
     * 查询玩家详情
     *
     * @param id 玩家详情主键
     * @return 玩家详情
     */
    @Override
    public PlayerDetails selectPlayerDetailsById(Long id) {
        return playerDetailsMapper.selectPlayerDetailsById(id);
    }

    /**
     * 查询玩家详情列表
     *
     * @param playerDetails 玩家详情
     * @return 玩家详情
     */
    @Override
    public List<PlayerDetails> selectPlayerDetailsList(PlayerDetails playerDetails) {
        if (playerDetails.getUserName() != null) {
            playerDetails.setUserName(playerDetails.getUserName().toLowerCase());
        }
        return playerDetailsMapper.selectPlayerDetailsList(playerDetails);
    }

    /**
     * 新增玩家详情
     *
     * @param playerDetails 玩家详情
     * @return 结果
     */
    @Override
    public int insertPlayerDetails(PlayerDetails playerDetails) {
        playerDetails.setCreateTime(DateUtils.getNowDate());
        return playerDetailsMapper.insertPlayerDetails(playerDetails);
    }


    /**
     * 修改玩家详情
     *
     * @param playerDetails 玩家详情
     * @param checkOperator 是否检查管理员权限
     * @return 结果
     */
    @Override
    public int updatePlayerDetails(PlayerDetails playerDetails, boolean checkOperator) {

        // 只有在需要检查管理员权限时才执行这部分逻辑
        if (checkOperator && playerDetails.getIdentity() != null) {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            playerDetails.setUpdateBy(name);
            playerDetails.setUpdateTime(DateUtils.getNowDate());
            if (playerDetails.getIdentity().equals(Identity.OPERATOR.getValue())) {
                OperatorList operator = new OperatorList();
                operator.setUserName(playerDetails.getUserName());
                // 查询玩家之前是否获得过管理员权限
                final List<OperatorList> operatorLists = operatorListService.selectOperatorListList(operator);
                if (operatorLists != null && !operatorLists.isEmpty()) {
                    operator = operatorLists.get(0);
                    operator.setStatus(1L);
                    operator.setUpdateBy(name);
                    operator.setUpdateTime(DateUtils.getNowDate());
                    operatorListService.updateOperatorList(operator);
                } else {
                    operator.setStatus(1L);
                    operator.setCreateTime(DateUtils.getNowDate());
                    operator.setCreateBy(name);
                    if (playerDetails.getRemark() != null) {
                        operator.setRemark(playerDetails.getRemark());
                    }
                    operatorListService.insertOperatorList(operator);
                }

                // 发送命令
                rconService.sendCommand("all", String.format(Command.OP_ADD, playerDetails.getUserName()), true);
            }

            if (playerDetails.getIdentity().equals(Identity.PLAYER.getValue())) {
                OperatorList operator = new OperatorList();
                operator.setUserName(playerDetails.getUserName());
                // 查询玩家是否处于管理员列表
                final List<OperatorList> operatorLists = operatorListService.selectOperatorListList(operator);
                if (operatorLists != null && !operatorLists.isEmpty()) {
                    operator = operatorLists.get(0);
                    if (operator.getStatus().equals(1L)) {
                        operator.setStatus(0L);
                        operator.setUpdateBy(name);
                        operator.setUpdateTime(DateUtils.getNowDate());

                        operatorListService.updateOperatorList(operator);

                        // 发送命令
                        rconService.sendCommand("all", String.format(Command.OP_REMOVE, playerDetails.getUserName()), true);
                    }
                }
            }
        }
        return playerDetailsMapper.updatePlayerDetails(playerDetails);
    }

    /**
     * 批量删除玩家详情
     *
     * @param ids 需要删除的玩家详情主键
     * @return 结果
     */
    @Override
    public int deletePlayerDetailsByIds(Long[] ids) {
        return playerDetailsMapper.deletePlayerDetailsByIds(ids);
    }

    /**
     * 根据条件删除玩家详情信息
     *
     * @param info
     * @return
     */
    @Override
    public int deletePlayerDetailsByInfo(PlayerDetails info) {
        return playerDetailsMapper.deletePlayerDetailsByInfo(info);
    }

    /**
     * 删除玩家详情信息
     *
     * @param id 玩家详情主键
     * @return 结果
     */
    @Override
    public int deletePlayerDetailsById(Long id) {
        return playerDetailsMapper.deletePlayerDetailsById(id);
    }

    /**
     * 批量更新玩家最后上线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    @Override
    public int updateLastOnlineTimeByUserNames(List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("userNames", userNames);
        // 使用系统默认时区创建时间
        TimeZone china = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar calendar = Calendar.getInstance(china);
        params.put("currentTime", calendar.getTime());
        return playerDetailsMapper.updateLastOnlineTimeByUserNames(params);
    }

    /**
     * 批量更新玩家最后下线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    @Override
    public int updateLastOfflineTimeByUserNames(List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("userNames", userNames);
        // 使用系统默认时区创建时间
        TimeZone china = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar calendar = Calendar.getInstance(china);
        params.put("currentTime", calendar.getTime());
        return playerDetailsMapper.updateLastOfflineTimeByUserNames(params);
    }
}
