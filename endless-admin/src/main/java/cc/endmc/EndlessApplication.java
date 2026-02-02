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

/**
 * 启动程序
 *
 * @author ruoyi
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = "cc.endmc")
public class EndlessApplication {
    public static void main(String[] args) {
        // 初始化配置文件
        try {
            InitConfigService initConfigService = new InitConfigService();
            initConfigService.initializeConfigs();
        } catch (Exception e) {
            System.err.println("配置文件初始化失败，请检查应用程序权限！");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // System.setProperty("spring.devtools.restart.enabled", "false");
        // 启动应用
        ConfigurableApplicationContext context = SpringApplication.run(EndlessApplication.class, args);

        // 获取版本信息
        String version = context.getEnvironment().getProperty("endless.version", "Unknown");

        // 收集初始化数据
        int serverCount = context.getBean(IServerInfoService.class).selectServerInfoList(new ServerInfo()).size();
        int commandCount = (RconService.COMMAND_INFO != null ? RconService.COMMAND_INFO.size() : 0);
        int emailTemplateCount = EmailTempCache.size();
        int rconConnectionCount = RconCache.size();
        int nodeServerCount = NodeCache.size();
        int botCount = context.getBean(BotManager.class).getAllBots().size();
        String updateTime = DateUtils.getTime();

        // 打印启动横幅
        System.out.println("""
                
                (♥◠‿◠)ﾉﾞ  Endless启动成功   ლ(´ڡ`ლ)ﾞ
                  _____   _   _   _____   _       _____   _____   _____\s
                 |  ___| | \\ | | |  _  \\ | |     |  ___| |  ___| |  ___|
                 | |___  |  \\| | | | | | | |     | |___  | |___  | |___\s
                 |  ___| | . ` | | | | | | |     |  ___| |_____| |_____|
                 | |___  | |\\  | | |_| | | |___  | |___   _____   _____\s
                 |_____| |_| \\_| |_____/ |_____| |_____| |_____| |_____|
                                                                        \s
                                    Version: %s
                """.formatted(version));

        // 打印初始化信息汇总
        System.out.println("""
                ╔════════════════════════════════════════════════════════════════════════════╗
                ║                      ENDLESS v%-8s 初始化信息汇总                            ║
                ╠════════════════════════════════════════════════════════════════════════════╣
                ║ 🎮 服务器缓存数量        : %-45d║
                ║ 📝 缓存指令数量          : %-45d║
                ║ 📧 邮件模板数量          : %-45d║
                ║ 🔌 Rcon连接数量          : %-45d║
                ║ 🖥️ 节点服务器数量        : %-45d║
                ║ 🤖 QQ机器人数量          : %-45d║
                ║ ⏱️ 初始化完成时间        : %-45s║
                ╚════════════════════════════════════════════════════════════════════════════╝
                """.formatted(
                version,
                serverCount,
                commandCount,
                emailTemplateCount,
                rconConnectionCount,
                nodeServerCount,
                botCount,
                updateTime
        ));
    }
}
