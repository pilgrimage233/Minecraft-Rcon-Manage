package cc.endmc.server.service.open;


import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.request.ChangeIdRequest;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

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

    /**
     * 申请白名单
     *
     * @param request       请求
     * @param whitelistInfo 白名单信息
     * @param header        请求头
     * @return 结果
     */
    AjaxResult apply(HttpServletRequest request, WhitelistInfo whitelistInfo, Map<String, String> header);

    /**
     * 验证白名单
     *
     * @param request 请求
     * @param code    验证码
     * @param header  请求头
     * @return 结果
     */
    AjaxResult verify(HttpServletRequest request, String code, Map<String, String> header);

    /**
     * 请求更改游戏ID
     *
     * @param request       请求
     * @param changeRequest 更改请求
     * @return 结果
     */
    AjaxResult requestChangeId(HttpServletRequest request, ChangeIdRequest changeRequest);

    /**
     * 确认更改游戏ID
     *
     * @param request 请求
     * @param code    验证码
     * @param qqNum   QQ号
     * @return 结果
     */
    AjaxResult confirmChangeId(HttpServletRequest request, String code, String qqNum);

    /**
     * 已登录白名单用户更改游戏ID
     *
     * @param request      请求
     * @param whitelistId  白名单ID
     * @param newUserName  新游戏ID
     * @param changeReason 更改原因
     * @param qqNum        QQ号
     * @param operatorName 操作人
     * @return 结果
     */
    AjaxResult changeIdForWhitelistUser(HttpServletRequest request,
                                        Long whitelistId,
                                        String newUserName,
                                        String changeReason,
                                        String qqNum,
                                        String operatorName);

    /**
     * 获取玩家白名单信息
     *
     * @param params 参数
     * @return 结果
     */
    Map<String, Object> check(Map<String, String> params);

    /**
     * 根据游戏ID获取服务器信息
     *
     * @param gameId 游戏ID
     * @return 结果
     */
    AjaxResult getServerInfoByGameId(String gameId);
}
