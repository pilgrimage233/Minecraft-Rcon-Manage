package cc.endmc.node.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 输入验证工具类
 * 提供路径验证、URL 验证等安全验证功能
 *
 * @author Memory
 */
@Slf4j
public class InputValidator {

    // 路径遍历攻击模式
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "(\\.\\.[\\\\/])|(\\.\\.$)|(^[\\\\/])|(%2e%2e)|(%252e%252e)|(%c0%ae%c0%ae)|(%c1%9c)|(%c1%af)"
    );

    // 危险文件扩展名
    private static final Pattern DANGEROUS_FILE_PATTERN = Pattern.compile(
            "\\.(exe|bat|cmd|sh|ps1|vbs|js|jar|war|ear|class|dll|so|dylib)$",
            Pattern.CASE_INSENSITIVE
    );

    // URL 协议白名单
    private static final Pattern ALLOWED_PROTOCOL_PATTERN = Pattern.compile(
            "^(https?|ftp)://.*$"
    );

    // 私有 IP 地址模式（防止 SSRF）
    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
            "^(127\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.|0\\.|localhost|\\[::1\\])"
    );

    /**
     * 验证文件路径是否安全
     * 防止路径遍历攻击
     *
     * @param path 文件路径
     * @return 是否安全
     */
    public static boolean isSafePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // 检查路径遍历模式
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            log.warn("检测到路径遍历攻击: {}", path);
            return false;
        }

        // 规范化路径并检查
        try {
            Path normalizedPath = Paths.get(path).normalize();
            String normalized = normalizedPath.toString();

            // 检查规范化后的路径是否仍然包含 ..
            if (normalized.contains("..")) {
                log.warn("规范化后仍包含路径遍历: {}", path);
                return false;
            }
        } catch (Exception e) {
            log.warn("路径验证失败: {}", path, e);
            return false;
        }

        return true;
    }

    /**
     * 验证并规范化文件路径
     *
     * @param path 原始路径
     * @return 规范化后的安全路径，如果路径不安全返回 null
     */
    public static String sanitizePath(String path) {
        if (!isSafePath(path)) {
            return null;
        }

        try {
            Path normalizedPath = Paths.get(path).normalize();
            return normalizedPath.toString();
        } catch (Exception e) {
            log.warn("路径规范化失败: {}", path, e);
            return null;
        }
    }

    /**
     * 验证文件名是否安全
     *
     * @param filename 文件名
     * @return 是否安全
     */
    public static boolean isSafeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // 检查是否包含路径分隔符
        if (filename.contains("/") || filename.contains("\\")) {
            log.warn("文件名包含路径分隔符: {}", filename);
            return false;
        }

        // 检查是否包含危险字符
        if (filename.contains("..") || filename.contains("~")) {
            log.warn("文件名包含危险字符: {}", filename);
            return false;
        }

        // 检查危险文件扩展名
        if (DANGEROUS_FILE_PATTERN.matcher(filename).find()) {
            log.warn("文件名包含危险扩展名: {}", filename);
            return false;
        }

        return true;
    }

    /**
     * 验证 URL 是否安全
     * 防止 SSRF 攻击
     *
     * @param url URL 地址
     * @return 是否安全
     */
    public static boolean isSafeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // 检查协议
        if (!ALLOWED_PROTOCOL_PATTERN.matcher(url).matches()) {
            log.warn("URL 协议不允许: {}", url);
            return false;
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null) {
                log.warn("URL 无主机名: {}", url);
                return false;
            }

            // 检查是否是私有 IP（防止 SSRF）
            if (PRIVATE_IP_PATTERN.matcher(host).matches()) {
                log.warn("URL 指向私有 IP: {}", url);
                return false;
            }

            // 检查端口范围
            int port = uri.getPort();
            if (port > 0 && (port < 1 || port > 65535)) {
                log.warn("URL 端口无效: {}", url);
                return false;
            }

            return true;
        } catch (URISyntaxException e) {
            log.warn("URL 格式无效: {}", url, e);
            return false;
        }
    }

    /**
     * 验证节点 ID 是否有效
     *
     * @param nodeId 节点 ID
     * @return 是否有效
     */
    public static boolean isValidNodeId(Long nodeId) {
        return nodeId != null && nodeId > 0;
    }

    /**
     * 验证服务器 ID 是否有效
     *
     * @param serverId 服务器 ID
     * @return 是否有效
     */
    public static boolean isValidServerId(Integer serverId) {
        return serverId != null && serverId > 0;
    }

    /**
     * 验证端口号是否有效
     *
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(Integer port) {
        return port != null && port > 0 && port <= 65535;
    }

    /**
     * 验证 IP 地址是否有效
     *
     * @param ip IP 地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // IPv4 验证
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (ip.matches(ipv4Pattern)) {
            return true;
        }

        // IPv6 验证（简化版）
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        return ip.matches(ipv6Pattern) || ip.equals("::1") || ip.equals("localhost");
    }

    /**
     * 清理用户输入，防止 XSS
     *
     * @param input 用户输入
     * @return 清理后的输入
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // 移除潜在的 XSS 字符
        return input.replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("'", "&#x27;")
                    .replaceAll("/", "&#x2F;");
    }

    /**
     * 验证字符串长度是否在限制内
     *
     * @param str     字符串
     * @param maxLength 最大长度
     * @return 是否有效
     */
    public static boolean isValidLength(String str, int maxLength) {
        return str != null && str.length() <= maxLength;
    }

    /**
     * 验证 Token 格式是否有效
     *
     * @param token Token 字符串
     * @return 是否有效
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Token 应该是字母数字组合，长度在 16-128 之间
        return token.matches("^[a-zA-Z0-9_-]{16,128}$");
    }
}
