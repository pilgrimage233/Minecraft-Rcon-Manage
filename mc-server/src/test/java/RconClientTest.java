import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.DomainToIp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * ClassName: Test <br>
 * Description:
 * date: 2024/1/2 01:07 <br>
 *
 * @author ~~~ <br>
 * @since JDK 1.8
 */
@RunWith(JUnit4.class)
public class RconClientTest {

    final static String ip = "i3.xlhost.cn";
    final static String password = "qzWXecRVtbYNum753";
    final static int port = 33002;

    Logger logger = Logger.getLogger("Test");

    @org.junit.Test
    public void medthod() {
        try {
            RconClient client = RconClient.open(DomainToIp.domainToIp(ip), port, password);

            final String list = client.sendCommand("ban kissthefire");
            System.err.println(list);

            client.close();
        } catch (Exception e) {
            logger.warning(StringUtils.format("Failed to connect to the server: {}", e.getMessage()));
        }
    }

    @Test
    public void test() {
        String s = "whitelist remove kiss";
        System.err.println(s.substring(17));
    }

    @Test
    public void test1() {
        String name = "svg";
        String[] split = {"svg"};
        if (!Arrays.asList(split).contains(name.toUpperCase())) {
            System.err.println(1);
        }
    }

    @Test
    public void test2() {
  /*      try (Rcon client = Rcon.open(DomainToIp.domainToIp(ip), port)) {
            if (client.authenticate(password)) {
                System.err.println(client.sendCommand("list"));
            } else {
                System.err.println("Failed to authenticate");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}
