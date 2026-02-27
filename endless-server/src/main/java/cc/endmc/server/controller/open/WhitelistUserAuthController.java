package cc.endmc.server.controller.open;

import cc.endmc.common.constant.UserConstants;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.uuid.IdUtils;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.permission.WhitelistUser;
import cc.endmc.server.domain.permission.WhitelistUserPrivacy;
import cc.endmc.server.domain.permission.WhitelistUserSession;
import cc.endmc.server.domain.player.PlayerOnlineRecord;
import cc.endmc.server.mapper.player.PlayerOnlineRecordMapper;
import cc.endmc.server.request.*;
import cc.endmc.server.service.open.IOpenApiService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.permission.IWhitelistUserPrivacyService;
import cc.endmc.server.service.permission.IWhitelistUserService;
import cc.endmc.server.utils.SecureCodeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 白名单用户登录接口
 */
@SignVerify(message = "验证失败", rateLimitCount = 50L)
@RestController
@RequestMapping("/api/v1/whitelist-user")
@RequiredArgsConstructor
public class WhitelistUserAuthController extends BaseController {
    private static final String TOKEN_HEADER = "Whitelist-Token";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10;

    private final RedisCache redisCache;
    private final EmailService emailService;
    private final IWhitelistInfoService whitelistInfoService;
    private final IWhitelistUserService whitelistUserService;
    private final IWhitelistUserPrivacyService whitelistUserPrivacyService;
    private final IOpenApiService openApiService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PlayerOnlineRecordMapper playerOnlineRecordMapper;

    @Value("${token.expireTime:30}")
    private int expireTime;

    @Value("${whitelist.demo.enabled:false}")
    private boolean whitelistDemoEnabled;

    @Value("${whitelist.demo.username:}")
    private String whitelistDemoUsername;

    /**
     * 发送验证码接口
     *
     * @param request 发送验证码请求
     * @return 结果
     */
    @PostMapping("/sendCode")
    public AjaxResult sendCode(@RequestBody WhitelistUserSendCodeRequest request) {
        if (request == null || StringUtils.isEmpty(request.getQqNum())) {
            return error("QQ号不能为空");
        }

        String qqNum = request.getQqNum().trim();
        if (SecureCodeUtil.hasActiveCode(qqNum, CacheKey.WHITELIST_USER_VERIFY_KEY)) {
            return error("验证码已发送，请稍后再试");
        }

        WhitelistInfo whitelistInfo = selectApprovedWhitelist(qqNum);
        if (whitelistInfo == null) {
            return error("该QQ未通过白名单审核");
        }

        String code = SecureCodeUtil.generateNumericCode(
                qqNum,
                CacheKey.WHITELIST_USER_VERIFY_KEY,
                CODE_LENGTH,
                CODE_EXPIRE_MINUTES
        );
        if (StringUtils.isEmpty(code)) {
            return error("验证码生成失败");
        }

        SecureCodeUtil.markActiveCode(qqNum, CacheKey.WHITELIST_USER_VERIFY_KEY, CODE_EXPIRE_MINUTES);
        try {
            emailService.push(qqNum + EmailTemplates.QQ_EMAIL,
                    EmailTemplates.WHITELIST_LOGIN_CODE_TITLE,
                    EmailTemplates.getWhitelistLoginCodeTemplate(code));
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            SecureCodeUtil.deleteCode(code, CacheKey.WHITELIST_USER_VERIFY_KEY);
            SecureCodeUtil.clearActiveCode(qqNum, CacheKey.WHITELIST_USER_VERIFY_KEY);
            return error("验证码发送失败");
        }

        return success("验证码已发送");
    }

    /**
     * 注册或登录接口
     *
     * @param request 注册请求
     * @return 结果
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody WhitelistUserRegisterRequest request) {
        if (request == null) {
            return error("参数不能为空");
        }

        String qqNum = StringUtils.trim(request.getQqNum());
        String code = StringUtils.trim(request.getCode());
        String userName = StringUtils.trim(request.getUserName());
        String password = StringUtils.trim(request.getPassword());

        if (StringUtils.isEmpty(qqNum) || StringUtils.isEmpty(code)
                || StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            return error("参数不能为空");
        }

        if (userName.length() < UserConstants.USERNAME_MIN_LENGTH
                || userName.length() > UserConstants.USERNAME_MAX_LENGTH) {
            return error("账号长度不符合要求");
        }

        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            return error("密码长度不符合要求");
        }

        if (!SecureCodeUtil.verifyCode(code, CacheKey.WHITELIST_USER_VERIFY_KEY, qqNum)) {
            return error("验证码错误或已过期");
        }

        WhitelistInfo whitelistInfo = selectApprovedWhitelist(qqNum);
        if (whitelistInfo == null) {
            return error("该QQ未通过白名单审核");
        }

        WhitelistUser userByName = whitelistUserService.selectWhitelistUserByUserName(userName);
        if (userByName != null && !qqNum.equals(userByName.getQqNum())) {
            return error("账号已被占用");
        }

        WhitelistUser existing = whitelistUserService.selectWhitelistUserByQqNum(qqNum);
        String encodedPassword = passwordEncoder.encode(password);
        if (existing == null) {
            WhitelistUser whitelistUser = new WhitelistUser();
            whitelistUser.setWhitelistId(whitelistInfo.getId());
            whitelistUser.setQqNum(qqNum);
            whitelistUser.setUserName(userName);
            whitelistUser.setPassword(encodedPassword);
            whitelistUser.setStatus(UserConstants.NORMAL);
            whitelistUser.setRoleLevel(1);
            whitelistUser.setRoleTitle("成员");
            whitelistUser.setCanInitiateVote(0);
            whitelistUserService.insertWhitelistUser(whitelistUser);
        } else {
            existing.setWhitelistId(whitelistInfo.getId());
            existing.setUserName(userName);
            existing.setPassword(encodedPassword);
            existing.setStatus(UserConstants.NORMAL);
            if (existing.getRoleLevel() == null) {
                existing.setRoleLevel(1);
            }
            if (StringUtils.isEmpty(existing.getRoleTitle())) {
                existing.setRoleTitle("成员");
            }
            if (existing.getCanInitiateVote() == null) {
                existing.setCanInitiateVote(0);
            }
            whitelistUserService.updateWhitelistUser(existing);
        }

        SecureCodeUtil.deleteCode(code, CacheKey.WHITELIST_USER_VERIFY_KEY);
        SecureCodeUtil.clearActiveCode(qqNum, CacheKey.WHITELIST_USER_VERIFY_KEY);
        return success("设置成功");
    }

    /**
     * 登录接口
     *
     * @param request 登录请求
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody WhitelistUserLoginRequest request) {
        if (request == null || StringUtils.isEmpty(request.getUserName())
                || StringUtils.isEmpty(request.getPassword())) {
            return error("账号或密码不能为空");
        }

        WhitelistUser whitelistUser = whitelistUserService.selectWhitelistUserByUserName(request.getUserName().trim());
        if (whitelistUser == null) {
            return error("账号或密码错误");
        }

        if (UserConstants.USER_DISABLE.equals(whitelistUser.getStatus())) {
            return error("账号已停用");
        }

        if (!passwordEncoder.matches(request.getPassword(), whitelistUser.getPassword())) {
            return error("账号或密码错误");
        }

        String token = IdUtils.fastUUID();
        long loginTime = System.currentTimeMillis();
        long expireAt = loginTime + TimeUnit.MINUTES.toMillis(expireTime);

        WhitelistUserSession session = new WhitelistUserSession();
        session.setUserId(whitelistUser.getId());
        session.setWhitelistId(whitelistUser.getWhitelistId());
        session.setUserName(whitelistUser.getUserName());
        session.setQqNum(whitelistUser.getQqNum());
        Integer roleLevel = whitelistUser.getRoleLevel();
        session.setRoleLevel(roleLevel == null ? 1 : roleLevel);
        session.setRoleTitle(StringUtils.isEmpty(whitelistUser.getRoleTitle()) ? "成员" : whitelistUser.getRoleTitle());
        Integer canInitiateVote = whitelistUser.getCanInitiateVote();
        session.setCanInitiateVote(canInitiateVote == null ? 0 : canInitiateVote);
        session.setToken(token);
        session.setLoginTime(loginTime);
        session.setExpireTime(expireAt);

        redisCache.setCacheObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token, session, expireTime, TimeUnit.MINUTES);
        whitelistUserService.updateWhitelistUserLoginTime(whitelistUser.getId(), new Date());

        return Objects.requireNonNull(AjaxResult.success()
                        .put("token", token))
                .put("expireTime", expireTime);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/me")
    public AjaxResult me(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return error("未登录");
        }

        WhitelistUserSession session = redisCache.getCacheObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token);
        if (session == null) {
            return error("登录已过期");
        }

        return success(session);
    }

    /**
     * 获取用户资料
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/profile")
    public AjaxResult profile(HttpServletRequest request) {
        WhitelistUserSession session = getSessionFromToken(request);
        if (session == null) {
            return error("未登录");
        }

        String gameId = null;
        if (session.getWhitelistId() != null) {
            WhitelistInfo whitelistInfo = whitelistInfoService.selectWhitelistInfoById(session.getWhitelistId());
            if (whitelistInfo != null && StringUtils.isNotEmpty(whitelistInfo.getUserName())) {
                gameId = whitelistInfo.getUserName();
            }
        }

        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(gameId)) {
            params.put("id", gameId);
        } else if (StringUtils.isNotEmpty(session.getUserName())) {
            params.put("id", session.getUserName());
        } else if (StringUtils.isNotEmpty(session.getQqNum())) {
            params.put("qq", session.getQqNum());
        }

        Map<String, Object> checkInfo = params.isEmpty()
                ? new HashMap<>()
                : whitelistInfoService.check(params, false);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", session.getUserId());
        result.put("whitelistId", session.getWhitelistId());
        result.put("userName", session.getUserName());
        result.put("gameId", gameId);
        result.put("qqNum", session.getQqNum());
        result.put("roleLevel", session.getRoleLevel());
        result.put("roleTitle", session.getRoleTitle());
        result.put("canInitiateVote", session.getCanInitiateVote());
        result.put("token", session.getToken());
        result.put("loginTime", session.getLoginTime());
        result.put("expireTime", session.getExpireTime());
        result.put("checkInfo", checkInfo);

        String recordUserName = gameId;
        if (gameId != null && !gameId.isEmpty()) {
            recordUserName = gameId.toLowerCase();
        }
        List<PlayerOnlineRecord> records = playerOnlineRecordMapper
                .selectRecentRecords(session.getWhitelistId(), recordUserName, 10);
        result.put("onlineRecords", records);

        return success(result);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return error("未登录");
        }

        redisCache.deleteObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token);
        return success("已退出");
    }

    /**
     * 更改密码
     *
     * @param request 请求
     * @param body    更改密码请求
     * @return 结果
     */
    @PostMapping("/changePassword")
    public AjaxResult changePassword(HttpServletRequest request,
                                     @RequestBody WhitelistUserChangePasswordRequest body) {
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return error("未登录");
        }
        if (body == null || StringUtils.isEmpty(body.getOldPassword()) || StringUtils.isEmpty(body.getNewPassword())) {
            return error("参数不能为空");
        }
        if (body.getNewPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || body.getNewPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            return error("密码长度不符合要求");
        }

        WhitelistUserSession session = redisCache.getCacheObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token);
        if (session == null) {
            return error("登录已过期");
        }

        WhitelistUser whitelistUser = whitelistUserService.selectWhitelistUserById(session.getUserId());
        if (whitelistUser == null) {
            return error("用户不存在");
        }
        if (isDemoWhitelistUser(whitelistUser)) {
            return error("演示账户不允许修改密码");
        }
        if (!passwordEncoder.matches(body.getOldPassword(), whitelistUser.getPassword())) {
            return error("原密码错误");
        }

        whitelistUser.setPassword(passwordEncoder.encode(body.getNewPassword()));
        whitelistUserService.updateWhitelistUser(whitelistUser);
        return success("密码修改成功");
    }

    private boolean isDemoWhitelistUser(WhitelistUser whitelistUser) {
        if (!whitelistDemoEnabled || whitelistUser == null || StringUtils.isEmpty(whitelistDemoUsername)) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(StringUtils.trim(whitelistUser.getUserName()), StringUtils.trim(whitelistDemoUsername));
    }

    /**
     * 请求更改游戏ID
     *
     * @param request 请求
     * @param body    更改请求
     * @return 结果
     */
    @PostMapping("/changeGameId")
    public AjaxResult changeGameId(HttpServletRequest request,
                                   @RequestBody WhitelistUserChangeGameIdRequest body) {
        WhitelistUserSession session = getSessionFromToken(request);
        if (session == null) {
            return error("未登录");
        }
        if (body == null || StringUtils.isEmpty(body.getNewUserName())) {
            return error("参数不能为空");
        }
        if (session.getWhitelistId() == null) {
            return error("未绑定白名单信息");
        }

        return openApiService.changeIdForWhitelistUser(
                request,
                session.getWhitelistId(),
                body.getNewUserName(),
                body.getChangeReason(),
                session.getQqNum(),
                session.getUserName()
        );
    }

    /**
     * 获取隐私设置
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/privacy")
    public AjaxResult getPrivacy(HttpServletRequest request) {
        WhitelistUserSession session = getSessionFromToken(request);
        if (session == null) {
            return error("未登录");
        }

        WhitelistUserPrivacy privacy = whitelistUserPrivacyService
                .selectWhitelistUserPrivacyByWhitelistId(session.getWhitelistId());
        if (privacy == null) {
            privacy = buildDefaultPrivacy(session.getWhitelistId());
        }
        return success(privacy);
    }

    /**
     * 更新隐私设置
     *
     * @param request 请求
     * @param body    隐私设置更新请求
     * @return 结果
     */
    @PostMapping("/privacy")
    public AjaxResult updatePrivacy(HttpServletRequest request,
                                    @RequestBody WhitelistUserPrivacyUpdateRequest body) {
        WhitelistUserSession session = getSessionFromToken(request);
        if (session == null) {
            return error("未登录");
        }
        if (body == null) {
            return error("参数不能为空");
        }

        WhitelistUserPrivacy privacy = whitelistUserPrivacyService
                .selectWhitelistUserPrivacyByWhitelistId(session.getWhitelistId());
        if (privacy == null) {
            privacy = buildDefaultPrivacy(session.getWhitelistId());
            applyPrivacyUpdate(privacy, body);
            whitelistUserPrivacyService.insertWhitelistUserPrivacy(privacy);
        } else {
            applyPrivacyUpdate(privacy, body);
            whitelistUserPrivacyService.updateWhitelistUserPrivacy(privacy);
        }

        clearPlayerInfoCache(session.getWhitelistId());
        return success("更新成功");
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }
        return token;
    }

    private WhitelistUserSession getSessionFromToken(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return redisCache.getCacheObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token);
    }

    private WhitelistUserPrivacy buildDefaultPrivacy(Long whitelistId) {
        WhitelistUserPrivacy privacy = new WhitelistUserPrivacy();
        privacy.setWhitelistId(whitelistId);
        privacy.setShowQq(1);
        privacy.setShowCity(1);
        privacy.setShowLastOnline(1);
        privacy.setShowGameTime(1);
        privacy.setShowNameHistory(1);
        privacy.setShowQuizResult(1);
        privacy.setShowUuid(1);
        return privacy;
    }

    private void applyPrivacyUpdate(WhitelistUserPrivacy privacy, WhitelistUserPrivacyUpdateRequest body) {
        if (body.getShowQq() != null) {
            privacy.setShowQq(body.getShowQq());
        }
        if (body.getShowCity() != null) {
            privacy.setShowCity(body.getShowCity());
        }
        if (body.getShowLastOnline() != null) {
            privacy.setShowLastOnline(body.getShowLastOnline());
        }
        if (body.getShowGameTime() != null) {
            privacy.setShowGameTime(body.getShowGameTime());
        }
        if (body.getShowNameHistory() != null) {
            privacy.setShowNameHistory(body.getShowNameHistory());
        }
        if (body.getShowQuizResult() != null) {
            privacy.setShowQuizResult(body.getShowQuizResult());
        }
        if (body.getShowUuid() != null) {
            privacy.setShowUuid(body.getShowUuid());
        }
    }

    private void clearPlayerInfoCache(Long whitelistId) {
        if (whitelistId == null) {
            return;
        }
        WhitelistInfo whitelistInfo = whitelistInfoService.selectWhitelistInfoById(whitelistId);
        if (whitelistInfo == null) {
            return;
        }
        if (StringUtils.isNotEmpty(whitelistInfo.getUserName())) {
            redisCache.deleteObject(CacheKey.PLAYER_INFO_KEY + whitelistInfo.getUserName().toLowerCase());
        }
        if (StringUtils.isNotEmpty(whitelistInfo.getQqNum())) {
            redisCache.deleteObject(CacheKey.PLAYER_INFO_KEY + whitelistInfo.getQqNum());
        }
    }

    private WhitelistInfo selectApprovedWhitelist(String qqNum) {
        WhitelistInfo query = new WhitelistInfo();
        query.setQqNum(qqNum);
        query.setStatus("1");
        List<WhitelistInfo> list = whitelistInfoService.selectWhitelistInfoList(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }
}
