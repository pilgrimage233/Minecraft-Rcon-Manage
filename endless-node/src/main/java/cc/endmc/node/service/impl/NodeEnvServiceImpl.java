package cc.endmc.node.service.impl;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.node.domain.NodeEnv;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.mapper.NodeEnvMapper;
import cc.endmc.node.service.INodeEnvService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.node.utils.ApiUtil;
import cc.endmc.node.utils.NodeHttpUtil;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点Java多版本环境管理Service业务层处理
 *
 * @author Memory
 * @date 2025-11-25
 */
@Slf4j
@Service
public class NodeEnvServiceImpl implements INodeEnvService {
    @Autowired
    private NodeEnvMapper nodeEnvMapper;

    @Autowired
    private INodeServerService nodeServerService;

    /**
     * 查询节点Java多版本环境管理
     *
     * @param id 节点Java多版本环境管理主键
     * @return 节点Java多版本环境管理
     */
    @Override
    public NodeEnv selectNodeEnvById(Long id) {
        return nodeEnvMapper.selectNodeEnvById(id);
    }

    /**
     * 查询节点Java多版本环境管理列表
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 节点Java多版本环境管理
     */
    @Override
    public List<NodeEnv> selectNodeEnvList(NodeEnv nodeEnv) {
        return nodeEnvMapper.selectNodeEnvList(nodeEnv);
    }

    /**
     * 新增节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    @Override
    public int insertNodeEnv(NodeEnv nodeEnv) {
        // 验证节点服务器是否存在
        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeEnv.getNodeId());
        if (nodeServer == null) {
            throw new RuntimeException("节点服务器不存在");
        }

        // 如果提供了路径，验证Java环境
        if (nodeEnv.getPath() != null && !nodeEnv.getPath().trim().isEmpty()) {
            try {
                Map<String, Object> verifyResult = verifyJavaEnvironment(nodeServer, nodeEnv.getPath());

                if (Boolean.TRUE.equals(verifyResult.get("valid"))) {
                    // 自动填充版本信息（如果未提供）
                    if (nodeEnv.getVersion() == null || nodeEnv.getVersion().trim().isEmpty()) {
                        nodeEnv.setVersion((String) verifyResult.get("version"));
                    }
                    if (nodeEnv.getJavaHome() == null || nodeEnv.getJavaHome().trim().isEmpty()) {
                        nodeEnv.setJavaHome((String) verifyResult.get("javaHome"));
                    }
                    if (nodeEnv.getBinPath() == null || nodeEnv.getBinPath().trim().isEmpty()) {
                        nodeEnv.setBinPath((String) verifyResult.get("binPath"));
                    }
                    if (nodeEnv.getArch() == null || nodeEnv.getArch().trim().isEmpty()) {
                        nodeEnv.setArch((String) verifyResult.get("arch"));
                    }
                    if (nodeEnv.getSource() == null || nodeEnv.getSource().trim().isEmpty()) {
                        nodeEnv.setSource((String) verifyResult.get("vendor"));
                    }
                    nodeEnv.setValid(1);
                } else {
                    nodeEnv.setValid(0);
                    log.warn("Java环境验证失败: {}", verifyResult.get("error"));
                }
            } catch (Exception e) {
                log.error("验证Java环境时发生错误", e);
                nodeEnv.setValid(0);
            }
        }

        nodeEnv.setCreateTime(DateUtils.getNowDate());
        return nodeEnvMapper.insertNodeEnv(nodeEnv);
    }

    /**
     * 验证节点上的Java环境
     */
    private Map<String, Object> verifyJavaEnvironment(NodeServer nodeServer, String javaPath) {
        try {
            String url = ApiUtil.getJavaEnvVerifyApi(nodeServer);

            JSONObject requestBody = new JSONObject();
            requestBody.put("javaPath", javaPath);

            HttpResponse response = NodeHttpUtil.createPost(nodeServer, url)
                    .body(requestBody.toJSONString())
                    .execute();

            if (response.isOk() && StringUtils.isNotEmpty(response.body())) {
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                return new HashMap<>(jsonObject);
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("valid", false);
                errorResult.put("error", "无响应或请求失败");
                return errorResult;
            }
        } catch (Exception e) {
            log.error("调用节点API验证Java环境失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 修改节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    @Override
    public int updateNodeEnv(NodeEnv nodeEnv) {
        nodeEnv.setUpdateTime(DateUtils.getNowDate());
        return nodeEnvMapper.updateNodeEnv(nodeEnv);
    }

    /**
     * 批量删除节点Java多版本环境管理
     *
     * @param ids 需要删除的节点Java多版本环境管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeEnvByIds(Long[] ids) {
        return nodeEnvMapper.deleteNodeEnvByIds(ids);
    }

    /**
     * 删除节点Java多版本环境管理信息
     *
     * @param id 节点Java多版本环境管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeEnvById(Long id) {
        return nodeEnvMapper.deleteNodeEnvById(id);
    }

    /**
     * 验证Java环境
     */
    @Override
    public AjaxResult verifyEnvironment(NodeEnv nodeEnv) {
        if (nodeEnv.getNodeId() == null) {
            return AjaxResult.error("节点ID不能为空");
        }
        if (nodeEnv.getPath() == null || nodeEnv.getPath().trim().isEmpty()) {
            return AjaxResult.error("Java路径不能为空");
        }

        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeEnv.getNodeId());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            Map<String, Object> result = verifyJavaEnvironment(nodeServer, nodeEnv.getPath());
            return AjaxResult.success("验证完成", result);
        } catch (Exception e) {
            log.error("验证Java环境失败", e);
            return AjaxResult.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 扫描节点上的Java环境
     */
    @Override
    public AjaxResult scanEnvironments(Long nodeId) {
        if (nodeId == null) {
            return AjaxResult.error("节点ID不能为空");
        }

        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeId);
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            String url = ApiUtil.getJavaEnvScanApi(nodeServer);

            HttpResponse response = NodeHttpUtil.createGet(nodeServer, url)
                    .execute();

            if (response.isOk() && StringUtils.isNotEmpty(response.body())) {
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                return AjaxResult.success("扫描完成", jsonObject);
            } else {
                return AjaxResult.error("扫描失败: 无响应或请求失败");
            }
        } catch (Exception e) {
            log.error("扫描Java环境失败", e);
            return AjaxResult.error("扫描失败: " + e.getMessage());
        }
    }
}
