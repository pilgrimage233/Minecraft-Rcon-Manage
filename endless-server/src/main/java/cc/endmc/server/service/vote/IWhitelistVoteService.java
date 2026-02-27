package cc.endmc.server.service.vote;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.server.domain.vote.VoteInstance;
import cc.endmc.server.domain.vote.VoteTemplate;
import cc.endmc.server.dto.VoteCastRequest;
import cc.endmc.server.dto.VoteCreateRequest;
import cc.endmc.server.dto.VoteTemplateCreateRequest;

import java.util.List;

public interface IWhitelistVoteService {
    List<VoteTemplate> selectEnabledVoteTemplateList();

    List<VoteInstance> selectVoteInstanceList(VoteInstance voteInstance);

    VoteInstance selectVoteInstanceDetail(Long voteId);

    AjaxResult createCustomVoteTemplate(VoteTemplateCreateRequest request, String username);

    AjaxResult createVote(VoteCreateRequest request, Long userId, String username);

    AjaxResult castVote(VoteCastRequest request, Long userId, String username);
}
