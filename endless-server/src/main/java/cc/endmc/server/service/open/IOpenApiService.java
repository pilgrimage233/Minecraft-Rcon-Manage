package cc.endmc.server.service.open;


import cc.endmc.common.core.domain.AjaxResult;
import com.alibaba.fastjson2.JSONObject;

/**
 * Open API 接口服务
 *
 * @author Memory
 * @date 2026-1-3
 */
public interface IOpenApiService {

    /**
     * 提交白名单问卷
     *
     * @param params 参数
     * @return 结果
     */
    AjaxResult submitWhitelistQuizSubmission(JSONObject params);

    /**
     * 获取白名单列表
     *
     * @return 结果
     */
    AjaxResult getWhiteList();

    /**
     * 获取问卷题目
     *
     * @return 结果
     */
    AjaxResult getQuestions();

    /**
     * 获取问卷详情
     *
     * @return 结果
     */
    AjaxResult getQuizDetail(Long id);

    /**
     * 聚合查询
     *
     * @return 结果
     */
    AjaxResult aggregateQuery();

    /**
     * 从服务器获取白名单列表
     *
     * @return 结果
     */
    AjaxResult getWhiteListForServer();

    /**
     * 获取在线玩家信息
     *
     * @return 结果
     */
    AjaxResult getOnlinePlayer();

    /**
     * 检查答题状态
     *
     * @param code 验证码
     * @return 结果
     */
    AjaxResult checkQuizStatus(String code);

    /**
     * 获取服务器状态
     *
     * @return 结果
     */
    AjaxResult getServerStatus();

    /**
     * 获取控制台日志
     *
     * @return 结果
     */
    AjaxResult getConsole(Integer id, Integer lines);

    /**
     * Minecraft服务器消息推送
     * 用于游戏内消息转发到QQ群
     *
     * @param param 消息参数
     * @return 结果
     */
    AjaxResult pushMessage(JSONObject param);

    /**
     * 获取消息推送队列状态
     *
     * @return 队列状态信息
     */
    AjaxResult getMessageQueueStatus();
}
