package cc.endmc.server.cache;

import cc.endmc.server.common.rconclient.RconClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/*
 * RCON客户端缓存
 * 作者：Memory
 */
public class RconCache {

    private static final Logger LOGGER = Logger.getLogger(RconCache.class.getName());

    // private static final Map<String, RconClient> map = new HashMap<>();
    private static final ConcurrentHashMap<String, RconClient> map = new ConcurrentHashMap<>();

    public static void put(String key, RconClient value) {
        map.put(key, value);
    }

    public static RconClient get(String key) {
        return map.get(key);
    }

    public static void remove(String key) {
        if (key == null) {
            return;
        }
        evict(key);
    }

    public static void close(String key) {
        remove(key);
    }

    public static void closeAll() {
        closeAndClearAll();
    }

    public static void clear() {
        closeAndClearAll();
    }

    public static void clearAll() {
        closeAndClearAll();
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
        return Collections.unmodifiableMap(map);
    }

    private static void closeAndClearAll() {
        if (map.isEmpty()) {
            return;
        }

        Map<String, RconClient> snapshot = new HashMap<>(map);
        map.clear();

        for (Map.Entry<String, RconClient> entry : snapshot.entrySet()) {
            closeClient(entry.getKey(), entry.getValue());
        }
    }

    private static void evict(String key) {
        RconClient client = map.remove(key);
        closeClient(key, client);
    }

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
