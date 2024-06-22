import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: JmxTest <br>
 * Description:
 * date: 2024/6/21 下午1:50 <br>
 *
 * @author Administrator <br>
 * @since JDK 1.8
 */
@RunWith(JUnit4.class)
public class JmxTest {

    @Test
    public void test() throws IOException {
        System.out.println("JmxTest.test");
        // JMX服务URL，格式为：service:jmx:protocol:sap[:server_id]
        String serviceURL = "service:jmx:rmi:///jndi/rmi://123.57.21.212:7024/jmxrmi";

        // 创建JMXServiceURL对象
        JMXServiceURL jmxServiceURL = new JMXServiceURL(serviceURL);

        // 连接到远程JMX代理
        Map<String, Object> environment = new HashMap<>();
        // 如果需要认证，可以在environment中设置用户名和密码
        // environment.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "org.example.jmx.remote.server");
        // environment.put("jmx.remote.credentials", new String[] { "username", "password" });
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL);

        // 获取MBean服务器连接
        MBeanServerConnection mbsConnection = jmxConnector.getMBeanServerConnection();

        // 从MBean服务器连接中读取MBean信息
        // ObjectName的格式为：domain:key=value,key=value...
        ObjectName objectName = null;
        try {
            objectName = new ObjectName("java.lang:type=Memory");
            MemoryUsage heapMemoryUsage = (MemoryUsage) mbsConnection.getAttribute(objectName, "HeapMemoryUsage");
            System.out.println("Heap Memory Usage: " + heapMemoryUsage);

        } catch (MalformedObjectNameException | MBeanException | AttributeNotFoundException |
                 InstanceNotFoundException | ReflectionException e) {
            throw new RuntimeException(e);
        }
        // 关闭连接
        jmxConnector.close();
    }

}
