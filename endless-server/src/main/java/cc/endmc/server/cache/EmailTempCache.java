package cc.endmc.server.cache;

import cc.endmc.server.domain.email.CustomEmailTemplates;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: EmailTempCache <br>
 * Description: 邮件模板缓存 <br>
 * date: 2025/10/7 07:27 <br>
 *
 * @author Memory <br>
 */
public class EmailTempCache {
    private static final ConcurrentHashMap<String, CustomEmailTemplates> map = new ConcurrentHashMap<>();

    public static void put(String key, CustomEmailTemplates templates) {
        map.put(key, templates);
    }

    public static CustomEmailTemplates get(String key) {
        if (key == null) {
            return map.get("default");
        }
        return map.get(key) == null ? map.get("default") : map.get(key); // 指定为空返回默认

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

    public static boolean containsValue(CustomEmailTemplates value) {
        return map.containsValue(value);
    }

    public static int size() {
        return map.size();
    }

    public static boolean isEmpty() {
        return map.isEmpty();
    }

    public static Map<String, CustomEmailTemplates> getMap() {
        return map;
    }
}
