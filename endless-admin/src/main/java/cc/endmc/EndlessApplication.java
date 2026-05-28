package cc.endmc;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.config.InitConfigService;
import cc.endmc.node.common.NodeCache;
import cc.endmc.server.cache.EmailTempCache;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.ws.BotManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@EnableScheduling
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }, scanBasePackages = "cc.endmc")
public class EndlessApplication {
    private static final Logger log = LoggerFactory.getLogger(EndlessApplication.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // 初始化配置文件
        try {
            InitConfigService initConfigService = new InitConfigService();
            initConfigService.initializeConfigs();
        } catch (Exception e) {
            System.err.println("配置文件初始化失败，请检查应用程序权限！");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // 启动应用
        ConfigurableApplicationContext context = SpringApplication.run(EndlessApplication.class, args);

        // 获取版本信息
        String version = context.getEnvironment().getProperty("endless.version", "Unknown");
        String serverPort = context.getEnvironment().getProperty("server.port", "8080");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path", "");
        if (contextPath.isEmpty() || "/".equals(contextPath)) {
            contextPath = "";
        }
        String setupUrl = String.format("http://localhost:%s%s/setup.html", serverPort, contextPath);
        log.info("🔧 配置向导地址: {} (仅本机访问，可设置 setup.allow-remote=true)", setupUrl);

        // 打印启动横幅和初始化信息汇总
        long elapsed = System.currentTimeMillis() - startTime;
        printStartupBanner(version);
        printStartupSummary(context, version, elapsed);
    }

    /**
     * 打印启动横幅
     */
    private static void printStartupBanner(String version) {
        log.info("""

                (♥◠‿◠)ﾉﾞ  Endless启动成功   ლ(´ڡ`ლ)ﾞ
                  _____   _   _   _____   _       _____   _____   _____\s
                 |  ___| | \\ | | |  _  \\ | |     |  ___| |  ___| |  ___|
                 | |___  |  \\| | | | | | | |     | |___  | |___  | |___\s
                 |  ___| | . ` | | | | | | |     |  ___| |_____| |_____|
                 | |___  | |\\  | | |_| | | |___  | |___   _____   _____\s
                 |_____| |_| \\_| |_____/ |_____| |_____| |_____| |_____|
                                                                        \s
                                    Version: {}""", version);
    }

    /**
     * 打印初始化信息汇总（使用 Logger 输出，保持日志一致）
     */
    private static void printStartupSummary(ConfigurableApplicationContext context,
            String version, long elapsedMs) {
        // 收集缓存数据
        int serverCount = context.getBean(IServerInfoService.class)
                .selectServerInfoList(new ServerInfo()).size();
        int commandCount = (RconService.COMMAND_INFO != null ? RconService.COMMAND_INFO.size() : 0);
        int emailTemplateCount = EmailTempCache.size();
        int rconConnectionCount = RconCache.size();
        int nodeServerCount = NodeCache.size();
        int botCount = context.getBean(BotManager.class).getAllBots().size();

        // 收集运行时信息
        Runtime runtime = Runtime.getRuntime();
        int maxMemory = (int) (runtime.maxMemory() / 1024 / 1024);
        int totalMemory = (int) (runtime.totalMemory() / 1024 / 1024);
        int freeMemory = (int) (runtime.freeMemory() / 1024 / 1024);
        int usedMemory = totalMemory - freeMemory;
        int processors = runtime.availableProcessors();
        String javaVersion = System.getProperty("java.version", "Unknown");
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        String profiles = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";
        String updateTime = DateUtils.getTime();
        double elapsedSec = elapsedMs / 1000.0;
        String heapInfo = usedMemory + "MB / " + maxMemory + "MB";
        String elapsedInfo = String.format("%.1f 秒", elapsedSec);

        // 使用 String.format 构建格式化文本，再通过 Logger 输出（统一日志来源）
        String summary = String.format("""

                ╔══════════════════════════════════════════════════════════════════════════════╗
                ║                        ENDLESS v%-8s 初始化信息汇总                              ║
                ╠══════════════════════════════════════════════════════════════════════════════╣
                ║  🎮 服务器缓存数量      : %-47d║
                ║  📝 缓存指令数量        : %-47d║
                ║  📧 邮件模板数量        : %-47d║
                ║  🔌 RCON 连接数量       : %-47d║
                ║  🖥️  节点服务器数量      : %-47d║
                ║  🤖 QQ 机器人数量       : %-47d║
                ╠══════════════════════════════════════════════════════════════════════════════╣
                ║  ☕ Java 版本           : %-47s║
                ║  📋 Spring Profiles     : %-47s║
                ║  🧠 JVM 内存 (已用/最大) : %-47s║
                ║  ⚙️  CPU 核心数          : %-47d║
                ║  ⏱️  启动耗时            : %-47s║
                ║  🕐 初始化完成时间      : %-47s║
                ╚══════════════════════════════════════════════════════════════════════════════╝
                """,
                version,
                serverCount,
                commandCount,
                emailTemplateCount,
                rconConnectionCount,
                nodeServerCount,
                botCount,
                javaVersion,
                profiles,
                heapInfo,
                processors,
                elapsedInfo,
                updateTime);
        log.info(summary);
    }
}
