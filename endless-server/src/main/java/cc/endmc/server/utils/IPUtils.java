package cc.endmc.server.utils;

import cc.endmc.common.utils.ip.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class IPUtils {

    /**
     * 将域名转换为IP地址
     *
     * @param input 域名或IP地址
     * @return IP地址
     */
    public static String domainToIp(String input) {
        if (isValidIpOrDomain(input)) {
            return input;
        }
        InetAddress address = null;
        try {
            address = InetAddress.getByName(input);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        log.info("Domain: {}", input);
        log.info("IP Address: {}", address.getHostAddress());
        return address.getHostAddress();
    }

    /**
     * 验证字符串是否是有效的IP地址或域名
     *
     * @param input 需要验证的字符串
     * @return 是否有效
     */
    public static boolean isValidIpOrDomain(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // IPv4地址正则表达式
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // IPv6地址正则表达式 (简化版)
        String ipv6Pattern = "^(([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:))|(([0-9a-fA-F]{1,4}:){6}(:[0-9a-fA-F]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-fA-F]{1,4}:){5}(((:[0-9a-fA-F]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-fA-F]{1,4}:){4}(((:[0-9a-fA-F]{1,4}){1,3})|((:[0-9a-fA-F]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){3}(((:[0-9a-fA-F]{1,4}){1,4})|((:[0-9a-fA-F]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){2}(((:[0-9a-fA-F]{1,4}){1,5})|((:[0-9a-fA-F]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){1}(((:[0-9a-fA-F]{1,4}){1,6})|((:[0-9a-fA-F]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9a-fA-F]{1,4}){1,7})|((:[0-9a-fA-F]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))$";

        // 域名正则表达式
        String domainPattern = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,63}$";

        // 使用正则表达式验证
        return input.matches(ipv4Pattern) || input.matches(ipv6Pattern) || input.matches(domainPattern);
    }

    /**
     * 获取客户端真实IP地址
     * 支持通过配置多个IP头获取真实IP
     */
    public static String getClientIpAddress(HttpServletRequest request, String ipHeaderName) {
        if (ipHeaderName.contains(",")) {
            for (String header : ipHeaderName.split(",")) {
                String ip = request.getHeader(header.trim());
                if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                    if (!IpUtils.internalIp(ip.split(",")[0].trim())) {
                        return ip.split(",")[0].trim();
                    }
                }
            }
            return request.getRemoteAddr();
        }
        return request.getRemoteAddr();
    }

}
