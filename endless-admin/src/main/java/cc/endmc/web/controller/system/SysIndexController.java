package cc.endmc.web.controller.system;

import cc.endmc.common.config.EndlessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * 首页
 *
 * @author ruoyi
 */
@Controller
public class SysIndexController {
    /**
     * 系统基础配置
     */
    @Autowired
    private EndlessConfig ruoyiConfig;

    /**
     * 访问首页 -> 重定向到静态页面
     */
    @RequestMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    /**
     * 提供系统信息给前端静态页填充
     */
    @GetMapping("/system/info")
    @ResponseBody
    public Map<String, Object> systemInfo() {
        Map<String, Object> map = new java.util.HashMap<>(8);
        map.put("status", "OJBK");
        // 基础信息
        map.put("name", ruoyiConfig.getName());
        map.put("version", ruoyiConfig.getVersion());

        // JVM 内存信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        Map<String, Object> memory = new java.util.HashMap<>(4);
        memory.put("used", formatBytes(usedMemory));
        memory.put("total", formatBytes(totalMemory));
        memory.put("max", formatBytes(maxMemory));
        memory.put("usagePercent", Math.round((double) usedMemory / totalMemory * 100));
        map.put("memory", memory);

        // 系统运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        map.put("uptime", formatUptime(uptime));
        map.put("uptimeMs", uptime);

        // CPU 核心数
        map.put("cpuCores", runtime.availableProcessors());

        // 操作系统信息
        map.put("osName", System.getProperty("os.name"));
        map.put("osVersion", System.getProperty("os.version"));
        map.put("osArch", System.getProperty("os.arch"));

        // Java 版本
        map.put("javaVersion", System.getProperty("java.version"));

        // 当前时间
        map.put("serverTime", new java.util.Date());

        return map;
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, secs);
        } else {
            return String.format("%d秒", secs);
        }
    }
}
