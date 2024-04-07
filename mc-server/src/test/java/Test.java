import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.server.common.DomainToIp;

import java.util.Arrays;

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
        RconClient open = RconClient.open(DomainToIp.domainToIp("192.168.6.187"), 123456, "passwd");

        String list = open.sendCommand("whitelist add test123");
        String[] split = list.substring(list.indexOf(": ")).split(", ");
        // split内容转小写
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].toLowerCase();
        }
        System.err.println(Arrays.toString(split));
    }
}
