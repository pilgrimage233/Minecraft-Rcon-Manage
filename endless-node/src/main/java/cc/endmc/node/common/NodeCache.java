package cc.endmc.node.common;

import cc.endmc.node.domain.NodeServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点服务器缓存
 * 用于存储节点服务器的实例
 * 使用ConcurrentHashMap实现线程安全的缓存
 * 作者：Memory
 */
public class NodeCache {

    private static final ConcurrentHashMap<Long, NodeServer> map = new ConcurrentHashMap<>();

    public static void put(Long key, NodeServer value) {
        map.put(key, value);
    }

    public static NodeServer get(Long key) {
        return map.get(key);
    }

    public static void remove(Long key) {
        map.remove(key);
    }

    public static void clear() {
        map.clear();
    }

    public static boolean containsKey(Long key) {
        return map.containsKey(key);
    }

    public static boolean containsValue(NodeServer value) {
        return map.containsValue(value);
    }

    public static int size() {
        return map.size();
    }

    public static boolean isEmpty() {
        return map.isEmpty();
    }

    public static Map<Long, NodeServer> getMap() {
        return map;
    }

}
