package cc.endmc.server.model;

import lombok.Data;

/**
 * Minecraft服务器信息封装类
 */
@Data
public class MinecraftServerInfo {

    private String host;
    private int port;
    private long latency;
    private String version;
    private int protocolVersion;
    private int onlinePlayers;
    private int maxPlayers;
    private String motd;
    private String favicon;
    private boolean reachable;

    public MinecraftServerInfo() {
        this.reachable = false;
    }

    @Override
    public String toString() {
        return "MinecraftServerInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", latency=" + latency +
                ", version='" + version + '\'' +
                ", protocolVersion=" + protocolVersion +
                ", onlinePlayers=" + onlinePlayers +
                ", maxPlayers=" + maxPlayers +
                ", motd='" + motd + '\'' +
                ", favicon='" + favicon + '\'' +
                ", reachable=" + reachable +
                '}';
    }
}