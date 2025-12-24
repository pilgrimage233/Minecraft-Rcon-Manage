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

    // 认证相关
    public static final String REGISTER_API = "/api/auth/register";
    public static final String UN_REGISTER_API = "/api/auth/unregister";

    // 系统相关
    public static final String SYSTEM_INFO = "/api/system/info";
    public static final String HARDWARE = "/api/system/hardware";
    public static final String LOAD = "/api/system/load";
    public static final String HEARTBEAT = "/api/system/heartbeat";
    public static final String CALLBACK_LAST_COMMUNICATION = "/api/system/communication-callback-with-ip/{uuid}/{ip}";
    public static final String UPDATE_MASTER_INFO = "/api/system/master-info-update";
    public static final String TEST_CONNECTION = "/api/system/test-connection";

    // 文件相关
    public static final String FILE_LIST = "/api/files/list";
    public static final String FILE_DOWNLOAD = "/api/files/download";
    public static final String FILE_DOWNLOAD_FROM_URL = "/api/files/download-from-url";
    public static final String FILE_UPLOAD = "/api/files/upload";
    public static final String FILE_DELETE = "/api/files/delete";

    // Java环境相关
    public static final String JAVA_ENV_VERIFY = "/api/java-env/verify";
    public static final String JAVA_ENV_SCAN = "/api/java-env/scan";
    public static final String JAVA_ENV_INSTALL = "/api/java-env/install";
    public static final String JAVA_ENV_CANCEL = "/api/java-env/install/cancel";

    // 实例相关
    public static final String CREATE_INSTANCE = "/api/servers/create";
    public static final String LIST_INSTANCES = "/api/servers/list";
    public static final String START_INSTANCE = "/api/servers/%d/start";
    public static final String STOP_INSTANCE = "/api/servers/%d/stop";
    public static final String RESTART_INSTANCE = "/api/servers/%d/restart";
    public static final String KILL_INSTANCE = "/api/servers/%d/kill";
    public static final String UPDATE_INSTANCE = "/api/servers/%d";
    public static final String DELETE_INSTANCE = "/api/servers/%d";
    public static final String CONSOLE_INSTANCE = "/api/servers/%d/console";
    public static final String CONSOLE_HISTORY_INSTANCE = "/api/servers/%d/console/history";
    public static final String COMMAND_INSTANCE = "/api/servers/%d/command";
    public static final String STATUS_INSTANCE = "/api/servers/%d/status";
    public static final String PLAYERS_INSTANCE = "/api/servers/%d/players";
    public static final String PLAYER_ACTION_INSTANCE = "/api/servers/%d/players/%s/action";
    public static final String QUERY_DIAGNOSTIC_INSTANCE = "/api/servers/%d/query-diagnostic";

    // WebSocket相关
    public static final String WEBSOCKET_ENDPOINT = "/ws";
    public static final String WEBSOCKET_CONSOLE = "/topic/console/";
    public static final String WEBSOCKET_SUBSCRIBE = "/app/console/subscribe";

    // 基础URL构建
    public static String getBaseUrl(NodeServer nodeServer) {
        return format("%s://%s:%d", nodeServer.getProtocol(), nodeServer.getIp(), nodeServer.getPort());
    }

    private static String buildUrl(NodeServer nodeServer, String endpoint) {
        return getBaseUrl(nodeServer) + endpoint;
    }

    private static String buildUrl(NodeServer nodeServer, String template, int serverId) {
        return getBaseUrl(nodeServer) + format(template, serverId);
    }

    // 认证API
    public static String getRegisterApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, REGISTER_API);
    }

    public static String getUnRegisterApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, UN_REGISTER_API);
    }

    // 系统API
    public static String getSystemInfoApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, SYSTEM_INFO);
    }

    public static String getHardwareApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, HARDWARE);
    }

    public static String getLoadApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, LOAD);
    }

    public static String getHeartbeatApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, HEARTBEAT);
    }

    public static String getCallbackLastCommunicationApi(NodeServer nodeServer, String uuid, String ip) {
        return buildUrl(nodeServer, CALLBACK_LAST_COMMUNICATION.replace("{uuid}", uuid).replace("{ip}", ip));
    }

    public static String getUpdateMasterInfoApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, UPDATE_MASTER_INFO);
    }

    public static String getTestConnectionApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, TEST_CONNECTION);
    }

    // 文件API
    public static String getFileListApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, FILE_LIST);
    }

    public static String getFileDownloadApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, FILE_DOWNLOAD);
    }

    public static String getFileUploadApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, FILE_UPLOAD);
    }

    public static String getFileDownloadFromUrlApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, FILE_DOWNLOAD_FROM_URL);
    }

    public static String getFileDeleteApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, FILE_DELETE);
    }

    // Java环境API
    public static String getJavaEnvVerifyApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, JAVA_ENV_VERIFY);
    }

    public static String getJavaEnvScanApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, JAVA_ENV_SCAN);
    }

    // 实例管理API
    public static String getCreateInstanceApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, CREATE_INSTANCE);
    }

    public static String getListInstancesApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, LIST_INSTANCES);
    }

    public static String getStartInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, START_INSTANCE, serverId);
    }

    public static String getStopInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, STOP_INSTANCE, serverId);
    }

    public static String getRestartInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, RESTART_INSTANCE, serverId);
    }

    public static String getKillInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, KILL_INSTANCE, serverId);
    }

    public static String getUpdateInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, UPDATE_INSTANCE, serverId);
    }

    public static String getDeleteInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, DELETE_INSTANCE, serverId);
    }

    public static String getConsoleInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, CONSOLE_INSTANCE, serverId);
    }

    public static String getConsoleHistoryInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, CONSOLE_HISTORY_INSTANCE, serverId);
    }

    public static String getCommandInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, COMMAND_INSTANCE, serverId);
    }

    public static String getStatusInstanceApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, STATUS_INSTANCE, serverId);
    }

    public static String getServerPlayersApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, PLAYERS_INSTANCE, serverId);
    }

    public static String getPlayerActionApi(NodeServer nodeServer, int serverId, String playerName) {
        return getBaseUrl(nodeServer) + format(PLAYER_ACTION_INSTANCE, serverId, playerName);
    }

    public static String getQueryDiagnosticApi(NodeServer nodeServer, int serverId) {
        return buildUrl(nodeServer, QUERY_DIAGNOSTIC_INSTANCE, serverId);
    }

    // WebSocket API
    public static String getWebSocketUrl(NodeServer nodeServer) {
        return buildUrl(nodeServer, WEBSOCKET_ENDPOINT);
    }

    /**
     * 获取Java环境安装API
     */
    public static String getJavaEnvInstallApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, JAVA_ENV_INSTALL);
    }

    /**
     * 获取Java环境安装取消API
     */
    public static String getJavaEnvCancelApi(NodeServer nodeServer) {
        return buildUrl(nodeServer, JAVA_ENV_CANCEL);
    }

}
