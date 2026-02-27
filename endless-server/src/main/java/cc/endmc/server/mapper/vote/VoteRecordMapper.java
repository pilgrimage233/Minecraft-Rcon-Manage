package cc.endmc.server.mapper.vote;

import cc.endmc.server.domain.vote.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteRecordMapper {
    int insertVoteRecord(VoteRecord voteRecord);

    int countVotedByUser(@Param("voteId") Long voteId, @Param("voterUserName") String voterUserName);

    Integer countAgreeVotes(Long voteId);

    Integer countRejectVotes(Long voteId);

    List<VoteRecord> selectVoteRecordListByVoteId(Long voteId);
}
