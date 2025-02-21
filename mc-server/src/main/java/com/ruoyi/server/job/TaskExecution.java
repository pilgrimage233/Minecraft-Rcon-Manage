package com.ruoyi.server.job;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.service.RconService;
import com.ruoyi.server.domain.other.RegularCmd;
import com.ruoyi.server.mapper.other.RegularCmdMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskExecution {

    @Autowired
    private RconService rconService;

    @Autowired
    private RegularCmdMapper regularCmdMapper;

    public void execute(RegularCmd regular) {
        log.info("TaskExecution开始执行任务: {}", regular.getTaskId());

        if (regular == null) {
            log.error("定时任务为空");
            return;
        }

        // 被执行服务器
        String executeServer = regular.getExecuteServer();
        if (StringUtils.isEmpty(executeServer)) {
            log.error("执行服务器为空");
            return;
        }

        log.info("执行定时任务：server=[{}], cmd=[{}]", executeServer, regular.getCmd());

        try {
            final String result = rconService.sendCommand(executeServer, regular.getCmd(), false);
            log.info("服务器 [{}] 执行结果：{}", executeServer, result);

            regular.setResult(result);
            // 历史结果
            JSONObject json = new JSONObject();
            if (regular.getHistoryResult() != null) {
                json = JSONObject.parseObject(regular.getHistoryResult());
                json.put(DateUtils.getTime(), result);
                if (json.size() > regular.getHistoryCount()) {
                    json.remove(json.keySet().iterator().next());
                }
            } else {
                json.put(DateUtils.getTime(), result);
            }
            regular.setHistoryResult(json.toJSONString());
            regular.setExecuteCount(regular.getExecuteCount() + 1);
            regular.setUpdateTime(DateUtils.getNowDate());

            // 直接使用 Mapper 更新结果和执行次数，避免触发调度更新
            regularCmdMapper.updateRegularCmd(regular);

        } catch (Exception e) {
            log.error("执行命令失败: server=[{}], cmd=[{}], error={}",
                    executeServer, regular.getCmd(), e.getMessage(), e);
        }

        log.info("TaskExecution任务执行完成: {}", regular.getTaskId());
    }
}