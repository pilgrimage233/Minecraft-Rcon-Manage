package cc.endmc.node.service.impl;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.mapper.NodeServerMapper;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.node.utils.ApiUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点服务器Service业务层处理
 *
 * @author Memory
 * @date 2025-04-14
 */
@Slf4j
@Service
public class NodeServerServiceImpl implements INodeServerService {
    @Autowired
    private NodeServerMapper nodeServerMapper;

    @Value("${ruoyi.version}")
    private String version;

    /**
     * 查询节点服务器
     *
     * @param id 节点服务器主键
     * @return 节点服务器
     */
    @Override
    public NodeServer selectNodeServerById(Long id) {
        return nodeServerMapper.selectNodeServerById(id);
    }

    /**
     * 查询节点服务器列表
     *
     * @param nodeServer 节点服务器
     * @return 节点服务器
     */
    @Override
    public List<NodeServer> selectNodeServerList(NodeServer nodeServer) {
        return nodeServerMapper.selectNodeServerList(nodeServer);
    }

    /**
     * 新增节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    @Override
    public AjaxResult insertNodeServer(NodeServer nodeServer) {
        return register(nodeServer);
    }

    /**
     * 注册节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    private AjaxResult register(NodeServer nodeServer) {
        log.info("开始注册节点: {}", nodeServer.getIp() + ":" + nodeServer.getPort());
        // 注册节点
        String url = ApiUtil.getRegisterApi(nodeServer);
        final String token = nodeServer.getToken();

        JSONObject params = new JSONObject();
        params.put("secretKey", token);
        params.put("version", version);
        params.put("ipAddress", IpUtils.getHostIp());

        if (nodeServer.getId() != null) {
            params.put("uuid", nodeServer.getUuid());
        }

        final HttpResponse response = HttpUtil.createPost(url)
                .header(ApiUtil.X_ENDLESS_TOKEN, token)
                .body(params.toJSONString())
                .execute();
        log.info("节点注册请求: {}", params.toJSONString());

        if (!response.isOk() || StringUtils.isEmpty(response.body())) return AjaxResult.error("节点注册失败");

        JSONObject jsonObject = JSONObject.parseObject(response.body());
        // log.info("节点注册返回: {}", jsonObject);

        if (!jsonObject.getBoolean("success") || jsonObject.getString("message").contains("已注册")) {
            log.error("节点注册失败: {}", jsonObject.getString("message"));
            return AjaxResult.error("节点注册失败: " + jsonObject.getString("message"));
        }

        log.info("节点注册成功: {}", jsonObject);

        nodeServer.setUuid(jsonObject.getString("nodeUuid"));
        nodeServer.setOsType(jsonObject.getString("osType"));
        nodeServer.setVersion(jsonObject.getString("version"));

        int result;
        if (nodeServer.getId() != null) {
            result = nodeServerMapper.updateNodeServer(nodeServer);
        } else {
            result = nodeServerMapper.insertNodeServer(nodeServer);
        }

        if (result > 0) {
            NodeCache.put(nodeServer.getId(), nodeServer); // 刷新缓存
            return AjaxResult.success("节点注册成功", nodeServer.getUuid());
        } else {
            // 回滚节点注册状态，注销节点
            jsonObject = new JSONObject();
            jsonObject.put("secretKey", token);
            jsonObject.put("uuid", nodeServer.getUuid());

            final HttpResponse execute = HttpUtil.createPost(ApiUtil.getUnRegisterApi(nodeServer))
                    .header(ApiUtil.X_ENDLESS_TOKEN, token)
                    .body(jsonObject.toJSONString())
                    .execute();
            log.info("节点注销请求: {}", jsonObject.toJSONString());
            if (execute.isOk() && execute.body().contains("节点注销成功")) {
                log.info("节点注销成功: {}", execute.body());
            } else {
                log.error("节点注销失败: {}", execute.body());
            }

            log.error("节点注册成功,但是数据库记录添加失败!: {}", jsonObject.getString("message"));
            return AjaxResult.error("节点注册成功,但是数据库记录添加失败!: " + jsonObject.getString("message"));
        }
    }

    /**
     * 修改节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    @Override
    public AjaxResult updateNodeServer(NodeServer nodeServer) {
        return register(nodeServer);
    }

    /**
     * 批量删除节点服务器
     *
     * @param ids 需要删除的节点服务器主键
     * @return 结果
     */
    @Override
    public int deleteNodeServerByIds(Long[] ids) {
        return nodeServerMapper.deleteNodeServerByIds(ids);
    }

    /**
     * 删除节点服务器信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    @Override
    public int deleteNodeServerById(Long id) {
        return nodeServerMapper.deleteNodeServerById(id);
    }

    /**
     * 获取节点服务器信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    @Override
    public AjaxResult getServerInfo(@NotNull Long id) {
        // 获取节点服务器信息
        NodeServer nodeServer = getCache(id);

        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        final HttpResponse execute = HttpUtil.createGet(ApiUtil.getHardwareApi(nodeServer))
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .execute();

        if (execute.isOk()) {
            JSONObject jsonObject = JSONObject.parseObject(execute.body());
            if (jsonObject.getBoolean("success")) {
                return AjaxResult.success(jsonObject);
            } else {
                return AjaxResult.error(jsonObject.getString("message"));
            }
        } else {
            return AjaxResult.error("获取节点服务器信息失败");
        }
    }

    /**
     * 获取节点服务器负载信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    @Override
    public AjaxResult getServerLoad(Long id) {
        // 获取节点服务器信息
        NodeServer nodeServer = getCache(id);

        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        final HttpResponse execute = HttpUtil.createGet(ApiUtil.getLoadApi(nodeServer))
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .execute();

        if (execute.isOk()) {
            JSONObject jsonObject = JSONObject.parseObject(execute.body());
            if (jsonObject.getBoolean("success")) {
                return AjaxResult.success(jsonObject);
            } else {
                return AjaxResult.error(jsonObject.getString("message"));
            }
        } else {
            return AjaxResult.error("获取节点服务器负载信息失败");
        }
    }

    /**
     * 获取节点服务器文件列表
     *
     * @param params 参数
     * @return 结果
     */
    @Override
    public AjaxResult getFileList(Map<String, Object> params) {
        if (!params.containsKey("id")) {
            return AjaxResult.error("缺少参数: id");
        }
        Integer id = (Integer) params.get("id");
        String path = null;
        if (params.containsKey("path")) {
            path = params.get("path").toString();
        }

        NodeServer nodeServer = getCache(id.longValue());

        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        Map<String, Object> param = new HashMap<>();
        param.put("path", path);
        final HttpResponse execute = HttpUtil.createGet(ApiUtil.getFileListApi(nodeServer))
                .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                .form(param)
                .execute();

        if (execute.isOk()) {
            JSONObject jsonObject = JSONObject.parseObject(execute.body());
            if (jsonObject.getBoolean("success")) {
                return AjaxResult.success(jsonObject);
            } else {
                return AjaxResult.error(jsonObject.getString("message"));
            }
        } else {
            return AjaxResult.error("获取节点服务器文件列表失败");
        }

    }

    /**
     * 下载文件
     *
     * @param response 响应
     * @param params   参数
     * @return 结果
     */
    @Override
    public void download(HttpServletResponse response, Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("path")) {
            log.error("缺少参数: id 或 path");
            return;
        }

        final Integer id = (Integer) params.get("id");
        final String path = (String) params.get("path");

        NodeServer nodeServer = getCache(id.longValue());
        if (nodeServer == null) {
            log.error("节点服务器不存在: {}", id);
            return;
        }

        try {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            // 发送请求
            HttpResponse httpResponse = HttpUtil.createGet(ApiUtil.getFileDownloadApi(nodeServer))
                    .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                    .form(param)
                    .execute();

            if (!httpResponse.isOk()) {
                log.error("下载文件失败: {}", httpResponse.body());
                return;
            }

            // 获取文件名
            String filename = path.substring(path.lastIndexOf("\\") + 1);

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // 使用NIO进行文件传输
            try (InputStream inputStream = httpResponse.bodyStream();
                 OutputStream outputStream = response.getOutputStream()) {

                // 创建直接缓冲区
                ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
                ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
                WritableByteChannel outputChannel = Channels.newChannel(outputStream);

                // 使用NIO进行数据传输
                while (inputChannel.read(buffer) != -1) {
                    buffer.flip();
                    outputChannel.write(buffer);
                    buffer.clear();
                }

                // 确保所有数据都被写入
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 获取节点服务器缓存
     *
     * @param id 节点服务器主键
     * @return 节点服务器
     */
    public NodeServer getCache(Long id) {
        if (NodeCache.containsKey(id)) {
            return NodeCache.get(id);
        } else {
            NodeServer nodeServer = nodeServerMapper.selectNodeServerById((long) id);
            if (nodeServer != null) {
                NodeCache.put(id, nodeServer);
                return nodeServer;
            } else {
                log.error("节点服务器不存在: {}", id);
                return null;
            }
        }
    }

    /**
     * 保存文件
     *
     * @param params 参数
     * @return 结果
     */
    @Override
    public AjaxResult upload(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("path") || !params.containsKey("file")) {
            return AjaxResult.error("缺少必要参数");
        }

        final Integer id = (Integer) params.get("id");
        final String path = (String) params.get("path");
        final MultipartFile file = (MultipartFile) params.get("file");

        NodeServer nodeServer = getCache(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            HttpRequest request = HttpUtil.createPost(ApiUtil.getFileUploadApi(nodeServer))
                    .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                    .form("path", path);

            // MultipartFile需要转换成 File 对象
            if (file != null) {
                // 创建临时文件
                File tempFile = File.createTempFile("upload_", "_" + ((MultipartFile) file).getOriginalFilename());
                ((MultipartFile) file).transferTo(tempFile);
                // 添加文件到表单
                request.form("file", tempFile);
                // 设置超时
                request.timeout(30000);
            }

            HttpResponse httpResponse = request.execute();
            JSONObject jsonObject = JSONObject.parseObject(httpResponse.body());

            if (!httpResponse.isOk()) {
                return AjaxResult.error(jsonObject.getString("error"));
            }

            if (jsonObject.getBoolean("success")) {
                return AjaxResult.success("上传成功");
            } else {
                return AjaxResult.error(jsonObject.getString("message"));
            }
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return AjaxResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult downloadFromUrl(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("path") || !params.containsKey("url")) {
            return AjaxResult.error("缺少必要参数");
        }
        final Integer id = (Integer) params.get("id");
        final String path = (String) params.get("path");
        final String url = (String) params.get("url");

        NodeServer nodeServer = getCache(id.longValue());

        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            Map<String, Object> param = new HashMap<>();
            param.put("path", path);
            param.put("url", url);
            // 发送请求
            HttpResponse httpResponse = HttpUtil.createPost(ApiUtil.getFileDownloadFromUrlApi(nodeServer))
                    .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                    .form(param)
                    .execute();

            if (!httpResponse.isOk()) {
                return AjaxResult.error("下载文件失败: " + httpResponse.body());
            }

            JSONObject jsonObject = JSONObject.parseObject(httpResponse.body());
            if (jsonObject.getBoolean("success")) {
                return AjaxResult.success(jsonObject);
            } else {
                return AjaxResult.error(jsonObject.getString("message"));
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return AjaxResult.error("下载文件失败: " + e.getMessage());
        }
    }

}
