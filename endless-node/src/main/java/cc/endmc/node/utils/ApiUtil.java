package cc.endmc.node.utils;

import cc.endmc.node.domain.NodeServer;

import static java.lang.String.format;

/**
 * @author Memory
 * @description API工具类
 * @createDate 2025-04-14 22:33:58
 */
public class ApiUtil {

    public static final String X_ENDLESS_TOKEN = "X-Endless-Token";

    // 注册API
    public static final String REGISTER_API = "/api/auth/register";

    // 注销API
    public static final String UN_REGISTER_API = "/api/auth/unregister";

    // 获取节点信息API
    public static final String SYSTEM_INFO = "/api/system/info";

    // 获取系统硬件信息
    public static final String HARDWARE = "/api/system/hardware";

    // 获取系统负载信息
    public static final String LOAD = "/api/system/load";

    // 获取系统文件列表
    public static final String FILE_LIST = "/api/files/list";

    // 下载文件
    public static final String FILE_DOWNLOAD = "/api/files/download";

    public static final String FILE_DOWNLOAD_FROM_URL = "/api/files/download-from-url";

    // 上传文件
    public static final String FILE_UPLOAD = "/api/files/upload";

    // 心跳检测
    public static final String HEARTBEAT = "/api/system/heartbeat";

    // 最后通信回调（带IP地址）
    public static final String CALLBACK_LAST_COMMUNICATION = "/api/system/communication-callback-with-ip/{uuid}/{ip}";

    // 更新主控信息
    public static final String UPDATE_MASTER_INFO = "/api/system/master-info-update";

    // 创建实例
    public static final String CREATE_INSTANCE = "/api/servers/create";

    // 实例列表
    public static final String LIST_INSTANCES = "/api/servers/list";

    // 实例控制
    public static final String START_INSTANCE = "/api/servers/%d/start";
    public static final String STOP_INSTANCE = "/api/servers/%d/stop";
    public static final String RESTART_INSTANCE = "/api/servers/%d/restart";
    public static final String KILL_INSTANCE = "/api/servers/%d/kill";
    public static final String DELETE_INSTANCE = "/api/servers/%d";
    public static final String CONSOLE_INSTANCE = "/api/servers/%d/console";
    public static final String CONSOLE_HISTORY_INSTANCE = "/api/servers/%d/console/history";
    public static final String COMMAND_INSTANCE = "/api/servers/%d/command";
    public static final String STATUS_INSTANCE = "/api/servers/%d/status";

    public static final String WEBSOCKET_ENDPOINT = "/ws";
    // WebSocket 控制台
    public static final String WEBSOCKET_CONSOLE = "/topic/console/";
    // WebSocket 订阅地址
    public static final String WEBSOCKET_SUBSCRIBE = "/app/console/subscribe";


    public static String getBaseUrl(NodeServer nodeServer) {
        return format("%s://%s:%d", nodeServer.getProtocol(), nodeServer.getIp(), nodeServer.getPort());
    }

    public static String getRegisterApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + REGISTER_API;
    }

    public static String getUnRegisterApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + UN_REGISTER_API;
    }

    public static String getSystemInfoApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + SYSTEM_INFO;
    }

    public static String getHardwareApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + HARDWARE;
    }

    public static String getLoadApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + LOAD;
    }

    public static String getFileListApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + FILE_LIST;
    }

    public static String getFileDownloadApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + FILE_DOWNLOAD;
    }

    public static String getFileUploadApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + FILE_UPLOAD;
    }

    public static String getFileDownloadFromUrlApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + FILE_DOWNLOAD_FROM_URL;
    }

    public static String getHeartbeatApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + HEARTBEAT;
    }

    public static String getCallbackLastCommunicationApi(NodeServer nodeServer, String uuid, String ip) {
        return getBaseUrl(nodeServer) + CALLBACK_LAST_COMMUNICATION.replace("{uuid}", uuid).replace("{ip}", ip);
    }

    public static String getUpdateMasterInfoApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + UPDATE_MASTER_INFO;
    }

    public static String getCreateInstanceApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + CREATE_INSTANCE;
    }

    public static String getListInstancesApi(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + LIST_INSTANCES;
    }

    public static String getStartInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(START_INSTANCE, serverId);
    }

    public static String getStopInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(STOP_INSTANCE, serverId);
    }

    public static String getRestartInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(RESTART_INSTANCE, serverId);
    }

    public static String getKillInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(KILL_INSTANCE, serverId);
    }

    public static String getDeleteInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(DELETE_INSTANCE, serverId);
    }

    public static String getConsoleInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(CONSOLE_INSTANCE, serverId);
    }

    public static String getConsoleHistoryInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(CONSOLE_HISTORY_INSTANCE, serverId);
    }

    public static String getCommandInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(COMMAND_INSTANCE, serverId);
    }

    public static String getStatusInstanceApi(NodeServer nodeServer, int serverId) {
        return getBaseUrl(nodeServer) + String.format(STATUS_INSTANCE, serverId);
    }

    public static String getWebSocketUrl(NodeServer nodeServer) {
        return getBaseUrl(nodeServer) + WEBSOCKET_ENDPOINT;
    }

}
