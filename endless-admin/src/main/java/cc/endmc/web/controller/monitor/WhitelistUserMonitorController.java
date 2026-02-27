package cc.endmc.web.controller.monitor;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.domain.permission.WhitelistUser;
import cc.endmc.server.domain.permission.WhitelistUserSession;
import cc.endmc.server.request.WhitelistUserRoleUpdateRequest;
import cc.endmc.server.service.permission.IWhitelistUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 白名单用户登录监控
 */
@RestController
@RequestMapping("/monitor/whitelist-user")
public class WhitelistUserMonitorController extends BaseController {
    private final RedisCache redisCache;
    private final IWhitelistUserService whitelistUserService;

    public WhitelistUserMonitorController(RedisCache redisCache, IWhitelistUserService whitelistUserService) {
        this.redisCache = redisCache;
        this.whitelistUserService = whitelistUserService;
    }

    @PreAuthorize("@ss.hasPermi('monitor:whitelist-user:list')")
    @GetMapping("/list")
    public TableDataInfo list(String qqNum, String userName) {
        Collection<String> keys = redisCache.keys(CacheKey.WHITELIST_USER_TOKEN_KEY + "*");
        List<WhitelistUserSession> sessions = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                WhitelistUserSession session = redisCache.getCacheObject(key);
                if (session == null) {
                    continue;
                }
                if (StringUtils.isNotEmpty(qqNum) && !qqNum.equals(session.getQqNum())) {
                    continue;
                }
                if (StringUtils.isNotEmpty(userName) && !userName.equalsIgnoreCase(session.getUserName())) {
                    continue;
                }
                sessions.add(session);
            }
        }
        sessions.sort(Comparator.comparing(WhitelistUserSession::getLoginTime).reversed());
        return getDataTable(sessions);
    }

    @PreAuthorize("@ss.hasPermi('monitor:whitelist-user:list')")
    @GetMapping("/summary")
    public AjaxResult summary() {
        Collection<String> keys = redisCache.keys(CacheKey.WHITELIST_USER_TOKEN_KEY + "*");
        int onlineCount = 0;
        if (keys != null) {
            for (String key : keys) {
                if (redisCache.getCacheObject(key) != null) {
                    onlineCount++;
                }
            }
        }
        long registeredCount = whitelistUserService.countWhitelistUsers();
        Map<String, Object> data = new HashMap<>();
        data.put("registeredCount", registeredCount);
        data.put("onlineCount", onlineCount);
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('monitor:whitelist-user:list')")
    @GetMapping("/registered/list")
    public TableDataInfo registeredList(WhitelistUser whitelistUser) {
        startPage();
        List<WhitelistUser> list = whitelistUserService.selectWhitelistUserList(whitelistUser);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:whitelist-user:role')")
    @Log(title = "白名单用户角色", businessType = BusinessType.UPDATE)
    @PutMapping("/role")
    public AjaxResult updateRole(@RequestBody WhitelistUserRoleUpdateRequest request) {
        if (request == null || request.getUserId() == null) {
            return error("用户ID不能为空");
        }
        if (request.getRoleLevel() == null || request.getRoleLevel() < 1) {
            return error("等级必须大于0");
        }
        String roleTitle = StringUtils.trim(request.getRoleTitle());
        if (StringUtils.isEmpty(roleTitle)) {
            return error("头衔不能为空");
        }
        int canInitiate = request.getCanInitiateVote() != null && request.getCanInitiateVote() == 1 ? 1 : 0;
        int rows = whitelistUserService.updateWhitelistUserRole(
                request.getUserId(),
                request.getRoleLevel(),
                roleTitle,
                canInitiate,
                getUsername()
        );
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('monitor:whitelist-user:forceLogout')")
    @Log(title = "白名单用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{token}")
    public AjaxResult forceLogout(@PathVariable String token) {
        redisCache.deleteObject(CacheKey.WHITELIST_USER_TOKEN_KEY + token);
        return success();
    }
}
