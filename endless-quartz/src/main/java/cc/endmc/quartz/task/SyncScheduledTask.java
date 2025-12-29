package cc.endmc.quartz.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 同步定时任务
 * 定期执行RCON服务器关联实例的OP和封禁名单同步
 *
 * @author Memory
 * @date 2025-12-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduledTask {

    private final SyncTask syncTask;

    /**
     * 每30分钟执行一次同步任务
     * 同步所有RCON服务器关联实例的OP和封禁名单
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 60000) // 30分钟执行一次，初始延迟1分钟
    public void syncAllServerData() {
        log.info("开始执行定时同步任务");
        try {
            syncTask.syncAllServerData();
            log.info("定时同步任务执行完成");
        } catch (Exception e) {
            log.error("定时同步任务执行失败: {}", e.getMessage(), e);
        }
    }
}