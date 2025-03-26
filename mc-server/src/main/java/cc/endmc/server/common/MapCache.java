package cc.endmc.server.common;


import cc.endmc.server.common.rconclient.RconClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Map缓存
 * 作者：Memory
 */
public class MapCache {
    private static final Map<String, RconClient> map = new HashMap<>();
    // private static final ConcurrentHashMap<String, RconClient> map = new ConcurrentHashMap<>();

    public static void put(String key, RconClient value) {
        map.put(key, value);
    }

    public static RconClient get(String key) {
        return map.get(key);
    }

    public static void remove(String key) {
        map.remove(key);
    }

    public static void clear() {
        map.clear();
    }

    public static boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public static boolean containsValue(RconClient value) {
        return map.containsValue(value);
    }

    public static int size() {
        return map.size();
    }

    public static boolean isEmpty() {
        return map.isEmpty();
    }

    public static Map<String, RconClient> getMap() {
        return map;
    }

}
