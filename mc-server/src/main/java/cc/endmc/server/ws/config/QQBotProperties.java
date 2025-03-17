package cc.endmc.server.ws.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "qq.bot")
public class QQBotProperties {
    private boolean enable;
    private String token;
    private WebSocket ws;
    private Http http;
    private String groupId;
    private String manager;

    /**
     * 获取群号列表
     * 将配置中的群号字符串转换为Long类型的List
     *
     * @return 群号List
     */
    public List<Long> getGroupIds() {
        if (groupId == null || groupId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(groupId.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * 获取管理员QQ号列表
     * 将配置中的管理员QQ号字符串转换为Long类型的List
     *
     * @return 管理员QQ号List
     */
    public List<Long> getManagers() {
        if (manager == null || manager.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(manager.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Data
    public static class WebSocket {
        private String url;
    }

    @Data
    public static class Http {
        private String url;
    }
} 