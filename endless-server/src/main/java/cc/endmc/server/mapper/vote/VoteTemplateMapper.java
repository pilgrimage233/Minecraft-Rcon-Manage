package cc.endmc.server.mapper.vote;

import cc.endmc.server.domain.vote.VoteTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VoteTemplateMapper {
    int insertVoteTemplate(VoteTemplate voteTemplate);

    VoteTemplate selectVoteTemplateById(Long id);

    VoteTemplate selectVoteTemplateByCode(String templateCode);

    List<VoteTemplate> selectEnabledVoteTemplateList();
}
