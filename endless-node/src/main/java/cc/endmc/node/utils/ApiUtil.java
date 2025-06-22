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
}
