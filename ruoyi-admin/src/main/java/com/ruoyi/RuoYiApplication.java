package com.ruoyi;

import com.ruoyi.config.InitConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoYiApplication {
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
        ConfigurableApplicationContext context = SpringApplication.run(RuoYiApplication.class, args);
        
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
