package cc.endmc;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.config.InitConfigService;
import cc.endmc.server.common.MapCache;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.ws.BotManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EndlessApplication {
    public static void main(String[] args) {
        // 设置默认时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

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

        System.out.println("(♥◠‿◠)ﾉﾞ  Endless启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "  _____   _   _   _____   _       _____   _____   _____  \n" +
                " |  ___| | \\ | | |  _  \\ | |     |  ___| |  ___| |  ___| \n" +
                " | |___  |  \\| | | | | | | |     | |___  | |___  | |___  \n" +
                " |  ___| | . ` | | | | | | |     |  ___| |_____| |_____| \n" +
                " | |___  | |\\  | | |_| | | |___  | |___   _____   _____  \n" +
                " |_____| |_| \\_| |_____/ |_____| |_____| |_____| |_____| \n" +
                "                                                          ");

        // 打印初始化信息汇总
        System.out.println("\n" +
                "╔════════════════════════════════════════════════════════════════════════════╗\n" +
                "║                              ENDLESS 初始化信息汇总                          ║\n" +
                "╠════════════════════════════════════════════════════════════════════════════╣\n" +
                "║ 📊 服务器信息缓存数量: " + context.getBean(IServerInfoService.class).selectServerInfoList(new ServerInfo()).size() + "\n" +
                "║ 📝 缓存指令数量: " + (RconService.COMMAND_INFO != null ? RconService.COMMAND_INFO.size() : 0) + "\n" +
                "║ 🔌 Rcon连接服务器数量: " + MapCache.size() + "\n" +
                // "║ 🖥️ 节点服务器数量: " + NodeCache.size() + "\n" +
                "║ 🤖 QQ机器人数量: " + context.getBean(BotManager.class).getAllBots().size() + "\n" +
                "║ ⏱️ 服务器信息更新时间: " + DateUtils.getNowDate() + "\n" +
                "╚════════════════════════════════════════════════════════════════════════════╝\n");
    }
}
