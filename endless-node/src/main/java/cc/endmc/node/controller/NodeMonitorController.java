package cc.endmc.node.controller;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.ws.NodeConnectionPool;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查和监控指标控制器
 * 提供系统健康状态和性能指标
 *
 * @author Memory
 */
@Slf4j
@RestController
@RequestMapping("/node/monitor")
@RequiredArgsConstructor
public class NodeMonitorController {

    private final NodeConnectionPool connectionPool;

    @Value("${endless.version}")
    private String version;

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public AjaxResult health() {
        Map<String, Object> health = new HashMap<>();

        try {
            // 基本状态
            health.put("status", "UP");
            health.put("version", version);
            health.put("timestamp", System.currentTimeMillis());

            // 运行时信息
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            health.put("uptime", runtime.getUptime());
            health.put("startTime", runtime.getStartTime());

            // 内存信息
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("heapUsed", memory.getHeapMemoryUsage().getUsed());
            memoryInfo.put("heapMax", memory.getHeapMemoryUsage().getMax());
            memoryInfo.put("nonHeapUsed", memory.getNonHeapMemoryUsage().getUsed());
            health.put("memory", memoryInfo);

            // 线程信息
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("threadCount", threads.getThreadCount());
            threadInfo.put("daemonThreadCount", threads.getDaemonThreadCount());
            threadInfo.put("peakThreadCount", threads.getPeakThreadCount());
            health.put("threads", threadInfo);

            return AjaxResult.success(health);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return AjaxResult.error("健康检查失败");
        }
    }

    /**
     * 获取连接池指标
     */
    @GetMapping("/pool")
    public AjaxResult poolMetrics() {
        try {
            Map<String, Object> metrics = connectionPool.getPoolMetrics();
            return AjaxResult.success(metrics);
        } catch (Exception e) {
            log.error("获取连接池指标失败", e);
            return AjaxResult.error("获取连接池指标失败");
        }
    }

    /**
     * 获取缓存指标
     */
    @GetMapping("/cache")
    public AjaxResult cacheMetrics() {
        try {
            CacheStats stats = NodeCache.getStats();

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("size", NodeCache.size());
            metrics.put("hitCount", stats.hitCount());
            metrics.put("missCount", stats.missCount());
            metrics.put("hitRate", stats.hitRate());
            metrics.put("evictionCount", stats.evictionCount());
            metrics.put("loadCount", stats.loadCount());
            metrics.put("averageLoadPenalty", stats.averageLoadPenalty());

            return AjaxResult.success(metrics);
        } catch (Exception e) {
            log.error("获取缓存指标失败", e);
            return AjaxResult.error("获取缓存指标失败");
        }
    }

    /**
     * 获取系统指标
     */
    @GetMapping("/system")
    public AjaxResult systemMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();

            // 运行时信息
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            metrics.put("jvmName", runtime.getVmName());
            metrics.put("jvmVersion", runtime.getVmVersion());
            metrics.put("uptime", runtime.getUptime());

            // 内存信息
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("heapUsed", memory.getHeapMemoryUsage().getUsed());
            memoryInfo.put("heapMax", memory.getHeapMemoryUsage().getMax());
            memoryInfo.put("heapCommitted", memory.getHeapMemoryUsage().getCommitted());
            memoryInfo.put("nonHeapUsed", memory.getNonHeapMemoryUsage().getUsed());
            metrics.put("memory", memoryInfo);

            // 线程信息
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("threadCount", threads.getThreadCount());
            threadInfo.put("daemonThreadCount", threads.getDaemonThreadCount());
            threadInfo.put("peakThreadCount", threads.getPeakThreadCount());
            threadInfo.put("totalStartedThreadCount", threads.getTotalStartedThreadCount());
            metrics.put("threads", threadInfo);

            // 操作系统信息
            com.sun.management.OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);
            Map<String, Object> osInfo = new HashMap<>();
            osInfo.put("name", os.getName());
            osInfo.put("version", os.getVersion());
            osInfo.put("arch", os.getArch());
            osInfo.put("availableProcessors", os.getAvailableProcessors());
            osInfo.put("systemLoadAverage", os.getSystemLoadAverage());
            osInfo.put("processCpuLoad", os.getProcessCpuLoad());
            osInfo.put("totalPhysicalMemorySize", os.getTotalPhysicalMemorySize());
            osInfo.put("freePhysicalMemorySize", os.getFreePhysicalMemorySize());
            metrics.put("os", osInfo);

            // 节点缓存信息
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("cachedNodeCount", NodeCache.size());
            nodeInfo.put("isEmpty", NodeCache.isEmpty());
            metrics.put("nodeCache", nodeInfo);

            return AjaxResult.success(metrics);
        } catch (Exception e) {
            log.error("获取系统指标失败", e);
            return AjaxResult.error("获取系统指标失败");
        }
    }

    /**
     * 获取所有指标
     */
    @GetMapping("/all")
    public AjaxResult allMetrics() {
        try {
            Map<String, Object> allMetrics = new HashMap<>();

            // 健康状态
            allMetrics.put("health", getHealthStatus());

            // 连接池指标
            allMetrics.put("connectionPool", connectionPool.getPoolMetrics());

            // 缓存指标
            CacheStats stats = NodeCache.getStats();
            Map<String, Object> cacheMetrics = new HashMap<>();
            cacheMetrics.put("size", NodeCache.size());
            cacheMetrics.put("hitRate", stats.hitRate());
            cacheMetrics.put("evictionCount", stats.evictionCount());
            allMetrics.put("cache", cacheMetrics);

            // 系统指标
            allMetrics.put("system", getSystemMetrics());

            return AjaxResult.success(allMetrics);
        } catch (Exception e) {
            log.error("获取所有指标失败", e);
            return AjaxResult.error("获取所有指标失败");
        }
    }

    private Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("version", version);
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        metrics.put("uptime", runtime.getUptime());

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        metrics.put("heapUsed", memory.getHeapMemoryUsage().getUsed());
        metrics.put("heapMax", memory.getHeapMemoryUsage().getMax());

        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        metrics.put("threadCount", threads.getThreadCount());

        return metrics;
    }
}
