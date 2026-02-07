package cc.endmc.server.task;

import cc.endmc.server.service.open.impl.v1.OpenApiServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 服务器状态缓存刷新任务
 * 定时刷新服务器状态缓存，确保数据的实时性和准确性
 *
 * @author Memory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerStatusCacheTask {

    private final OpenApiServiceImpl openApiService;

    /**
     * 定时刷新服务器状态缓存
     * 每分钟执行一次，初始延迟15秒
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    public void refreshServerStatusCache() {
        try {
            openApiService.refreshServerStatusCache();
        } catch (Exception e) {
            log.error("Server status cache refresh failed: {}", e.getMessage(), e);
        }
    }
}
