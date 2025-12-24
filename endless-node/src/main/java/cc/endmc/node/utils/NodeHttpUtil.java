package cc.endmc.node.utils;

import cc.endmc.node.domain.NodeServer;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;

/**
 * 节点HTTP请求工具类
 * 自动添加节点认证Token的Header
 *
 * @author Memory
 */
public class NodeHttpUtil {

    private static final Integer TIMEOUT = 20000;

    /**
     * 创建GET请求，自动添加Token Header
     *
     * @param nodeServer 节点服务器对象
     * @param url        请求URL
     * @return HttpRequest对象，已添加Token Header
     */
    public static HttpRequest createGet(NodeServer nodeServer, String url) {
        return HttpUtil.createGet(url)
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .timeout(TIMEOUT);
    }

    /**
     * 创建POST请求，自动添加Token Header
     *
     * @param nodeServer 节点服务器对象
     * @param url        请求URL
     * @return HttpRequest对象，已添加Token Header
     */
    public static HttpRequest createPost(NodeServer nodeServer, String url) {
        return HttpUtil.createPost(url)
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .timeout(TIMEOUT);
    }

    /**
     * 创建PUT请求，自动添加Token Header
     *
     * @param nodeServer 节点服务器对象
     * @param url        请求URL
     * @return HttpRequest对象，已添加Token Header
     */
    public static HttpRequest createPut(NodeServer nodeServer, String url) {
        return HttpUtil.createRequest(Method.PUT, url)
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .timeout(TIMEOUT);
    }

    /**
     * 创建DELETE请求，自动添加Token Header
     *
     * @param nodeServer 节点服务器对象
     * @param url        请求URL
     * @return HttpRequest对象，已添加Token Header
     */
    public static HttpRequest createDelete(NodeServer nodeServer, String url) {
        return HttpUtil.createRequest(Method.DELETE, url)
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .timeout(TIMEOUT);
    }

    /**
     * 创建自定义方法的请求，自动添加Token Header
     *
     * @param nodeServer 节点服务器对象
     * @param method     请求方法
     * @param url        请求URL
     * @return HttpRequest对象，已添加Token Header
     */
    public static HttpRequest createRequest(NodeServer nodeServer, Method method, String url) {
        return HttpUtil.createRequest(method, url)
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .timeout(TIMEOUT);
    }
}

