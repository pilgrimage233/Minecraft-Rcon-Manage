package cc.endmc.server.service.vote.impl;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.vote.VoteInstance;
import cc.endmc.server.domain.vote.VoteRecord;
import cc.endmc.server.domain.vote.VoteTemplate;
import cc.endmc.server.dto.VoteCastRequest;
import cc.endmc.server.dto.VoteCreateRequest;
import cc.endmc.server.dto.VoteTemplateCreateRequest;
import cc.endmc.server.mapper.permission.BanlistInfoMapper;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.mapper.vote.VoteInstanceMapper;
import cc.endmc.server.mapper.vote.VoteRecordMapper;
import cc.endmc.server.mapper.vote.VoteTemplateMapper;
import cc.endmc.server.service.vote.IWhitelistVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WhitelistVoteServiceImpl implements IWhitelistVoteService {

    private final VoteTemplateMapper voteTemplateMapper;
    private final VoteInstanceMapper voteInstanceMapper;
    private final VoteRecordMapper voteRecordMapper;
    private final WhitelistInfoMapper whitelistInfoMapper;
    private final BanlistInfoMapper banlistInfoMapper;
    private final RconService rconService;

    @Override
    public List<VoteTemplate> selectEnabledVoteTemplateList() {
        return voteTemplateMapper.selectEnabledVoteTemplateList();
    }

    @Override
    public List<VoteInstance> selectVoteInstanceList(VoteInstance voteInstance) {
        return voteInstanceMapper.selectVoteInstanceList(voteInstance);
    }

    @Override
    public VoteInstance selectVoteInstanceDetail(Long voteId) {
        VoteInstance voteInstance = voteInstanceMapper.selectVoteInstanceById(voteId);
        if (voteInstance != null) {
            voteInstance.setVoteRecords(voteRecordMapper.selectVoteRecordListByVoteId(voteId));
        }
        return voteInstance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createCustomVoteTemplate(VoteTemplateCreateRequest request, String username) {
        if (request == null) {
            return AjaxResult.error("请求参数不能为空");
        }

        String templateName = StringUtils.trim(request.getTemplateName());
        String commandTemplate = StringUtils.trim(request.getActionCommandTemplate());
        if (StringUtils.isBlank(templateName)) {
            return AjaxResult.error("投票名称不能为空");
        }
        if (StringUtils.isBlank(commandTemplate)) {
            return AjaxResult.error("执行命令不能为空");
        }

        Integer requiredVotesValue = request.getMinRequiredVotes();
        int requiredVotes = requiredVotesValue == null ? 3 : requiredVotesValue;
        if (requiredVotes < 1) {
            requiredVotes = 1;
        }

        Integer durationSecondsValue = request.getVoteDurationSeconds();
        int durationSeconds = durationSecondsValue == null ? 300 : durationSecondsValue;
        if (durationSeconds < 30) {
            durationSeconds = 30;
        }

        String targetType = StringUtils.trim(request.getTargetType());
        if (StringUtils.isBlank(targetType)) {
            targetType = "OTHER";
        }

        VoteTemplate voteTemplate = new VoteTemplate();
        voteTemplate.setTemplateCode("CUSTOM_" + System.currentTimeMillis());
        voteTemplate.setTemplateName(templateName);
        voteTemplate.setTemplateDesc(StringUtils.trim(request.getTemplateDesc()));
        voteTemplate.setTargetType(targetType.toUpperCase());
        voteTemplate.setActionType("RCON_COMMAND");
        voteTemplate.setActionCommandTemplate(commandTemplate);
        voteTemplate.setMinRequiredVotes(requiredVotes);
        voteTemplate.setVoteDurationSeconds(durationSeconds);
        voteTemplate.setNeedReason(request.getNeedReason() != null && request.getNeedReason() == 1 ? 1 : 0);
        voteTemplate.setEnabled(1);
        voteTemplate.setSortOrder(1000);
        voteTemplate.setCreateBy(username);
        voteTemplate.setRemark("后台自定义投票模板");

        voteTemplateMapper.insertVoteTemplate(voteTemplate);
        return AjaxResult.success(voteTemplate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createVote(VoteCreateRequest request, Long userId, String username) {
        if (request == null) {
            return AjaxResult.error("请求参数不能为空");
        }

        VoteTemplate template = resolveTemplate(request);
        if (template == null) {
            return AjaxResult.error("投票模板不存在");
        }
        if (template.getEnabled() == null || template.getEnabled() != 1) {
            return AjaxResult.error("该投票模板未启用");
        }

        String targetPlayerName = StringUtils.trim(request.getTargetPlayerName());
        Long targetWhitelistId = request.getTargetWhitelistId();

        if (targetWhitelistId != null && StringUtils.isBlank(targetPlayerName)) {
            WhitelistInfo whitelistInfo = whitelistInfoMapper.selectWhitelistInfoById(targetWhitelistId);
            if (whitelistInfo != null) {
                targetPlayerName = whitelistInfo.getUserName();
            }
        }

        boolean requireTarget = !"OTHER".equalsIgnoreCase(StringUtils.trim(template.getTargetType()));
        if (requireTarget && StringUtils.isBlank(targetPlayerName) && targetWhitelistId == null && StringUtils.isBlank(request.getTargetRef())) {
            return AjaxResult.error("投票目标不能为空");
        }

        if (template.getNeedReason() != null && template.getNeedReason() == 1 && StringUtils.isBlank(request.getReason())) {
            return AjaxResult.error("该投票必须填写发起原因");
        }

        int ongoingCount = voteInstanceMapper.countOngoingVote(request.getServerId(), template.getTemplateCode(), targetPlayerName, targetWhitelistId);
        if (ongoingCount > 0) {
            return AjaxResult.error("该目标已有进行中的同类投票");
        }

        Date now = new Date();
        Integer durationConfig = template.getVoteDurationSeconds();
        int durationSeconds = (durationConfig == null || durationConfig <= 0)
                ? 300 : durationConfig;
        Integer requiredConfig = template.getMinRequiredVotes();
        int requiredVotes = (requiredConfig == null || requiredConfig <= 0)
                ? 1 : requiredConfig;

        VoteInstance voteInstance = new VoteInstance();
        voteInstance.setTemplateId(template.getId());
        voteInstance.setTemplateCode(template.getTemplateCode());
        voteInstance.setTemplateName(template.getTemplateName());
        voteInstance.setServerId(request.getServerId());
        voteInstance.setTargetType(template.getTargetType());
        voteInstance.setTargetPlayerName(targetPlayerName);
        voteInstance.setTargetWhitelistId(targetWhitelistId);
        voteInstance.setTargetRef(request.getTargetRef());
        voteInstance.setInitiatorUserId(userId);
        voteInstance.setInitiatorUserName(username);
        voteInstance.setRequiredVotes(requiredVotes);
        voteInstance.setAgreeVotes(1);
        voteInstance.setRejectVotes(0);
        voteInstance.setStatus(requiredVotes <= 1 ? "PASSED" : "ONGOING");
        voteInstance.setExpireTime(new Date(now.getTime() + durationSeconds * 1000L));
        voteInstance.setFinishedTime(requiredVotes <= 1 ? now : null);
        voteInstance.setExecuteStatus("PENDING");
        voteInstance.setReason(request.getReason());
        voteInstance.setExtraContext(request.getExtraContext());

        voteInstanceMapper.insertVoteInstance(voteInstance);

        VoteRecord initiatorRecord = new VoteRecord();
        initiatorRecord.setVoteId(voteInstance.getId());
        initiatorRecord.setVoterUserId(userId);
        initiatorRecord.setVoterUserName(username);
        initiatorRecord.setVoteDecision(1);
        initiatorRecord.setVoteComment("发起投票");
        voteRecordMapper.insertVoteRecord(initiatorRecord);

        if ("PASSED".equals(voteInstance.getStatus())) {
            executeVoteAction(voteInstance, template, username);
        }

        return AjaxResult.success(voteInstance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult castVote(VoteCastRequest request, Long userId, String username) {
        if (request == null || request.getVoteId() == null) {
            return AjaxResult.error("投票ID不能为空");
        }
        if (request.getVoteDecision() == null || (request.getVoteDecision() != 1 && request.getVoteDecision() != 2)) {
            return AjaxResult.error("投票选项无效");
        }

        VoteInstance voteInstance = voteInstanceMapper.selectVoteInstanceById(request.getVoteId());
        if (voteInstance == null) {
            return AjaxResult.error("投票不存在");
        }

        Date now = new Date();
        if (voteInstance.getExpireTime() != null && voteInstance.getExpireTime().before(now) && "ONGOING".equals(voteInstance.getStatus())) {
            voteInstance.setStatus("EXPIRED");
            voteInstance.setFinishedTime(now);
            voteInstanceMapper.updateVoteInstance(voteInstance);
            return AjaxResult.error("该投票已过期");
        }

        if (!"ONGOING".equals(voteInstance.getStatus())) {
            return AjaxResult.error("该投票已结束，无法继续投票");
        }

        int votedCount = voteRecordMapper.countVotedByUser(request.getVoteId(), username);
        if (votedCount > 0) {
            return AjaxResult.error("你已投过票");
        }

        VoteRecord voteRecord = new VoteRecord();
        voteRecord.setVoteId(request.getVoteId());
        voteRecord.setVoterUserId(userId);
        voteRecord.setVoterUserName(username);
        voteRecord.setVoteDecision(request.getVoteDecision());
        voteRecord.setVoteComment(request.getVoteComment());
        voteRecordMapper.insertVoteRecord(voteRecord);

        Integer agreeVotes = voteRecordMapper.countAgreeVotes(request.getVoteId());
        Integer rejectVotes = voteRecordMapper.countRejectVotes(request.getVoteId());

        voteInstance.setAgreeVotes(agreeVotes == null ? 0 : agreeVotes);
        voteInstance.setRejectVotes(rejectVotes == null ? 0 : rejectVotes);

        if (voteInstance.getAgreeVotes() >= voteInstance.getRequiredVotes()) {
            voteInstance.setStatus("PASSED");
            voteInstance.setFinishedTime(now);
        } else if (voteInstance.getRejectVotes() >= voteInstance.getRequiredVotes()) {
            voteInstance.setStatus("REJECTED");
            voteInstance.setFinishedTime(now);
        }

        voteInstanceMapper.updateVoteInstance(voteInstance);

        if ("PASSED".equals(voteInstance.getStatus())) {
            VoteTemplate template = voteTemplateMapper.selectVoteTemplateById(voteInstance.getTemplateId());
            executeVoteAction(voteInstance, template, username);
        }

        VoteInstance latest = voteInstanceMapper.selectVoteInstanceById(request.getVoteId());
        latest.setVoteRecords(voteRecordMapper.selectVoteRecordListByVoteId(request.getVoteId()));
        return AjaxResult.success(latest);
    }

    private void executeVoteAction(VoteInstance voteInstance, VoteTemplate template, String operator) {
        if (voteInstance == null || template == null) {
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(voteInstance.getExecuteStatus())) {
            return;
        }

        String actionType = StringUtils.trim(template.getActionType());
        if (!"RCON_COMMAND".equalsIgnoreCase(actionType)) {
            voteInstance.setExecuteStatus("SKIPPED");
            voteInstance.setExecuteResult("非RCON动作，已跳过执行");
            voteInstanceMapper.updateVoteInstance(voteInstance);
            return;
        }

        String commandTemplate = StringUtils.trim(template.getActionCommandTemplate());
        if (StringUtils.isBlank(commandTemplate)) {
            voteInstance.setExecuteStatus("SKIPPED");
            voteInstance.setExecuteResult("未配置执行命令模板");
            voteInstanceMapper.updateVoteInstance(voteInstance);
            return;
        }

        String command = renderCommand(commandTemplate, voteInstance);
        String serverKey = voteInstance.getServerId() == null ? "all" : String.valueOf(voteInstance.getServerId());

        try {
            String commandResult = rconService.sendCommand(serverKey, command, true, voteInstance.getReason());
            voteInstance.setExecuteStatus("SUCCESS");
            voteInstance.setExecuteResult(StringUtils.isBlank(commandResult) ? "执行成功" : commandResult);
            voteInstanceMapper.updateVoteInstance(voteInstance);

            applyBusinessSideEffects(voteInstance, operator);
        } catch (Exception ex) {
            voteInstance.setExecuteStatus("FAILED");
            voteInstance.setExecuteResult("执行失败: " + ex.getMessage());
            voteInstanceMapper.updateVoteInstance(voteInstance);
        }
    }

    private String renderCommand(String commandTemplate, VoteInstance voteInstance) {
        String targetPlayer = StringUtils.trim(voteInstance.getTargetPlayerName());
        String reason = StringUtils.trim(voteInstance.getReason());

        if (StringUtils.isBlank(targetPlayer) && voteInstance.getTargetWhitelistId() != null) {
            WhitelistInfo whitelistInfo = whitelistInfoMapper.selectWhitelistInfoById(voteInstance.getTargetWhitelistId());
            if (whitelistInfo != null) {
                targetPlayer = whitelistInfo.getUserName();
            }
        }

        if (StringUtils.isBlank(reason)) {
            reason = "vote passed";
        }

        return commandTemplate
                .replace("{targetPlayer}", StringUtils.nvl(targetPlayer, ""))
                .replace("{reason}", reason)
                .replace("{targetRef}", StringUtils.nvl(voteInstance.getTargetRef(), ""));
    }

    private void applyBusinessSideEffects(VoteInstance voteInstance, String operator) {
        String templateCode = StringUtils.trim(voteInstance.getTemplateCode());
        if (StringUtils.isBlank(templateCode)) {
            return;
        }

        WhitelistInfo whitelistInfo = resolveWhitelistInfo(voteInstance);
        if (whitelistInfo == null) {
            return;
        }

        if ("BAN_PLAYER".equalsIgnoreCase(templateCode)) {
            BanlistInfo query = new BanlistInfo();
            query.setWhiteId(whitelistInfo.getId());
            List<BanlistInfo> list = banlistInfoMapper.selectBanlistInfoList(query);

            BanlistInfo banlistInfo;
            if (list == null || list.isEmpty()) {
                banlistInfo = new BanlistInfo();
                banlistInfo.setWhiteId(whitelistInfo.getId());
                banlistInfo.setUserName(whitelistInfo.getUserName());
                banlistInfo.setState(1L);
                banlistInfo.setReason(StringUtils.nvl(voteInstance.getReason(), "投票封禁"));
                banlistInfo.setCreateBy(operator);
                banlistInfoMapper.insertBanlistInfo(banlistInfo);
            } else {
                banlistInfo = list.getFirst();
                banlistInfo.setState(1L);
                banlistInfo.setReason(StringUtils.nvl(voteInstance.getReason(), "投票封禁"));
                banlistInfo.setUpdateBy(operator);
                banlistInfoMapper.updateBanlistInfo(banlistInfo);
            }

            whitelistInfo.setStatus("0");
            whitelistInfo.setAddState("9");
            whitelistInfo.setRemoveReason(StringUtils.nvl(voteInstance.getReason(), "投票封禁"));
            whitelistInfo.setRemoveTime(new Date());
            whitelistInfo.setUpdateBy(operator);
            whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
            return;
        }

        if ("REMOVE_WHITELIST_PLAYER".equalsIgnoreCase(templateCode)) {
            whitelistInfo.setStatus("0");
            whitelistInfo.setAddState("2");
            whitelistInfo.setRemoveReason(StringUtils.nvl(voteInstance.getReason(), "投票移出白名单"));
            whitelistInfo.setRemoveTime(new Date());
            whitelistInfo.setUpdateBy(operator);
            whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
        }
    }

    private WhitelistInfo resolveWhitelistInfo(VoteInstance voteInstance) {
        if (voteInstance.getTargetWhitelistId() != null) {
            return whitelistInfoMapper.selectWhitelistInfoById(voteInstance.getTargetWhitelistId());
        }

        String targetPlayerName = StringUtils.trim(voteInstance.getTargetPlayerName());
        if (StringUtils.isBlank(targetPlayerName)) {
            return null;
        }

        WhitelistInfo query = new WhitelistInfo();
        query.setUserName(targetPlayerName);
        List<WhitelistInfo> list = whitelistInfoMapper.selectWhitelistInfoList(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    private VoteTemplate resolveTemplate(VoteCreateRequest request) {
        if (request.getTemplateId() != null) {
            return voteTemplateMapper.selectVoteTemplateById(request.getTemplateId());
        }
        if (StringUtils.isNotBlank(request.getTemplateCode())) {
            return voteTemplateMapper.selectVoteTemplateByCode(request.getTemplateCode());
        }
        return null;
    }
}
