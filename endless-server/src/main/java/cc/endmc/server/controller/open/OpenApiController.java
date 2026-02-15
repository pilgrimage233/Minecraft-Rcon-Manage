package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.request.ChangeIdRequest;
import cc.endmc.server.service.open.IOpenApiService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公共接口
 * 用于提供一些公共的接口
 * 例如: 聚合查询, 获取白名单列表等
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SignVerify(message = "验证失败", rateLimitCount = 50L)
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
    @SignVerify(rateLimitCount = 20L)
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
     * @param param 消息参数 {playerId: 玩家ID, playerName: 玩家名称, message: 消息内容, serverId: 服务器ID, targetGroups: 目标群组（可选，多个群组用逗号分隔）}
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

    /**
     * 申请白名单
     *
     * @param request       请求
     * @param whitelistInfo 白名单信息
     * @param header        请求头
     * @return 结果
     */
    @SneakyThrows
    @PostMapping("/apply")
    public AjaxResult apply(HttpServletRequest request, @RequestBody WhitelistInfo whitelistInfo, @RequestHeader Map<String, String> header) {
        return openApiService.apply(request, whitelistInfo, header);
    }

    /**
     * 验证白名单
     * 此接口不受权限控制!
     *
     * @param request 请求
     * @param code    验证码
     * @param header  请求头
     * @return 结果
     */
    @GetMapping("/verify")
    public AjaxResult verify(HttpServletRequest request, @RequestParam String code, @RequestHeader Map<String, String> header) {
        return openApiService.verify(request, code, header);
    }

    /**
     * 请求更改游戏ID - 第一步：验证信息并发送验证码
     *
     * @param request       请求
     * @param changeRequest 更改请求
     * @return 结果
     */
    @PostMapping("/requestChangeId")
    public AjaxResult requestChangeId(HttpServletRequest request, @RequestBody ChangeIdRequest changeRequest) {
        return openApiService.requestChangeId(request, changeRequest);
    }

    /**
     * 确认更改游戏ID - 第二步：验证验证码并执行更改
     *
     * @param request 请求
     * @param code    验证码
     * @param qqNum   QQ号（用于验证）
     * @return 结果
     */
    @PostMapping("/confirmChangeId")
    public AjaxResult confirmChangeId(HttpServletRequest request,
                                      @RequestParam String code,
                                      @RequestParam String qqNum) {
        return openApiService.confirmChangeId(request, code, qqNum);
    }

    /**
     * 检查白名单信息
     *
     * @param params 参数
     * @return 结果
     */
    @GetMapping("check")
    public AjaxResult check(@RequestParam Map<String, String> params) {

        if (params.isEmpty()) {
            return error("查询信息不能为空!");
        }

        return success(openApiService.check(params));
    }

    /**
     * 根据游戏ID获取服务器信息
     *
     * @param gameId 游戏ID
     * @return 结果
     */
    @GetMapping("/getServerInfoByGameId/{gameId}")
    public AjaxResult getServerInfoByGameId(@PathVariable String gameId) {
        return openApiService.getServerInfoByGameId(gameId);
    }

}