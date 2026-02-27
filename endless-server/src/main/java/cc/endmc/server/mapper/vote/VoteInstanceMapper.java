package cc.endmc.server.mapper.vote;

import cc.endmc.server.domain.vote.VoteInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteInstanceMapper {
    VoteInstance selectVoteInstanceById(Long id);

    List<VoteInstance> selectVoteInstanceList(VoteInstance voteInstance);

    int insertVoteInstance(VoteInstance voteInstance);

    int updateVoteInstance(VoteInstance voteInstance);

    int countOngoingVote(@Param("serverId") Long serverId,
                         @Param("templateCode") String templateCode,
                         @Param("targetPlayerName") String targetPlayerName,
                         @Param("targetWhitelistId") Long targetWhitelistId);
}
