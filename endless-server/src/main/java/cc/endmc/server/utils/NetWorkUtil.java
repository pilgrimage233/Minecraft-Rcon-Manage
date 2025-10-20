package cc.endmc.server.utils;

import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.model.MinecraftServerInfo;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ClassName: NetWorkUtil <br>
 * Description: 网络工具类，提供服务器连通性测试、延迟测试等功能
 * date: 2025/10/20 17:57 <br>
 *
 * @author Memory <br>
 * @since JDK 1.8
 */
@Slf4j
public class NetWorkUtil {

    // 默认超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 3000;

    // 测试连通性
    public static boolean testConnectivity(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            return true;
        } catch (Exception e) {
            log.debug("连接测试失败: {}:{}, 错误: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * 获取Minecraft服务器延迟
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @return MinecraftServerInfo对象，包含服务器详细信息
     */
    public static MinecraftServerInfo getMinecraftServerLatency(String host, int port) {
        MinecraftServerInfo serverInfo = new MinecraftServerInfo();
        serverInfo.setHost(host);
        serverInfo.setPort(port);

        try {
            // 解析域名和SRV记录
            ServerAddress serverAddress = resolveServerAddress(host, port);
            String resolvedHost = serverAddress.host;
            int resolvedPort = serverAddress.port;

            serverInfo.setHost(resolvedHost);
            serverInfo.setPort(resolvedPort);

            try (Socket socket = new Socket()) {
                long startTime = System.currentTimeMillis();
                socket.connect(new java.net.InetSocketAddress(resolvedHost, resolvedPort), 5000); // 5秒超时
                long endTime = System.currentTimeMillis();

                // 发送握手包
                sendHandshake(socket, resolvedHost, resolvedPort);

                // 发送状态请求包
                sendStatusRequest(socket);

                // 读取响应
                String response = readResponse(socket);

                // 解析响应
                parseServerResponse(response, serverInfo);

                // 设置延迟和可达性
                serverInfo.setLatency(endTime - startTime);
                serverInfo.setReachable(true);

                return serverInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            serverInfo.setReachable(false);
            return serverInfo;
        }
    }

    /**
     * 解析服务器地址，支持域名和SRV记录查询
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @return 解析后的服务器地址信息
     */
    private static ServerAddress resolveServerAddress(String host, int port) {
        // 默认端口
        if (port <= 0) {
            port = 25565;
        }

        // 尝试查询SRV记录
        try {
            DirContext context = new InitialDirContext();
            Attributes attributes = context.getAttributes(
                    "dns:///_minecraft._tcp." + host,
                    new String[]{"SRV"}
            );

            Attribute attribute = attributes.get("SRV");
            if (attribute != null) {
                String srvRecord = (String) attribute.get();
                if (srvRecord != null) {
                    String[] parts = srvRecord.split(" ");
                    if (parts.length >= 4) {
                        // 格式: 优先级 权重 端口 目标主机
                        int srvPort = Integer.parseInt(parts[2]);
                        String srvHost = parts[3].endsWith(".") ?
                                parts[3].substring(0, parts[3].length() - 1) : parts[3];
                        return new ServerAddress(srvHost, srvPort);
                    }
                }
            }
        } catch (Exception e) {
            // SRV查询失败，使用默认端口
            // e.printStackTrace(); // 可以选择不打印SRV查询失败的日志
        }

        // 返回原始主机和端口
        return new ServerAddress(host, port);
    }

    /**
     * 解析服务器响应
     *
     * @param response   服务器响应JSON
     * @param serverInfo 服务器信息对象
     */
    private static void parseServerResponse(String response, cc.endmc.server.model.MinecraftServerInfo serverInfo) {
        try {
            // 解析JSON响应
            JSONObject jsonObject = JSONObject.parseObject(response);

            // 解析版本信息
            if (jsonObject.containsKey("version")) {
                JSONObject versionObject = jsonObject.getJSONObject("version");
                if (versionObject.containsKey("name")) {
                    serverInfo.setVersion(versionObject.getString("name"));
                }
                if (versionObject.containsKey("protocol")) {
                    serverInfo.setProtocolVersion(versionObject.getIntValue("protocol"));
                }
            }

            // 解析玩家信息
            if (jsonObject.containsKey("players")) {
                JSONObject playersObject = jsonObject.getJSONObject("players");
                if (playersObject.containsKey("online")) {
                    serverInfo.setOnlinePlayers(playersObject.getIntValue("online"));
                }
                if (playersObject.containsKey("max")) {
                    serverInfo.setMaxPlayers(playersObject.getIntValue("max"));
                }
            }

            // 解析MOTD
            if (jsonObject.containsKey("description")) {
                Object descriptionElement = jsonObject.get("description");
                if (descriptionElement instanceof JSONObject) {
                    // 处理复杂格式的MOTD
                    serverInfo.setMotd(parseMotdFromJson((JSONObject) descriptionElement));
                } else {
                    serverInfo.setMotd(jsonObject.getString("description"));
                }
            }

            // 解析favicon
            if (jsonObject.containsKey("favicon")) {
                serverInfo.setFavicon(jsonObject.getString("favicon"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从JSON对象中解析MOTD
     *
     * @param jsonObject MOTD的JSON对象
     * @return 解析后的MOTD字符串
     */
    private static String parseMotdFromJson(JSONObject jsonObject) {
        try {
            if (jsonObject.containsKey("text")) {
                return jsonObject.getString("text");
            } else if (jsonObject.containsKey("extra")) {
                JSONArray extraArray = jsonObject.getJSONArray("extra");
                StringBuilder motdBuilder = new StringBuilder();
                for (int i = 0; i < extraArray.size(); i++) {
                    Object element = extraArray.get(i);
                    if (element instanceof JSONObject) {
                        JSONObject extraObject = (JSONObject) element;
                        if (extraObject.containsKey("text")) {
                            motdBuilder.append(extraObject.getString("text"));
                        }
                    } else {
                        motdBuilder.append(element.toString());
                    }
                }
                return motdBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 读取响应
     */
    private static String readResponse(Socket socket) throws IOException {
        // 读取包长度
        int length = readVarInt(socket);

        // 读取包ID
        int packetId = readVarInt(socket);

        // 读取数据长度
        int dataLength = readVarInt(socket);

        // 读取数据
        byte[] data = new byte[dataLength];
        int read = 0;
        while (read < dataLength) {
            int received = socket.getInputStream().read(data, read, dataLength - read);
            if (received == -1) {
                throw new IOException("连接已关闭");
            }
            read += received;
        }

        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 测试RCON连接通断
     *
     * @param serverId 服务器ID
     * @return 是否连通
     */
    public static boolean testRconConnection(String serverId) {
        try {
            if (serverId == null) {
                log.error("测试RCON连接失败: 参数为空");
                return false;
            }
            if (!RconCache.containsKey(serverId)) {
                return false;
            }

            final RconClient client = RconCache.get(serverId);
            if (client == null || !client.isSocketChannelOpen()) {
                log.error("测试RCON连接失败: RCON客户端未连接");
                return false;
            }

            // 通过发送一个简单的命令来测试RCON连接
            // 通常list命令不会对服务器造成影响
            // final String result = RconCache.get(serverId).sendCommand("list");
            // String result = rconService.sendCommand(serverId, "list");
            return client.isSocketChannelOpen();
        } catch (Exception e) {
            log.error("测试RCON连接失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送握手包
     */
    private static void sendHandshake(Socket socket, String host, int port) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // 握手包ID (0x00)
        dos.writeByte(0x00);
        // 协议版本 (使用47以兼容1.8+的状态请求)
        writeVarInt(dos, 47);
        // 服务器地址
        writeString(dos, host);
        // 服务器端口
        dos.writeShort(port);
        // 下一步状态（1表示状态请求）
        writeVarInt(dos, 1);

        // 写入长度前缀
        byte[] handshakeData = baos.toByteArray();
        dos.close();

        DataOutputStream packetOut = new DataOutputStream(socket.getOutputStream());
        writeVarInt(packetOut, handshakeData.length);
        packetOut.write(handshakeData);
        packetOut.flush();
    }

    /**
     * 发送状态请求包
     */
    private static void sendStatusRequest(Socket socket) throws IOException {
        DataOutputStream packetOut = new DataOutputStream(socket.getOutputStream());
        // 状态请求包：长度1 + 包ID(0x00)
        writeVarInt(packetOut, 1); // 包长度
        writeVarInt(packetOut, 0); // 包ID
        packetOut.flush();
    }

    /**
     * 接收响应
     */
    private static byte[] receiveResponse(Socket socket, int timeout) throws IOException {
        // 设置超时
        socket.setSoTimeout(timeout);

        // 读取包长度
        int length = readVarInt(socket);

        // 读取包ID
        int packetId = readVarInt(socket);

        // 读取数据长度
        int dataLength = readVarInt(socket);

        // 读取数据
        byte[] data = new byte[dataLength];
        int read = 0;
        while (read < dataLength) {
            int received = socket.getInputStream().read(data, read, dataLength - read);
            if (received == -1) {
                throw new IOException("连接已关闭");
            }
            read += received;
        }

        return data;
    }

    /**
     * 写入VarInt
     */
    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            out.writeByte(temp);
        } while (value != 0);
    }

    /**
     * 读取VarInt
     */
    private static int readVarInt(Socket socket) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            int read = socket.getInputStream().read();
            if (read == -1) {
                throw new IOException("连接已关闭");
            }
            currentByte = (byte) read;
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;
            // VarInt最多5个字节
            if (position >= 32) throw new RuntimeException("VarInt too big");
        }

        return value;
    }

    /**
     * 写入字符串
     */
    private static void writeString(DataOutputStream out, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    /**
     * 服务器地址信息类
     */
    private static class ServerAddress {
        final String host;
        final int port;

        ServerAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
