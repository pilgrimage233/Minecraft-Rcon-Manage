package cc.endmc.server.cache;

import cc.endmc.server.domain.server.ServerCommandInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: ObjectCache <br>
 * Description:
 * date: 2024/4/16 下午8:48 <br>
 *
 * @author Administrator <br>
 * @since JDK 1.8
 */
public class ObjectCache {

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    public static void put(String key, Object value) {
        CACHE.put(key, value);
    }

    public static Object get(String key) {
        return CACHE.get(key);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static boolean containsKey(String key) {
        return CACHE.containsKey(key);
    }

    public static boolean containsValue(Object value) {
        return CACHE.containsValue(value);
    }

    public static int size() {
        return CACHE.size();
    }

    public static boolean isEmpty() {
        return CACHE.isEmpty();
    }

    public static Map<String, Object> getMap() {
        return CACHE;
    }

    public static void putAll(Map<String, Object> map) {
        CACHE.putAll(map);
    }

    public static void removeValue(Object value) {
        CACHE.values().remove(value);
    }

    public static void removeKey(String key) {
        CACHE.remove(key);
    }

    public static void removeAll() {
        CACHE.clear();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ServerCommandInfo> getCommandInfo() {
        Object value = ObjectCache.get("serverCommandInfo");
        if (value instanceof Map) {
            return (Map<String, ServerCommandInfo>) value;
        }
        return null;
    }
}
