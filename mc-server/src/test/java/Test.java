import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.DomainToIp;

/**
 * ClassName: Test <br>
 * Description:
 * date: 2024/1/2 01:07 <br>
 *
 * @author ~~~ <br>
 * @since JDK 1.8
 */

public class Test {
    public static void main(String[] args) {
        RconClient open = RconClient.open(DomainToIp.domainToIp("192.168.6.187"), 50003, "20021129");
        String list = open.sendCommand("whitelist list");
        System.err.println(list);
        String[] split = new String[0];
        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            // 以There are 88 whitelisted player(s): 为起点，并且以, 为分隔符，Linux系统下需要转义
            split = list.split("whitelisted player\\(s\\):")[1].split(", ");
            // split = list.split("There are 88 whitelisted player\\(s\\):")[1].trim().split(", ");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].toLowerCase().trim();
            }
        }
        for (String s : split) {
            System.err.println(s);
        }
    }
}
