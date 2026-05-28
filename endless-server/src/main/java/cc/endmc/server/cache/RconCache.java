package cc.endmc.server.cache;

import cc.endmc.server.common.rconclient.RconClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * RCON客户端缓存
 * 支持连接健康检查和空闲过期机制
 *
 * @author Memory
 */
public class RconCache {

    private static final Logger LOGGER = Logger.getLogger(RconCache.class.getName());

    /**
     * 默认空闲超时时间（毫秒）：5分钟
     */
    private static final long DEFAULT_IDLE_TIMEOUT_MS = 5 * 60 * 1000;

    /**
     * 存储 RconClient 及其最后访问时间
     */
    private static final ConcurrentHashMap<String, CacheEntry> map = new ConcurrentHashMap<>();

    /**
     * 缓存条目，包含客户端和最后访问时间
     */
    private static class CacheEntry {
        final RconClient client;
        volatile long lastAccessTime;

        CacheEntry(RconClient client) {
            this.client = client;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    /**
     * 放入缓存
     */
    public static void put(String key, RconClient value) {
        if (key == null || value == null) {
            return;
        }
        map.put(key, new CacheEntry(value));
    }

    /**
     * 获取缓存的客户端，并更新最后访问时间
     */
    public static RconClient get(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry entry = map.get(key);
        if (entry == null) {
            return null;
        }
        entry.updateAccessTime();
        return entry.client;
    }

    /**
     * 移除并关闭指定连接
     */
    public static void remove(String key) {
        if (key == null) {
            return;
        }
        evict(key);
    }

    /**
     * 关闭指定连接
     * 别名方法，功能与 {@link #remove(String)} 相同
     */
    public static void close(String key) {
        remove(key);
    }

    /**
     * 关闭所有连接并清空缓存
     * 别名方法，功能与 {@link #clear()} 相同
     */
    public static void closeAll() {
        closeAndClearAll();
    }

    /**
     * 关闭所有连接并清空缓存
     * 别名方法，功能与 {@link #closeAll()} 相同
     */
    public static void clear() {
        closeAndClearAll();
    }

    /**
     * 关闭所有连接并清空缓存
     * 别名方法，功能与 {@link #closeAll()} 相同
     */
    public static void clearAll() {
        closeAndClearAll();
    }

    /**
     * 检查是否包含指定 key
     */
    public static boolean containsKey(String key) {
        return key != null && map.containsKey(key);
    }

    /**
     * 检查是否包含指定 value
     */
    public static boolean containsValue(RconClient value) {
        if (value == null) {
            return false;
        }
        return map.values().stream().anyMatch(entry -> entry.client == value);
    }

    /**
     * 获取缓存大小
     */
    public static int size() {
        return map.size();
    }

    /**
     * 检查缓存是否为空
     */
    public static boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 获取所有连接的不可修改视图
     */
    public static Map<String, RconClient> getMap() {
        Map<String, RconClient> result = new HashMap<>();
        map.forEach((key, entry) -> result.put(key, entry.client));
        return Collections.unmodifiableMap(result);
    }

    /**
     * 检查指定连接是否健康（Socket 通道打开且连接状态正常）
     *
     * @param key 缓存 key
     * @return 如果连接健康返回 true，否则返回 false
     */
    public static boolean isHealthy(String key) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = map.get(key);
        if (entry == null) {
            return false;
        }
        RconClient client = entry.client;
        return client != null && client.isSocketChannelOpen();
    }

    /**
     * 清理空闲超时的连接
     * 应定期调用此方法以清理不活跃的连接
     *
     * @param idleTimeoutMs 空闲超时时间（毫秒）
     * @return 清理的连接数量
     */
    public static int cleanupIdleConnections(long idleTimeoutMs) {
        long now = System.currentTimeMillis();
        int cleaned = 0;

        Iterator<Map.Entry<String, CacheEntry>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheEntry> entry = iterator.next();
            CacheEntry cacheEntry = entry.getValue();

            if (now - cacheEntry.lastAccessTime > idleTimeoutMs) {
                LOGGER.info("清理空闲连接: " + entry.getKey() + " (空闲时间: " + (now - cacheEntry.lastAccessTime) + "ms)");
                closeClient(entry.getKey(), cacheEntry.client);
                iterator.remove();
                cleaned++;
            }
        }

        return cleaned;
    }

    /**
     * 清理空闲超时的连接（使用默认超时时间）
     *
     * @return 清理的连接数量
     */
    public static int cleanupIdleConnections() {
        return cleanupIdleConnections(DEFAULT_IDLE_TIMEOUT_MS);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息 Map
     */
    public static Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEntries", map.size());

        int healthyCount = 0;
        int unhealthyCount = 0;
        long now = System.currentTimeMillis();
        long oldestAccess = now;

        for (Map.Entry<String, CacheEntry> entry : map.entrySet()) {
            CacheEntry cacheEntry = entry.getValue();
            if (cacheEntry.client != null && cacheEntry.client.isSocketChannelOpen()) {
                healthyCount++;
            } else {
                unhealthyCount++;
            }
            if (cacheEntry.lastAccessTime < oldestAccess) {
                oldestAccess = cacheEntry.lastAccessTime;
            }
        }

        stats.put("healthyConnections", healthyCount);
        stats.put("unhealthyConnections", unhealthyCount);
        stats.put("oldestAccessAge", map.isEmpty() ? 0 : now - oldestAccess);

        return stats;
    }

    /**
     * 关闭并清空所有连接
     * 使用原子操作避免竞态条件
     */
    private static void closeAndClearAll() {
        if (map.isEmpty()) {
            return;
        }

        // 先获取所有条目的快照
        List<Map.Entry<String, CacheEntry>> snapshot = new ArrayList<>(map.entrySet());

        // 原子性地清空 map
        map.clear();

        // 关闭所有客户端
        for (Map.Entry<String, CacheEntry> entry : snapshot) {
            closeClient(entry.getKey(), entry.getValue().client);
        }
    }

    /**
     * 移除并关闭指定连接
     */
    private static void evict(String key) {
        CacheEntry entry = map.remove(key);
        if (entry != null) {
            closeClient(key, entry.client);
        }
    }

    /**
     * 关闭客户端连接
     */
    private static void closeClient(String key, RconClient client) {
        if (client == null) {
            return;
        }

        try {
            client.close();
        } catch (Exception e) {
            if (key == null) {
                LOGGER.warning("Failed to close RconClient: " + e.getMessage());
            } else {
                LOGGER.warning("Failed to close RconClient[" + key + "]: " + e.getMessage());
            }
        }
    }
}
