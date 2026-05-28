package cc.endmc.node.common;

import cc.endmc.node.domain.NodeServer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 节点服务器缓存
 * 使用 Caffeine 实现高性能缓存
 *
 * @author Memory
 */
@Slf4j
public class NodeCache {

    // 主缓存 - 使用 Caffeine
    private static final Cache<Long, NodeServer> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES) // 30 分钟过期
            .maximumSize(1000) // 最大 1000 个条目
            .recordStats() // 记录统计信息
            .build();

    // 备用缓存 - 用于持久化存储
    private static final Map<Long, NodeServer> backupMap = new ConcurrentHashMap<>();

    /**
     * 放入缓存
     */
    public static void put(Long key, NodeServer value) {
        if (key == null || value == null) {
            return;
        }
        cache.put(key, value);
        backupMap.put(key, value);
        log.debug("缓存已更新: nodeId={}", key);
    }

    /**
     * 获取缓存
     */
    public static NodeServer get(Long key) {
        if (key == null) {
            return null;
        }

        // 先从 Caffeine 缓存获取
        NodeServer value = cache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 如果 Caffeine 缓存中没有，从备用缓存获取
        value = backupMap.get(key);
        if (value != null) {
            // 重新放入 Caffeine 缓存
            cache.put(key, value);
            log.debug("从备用缓存恢复: nodeId={}", key);
        }

        return value;
    }

    /**
     * 移除缓存
     */
    public static void remove(Long key) {
        if (key == null) {
            return;
        }
        cache.invalidate(key);
        backupMap.remove(key);
        log.debug("缓存已移除: nodeId={}", key);
    }

    /**
     * 清空缓存
     */
    public static void clear() {
        cache.invalidateAll();
        backupMap.clear();
        log.info("缓存已清空");
    }

    /**
     * 检查是否包含键
     */
    public static boolean containsKey(Long key) {
        if (key == null) {
            return false;
        }
        return cache.getIfPresent(key) != null || backupMap.containsKey(key);
    }

    /**
     * 检查是否包含值
     */
    public static boolean containsValue(NodeServer value) {
        if (value == null) {
            return false;
        }
        return backupMap.containsValue(value);
    }

    /**
     * 获取缓存大小
     */
    public static int size() {
        return (int) cache.estimatedSize();
    }

    /**
     * 检查缓存是否为空
     */
    public static boolean isEmpty() {
        return cache.estimatedSize() == 0 && backupMap.isEmpty();
    }

    /**
     * 获取备用缓存 Map（用于兼容）
     */
    public static Map<Long, NodeServer> getMap() {
        return backupMap;
    }

    /**
     * 获取节点信息（带 DB 回退）
     * 优先从缓存获取，缓存未命中时调用 dbLookup 查询并缓存结果
     *
     * @param id       节点 ID
     * @param dbLookup DB 查询函数，如 mapper::selectNodeServerById
     * @return 节点信息，不存在时返回 null
     */
    public static NodeServer getOrLoad(Long id, Function<Long, NodeServer> dbLookup) {
        if (id == null) return null;
        NodeServer node = get(id);
        if (node != null) return node;
        node = dbLookup.apply(id);
        if (node != null) {
            put(id, node);
        }
        return node;
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStats getStats() {
        return cache.stats();
    }

    /**
     * 打印缓存统计信息
     */
    public static void printStats() {
        CacheStats stats = cache.stats();
        log.info("缓存统计 - 命中率: {:.2f}%, 命中次数: {}, 未命中次数: {}, 驱逐次数: {}, 大小: {}",
                stats.hitRate() * 100,
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount(),
                cache.estimatedSize());
    }

    /**
     * 刷新缓存（从数据库重新加载）
     */
    public static void refresh(Map<Long, NodeServer> data) {
        cache.invalidateAll();
        backupMap.clear();
        if (data != null) {
            backupMap.putAll(data);
            // 批量放入 Caffeine 缓存
            cache.putAll(data);
        }
        log.info("缓存已刷新，加载 {} 条数据", data != null ? data.size() : 0);
    }
}
