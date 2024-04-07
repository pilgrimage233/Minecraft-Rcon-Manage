package com.ruoyi.quartz.task;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.server.domain.WhitelistInfo;
import com.ruoyi.server.mapper.WhitelistInfoMapper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * ClassName: OnlineTask <br>
 * Description:
 * date: 2024/4/7 下午7:51 <br>
 *
 * @author Administrator <br>
 * @since JDK 1.8
 */
@Component("onlineTask")
public class OnlineTask {

    private static final Log log = LogFactory.getLog(OnlineTask.class);
    @Autowired
    private WhitelistInfoMapper whitelistInfoMapper;

    /**
     * 根据用户uuid同步用户名称
     * Api：<a href="https://sessionserver.mojang.com/session/minecraft/profile/">...</a>{uuid}
     */
    public void syncUserNameForUuid() {
        log.debug("syncUserNameForUuid start");
        ArrayList<String> list = new ArrayList<>();
        // 查询所有正版用户
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setOnlineFlag(1L);
        whitelistInfoMapper.selectWhitelistInfoList(whitelistInfo).forEach(whitelist -> {
            // 查询用户名称
            try {
                String json = HttpUtils.sendGet("https://sessionserver.mojang.com/session/minecraft/profile/" + whitelist.getUserUuid().replace("-", ""));
                if (StringUtils.isNotEmpty(json)) {
                    // json实例化
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    String name = jsonObject.getString("name");
                    if (name.equalsIgnoreCase(whitelist.getUserName())) {
                        return;
                    }
                    whitelist.setUserName(jsonObject.getString("name"));
                    list.add(whitelist.getUserName());
                    whitelistInfoMapper.updateWhitelistInfo(whitelist);
                }
            } catch (Exception e) {
                log.error("syncUserNameForUuid error", e);
            }
        });
        log.debug("syncUserNameForUuid list: " + list);
        log.debug("syncUserNameForUuid end");
    }
}
