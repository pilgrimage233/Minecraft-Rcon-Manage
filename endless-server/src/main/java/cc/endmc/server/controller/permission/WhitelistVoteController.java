package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.server.domain.vote.VoteInstance;
import cc.endmc.server.dto.VoteCastRequest;
import cc.endmc.server.dto.VoteCreateRequest;
import cc.endmc.server.dto.VoteTemplateCreateRequest;
import cc.endmc.server.service.vote.IWhitelistVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mc/whitelist/vote")
@RequiredArgsConstructor
public class WhitelistVoteController extends BaseController {

    private final IWhitelistVoteService whitelistVoteService;

    /**
     * 查询启用的投票模板
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:list')")
    @GetMapping("/template/list")
    public AjaxResult templateList() {
        return success(whitelistVoteService.selectEnabledVoteTemplateList());
    }

    /**
     * 新增自定义投票模板
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:template:add')")
    @Log(title = "白名单投票模板", businessType = BusinessType.INSERT)
    @PostMapping("/template/custom")
    public AjaxResult createCustomTemplate(@RequestBody VoteTemplateCreateRequest request) {
        return whitelistVoteService.createCustomVoteTemplate(request, getUsername());
    }

    /**
     * 查询投票列表
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:list')")
    @GetMapping("/list")
    public TableDataInfo list(VoteInstance voteInstance) {
        startPage();
        List<VoteInstance> list = whitelistVoteService.selectVoteInstanceList(voteInstance);
        return getDataTable(list);
    }

    /**
     * 查询投票详情
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:list')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable("id") Long id) {
        return success(whitelistVoteService.selectVoteInstanceDetail(id));
    }

    /**
     * 发起投票
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:create')")
    @Log(title = "白名单投票", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult create(@RequestBody VoteCreateRequest request) {
        return whitelistVoteService.createVote(request, getUserId(), getUsername());
    }

    /**
     * 跟投
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:vote:cast')")
    @Log(title = "白名单投票", businessType = BusinessType.UPDATE)
    @PostMapping("/cast")
    public AjaxResult cast(@RequestBody VoteCastRequest request) {
        return whitelistVoteService.castVote(request, getUserId(), getUsername());
    }
}
