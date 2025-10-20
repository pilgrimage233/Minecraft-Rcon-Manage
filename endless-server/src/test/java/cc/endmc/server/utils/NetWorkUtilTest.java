package cc.endmc.server.utils;

import cc.endmc.server.model.MinecraftServerInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NetWorkUtilTest {

    @Test
    public void testNetWorkUtil() {
        // 测试Minecraft服务器延迟获取功能
        System.out.println("Testing Minecraft server latency retrieval...");

        // 测试一个已知的Minecraft服务器
        MinecraftServerInfo serverInfo = NetWorkUtil.getMinecraftServerLatency("mc.hypixel.net", 25565);
        if (serverInfo.isReachable()) {
            System.out.println("Server is reachable!");
            System.out.println("Host: " + serverInfo.getHost());
            System.out.println("Port: " + serverInfo.getPort());
            System.out.println("Latency: " + serverInfo.getLatency() + "ms");
            System.out.println("Version: " + serverInfo.getVersion());
            System.out.println("Protocol Version: " + serverInfo.getProtocolVersion());
            System.out.println("Online Players: " + serverInfo.getOnlinePlayers());
            System.out.println("Max Players: " + serverInfo.getMaxPlayers());
            System.out.println("MOTD: " + serverInfo.getMotd());
            System.out.println("Favicon: " + (serverInfo.getFavicon() != null ? "Yes" : "No"));
        } else {
            System.out.println("Server is not reachable.");
        }

        // 测试一个SRV记录解析
        MinecraftServerInfo serverInfo2 = NetWorkUtil.getMinecraftServerLatency("hypixel.net", 25565);
        if (serverInfo2.isReachable()) {
            System.out.println("\nSRV Record test:");
            System.out.println("Host: " + serverInfo2.getHost());
            System.out.println("Port: " + serverInfo2.getPort());
            System.out.println("Latency: " + serverInfo2.getLatency() + "ms");
        } else {
            System.out.println("\nSRV Record test failed.");
        }

        // 测试一个不存在的服务器
        // System.out.println("\nTesting non-existent server...");
        // MinecraftServerInfo serverInfo3 = NetWorkUtil.getMinecraftServerLatency("non-existent-server.example.com", 25565);
        // if (!serverInfo3.isReachable()) {
        //     System.out.println("Non-existent server correctly identified as unreachable.");
        // }
    }
}