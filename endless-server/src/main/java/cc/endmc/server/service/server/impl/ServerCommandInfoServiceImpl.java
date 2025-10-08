package cc.endmc.server.service.server.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.cache.ObjectCache;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.mapper.server.ServerCommandInfoMapper;
import cc.endmc.server.service.server.IServerCommandInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 指令管理Service业务层处理
 *
 * @author ruoyi
 * @date 2024-04-16
 */
@Service
public class ServerCommandInfoServiceImpl implements IServerCommandInfoService {
    Logger logger = Logger.getLogger(ServerCommandInfoServiceImpl.class.getName());
    @Autowired
    private ServerCommandInfoMapper serverCommandInfoMapper;

    /**
     * 查询指令管理
     *
     * @param id 指令管理主键
     * @return 指令管理
     */
    @Override
    public ServerCommandInfo selectServerCommandInfoById(Long id) {
        return serverCommandInfoMapper.selectServerCommandInfoById(id);
    }

    /**
     * 查询指令管理列表
     *
     * @param serverCommandInfo 指令管理
     * @return 指令管理
     */
    @Override
    public List<ServerCommandInfo> selectServerCommandInfoList(ServerCommandInfo serverCommandInfo) {
        return serverCommandInfoMapper.selectServerCommandInfoList(serverCommandInfo);
    }

    /**
     * 新增指令管理
     *
     * @param serverCommandInfo 指令管理
     * @return 结果
     */
    @Override
    public int insertServerCommandInfo(ServerCommandInfo serverCommandInfo) {
        serverCommandInfo.setCreateTime(DateUtils.getNowDate());
        int result = 0;
        // 判断是否为多个服务器
        if (serverCommandInfo.getServerId().contains(",")) {
            int index = 0;
            String[] serverIds = serverCommandInfo.getServerId().split(",");
            for (String serverId : serverIds) {
                serverCommandInfo.setServerId(String.valueOf(Long.parseLong(serverId)));
                index = serverCommandInfoMapper.insertServerCommandInfo(serverCommandInfo);
                if (index == 0) {
                    return 0;
                }

                if (index == 1) {
                    index++;
                }
            }
            result = index;
        } else {
            result = serverCommandInfoMapper.insertServerCommandInfo(serverCommandInfo);
        }
        if (result != 0) {
            // 更新缓存
            initServerCommandInfo();
            RconService.COMMAND_INFO = ObjectCache.getCommandInfo();
        }
        return result;
    }

    /**
     * 修改指令管理
     *
     * @param serverCommandInfo 指令管理
     * @return 结果
     */
    @Override
    public int updateServerCommandInfo(ServerCommandInfo serverCommandInfo) {
        serverCommandInfo.setUpdateTime(DateUtils.getNowDate());
        final int i = serverCommandInfoMapper.updateServerCommandInfo(serverCommandInfo);
        if (i == 1) {
            // 更新缓存
            initServerCommandInfo();
            RconService.COMMAND_INFO = ObjectCache.getCommandInfo();
        }
        return i;
    }

    /**
     * 批量删除指令管理
     *
     * @param ids 需要删除的指令管理主键
     * @return 结果
     */
    @Override
    public int deleteServerCommandInfoByIds(Long[] ids) {
        return serverCommandInfoMapper.deleteServerCommandInfoByIds(ids);
    }

    /**
     * 删除指令管理信息
     *
     * @param id 指令管理主键
     * @return 结果
     */
    @Override
    public int deleteServerCommandInfoById(Long id) {
        return serverCommandInfoMapper.deleteServerCommandInfoById(id);
    }

    /**
     * 初始化缓存服务器指令
     */
    @Override
    public void initServerCommandInfo() {
        List<ServerCommandInfo> list = serverCommandInfoMapper.selectServerCommandInfoList(new ServerCommandInfo());
        final Map<String, ServerCommandInfo> map = new HashMap<>();
        for (ServerCommandInfo commandInfo : list) {
            map.put(commandInfo.getServerId(), commandInfo);
        }
        ObjectCache.put("serverCommandInfo", map);
        logger.warning("初始化缓存服务器指令：" + map.size() + " 条");
        // System.err.println("初始化缓存服务器指令：" + ObjectCache.getCommandInfo());
    }

}
