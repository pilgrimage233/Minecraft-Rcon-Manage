package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.service.open.IOpenApiService;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公共接口
 * 用于提供一些公共的接口
 * 例如: 聚合查询, 获取白名单列表等
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SignVerify(message = "API签名验证失败", rateLimitCount = 20L)
public class OpenApiController extends BaseController {

    private final IOpenApiService openApiService;

    /**
     * 聚合查询
     *
     * @return AjaxResult
     */
    @GetMapping("/aggregateQuery")
    public AjaxResult aggregateQuery() {
        return openApiService.aggregateQuery();
    }

    /**
     * 获取服务器白名单列表
     *
     * @return AjaxResult
     */
    @GetMapping("getWhiteListForServer")
    public AjaxResult getWhiteListForServer() {
        return openApiService.getWhiteListForServer();
    }

    /**
     * 获取在线玩家列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getOnlinePlayer")
    public AjaxResult getOnlinePlayer() {
        return openApiService.getOnlinePlayer();
    }

    /**
     * 从数据库获取白名单列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getWhiteList")
    public AjaxResult getWhiteList() {
        return openApiService.getWhiteList();
    }

    /**
     * 获取白名单答题列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getQuestions")
    public AjaxResult getQuestions() {
        return openApiService.getQuestions();
    }

    /**
     * 检查答题状态
     *
     * @param code 验证码
     * @return AjaxResult
     */
    @GetMapping("/checkQuizStatus")
    public AjaxResult checkQuizStatus(@RequestParam String code) {
        return openApiService.checkQuizStatus(code);
    }

    /**
     * 提交答题
     *
     * @param param 答题信息
     * @return AjaxResult
     */
    @PostMapping("/submitQuiz")
    public AjaxResult submitQuiz(@RequestBody JSONObject param) {
        return openApiService.submitWhitelistQuizSubmission(param);
    }

    /**
     * 获取答题详情
     *
     * @param id 答题记录ID
     * @return AjaxResult
     */
    @GetMapping("/getQuizDetail/{id}")
    public AjaxResult getQuizDetail(@PathVariable Long id) {

        if (id == null || id <= 0) {
            return error("参数错误");
        }

        return openApiService.getQuizDetail(id);
    }

    /**
     * 获取服务器状态
     *
     * @return AjaxResult
     */
    @SignVerify(rateLimitCount = 5)
    @GetMapping("/getServerStatus")
    public AjaxResult getServerStatus() {
        return openApiService.getServerStatus();
    }

    /**
     * 获取服务器控制台日志
     *
     * @param serverId 服务器ID
     * @param line     行数
     * @return AjaxResult
     */
    @SignVerify(enabled = false)
    @GetMapping("/getConsole/{serverId}/{line}/logs")
    public AjaxResult getConsole(@PathVariable Integer serverId, @PathVariable Integer line) {
        return openApiService.getConsole(serverId, line);
    }

    /**
     * Minecraft服务器消息推送
     * 用于游戏内消息转发到QQ群
     *
     * @param param 消息参数 {playerId: 玩家ID, playerName: 玩家名称, message: 消息内容, serverId: 服务器ID}
     * @return AjaxResult
     */
    @SignVerify(rateLimitCount = 100L, timestampValidity = 60 * 1000L)
    @PostMapping("/pushMessage")
    public AjaxResult pushMessage(@RequestBody JSONObject param) {
        return openApiService.pushMessage(param);
    }

    /**
     * 获取消息推送队列状态
     *
     * @return AjaxResult
     */
    @SignVerify(rateLimitCount = 10L)
    @GetMapping("/messageQueueStatus")
    public AjaxResult getMessageQueueStatus() {
        return openApiService.getMessageQueueStatus();
    }

}