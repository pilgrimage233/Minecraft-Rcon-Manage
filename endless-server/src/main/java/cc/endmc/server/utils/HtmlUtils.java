package cc.endmc.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {
    private static final Logger log = LoggerFactory.getLogger(HtmlUtils.class);

    /**
     * 从HTML内容中提取标题
     */
    public static String extractTitle(String htmlContent) {
        try {
            // 使用正则表达式提取<title>标签内容
            Pattern pattern = Pattern.compile("<title>(.*?)</title>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("提取标题失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从HTML内容中提取meta描述
     */
    public static String extractMetaDescription(String htmlContent) {
        try {
            // 使用正则表达式提取<meta name="description" content="...">标签内容
            Pattern pattern = Pattern.compile(
                    "<meta\\s+name=[\"']description[\"']\\s+content=[\"'](.*?)[\"']",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("提取描述失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从HTML内容中提取meta关键词
     */
    public static String extractMetaKeywords(String htmlContent) {
        try {
            // 使用正则表达式提取<meta name="keywords" content="...">标签内容
            Pattern pattern = Pattern.compile(
                    "<meta\\s+name=[\"']keywords[\"']\\s+content=[\"'](.*?)[\"']",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("提取关键词失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从HTML内容中提取字符集
     */
    public static String extractCharset(String htmlContent, cn.hutool.http.HttpResponse httpResponse) {
        try {
            // 首先尝试从Content-Type头中获取
            String contentType = httpResponse.header("Content-Type");
            if (contentType != null && contentType.contains("charset=")) {
                return contentType.substring(contentType.indexOf("charset=") + 8).trim();
            }

            // 然后尝试从meta标签中获取
            Pattern pattern = Pattern.compile(
                    "<meta\\s+http-equiv=[\"']Content-Type[\"']\\s+content=[\"'].*?charset=(.*?)[\"']",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("提取字符集失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从HTML内容中提取网站图标URL
     */
    public static String extractFavicon(String htmlContent, URL baseUrl) {
        try {
            // 尝试从link标签中获取
            Pattern pattern = Pattern.compile(
                    "<link\\s+rel=[\"'](?:shortcut\\s+)?icon[\"']\\s+href=[\"'](.*?)[\"']",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                String faviconUrl = matcher.group(1).trim();
                // 如果是相对路径，转换为绝对路径
                if (faviconUrl.startsWith("/")) {
                    return new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), faviconUrl).toString();
                } else if (!faviconUrl.startsWith("http")) {
                    return new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(),
                            baseUrl.getPath().substring(0, baseUrl.getPath().lastIndexOf('/') + 1) + faviconUrl).toString();
                }
                return faviconUrl;
            }

            // 如果没有找到，返回默认的favicon.ico路径
            return new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), "/favicon.ico").toString();
        } catch (Exception e) {
            log.warn("提取图标失败: {}", e.getMessage());
        }
        return null;
    }
}
