package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.domain.permission.WhitelistUserSession;
import cc.endmc.server.domain.vote.VoteInstance;
import cc.endmc.server.dto.VoteCastRequest;
import cc.endmc.server.dto.VoteCreateRequest;
import cc.endmc.server.service.vote.IWhitelistVoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SignVerify(message = "验证失败", rateLimitCount = 100L)
@RestController
@RequestMapping("/api/v1/whitelist-user/vote")
@RequiredArgsConstructor
public class WhitelistUserVoteController extends BaseController {
    private static final String TOKEN_HEADER = "Whitelist-Token";

    private final RedisCache redisCache;
    private final IWhitelistVoteService whitelistVoteService;

    @GetMapping("/template/list")
    public AjaxResult templateList() {
        return success(whitelistVoteService.selectEnabledVoteTemplateList());
    }

    @GetMapping("/list")
    public TableDataInfo list(VoteInstance voteInstance) {
        startPage();
        List<VoteInstance> list = whitelistVoteService.selectVoteInstanceList(voteInstance);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable("id") Long id) {
        return success(whitelistVoteService.selectVoteInstanceDetail(id));
    }

    @PostMapping("/create")
    public AjaxResult create(@RequestBody VoteCreateRequest request, HttpServletRequest httpServletRequest) {
        WhitelistUserSession session = getSessionFromToken(httpServletRequest);
        if (session == null) {
            return error("未登录");
        }
        if (session.getCanInitiateVote() == null || session.getCanInitiateVote() != 1) {
            return error("当前头衔权限不足，仅代表成员及以上可发起投票");
        }
        return whitelistVoteService.createVote(request, session.getUserId(), session.getUserName());
    }

    @PostMapping("/cast")
    public AjaxResult cast(@RequestBody VoteCastRequest request, HttpServletRequest httpServletRequest) {
        WhitelistUserSession session = getSessionFromToken(httpServletRequest);
        if (session == null) {
            return error("未登录");
        }
        return whitelistVoteService.castVote(request, session.getUserId(), session.getUserName());
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
}
