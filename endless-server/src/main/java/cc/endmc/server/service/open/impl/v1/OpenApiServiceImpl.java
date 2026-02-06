package cc.endmc.server.service.open.impl.v1;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.cache.QuizConfigCache;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.permission.WhitelistIdChangeHistory;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.player.vo.PlayerDetailsVo;
import cc.endmc.server.domain.quiz.WhitelistQuizAnswer;
import cc.endmc.server.domain.quiz.WhitelistQuizQuestion;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmissionDetail;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;
import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.dto.VerifySource;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.model.MinecraftServerInfo;
import cc.endmc.server.request.ApplyData;
import cc.endmc.server.request.ChangeIdRequest;
import cc.endmc.server.service.message.AsyncMessagePushService;
import cc.endmc.server.service.open.IOpenApiService;
import cc.endmc.server.service.other.IIpLimitInfoService;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.permission.IWhitelistIdChangeHistoryService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.quiz.IWhitelistQuizQuestionService;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import cc.endmc.server.service.relation.IRconNodeInstanceRelationService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Open API 接口服务
 *
 * @author Memory
 * @version V1.0
 * @data 2026-1-3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiServiceImpl implements IOpenApiService {

    private final RedisCache redisCache;
    private final AsyncManager asyncManager = AsyncManager.me();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Quiz相关服务
    private final IWhitelistQuizSubmissionService quizSubmissionService;
    private final IWhitelistQuizQuestionService quizQuestionService;
    private final QuizConfigCache quizConfigCache;

    // 权限相关服务
    private final IWhitelistInfoService whitelistInfoService;
    private final IOperatorListService operatorListService;
    private final IBanlistInfoService banlistInfoService;
    private final IWhitelistIdChangeHistoryService whitelistIdChangeHistoryService;

    // 服务器相关服务
    private final IServerInfoService serverInfoService;
    private final INodeServerService nodeServerService;
    private final INodeMinecraftServerService nodeMinecraftServerService;
    private final IRconNodeInstanceRelationService rconNodeInstanceRelationService;

    // 异步消息推送服务
    private final AsyncMessagePushService asyncMessagePushService;

    // 其他服务
    private final IIpLimitInfoService iIpLimitInfoService;
    private final IPlayerDetailsService playerDetailsService;
    private final EmailService emailService;

    // Mapper
    private final WhitelistInfoMapper whitelistInfoMapper;
    private final PlayerDetailsMapper playerDetailsMapper;
    private final ServerInfoMapper serverInfoMapper;

    // 配置属性
    @Value("${app-url}")
    private String appUrl;
    @Value("${whitelist.iplimit}")
    private String iplimit;
    @Value("${whitelist.email}")
    private String ADMIN_EMAIL;
    @Value("${app.ip-header-name:X-Real-IP}")
    private String ipHeaderName;

    /**
     * 提交白名单问卷答案
     *
     * @param params 提交参数，包含验证码和答案列表
     * @return 提交结果
     */
    @Override
    public AjaxResult submitWhitelistQuizSubmission(JSONObject params) {
        if (params.isEmpty()) {
            return AjaxResult.error("参数不能为空");
        }
        final String code = params.getString("code");
        final JSONArray answers = params.getJSONArray("answers");

        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证码不能为空");
        }

        // 从缓存中获取验证信息
        Map<String, Object> cache = redisCache.getCacheObject(CacheKey.VERIFY_KEY + code);
        if (cache == null) {
            Object object = redisCache.getCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code);
            cache = new HashMap<>();
            cache.put("whitelistInfo", object);
        }

        // 验证码不存在或已失效
        if (cache == null || cache.isEmpty()) {
            return AjaxResult.error("验证码已失效");
        }

        // 获取白名单信息
        Object object = cache.get("whitelistInfo");
        if (object == null) {
            return AjaxResult.error("验证信息不完整");
        }

        final WhitelistInfo whitelistInfo = JSONObject.parseObject(JSONObject.toJSONString(object), WhitelistInfo.class);

        // 检查该用户是否已经提交过问卷
        WhitelistQuizSubmission existingSubmission = new WhitelistQuizSubmission();
        existingSubmission.setPlayerName(whitelistInfo.getUserName());
        List<WhitelistQuizSubmission> existingSubmissions = quizSubmissionService.selectWhitelistQuizSubmissionList(existingSubmission);
        if (existingSubmissions != null && !existingSubmissions.isEmpty()) {
            return AjaxResult.error("您已经提交过问卷");
        }

        List<Long> questionIds = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            JSONObject answerObj = answers.getJSONObject(i);
            Long questionId = answerObj.getLong("questionId");
            if (questionId != null) {
                questionIds.add(questionId);
            }
        }

        if (questionIds.isEmpty()) {
            return AjaxResult.error("没有有效的答题记录");
        }

        // 查询所有题目信息
        final List<WhitelistQuizQuestion> questions = quizQuestionService.selectWhitelistQuizQuestionByIds(questionIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList()));
        if (questions.isEmpty()) {
            return AjaxResult.error("未找到有效题目信息");
        }

        // 创建提交记录
        WhitelistQuizSubmission submission = new WhitelistQuizSubmission();

        // 设置白名单ID - 如果缓存中的whitelistInfo有ID则直接使用，否则尝试查找
        if (whitelistInfo.getId() != null) {
            submission.setWhitelistId(whitelistInfo.getId());
        } else {
            // 尝试通过用户名查找已存在的白名单记录
            WhitelistInfo queryInfo = new WhitelistInfo();
            queryInfo.setUserName(whitelistInfo.getUserName());
            List<WhitelistInfo> existingWhitelists = whitelistInfoService.selectWhitelistInfoList(queryInfo);
            if (!existingWhitelists.isEmpty()) {
                submission.setWhitelistId(existingWhitelists.getFirst().getId());
            }
            // 如果找不到对应的白名单记录，whitelistId将保持为null，后续可以通过数据库触发器或定时任务来关联
        }

        submission.setPlayerName(whitelistInfo.getUserName());

        // 确保playerUuid不为空
        String playerUuid = whitelistInfo.getUserUuid();
        if (StringUtils.isEmpty(playerUuid)) {
            // 使用工具类获取玩家UUID
            boolean isOnline = whitelistInfo.getOnlineFlag() != null && whitelistInfo.getOnlineFlag() == 1;
            playerUuid = MinecraftUUIDUtil.getPlayerUUID(whitelistInfo.getUserName(), isOnline);

            // 同时更新whitelist信息中的UUID
            whitelistInfo.setUserUuid(playerUuid);

            // 更新缓存中的信息
            cache.put("whitelistInfo", whitelistInfo);
            String cacheKey = cache == redisCache.getCacheObject(CacheKey.VERIFY_KEY + code)
                    ? CacheKey.VERIFY_KEY + code
                    : CacheKey.VERIFY_FOR_BOT_KEY + code;
            redisCache.setCacheObject(cacheKey, cache, 30, TimeUnit.MINUTES);
        }

        submission.setPlayerUuid(playerUuid);
        submission.setSubmitTime(new Date());
        submission.setTotalScore(0L); // 初始化总分为0
        submission.setPassStatus(0);  // 初始设置为未通过

        // 创建详细记录列表
        List<WhitelistQuizSubmissionDetail> detailList = new ArrayList<>();
        long totalScore = 0;

        // 随机验证题型通过标志
        boolean random = false;
        boolean randomSuccess = false;

        // 遍历所有答案
        for (int i = 0; i < answers.size(); i++) {
            JSONObject answerObj = answers.getJSONObject(i);
            Long questionId = answerObj.getLong("questionId");
            String playerAnswer = answerObj.getString("answer");
            String verificationId = answerObj.getString("verificationId"); // 获取验证ID

            // 查找对应的问题
            WhitelistQuizQuestion question = questions.stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElse(null);

            if (question == null) {
                continue;
            }

            // 创建详细记录
            WhitelistQuizSubmissionDetail detail = new WhitelistQuizSubmissionDetail();
            detail.setQuestionId(questionId);
            detail.setQuestionType(question.getQuestionType());
            detail.setPlayerAnswer(playerAnswer);
            detail.setIsCorrect(0); // 默认为不正确
            detail.setScore(0L);    // 默认为0分

            // 根据问题类型判断答案是否正确并计算得分
            if (question.getQuestionType() == 4) {
                random = true;
                // 随机验证题型处理
                if (StringUtils.isNotEmpty(verificationId) && StringUtils.isNotEmpty(playerAnswer)) {
                    Map<String, Object> verificationData = redisCache.getCacheObject(CacheKey.VERIFICATION_KEY + verificationId);
                    if (verificationData != null && verificationData.containsKey("result")) {
                        String correctAnswer = (String) verificationData.get("result");
                        boolean isCorrect = correctAnswer.equalsIgnoreCase(playerAnswer.trim());
                        detail.setIsCorrect(isCorrect ? 1 : 0);
                        randomSuccess = isCorrect;
                        // 验证完成后删除缓存
                        redisCache.deleteObject(CacheKey.VERIFICATION_KEY + verificationId);
                    }
                }
            } else if (question.getWhitelistQuizAnswerList() != null && !question.getWhitelistQuizAnswerList().isEmpty()) {
                // 处理单选题和多选题
                if (question.getQuestionType() == 1 || question.getQuestionType() == 2) {
                    String[] selectedAnswers = playerAnswer.split(",");
                    boolean isCorrect = true;
                    double score = 0.0;

                    // 获取所有正确答案
                    List<WhitelistQuizAnswer> correctAnswers = question.getWhitelistQuizAnswerList().stream()
                            .filter(a -> a.getIsCorrect() == 1)
                            .toList();

                    // 单选题检查
                    if (question.getQuestionType() == 1) {
                        if (selectedAnswers.length == 1) {
                            for (WhitelistQuizAnswer answer : question.getWhitelistQuizAnswerList()) {
                                if (answer.getId().toString().equals(selectedAnswers[0])) {
                                    isCorrect = answer.getIsCorrect() == 1;
                                    score = answer.getScore() != null ? answer.getScore() : 0.0;
                                    break;
                                }
                            }
                        } else {
                            isCorrect = false;
                        }
                        // 多选题检查
                    } else if (question.getQuestionType() == 2) {
                        // 所有选择的ID
                        Set<String> selectedIds = new HashSet<>(Arrays.asList(selectedAnswers));

                        // 所有正确答案的ID
                        Set<String> correctIds = correctAnswers.stream()
                                .map(a -> a.getId().toString())
                                .collect(Collectors.toSet());

                        // 判断是否完全匹配
                        isCorrect = selectedIds.equals(correctIds);

                        if (isCorrect) {
                            // 累计所有正确答案的分数
                            score = correctAnswers.stream()
                                    .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0)
                                    .sum();
                        }
                    }

                    detail.setIsCorrect(isCorrect ? 1 : 0);
                    detail.setScore(Math.round(score));
                    totalScore += detail.getScore();

                    // 处理填空题
                } else if (question.getQuestionType() == 3) {
                    // 获取填空题的正确答案
                    WhitelistQuizAnswer correctAnswer = question.getWhitelistQuizAnswerList().stream()
                            .filter(a -> a.getIsCorrect() == 1)
                            .findFirst()
                            .orElse(null);

                    if (correctAnswer != null) {
                        boolean isCorrect = correctAnswer.getAnswerText().trim().equalsIgnoreCase(playerAnswer.trim());
                        detail.setIsCorrect(isCorrect ? 1 : 0);
                        if (isCorrect) {
                            detail.setScore(Math.round(correctAnswer.getScore() != null ? correctAnswer.getScore() : 0.0));
                            totalScore += detail.getScore();
                        }
                    }
                }
            }

            detailList.add(detail);
        }

        // 设置总分
        submission.setTotalScore(totalScore);

        // 检查是否通过及格线 - 使用缓存
        long passScore = quizConfigCache.getPassScore();
        if (totalScore >= passScore) {
            submission.setPassStatus(1); // 已通过
            submission.setReviewer("System(Auto_Quiz_Pass)"); // 自动审核
        }

        if (random && !randomSuccess) {
            submission.setPassStatus(0);
            submission.setReviewComment("用户未通过随机验证");
            submission.setReviewer(null);
        }

        // 设置详情列表
        submission.setWhitelistQuizSubmissionDetailList(detailList);

        // 保存提交记录
        int result = quizSubmissionService.insertWhitelistQuizSubmission(submission);

        if (result > 0) {
            return AjaxResult.success("提交成功");
        } else {
            return AjaxResult.error("提交失败");
        }
    }

    /**
     * 获取白名单列表
     *
     * @return 白名单列表
     */
    @Override
    public AjaxResult getWhiteList() {
        Map<String, String> result = new HashMap<>();

        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                return AjaxResult.success(cacheObject);
            }

            // 查询已通过审核且已添加的白名单用户
            WhitelistInfo query = new WhitelistInfo();
            query.setStatus("1"); // 已审核通过
            query.setAddState("1"); // 已添加
            final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);

            if (whitelistInfos.isEmpty()) {
                return AjaxResult.error("服务器白名单为空");
            }
            Map<String, Set<String>> cache = new HashMap<>();

            List<String> all = new ArrayList<>();
            // 遍历白名单列表
            for (WhitelistInfo whitelistInfo : whitelistInfos) {
                if (whitelistInfo.getServers().contains("all")) {
                    all.add(whitelistInfo.getUserName());
                } else {
                    for (String s : whitelistInfo.getServers().split(",")) {
                        if (cache.containsKey(s)) {
                            cache.get(s).add(whitelistInfo.getUserName());
                        } else {
                            Set<String> set = new HashSet<>();
                            set.add(whitelistInfo.getUserName());
                            cache.put(s, set);
                        }
                    }
                }
            }

            // 汇聚
            result.put("全部成员", Arrays.toString(all.toArray()));

            if (cache.isEmpty()) {
                return AjaxResult.success(result);
            }

            cache.forEach((String k, Set<String> v) -> {
                if (RconCache.containsKey(k)) {  // 只查询活跃服务器
                    final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                    result.put(nameTag, Arrays.toString(v.toArray()));
                }
            });

        } catch (Exception e) {
            log.error("获取白名单列表发生异常", e);
            return AjaxResult.error("系统繁忙,请稍后重试");
        }
        return AjaxResult.success(result);
    }

    /**
     * 获取白名单问卷题目
     *
     * @return 问题列表
     */
    @Override
    public AjaxResult getQuestions() {
        // 使用缓存检查答题功能是否开启
        if (!quizConfigCache.isQuizEnabled()) {
            return AjaxResult.success(new ArrayList<>());
        }

        final WhitelistQuizQuestion question = new WhitelistQuizQuestion();
        question.setStatus(1);

        // 从缓存获取配置
        boolean random = quizConfigCache.isRandomQuestion();
        int questionCount = quizConfigCache.getQuestionCount();

        // 随机抽取问题 - 使用VO查询
        List<WhitelistQuizQuestionVo> questions = quizQuestionService.selectWhitelistQuizQuestionVoList(question);

        if (random && questionCount > 0 && questionCount < questions.size()) {
            Collections.shuffle(questions);
            questions = questions.subList(0, questionCount);
        } else if (!random && questionCount > 0 && questionCount < questions.size()) {
            questions = questions.subList(0, questionCount);
        }

        if (!questions.isEmpty()) {
            // 根据 sortOrder 排序
            questions.sort(Comparator.comparing(WhitelistQuizQuestionVo::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));

            // 处理随机验证题型
            questions.forEach(q -> {
                if (q.getQuestionType() == 4) { // 随机验证题型
                    processRandomVerificationQuestion(q);
                }
            });
        }

        return AjaxResult.success(questions);
    }

    /**
     * 获取问卷答题详情
     *
     * @param id 答题记录ID
     * @return 答题详情
     */
    @Override
    public AjaxResult getQuizDetail(Long id) {
        final String quizSubmissionKey = CacheKey.QUIZ_SUBMISSION_KEY;
        final String quizSubmissionDetailKey = CacheKey.QUIZ_SUBMISSION_DETAIL_KEY;
        // 缓存查询
        Map<String, Object> cacheObject = new LinkedHashMap<>();
        if (redisCache.hasKey(quizSubmissionKey + id) && redisCache.getCacheMap(quizSubmissionKey + id) != null) {
            cacheObject = redisCache.getCacheMap(quizSubmissionKey + id);
        }

        if (redisCache.hasKey(quizSubmissionDetailKey + id) && redisCache.getCacheList(quizSubmissionDetailKey + id) != null) {
            List<Object> cacheList = redisCache.getCacheList(quizSubmissionDetailKey + id);
            cacheObject.put("答题详情", cacheList);
        }

        if (!cacheObject.isEmpty()) {
            return AjaxResult.success(cacheObject);
        }

        final WhitelistQuizSubmission submission = quizSubmissionService.selectWhitelistQuizSubmissionById(id);

        if (submission == null) {
            return AjaxResult.error("未找到答题记录");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ID", submission.getId());
        data.put("玩家名称", submission.getPlayerName());
        data.put("玩家UUID", submission.getPlayerUuid());
        data.put("提交时间", submission.getSubmitTime());
        data.put("总得分", submission.getTotalScore());
        data.put("是否通过", submission.getPassStatus() == 1 ? "是" : "否");
        data.put("审核人", submission.getReviewer() != null ? submission.getReviewer() : "无");
        data.put("审核意见", submission.getReviewComment() != null ? submission.getReviewComment() : "无");

        redisCache.setCacheMap(CacheKey.QUIZ_SUBMISSION_KEY + id, data, 1, TimeUnit.DAYS);

        if (submission.getWhitelistQuizSubmissionDetailList() != null) {
            List<Map<String, Object>> details = new ArrayList<>();
            for (WhitelistQuizSubmissionDetail detail : submission.getWhitelistQuizSubmissionDetailList()) {
                Map<String, Object> detailMap = new HashMap<>();
                detailMap.put("问题ID", detail.getQuestionId());
                final WhitelistQuizQuestion question = quizQuestionService.selectWhitelistQuizQuestionById(detail.getQuestionId());
                detailMap.put("问题内容", question != null ? question.getQuestionText() : "问题已删除");
                detailMap.put("问题类型", question != null ? question.getQuestionType() : "未知");
                detailMap.put("玩家答案", detail.getPlayerAnswer());
                // detailMap.put("是否正确", detail.getIsCorrect() == 1 ? "是" : "否");
                // detailMap.put("得分", detail.getScore());
                details.add(detailMap);
            }
            redisCache.setCacheList(CacheKey.QUIZ_SUBMISSION_DETAIL_KEY + id, details, 1, TimeUnit.DAYS);
            data.put("答题详情", details);
        }

        return AjaxResult.success(data);
    }


    /**
     * 处理随机验证题目
     * 根据保存的验证类型和难度级别，实时生成验证内容
     *
     * @param question 问题对象
     */
    private void processRandomVerificationQuestion(WhitelistQuizQuestionVo question) {
        if (question == null || StringUtils.isEmpty(question.getQuestionText())) {
            return;
        }
        // 生成唯一的验证ID
        String verificationId = UUID.randomUUID().toString();

        try {
            // 解析验证类型和难度级别
            String[] parts = question.getQuestionText().split(":");
            String verificationType = parts[0];
            String difficultyLevel = parts.length > 1 ? parts[1] : "easy";

            // 根据验证类型生成随机验证内容
            String verificationContent = generateVerificationContent(verificationType, difficultyLevel, verificationId);

            // 设置生成的验证内容
            question.setQuestionText(verificationContent);
            question.setVerificationId(verificationId);

            // 移除答案列表，随机验证题不需要预设答案
            if (question.getWhitelistQuizAnswerVoList() != null) {
                question.getWhitelistQuizAnswerVoList().clear();
            }
        } catch (Exception e) {
            log.error("处理随机验证题目出错", e);
            question.setQuestionText("验证生成失败，请刷新重试");
        }
    }

    /**
     * 生成随机验证内容
     *
     * @param type  验证类型：1-数学验证，2-字母验证
     * @param level 难度级别：easy-简单，medium-中等，hard-困难
     * @return 生成的验证内容
     */
    private String generateVerificationContent(String type, String level, String verificationId) {
        Random random = new Random();

        // 数学验证
        if ("1".equals(type)) {
            return generateMathVerification(level, random, verificationId);
        }
        // 字母验证
        else if ("2".equals(type)) {
            return generateLetterVerification(level, random, verificationId);
        }
        return "无效的验证类型";
    }

    /**
     * 生成数学验证内容
     */
    private String generateMathVerification(String level, Random random, String verificationId) {
        int num1, num2, result;
        String operator;

        switch (level) {
            case "easy":
                // 简单：1-100的加减法
                num1 = random.nextInt(100) + 1;  // 1-100
                num2 = random.nextInt(100) + 1;  // 1-100
                operator = random.nextBoolean() ? "+" : "-";
                result = operator.equals("+") ? num1 + num2 : num1 - num2;
                // 确保结果为正数
                if (result < 0) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                    result = num1 - num2;
                }
                break;
            case "medium":
                // 普通：1-1000的加减乘除
                num1 = random.nextInt(1000) + 1; // 1-1000
                num2 = random.nextInt(1000) + 1; // 1-1000
                int op = random.nextInt(4);
                switch (op) {
                    case 0:
                        operator = "+";
                        result = num1 + num2;
                        break;
                    case 1:
                        operator = "-";
                        result = num1 - num2;
                        // 确保结果为正数
                        if (result < 0) {
                            int temp = num1;
                            num1 = num2;
                            num2 = temp;
                            result = num1 - num2;
                        }
                        break;
                    case 2:
                        operator = "×";
                        // 限制乘法数字范围，避免结果过大
                        num1 = random.nextInt(50) + 1;  // 1-50
                        num2 = random.nextInt(20) + 1;  // 1-20
                        result = num1 * num2;
                        break;
                    default:
                        operator = "÷";
                        // 确保除法能整除
                        num2 = random.nextInt(20) + 1;  // 1-20
                        num1 = num2 * (random.nextInt(50) + 1);  // 确保能整除
                        result = num1 / num2;
                }
                break;
            case "hard":
                // 困难：1-10000的复杂运算
                num1 = random.nextInt(10000) + 1; // 1-10000
                num2 = random.nextInt(10000) + 1; // 1-10000
                int op2 = random.nextInt(5);
                switch (op2) {
                    case 0:
                        operator = "+";
                        result = num1 + num2;
                        break;
                    case 1:
                        operator = "-";
                        result = num1 - num2;
                        // 确保结果为正数
                        if (result < 0) {
                            int temp = num1;
                            num1 = num2;
                            num2 = temp;
                            result = num1 - num2;
                        }
                        break;
                    case 2:
                        operator = "×";
                        // 限制乘法数字范围，避免结果过大
                        num1 = random.nextInt(100) + 1;  // 1-100
                        num2 = random.nextInt(100) + 1;  // 1-100
                        result = num1 * num2;
                        break;
                    case 3:
                        operator = "÷";
                        // 确保除法能整除
                        num2 = random.nextInt(50) + 1;  // 1-50
                        num1 = num2 * (random.nextInt(200) + 1);  // 确保能整除
                        result = num1 / num2;
                        break;
                    default:
                        // 混合运算：先乘后加
                        operator = "×+";
                        int num3 = random.nextInt(100) + 1;
                        result = num1 * num2 + num3;
                        return String.format("请计算: %d × %d + %d = ?\n\n验证ID: %s",
                                num1, num2, num3, UUID.randomUUID().toString());
                }
                break;
            default:
                // 默认简单
                num1 = random.nextInt(100) + 1;
                num2 = random.nextInt(100) + 1;
                operator = "+";
                result = num1 + num2;
        }

        // 存储答案到Redis缓存中，设置30分钟过期
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("result", String.valueOf(result));
        redisCache.setCacheObject(CacheKey.VERIFICATION_KEY + verificationId, verificationData, 30, TimeUnit.MINUTES);

        return String.format("请计算: %d %s %d = ?\n\n", num1, operator, num2);
    }

    /**
     * 生成字母验证内容
     */
    private String generateLetterVerification(String level, Random random, String verificationId) {
        String letters;
        int length;

        switch (level) {
            case "easy":
                // 简单：5个随机大写字母
                length = 5;
                letters = generateRandomLetters(length, true, false, random);
                break;
            case "medium":
                // 中等：7个大小写混合字母
                length = 7;
                letters = generateRandomLetters(length, true, true, random);
                break;
            case "hard":
                // 困难：10个大小写字母和数字的组合
                length = 10;
                letters = generateRandomLetters(length, true, true, random) +
                        generateRandomDigits(3, random);
                // 打乱顺序
                char[] chars = letters.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    int j = random.nextInt(chars.length);
                    char temp = chars[i];
                    chars[i] = chars[j];
                    chars[j] = temp;
                }
                letters = new String(chars).substring(0, length);
                break;
            default:
                // 默认简单
                length = 5;
                letters = generateRandomLetters(length, true, false, random);
        }

        // 存储答案到Redis缓存中，设置30分钟过期
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("result", letters);
        redisCache.setCacheObject(CacheKey.VERIFICATION_KEY + verificationId, verificationData, 30, TimeUnit.MINUTES);

        return String.format("请输入以下字符: %s\n\n", letters);
    }

    /**
     * 生成随机字母
     */
    private String generateRandomLetters(int length, boolean includeUppercase, boolean includeLowercase, Random random) {
        StringBuilder sb = new StringBuilder();
        String uppercaseLetters = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // 排除容易混淆的字母
        String lowercaseLetters = "abcdefghijkmnpqrstuvwxyz"; // 排除容易混淆的字母

        String availableChars = "";
        if (includeUppercase) {
            availableChars += uppercaseLetters;
        }
        if (includeLowercase) {
            availableChars += lowercaseLetters;
        }

        if (availableChars.isEmpty()) {
            availableChars = uppercaseLetters; // 默认使用大写字母
        }

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(availableChars.length());
            sb.append(availableChars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 生成随机数字
     */
    private String generateRandomDigits(int length, Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 0-9
        }
        return sb.toString();
    }

    /**
     * 聚合查询
     *
     * @return 聚合查询结果
     */
    @Override
    public AjaxResult aggregateQuery() {
        Map<String, Object> result = new HashMap<>();

        // 在线玩家
        Map<String, Object> onlinePlayer = serverInfoService.getOnlinePlayer(false);
        result.put("onlinePlayer", onlinePlayer);

        // 申请数量
        List<WhitelistInfo> whitelistInfos = whitelistInfoMapper.selectWhitelistInfoList(new WhitelistInfo());
        result.put("applyCount", whitelistInfos.size());

        // 白名单数量
        int index = (int) whitelistInfos.stream().filter(whitelistInfo -> whitelistInfo.getStatus().equals("1")).count();
        result.put("whiteListCount", index);

        // 未通过数量
        index = (int) whitelistInfos.stream().filter(whitelistInfo -> whitelistInfo.getStatus().equals("0")).count();
        result.put("notPassCount", index);

        // OP数量
        final OperatorList op = new OperatorList();
        op.setStatus(1L);
        final List<OperatorList> operatorLists = operatorListService.selectOperatorListList(op);
        result.put("opCount", operatorLists.size());

        // 封禁数量
        final BanlistInfo banlistInfo = new BanlistInfo();
        banlistInfo.setState(1L);
        int banCount = banlistInfoService.selectBanlistInfoList(banlistInfo).size();
        result.put("banCount", banCount);

        // 在线前十
        final List<PlayerDetails> playerDetails = playerDetailsMapper.selectTopTenByGameTime();
        final List<PlayerDetailsVo> playerDetailsVos = new ArrayList<>();
        playerDetails.forEach(o -> {
            final PlayerDetailsVo vo = new PlayerDetailsVo();
            BeanUtils.copyProperties(o, vo);
            playerDetailsVos.add(vo);
        });
        result.put("topTen", playerDetailsVos);

        // 服务器数量
        List<ServerInfo> serverInfo = serverInfoMapper.selectServerInfoList(new ServerInfo());
        result.put("serverCount", serverInfo.size());

        // 节点统计
        try {
            List<NodeServer> nodeServers = nodeServerService.selectNodeServerList(new NodeServer());
            result.put("nodeCount", nodeServers.size());
            // 在线节点数量
            long onlineNodeCount = nodeServers.stream()
                    .filter(node -> "0".equals(node.getStatus()))
                    .count();
            result.put("onlineNodeCount", onlineNodeCount);
            // 离线节点数量
            long offlineNodeCount = nodeServers.stream()
                    .filter(node -> "1".equals(node.getStatus()))
                    .count();
            result.put("offlineNodeCount", offlineNodeCount);
            // 节点列表简要信息
            List<Map<String, Object>> nodeList = new ArrayList<>();
            for (NodeServer node : nodeServers) {
                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("id", node.getId());
                nodeInfo.put("name", node.getName());
                nodeInfo.put("status", node.getStatus());
                nodeInfo.put("version", node.getVersion());
                nodeInfo.put("osType", node.getOsType());
                nodeInfo.put("lastHeartbeat", node.getLastHeartbeat());
                nodeList.add(nodeInfo);
            }
            result.put("nodeList", nodeList);
        } catch (Exception e) {
            log.error("获取节点统计信息失败", e);
            result.put("nodeCount", 0);
            result.put("onlineNodeCount", 0);
            result.put("offlineNodeCount", 0);
            result.put("nodeList", new ArrayList<>());
        }

        return AjaxResult.success(result);
    }

    /**
     * 从服务器获取白名单列表
     *
     * @return 白名单列表
     */
    @Override
    public AjaxResult getWhiteListForServer() {
        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                return AjaxResult.success(cacheObject);
            }

            Map<String, String> map = new HashMap<>();
            RconCache.getMap().forEach((k, v) -> {
                final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                try {
                    final String list = v.sendCommand("whitelist list");
                    String[] split = new String[0];
                    if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
                        split = list.split("whitelisted player\\(s\\):")[1].trim().split(", ");
                    }
                    map.put(nameTag, Arrays.toString(split));
                } catch (Exception e) {
                    log.error("获取白名单列表失败, serverId: {}", k, e);
                }
            });

            // 更新缓存
            if (!map.isEmpty()) {
                log.info("更新白名单列表缓存");
                redisCache.setCacheObject(CacheKey.WHITE_LIST_KEY, map, 5, TimeUnit.MINUTES);
            }
            return AjaxResult.success(map);

        } catch (Exception e) {
            log.error("获取白名单列表发生异常", e);
            return AjaxResult.error("系统繁忙,请稍后重试");
        }
    }

    /**
     * 获取在线玩家信息
     *
     * @return 在线玩家信息
     */
    @Override
    public AjaxResult getOnlinePlayer() {
        return AjaxResult.success(serverInfoService.getOnlinePlayer(true));
    }

    /**
     * 检查答题状态
     *
     * @param code 验证码
     * @return 答题状态
     */
    @Override
    public AjaxResult checkQuizStatus(String code) {
        // 从缓存中获取验证信息
        Map<String, Object> cache;
        if (Boolean.TRUE.equals(redisCache.hasKey(CacheKey.VERIFY_KEY + code))) {
            cache = redisCache.getCacheObject(CacheKey.VERIFY_KEY + code);
        } else if (Boolean.TRUE.equals(redisCache.hasKey(CacheKey.VERIFY_FOR_BOT_KEY + code))) {
            Object object = redisCache.getCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code);
            cache = new HashMap<>();
            cache.put("whitelistInfo", object);
        } else {
            return AjaxResult.error("验证码已失效");
        }

        // 验证码不存在或已失效
        if (cache == null || cache.isEmpty()) {
            return AjaxResult.error("验证码已失效");
        }

        // 获取白名单信息
        Object object = cache.get("whitelistInfo");
        if (object == null) {
            return AjaxResult.error("验证信息不完整");
        }

        // 对象转换
        final WhitelistInfo whitelistInfo = JSONObject.parseObject(JSONObject.toJSONString(object), WhitelistInfo.class);

        // 查询答题记录
        final WhitelistQuizSubmission whitelistQuizSubmission = new WhitelistQuizSubmission();
        whitelistQuizSubmission.setPlayerName(whitelistInfo.getUserName());
        final List<WhitelistQuizSubmission> submissions = quizSubmissionService.selectWhitelistQuizSubmissionList(whitelistQuizSubmission);

        // 判断是否完成问卷
        if (submissions == null || submissions.isEmpty()) {
            return AjaxResult.success("未完成问卷");
        }

        // 已完成问卷
        return AjaxResult.success("已完成问卷");
    }

    /**
     * 获取服务器状态
     * 使用多线程并行检测所有服务器状态，提升响应速度
     * 每个服务器的检测任务独立执行，避免单个服务器超时影响整体响应
     *
     * @return 服务器状态信息
     */
    @Override
    public AjaxResult getServerStatus() {
        // 查询所有服务器
        final ServerInfo info = new ServerInfo();
        info.setStatus(1L); // 仅查询启用的服务器
        List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(info);

        if (serverInfos.isEmpty()) {
            return AjaxResult.error("未找到服务器信息");
        }

        // 使用CompletableFuture并行检测所有服务器状态
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();

        for (ServerInfo serverInfo : serverInfos) {
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                String cacheKey = CacheKey.MINECRAFT_SERVER_INFO + serverInfo.getId();

                // 检查缓存
                if (redisCache.hasKey(cacheKey)) {
                    return redisCache.getCacheMap(cacheKey);
                }

                // 构建服务器状态信息
                Map<String, Object> statusMap = new HashMap<>();
                String nameTag = serverInfo.getNameTag();
                statusMap.put("id", serverInfo.getId());
                statusMap.put("服务器名称", nameTag);
                statusMap.put("连接地址", serverInfo.getPlayAddress());
                statusMap.put("连接端口", String.valueOf(serverInfo.getPlayAddressPort()));
                statusMap.put("版本", serverInfo.getServerVersion());
                statusMap.put("核心", serverInfo.getServerCore());

                try {
                    // 异步检测RCON连接
                    final boolean rconConnection = NetWorkUtil.testRconConnection(String.valueOf(serverInfo.getId()));
                    statusMap.put("Rcon连接", rconConnection ? "成功" : "失败");

                    // 异步获取服务器延迟信息
                    final MinecraftServerInfo minecraftServerLatency = NetWorkUtil.getMinecraftServerLatency(
                            serverInfo.getPlayAddress(), serverInfo.getPlayAddressPort());
                    statusMap.put("在线状态", minecraftServerLatency.isReachable() ? "在线" : "离线");
                    statusMap.put("在线人数", String.valueOf(minecraftServerLatency.getOnlinePlayers()));
                    statusMap.put("最大人数", String.valueOf(minecraftServerLatency.getMaxPlayers()));
                    statusMap.put("延迟(ms)", String.valueOf(minecraftServerLatency.getLatency()));

                    // 判断服务状态指标
                    final boolean offline = statusMap.get("在线状态").equals("离线");
                    if (offline && !rconConnection) {
                        statusMap.put("指标", "服务熔断");
                    } else if (offline || !rconConnection) {
                        statusMap.put("指标", "服务降级");
                    } else {
                        statusMap.put("指标", "服务正常");
                    }
                } catch (Exception e) {
                    log.error("检测服务器{}状态失败,原因: {}", serverInfo.getNameTag(), e.getMessage());
                    statusMap.put("Rcon连接", "失败");
                    statusMap.put("在线状态", "离线");
                    statusMap.put("在线人数", "0");
                    statusMap.put("最大人数", "0");
                    statusMap.put("延迟(ms)", "0");
                    statusMap.put("指标", "服务熔断");
                }

                // 缓存结果
                redisCache.setCacheMap(cacheKey, statusMap, 3, TimeUnit.MINUTES);
                return statusMap;
            });
            futures.add(future);
        }

        // 等待所有异步任务完成，设置超时时间为5秒
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            ServerInfo serverInfo = serverInfos.get(i);
            try {
                Map<String, Object> result = futures.get(i).get(5, TimeUnit.SECONDS);
                data.add(result);
            } catch (Exception e) {
                log.error("获取服务器{}状态超时,原因: {}", serverInfo.getNameTag(), e.getMessage());
                // 超时时返回基本信息，状态设置为离线
                Map<String, Object> timeoutMap = new HashMap<>();
                timeoutMap.put("id", serverInfo.getId());
                timeoutMap.put("服务器名称", serverInfo.getNameTag());
                timeoutMap.put("连接地址", serverInfo.getPlayAddress());
                timeoutMap.put("连接端口", String.valueOf(serverInfo.getPlayAddressPort()));
                timeoutMap.put("版本", serverInfo.getServerVersion());
                timeoutMap.put("核心", serverInfo.getServerCore());
                timeoutMap.put("Rcon连接", "失败");
                timeoutMap.put("在线状态", "离线");
                timeoutMap.put("在线人数", "0");
                timeoutMap.put("最大人数", "0");
                timeoutMap.put("延迟(ms)", "0");
                timeoutMap.put("指标", "服务熔断");
                data.add(timeoutMap);
            }
        }

        return AjaxResult.success(data);
    }

    /**
     * 获取服务器控制台日志
     *
     * @param serverId  服务器ID
     * @param lineCount 日志行数
     * @return 控制台日志
     */
    @Override
    public AjaxResult getConsole(Integer serverId, Integer lineCount) {

        if (serverId == null || lineCount == null || lineCount <= 0) {
            return AjaxResult.error("参数错误");
        }

        try {
            // 查询Rcon节点实例关系
            RconNodeInstanceRelation rconNodeInstanceRelation = new RconNodeInstanceRelation();
            rconNodeInstanceRelation.setRconServerId(Long.valueOf(serverId));

            List<RconNodeInstanceRelation> relations = rconNodeInstanceRelationService.selectList(rconNodeInstanceRelation);
            if (relations.isEmpty()) {
                return AjaxResult.error("未找到服务器对应的节点实例关系");
            }

            RconNodeInstanceRelation relation = relations.getFirst();
            Long nodeId = relation.getNodeId();
            Long instanceId = relation.getInstanceId();

            if (nodeId == null || instanceId == null) {
                return AjaxResult.error("节点或实例信息不完整");
            }

            // 构建参数调用节点服务获取控制台历史日志
            Map<String, Object> params = new HashMap<>();
            params.put("id", nodeId.intValue());
            params.put("serverId", instanceId.intValue());

            // 调用节点服务获取控制台历史日志
            AjaxResult result = nodeMinecraftServerService.getConsoleHistory(params);

            if (result.isSuccess() && result.get(AjaxResult.DATA_TAG) != null) {
                // 如果需要限制行数，可以在这里处理
                Object data = result.get(AjaxResult.DATA_TAG);
                if (data instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> logs = (List<String>) data;

                    // 限制返回的日志行数
                    if (logs.size() > lineCount) {
                        logs = logs.subList(Math.max(0, logs.size() - lineCount), logs.size());
                    }

                    return AjaxResult.success(logs);
                } else if (data instanceof String logString) {
                    String[] lines = logString.split("\n");

                    // 限制返回的日志行数
                    if (lines.length > lineCount) {
                        String[] limitedLines = new String[lineCount];
                        System.arraycopy(lines, Math.max(0, lines.length - lineCount), limitedLines, 0, lineCount);
                        return AjaxResult.success(String.join("\n", limitedLines));
                    }

                    return AjaxResult.success(logString);
                }

                return result;
            } else {
                String errorMsg = result.get(AjaxResult.MSG_TAG) != null ?
                        result.get(AjaxResult.MSG_TAG).toString() : "未知错误";
                return AjaxResult.error("获取控制台日志失败: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("获取控制台日志异常, serverId: {}, lineCount: {}", serverId, lineCount, e);
            return AjaxResult.error("系统异常，请稍后重试");
        }
    }

    /**
     * Minecraft服务器消息推送
     * 用于游戏内消息转发到QQ群
     *
     * @param param 消息参数 {playerId: 玩家ID, playerName: 玩家名称, message: 消息内容, serverId: 服务器ID}
     * @return 推送结果
     */
    @Override
    public AjaxResult pushMessage(JSONObject param) {
        if (param == null || param.isEmpty()) {
            return AjaxResult.error("参数不能为空");
        }

        String playerName = param.getString("playerName");
        String message = param.getString("message");
        String serverId = param.getString("serverId");
        String targetGroups = param.getString("targetGroups");

        if (StringUtils.isEmpty(playerName) || StringUtils.isEmpty(message)) {
            return AjaxResult.error("玩家名称和消息内容不能为空");
        }

        try {
            // 使用异步服务推送消息
            CompletableFuture<Boolean> future = asyncMessagePushService.pushMessageAsync(playerName, message, serverId, targetGroups);

            log.debug("消息已提交到异步队列: player={}, message={}, targetGroups={}", playerName, message, targetGroups);
            return AjaxResult.success("消息推送已提交");

        } catch (Exception e) {
            log.error("推送消息失败", e);
            return AjaxResult.error("推送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取消息推送队列状态
     *
     * @return 队列状态信息
     */
    @Override
    public AjaxResult getMessageQueueStatus() {
        try {
            Map<String, Object> stats = asyncMessagePushService.getQueueStats();
            return AjaxResult.success(stats);
        } catch (Exception e) {
            log.error("获取队列状态失败", e);
            return AjaxResult.error("获取队列状态失败: " + e.getMessage());
        }
    }

    /**
     * 从缓存获取服务器名称
     *
     * @param serverId 服务器ID
     * @return 服务器名称
     */
    private String getServerNameFromCache(String serverId) {
        if (StringUtils.isEmpty(serverId)) {
            return "未知服务器";
        }

        try {
            // 先从缓存获取服务器信息Map
            Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);

            if (serverInfoMap != null && serverInfoMap.containsKey(serverId)) {
                Object serverObj = serverInfoMap.get(serverId);
                if (serverObj != null) {
                    ServerInfo serverInfo = null;

                    if (serverObj instanceof ServerInfo) {
                        serverInfo = (ServerInfo) serverObj;
                    } else {
                        try {
                            serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                        } catch (Exception e) {
                            log.warn("服务器信息转换失败，serverId: {}, 错误: {}", serverId, e.getMessage());
                        }
                    }

                    if (serverInfo != null && serverInfo.getNameTag() != null) {
                        return serverInfo.getNameTag();
                    }
                }

                log.debug("服务器ID {} 对应的服务器信息为空或无效", serverId);
                return "未知服务器";
            } else {
                log.debug("服务器ID {} 在缓存中不存在，使用默认名称", serverId);
                return "未知服务器";
            }

        } catch (Exception e) {
            log.error("从缓存获取服务器信息失败，serverId: {}", serverId, e);
            return "未知服务器";
        }
    }

    /**
     * 申请白名单
     */
    @Override
    @SneakyThrows
    public AjaxResult apply(HttpServletRequest request, WhitelistInfo whitelistInfo, Map<String, String> header) {
        // 1. 基础参数校验
        AjaxResult validationResult = validateApplyParams(whitelistInfo);
        if (validationResult != null) {
            return validationResult;
        }

        log.info("申请信息:{}", whitelistInfo);
        log.info("header:{}", header);

        // 2. 获取并验证User-Agent
        String userAgent = header.get("user-agent");
        AjaxResult uaCheckResult = validateUserAgent(userAgent);
        if (uaCheckResult != null) {
            return uaCheckResult;
        }

        // 3. 验证游戏ID和QQ号格式
        AjaxResult formatCheckResult = validateInputFormat(whitelistInfo);
        if (formatCheckResult != null) {
            return formatCheckResult;
        }

        // 4. 检查重复申请
        AjaxResult repeatCheckResult = checkRepeatApplication(whitelistInfo);
        if (repeatCheckResult != null) {
            return repeatCheckResult;
        }

        // 5. IP限流检查
        String ip = IPUtils.getClientIpAddress(request, ipHeaderName);
        AjaxResult limitResult = checkIpLimitForApply(ip, whitelistInfo, userAgent);
        if (limitResult != null) {
            return limitResult;
        }

        // 6. 检查是否有活跃的验证码
        if (SecureCodeUtil.hasActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_KEY)) {
            return AjaxResult.error("您已有一个待验证的申请，请勿重复提交！");
        }

        // 7. 生成安全的验证码（8位字母数字组合）
        String code = SecureCodeUtil.generateSecureCode(
                whitelistInfo.getQqNum(),
                CacheKey.VERIFY_KEY,
                8,
                30
        );

        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证码生成失败,请稍后再试!");
        }

        // 8. 标记活跃验证码
        SecureCodeUtil.markActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_KEY, 30);

        // 9. 创建玩家详情并获取地理位置
        PlayerDetails details = createPlayerDetailsForApply(whitelistInfo, ip);

        // 10. 缓存申请数据
        cacheApplyData(code, whitelistInfo, details);

        // 11. 生成验证链接并发送邮件
        String verifyUrl = buildVerifyUrl(header, code);
        emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(verifyUrl));

        return AjaxResult.success("验证邮件已发送,请查收! 如果未收到邮件,请检查垃圾箱或联系管理员!");
    }

    /**
     * 验证白名单
     */
    @Override
    public AjaxResult verify(HttpServletRequest request, String code, Map<String, String> header) {
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证失败,请勿直接访问此链接!");
        }

        // 1. 检查验证码并获取申请来源
        VerifySource verifySource = checkVerifyCode(code);
        if (verifySource == null) {
            return AjaxResult.error("验证失败,验证码无效!");
        }

        // 2. 解析申请数据
        ApplyData applyData;
        try {
            applyData = parseApplyData(verifySource);
        } catch (Exception e) {
            log.error("数据转换失败", e);
            return AjaxResult.error("验证失败,数据格式错误!");
        }

        // 3. IP限流检查
        String ip = IPUtils.getClientIpAddress(request, ipHeaderName);
        AjaxResult limitResult = checkIpLimitForVerify(ip, applyData.getWhitelistInfo(), header);
        if (limitResult != null) {
            return limitResult;
        }

        // 4. 更新地理位置信息
        updateLocationInfo(ip, applyData.getDetails());

        // 5. 保存玩家详情
        if (applyData.getDetails() != null) {
            playerDetailsService.insertPlayerDetails(applyData.getDetails());
        }

        // 6. 完善白名单信息
        completeWhitelistInfo(applyData.getWhitelistInfo(), verifySource.getSource());

        // 7. 检查是否自动审核通过
        boolean autoApproved = checkAutoApproval(applyData.getWhitelistInfo());

        // 8. 保存白名单申请
        if (whitelistInfoService.insertWhitelistInfo(applyData.getWhitelistInfo()) == 0) {
            return AjaxResult.error(EmailTemplates.APPLY_ERROR);
        }

        // 9. 删除验证码
        redisCache.deleteObject(verifySource.getCacheKey());

        // 10. 发送通知（异步）
        sendNotifications(applyData.getWhitelistInfo(), applyData.getDetails(), verifySource.getSource(), autoApproved);

        return AjaxResult.success(autoApproved ? "恭喜您！您的白名单申请已自动审核通过！" : EmailTemplates.APPLY_SUCCESS);
    }

    /**
     * 请求更改游戏ID
     */
    @Override
    public AjaxResult requestChangeId(HttpServletRequest request, ChangeIdRequest changeRequest) {
        // 1. 参数校验
        if (StringUtils.isEmpty(changeRequest.getOldUserName()) ||
                StringUtils.isEmpty(changeRequest.getNewUserName()) ||
                StringUtils.isEmpty(changeRequest.getQqNum())) {
            return AjaxResult.error("参数不能为空!");
        }

        // 2. 格式校验
        Pattern gameIdPattern = Pattern.compile("[a-zA-Z0-9_]{1,35}");
        if (!gameIdPattern.matcher(changeRequest.getOldUserName()).matches()) {
            return AjaxResult.error("旧游戏ID格式不正确!");
        }
        if (!gameIdPattern.matcher(changeRequest.getNewUserName()).matches()) {
            return AjaxResult.error("新游戏ID格式不正确!");
        }

        Pattern qqPattern = Pattern.compile("[0-9]{5,11}");
        if (!qqPattern.matcher(changeRequest.getQqNum()).matches()) {
            return AjaxResult.error("QQ号格式不正确!");
        }

        // 3. 查询旧ID对应的白名单信息
        WhitelistInfo query = new WhitelistInfo();
        query.setUserName(changeRequest.getOldUserName().toLowerCase());
        List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);

        if (whitelistInfos == null || whitelistInfos.isEmpty()) {
            return AjaxResult.error("未找到该游戏ID的白名单记录!");
        }

        WhitelistInfo whitelistInfo = whitelistInfos.getFirst();

        // 4. 验证QQ号是否匹配
        if (!whitelistInfo.getQqNum().equals(changeRequest.getQqNum())) {
            return AjaxResult.error("QQ号与该游戏ID不匹配!");
        }

        // 5. 检查新ID是否已存在
        WhitelistInfo newIdQuery = new WhitelistInfo();
        newIdQuery.setUserName(changeRequest.getNewUserName().toLowerCase());
        List<WhitelistInfo> newIdList = whitelistInfoService.selectWhitelistInfoList(newIdQuery);
        if (newIdList != null && !newIdList.isEmpty()) {
            return AjaxResult.error("新游戏ID已存在白名单中!");
        }

        // 6. 检查是否有活跃的验证码（防止重复申请）
        if (SecureCodeUtil.hasActiveCode(changeRequest.getQqNum(), CacheKey.CHANGE_ID_KEY)) {
            return AjaxResult.error("您已有一个待验证的更改请求，请勿重复申请！");
        }

        // 7. 生成验证码
        String code = SecureCodeUtil.generateNumericCode(
                changeRequest.getQqNum(),
                CacheKey.CHANGE_ID_KEY,
                6,
                30
        );

        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证码生成失败,请稍后再试!");
        }

        // 8. 缓存更改请求数据
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("oldUserName", changeRequest.getOldUserName().toLowerCase());
        cacheData.put("newUserName", changeRequest.getNewUserName().toLowerCase());
        cacheData.put("qqNum", changeRequest.getQqNum());
        cacheData.put("changeReason", changeRequest.getChangeReason());
        cacheData.put("whitelistId", whitelistInfo.getId());
        redisCache.setCacheObject(CacheKey.CHANGE_ID_KEY + code, cacheData, 30, TimeUnit.MINUTES);

        // 9. 标记该QQ号有活跃的验证码
        SecureCodeUtil.markActiveCode(changeRequest.getQqNum(), CacheKey.CHANGE_ID_KEY, 30);

        // 10. 发送验证邮件
        String verifyContent = String.format(
                "您好！<br><br>" +
                        "您正在申请更改白名单游戏ID：<br>" +
                        "旧ID：%s<br>" +
                        "新ID：%s<br><br>" +
                        "您的验证码是：<strong style='font-size: 24px; color: #409EFF;'>%s</strong><br><br>" +
                        "请在30分钟内使用此验证码完成更改。<br>" +
                        "如果这不是您的操作，请忽略此邮件。",
                changeRequest.getOldUserName(),
                changeRequest.getNewUserName(),
                code
        );

        try {
            emailService.push(changeRequest.getQqNum() + EmailTemplates.QQ_EMAIL,
                    "白名单ID更改验证", verifyContent);
        } catch (Exception e) {
            log.error("发送验证邮件失败", e);
            return AjaxResult.error("发送验证邮件失败，请稍后重试!");
        }

        return AjaxResult.success("验证码已发送到您的QQ邮箱，请查收!");
    }

    /**
     * 确认更改游戏ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult confirmChangeId(HttpServletRequest request, String code, String qqNum) {
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(qqNum)) {
            return AjaxResult.error("验证码和QQ号不能为空!");
        }

        // 1. 验证验证码和QQ号是否匹配
        if (!SecureCodeUtil.verifyCode(code, CacheKey.CHANGE_ID_KEY, qqNum)) {
            return AjaxResult.error("验证码无效、已过期或QQ号不匹配!");
        }

        // 2. 获取缓存数据
        String cacheKey = CacheKey.CHANGE_ID_KEY + code;
        if (!redisCache.hasKey(cacheKey)) {
            return AjaxResult.error("验证失败，数据不存在!");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> cacheData = redisCache.getCacheObject(cacheKey);
        if (cacheData == null) {
            return AjaxResult.error("验证失败，数据不存在!");
        }

        String oldUserName = (String) cacheData.get("oldUserName");
        String newUserName = (String) cacheData.get("newUserName");
        String cachedQqNum = (String) cacheData.get("qqNum");
        String changeReason = (String) cacheData.get("changeReason");
        Long whitelistId = Long.valueOf(cacheData.get("whitelistId").toString());

        // 3. 二次验证QQ号
        if (!cachedQqNum.equals(qqNum)) {
            return AjaxResult.error("QQ号验证失败!");
        }

        // 4. 再次检查新ID是否已存在（防止并发）
        WhitelistInfo newIdQuery = new WhitelistInfo();
        newIdQuery.setUserName(newUserName);
        List<WhitelistInfo> newIdList = whitelistInfoService.selectWhitelistInfoList(newIdQuery);
        if (newIdList != null && !newIdList.isEmpty()) {
            // 清理缓存
            redisCache.deleteObject(cacheKey);
            SecureCodeUtil.deleteCode(code, CacheKey.CHANGE_ID_KEY);
            SecureCodeUtil.clearActiveCode(qqNum, CacheKey.CHANGE_ID_KEY);
            return AjaxResult.error("新游戏ID已存在白名单中!");
        }

        // 5. 获取原白名单信息
        WhitelistInfo whitelistInfo = whitelistInfoService.selectWhitelistInfoById(whitelistId);
        if (whitelistInfo == null) {
            redisCache.deleteObject(cacheKey);
            SecureCodeUtil.deleteCode(code, CacheKey.CHANGE_ID_KEY);
            SecureCodeUtil.clearActiveCode(qqNum, CacheKey.CHANGE_ID_KEY);
            return AjaxResult.error("白名单记录不存在!");
        }

        String oldUuid = whitelistInfo.getUserUuid();

        // 6. 更新白名单信息
        boolean isOnline = whitelistInfo.getOnlineFlag() == 1;
        String newUuid = MinecraftUUIDUtil.getPlayerUUID(newUserName, isOnline);

        whitelistInfo.setUserName(newUserName);
        whitelistInfo.setUserUuid(newUuid);
        whitelistInfo.setUpdateBy("CHANGE_ID::" + oldUserName + "->" + newUserName);
        whitelistInfo.setUpdateTime(new Date());

        int updateResult = whitelistInfoService.updateWhitelistInfo(whitelistInfo, "SYSTEM");
        if (updateResult == 0) {
            return AjaxResult.error("更新白名单失败!");
        }

        // 7. 更新玩家详情
        PlayerDetails playerQuery = new PlayerDetails();
        playerQuery.setUserName(oldUserName);
        List<PlayerDetails> playerList = playerDetailsService.selectPlayerDetailsList(playerQuery);
        if (playerList != null && !playerList.isEmpty()) {
            PlayerDetails playerDetails = playerList.getFirst();
            playerDetails.setUserName(newUserName);
            playerDetails.setUpdateBy("CHANGE_ID::" + oldUserName + "->" + newUserName);
            playerDetails.setUpdateTime(new Date());
            playerDetailsService.updatePlayerDetails(playerDetails, true);
        }

        // 8. 记录更改历史
        String ip = IPUtils.getClientIpAddress(request, ipHeaderName);
        WhitelistIdChangeHistory history = new WhitelistIdChangeHistory();
        history.setOldUserName(oldUserName);
        history.setNewUserName(newUserName);
        history.setOldUserUuid(oldUuid);
        history.setNewUserUuid(newUuid);
        history.setQqNum(qqNum);
        history.setChangeReason(changeReason);
        history.setChangeTime(new Date());
        history.setIpAddress(ip);
        history.setStatus("1");
        history.setCreateBy("CHANGE_ID::" + oldUserName);
        whitelistIdChangeHistoryService.insertWhitelistIdChangeHistory(history);

        // 9. 清理所有相关缓存
        redisCache.deleteObject(cacheKey);
        SecureCodeUtil.deleteCode(code, CacheKey.CHANGE_ID_KEY);
        SecureCodeUtil.clearActiveCode(qqNum, CacheKey.CHANGE_ID_KEY);

        // 10. 发送通知邮件
        asyncManager.execute(new TimerTask() {
            @Override
            public void run() {
                String notifyContent = String.format(
                        "您好！<br><br>" +
                                "您的白名单游戏ID已成功更改：<br>" +
                                "旧ID：%s<br>" +
                                "新ID：%s<br>" +
                                "更改时间：%s<br><br>" +
                                "如果这不是您的操作，请立即联系管理员！",
                        oldUserName,
                        newUserName,
                        DateUtils.getTime()
                );
                try {
                    emailService.push(qqNum + EmailTemplates.QQ_EMAIL,
                            "白名单ID更改成功通知", notifyContent);
                } catch (Exception e) {
                    log.error("发送通知邮件失败", e);
                }
            }
        });

        return AjaxResult.success("游戏ID更改成功!");
    }

    /**
     * 检查白名单
     */
    @Override
    public Map<String, Object> check(Map<String, String> params) {
        return whitelistInfoService.check(params);
    }

    /**
     * 根据游戏ID获取可用服务器列表
     */
    @Override
    public AjaxResult getServerInfoByGameId(String gameId) {
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setUserName(gameId);
        final List<WhitelistInfo> list = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (list == null || list.isEmpty()) {
            return AjaxResult.error("抱歉，您未在白名单！");
        }
        whitelistInfo = list.getFirst();
        if (!whitelistInfo.getStatus().equals("1")) {
            return AjaxResult.error("抱歉，您未在白名单！");
        }
        if (whitelistInfo.getServers() == null || whitelistInfo.getServers().isEmpty()) {
            return AjaxResult.error("抱歉，您未分配服务器！");
        }
        // 获取已知存活服务器主键
        final Set<String> keySet = RconCache.getMap().keySet();
        // 获取所有服务器
        Map<String, Object> serverInfoMap;

        // 先从缓存获取服务器信息
        if (redisCache.hasKey(CacheKey.SERVER_INFO_MAP_KEY)) {
            serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
            log.debug("从缓存获取服务器信息成功");
        } else {
            // 缓存不存在，从数据库查询并更新缓存
            serverInfoMap = new HashMap<>();
            final List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(new ServerInfo());
            for (ServerInfo serverInfo : serverInfos) {
                serverInfoMap.put(serverInfo.getId().toString(), serverInfo);
            }
            // 更新缓存
            redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, serverInfoMap);
            log.debug("从数据库获取服务器信息并更新缓存");
        }

        List<Object> server = new ArrayList<>();
        if (!whitelistInfo.getServers().contains("all")) {
            for (String s : whitelistInfo.getServers().split(",")) {
                if (keySet.contains(s) && serverInfoMap.containsKey(s)) {
                    server.add(serverInfoMap.get(s));
                }
            }
        } else {
            for (String s : keySet) {
                if (serverInfoMap.containsKey(s)) {
                    server.add(serverInfoMap.get(s));
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object serverObj : server) {
            Map<String, Object> data = new HashMap<>();
            // 使用Map处理对象，避免类型转换问题
            Map<String, Object> serverMap = (Map<String, Object>) serverObj;
            data.put("nameTag", serverMap.get("nameTag"));
            data.put("ip", serverMap.get("playAddress"));
            data.put("port", serverMap.get("playAddressPort"));
            data.put("version", serverMap.get("serverVersion"));
            data.put("core", serverMap.get("serverCore"));
            data.put("up_time", serverMap.get("createTime"));
            data.put("status", "OK");
            result.add(data);
        }
        return AjaxResult.success(result);
    }

    /**
     * 验证申请参数
     */
    private AjaxResult validateApplyParams(WhitelistInfo whitelistInfo) {
        if (whitelistInfo == null || whitelistInfo.getUserName() == null || whitelistInfo.getQqNum() == null) {
            return AjaxResult.error("申请信息不能为空!");
        }
        return null;
    }

    /**
     * 验证User-Agent
     */
    private AjaxResult validateUserAgent(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return AjaxResult.error("请勿使用爬虫提交申请!");
        }

        // 黑名单检查
        String[] blackList = {
                "okhttp", "Postman", "curl", "python", "Go-http-client", "Java",
                "HttpClient", "Apache-HttpClient", "httpunit", "webclient",
                "webharvest", "wget", "libwww", "htmlunit", "pangolin"
        };
        for (String blocked : blackList) {
            if (userAgent.contains(blocked)) {
                return AjaxResult.error("请勿使用爬虫提交申请!");
            }
        }

        // 浏览器白名单检查
        String[] browserList = {"Mozilla", "Chrome", "Safari", "Edge", "Opera", "Firefox"};
        boolean isBrowser = false;
        for (String browser : browserList) {
            if (userAgent.contains(browser)) {
                isBrowser = true;
                break;
            }
        }
        if (!isBrowser) {
            return AjaxResult.error("请使用浏览器提交申请!");
        }

        return null;
    }

    /**
     * 验证输入格式
     */
    private AjaxResult validateInputFormat(WhitelistInfo whitelistInfo) {
        // 游戏ID正则匹配
        Pattern gameIdPattern = Pattern.compile("[a-zA-Z0-9_]{1,35}");
        if (!gameIdPattern.matcher(whitelistInfo.getUserName()).matches()) {
            return AjaxResult.error("游戏ID不合法!");
        }

        // QQ号正则匹配
        Pattern qqPattern = Pattern.compile("[0-9]{5,11}");
        if (!qqPattern.matcher(whitelistInfo.getQqNum()).matches()) {
            return AjaxResult.error("QQ号不合法!");
        }

        return null;
    }

    /**
     * 检查重复申请
     */
    private AjaxResult checkRepeatApplication(WhitelistInfo whitelistInfo) {
        List<WhitelistInfo> existingApplications = whitelistInfoService.checkRepeat(whitelistInfo);
        if (existingApplications.isEmpty()) {
            return null;
        }

        WhitelistInfo existing = existingApplications.getFirst();
        return switch (existing.getAddState()) {
            case "1" -> AjaxResult.success(String.format("用户:[%s]的提交已于 [%s] 日通过审核,审核人:[%s]",
                    existing.getUserName(),
                    dateFormat.format(existing.getAddTime()),
                    existing.getReviewUsers()));
            case "2" ->
                    AjaxResult.success(String.format("用户:[%s]的审核已于 [%s] 日被移除白名单,请规范游戏!如有疑问联系管理员",
                            existing.getUserName(),
                            dateFormat.format(existing.getAddTime())));
            default -> AjaxResult.success("正在审核,请勿重复提交申请~ 如有纰漏或加急请联系管理员!");
        };
    }

    /**
     * IP限流检查（申请阶段）
     */
    @SneakyThrows
    private AjaxResult checkIpLimitForApply(String ip, WhitelistInfo whitelistInfo, String userAgent) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String bodyParams = mapper.writeValueAsString(whitelistInfo);

        return WhitelistUtils.checkIpLimit(ip, iIpLimitInfoService, iplimit,
                whitelistInfo.getUserName(), userAgent, bodyParams);
    }

    /**
     * 验证验证码
     */
    private AjaxResult validateVerifyCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return AjaxResult.error("验证码生成失败,请稍后再试!");
        }
        if ("isExist".equals(code)) {
            return AjaxResult.error("请勿重复申请！");
        }
        return null;
    }

    /**
     * 创建玩家详情（申请阶段）
     */
    private PlayerDetails createPlayerDetailsForApply(WhitelistInfo whitelistInfo, String ip) {
        PlayerDetails details = new PlayerDetails();
        details.setUserName(whitelistInfo.getUserName());
        details.setQq(whitelistInfo.getQqNum());
        details.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
        details.setCreateTime(new Date());
        details.setIdentity(Identity.PLAYER.getValue());
        details.setGameTime(0L);

        // 获取地理位置（使用带缓存的方法）
        if (StringUtils.isNotEmpty(ip)) {
            String[] location = WhitelistUtils.getIpLocationWithCache(ip);
            if (location[0] != null) {
                details.setProvince(location[0]);
            }
            if (location[1] != null) {
                details.setCity(location[1]);
            }
        }

        return details;
    }

    /**
     * 缓存申请数据
     */
    private void cacheApplyData(String code, WhitelistInfo whitelistInfo, PlayerDetails details) {
        Map<String, Object> data = new HashMap<>();
        data.put("whitelistInfo", whitelistInfo);
        data.put("details", details);
        redisCache.setCacheObject(CacheKey.VERIFY_KEY + code, data, 30, TimeUnit.MINUTES);
    }

    /**
     * 构建验证链接
     */
    private String buildVerifyUrl(Map<String, String> header, String code) {
        String baseUrl = appUrl;

        // 从header获取前端地址
        if (header.containsKey("origon")) {
            baseUrl = header.get("origon");
        } else if (header.containsKey("referer")) {
            baseUrl = header.get("referer");
        }

        // 去掉末尾的斜杠
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/verify?code=" + code;
    }

    /**
     * 检查验证码并确定来源
     */
    private VerifySource checkVerifyCode(String code) {
        String webKey = CacheKey.VERIFY_KEY + code;
        String botKey = CacheKey.VERIFY_FOR_BOT_KEY + code;
        String batchKey = CacheKey.VERIFY_FOR_BATCH_KEY + code;

        if (redisCache.hasKey(webKey)) {
            return new VerifySource(webKey, "网页", false, false);
        } else if (redisCache.hasKey(botKey)) {
            return new VerifySource(botKey, "机器人", true, false);
        } else if (redisCache.hasKey(batchKey)) {
            return new VerifySource(batchKey, "批量", false, true);
        }
        return null;
    }

    /**
     * 解析申请数据
     */
    @SuppressWarnings("unchecked")
    private ApplyData parseApplyData(VerifySource verifySource) {
        Object cacheData = redisCache.getCacheObject(verifySource.getCacheKey());
        if (cacheData == null) {
            throw new RuntimeException("缓存数据为空");
        }

        WhitelistInfo whitelistInfo;
        PlayerDetails details;

        if (!verifySource.isFromBot() && !verifySource.isFromBatch()) {
            // Web端申请
            Map<String, Object> data = (Map<String, Object>) cacheData;
            whitelistInfo = ((JSONObject) data.get("whitelistInfo")).toJavaObject(WhitelistInfo.class);
            details = ((JSONObject) data.get("details")).toJavaObject(PlayerDetails.class);
        } else {
            // 机器人或批量申请
            whitelistInfo = (WhitelistInfo) cacheData;
            details = createPlayerDetails(whitelistInfo, verifySource);
        }

        return new ApplyData(whitelistInfo, details);
    }

    /**
     * 创建玩家详情
     */
    private PlayerDetails createPlayerDetails(WhitelistInfo whitelistInfo, VerifySource verifySource) {
        PlayerDetails details = new PlayerDetails();
        details.setUserName(whitelistInfo.getUserName());
        details.setQq(whitelistInfo.getQqNum());
        details.setCreateTime(new Date());
        details.setIdentity(Identity.PLAYER.getValue());
        details.setGameTime(0L);
        details.setCreateBy(verifySource.isFromBatch() ?
                "BATCH::apply::" + whitelistInfo.getUserName() :
                "BOT::apply::" + whitelistInfo.getUserName());
        return details;
    }

    /**
     * IP限流检查（验证阶段）
     */
    private AjaxResult checkIpLimitForVerify(String ip, WhitelistInfo whitelistInfo, Map<String, String> header) {
        if (StringUtils.isEmpty(ip)) {
            return null;
        }
        return WhitelistUtils.checkIpLimit(ip, iIpLimitInfoService, iplimit,
                whitelistInfo.getUserName(), header.get("user-agent"), null);
    }

    /**
     * 更新地理位置信息
     */
    private void updateLocationInfo(String ip, PlayerDetails details) {
        if (StringUtils.isEmpty(ip) || details == null) {
            return;
        }

        String[] location = WhitelistUtils.getIpLocationWithCache(ip);
        if (location[0] != null) {
            details.setProvince(location[0]);
        }
        if (location[1] != null) {
            details.setCity(location[1]);
        }
    }

    /**
     * 完善白名单信息
     */
    private void completeWhitelistInfo(WhitelistInfo whitelistInfo, String source) {
        // 生成UUID
        boolean isOnline = whitelistInfo.getOnlineFlag() == 1;
        String uuid = MinecraftUUIDUtil.getPlayerUUID(whitelistInfo.getUserName(), isOnline);
        whitelistInfo.setUserUuid(uuid);

        // 设置创建信息
        String prefix = switch (source) {
            case "机器人" -> "BOT";
            case "批量" -> "BATCH";
            default -> "WEB";
        };
        whitelistInfo.setCreateBy(prefix + "::apply::" + whitelistInfo.getUserName());

        // 设置时间和状态
        Date now = new Date();
        whitelistInfo.setCreateTime(now);
        whitelistInfo.setAddTime(now);
        whitelistInfo.setTime(now);
        whitelistInfo.setAddState("0");
        whitelistInfo.setStatus("0");
    }

    /**
     * 检查是否自动审核通过
     */
    private boolean checkAutoApproval(WhitelistInfo whitelistInfo) {
        // 检查答题功能是否开启
        if (!isQuizEnabled()) {
            log.info("答题功能未开启，跳过自动审批检查");
            return false;
        }

        // 检查自动通过功能是否启用
        if (!isAutoPassEnabled()) {
            return false;
        }

        // 检查玩家答题记录
        WhitelistQuizSubmission latestSubmission = getLatestQuizSubmission(whitelistInfo.getUserName());
        if (latestSubmission == null || latestSubmission.getPassStatus() == null || latestSubmission.getPassStatus() != 1) {
            return false;
        }

        // 自动审核通过
        whitelistInfo.setStatus("1");
        whitelistInfo.setReviewUsers("System(Auto)");
        whitelistInfo.setUpdateTime(new Date());
        log.info("用户[{}]的白名单申请已自动通过审核，答题分数：{}",
                whitelistInfo.getUserName(), latestSubmission.getTotalScore());
        return true;
    }

    /**
     * 检查答题功能是否开启 - 使用缓存
     */
    private boolean isQuizEnabled() {
        return quizConfigCache.isQuizEnabled();
    }

    /**
     * 检查自动通过功能是否启用 - 使用缓存
     */
    private boolean isAutoPassEnabled() {
        return quizConfigCache.isAutoPassEnabled();
    }

    /**
     * 获取最新的答题记录
     */
    private WhitelistQuizSubmission getLatestQuizSubmission(String playerName) {
        WhitelistQuizSubmission query = new WhitelistQuizSubmission();
        query.setPlayerName(playerName);
        List<WhitelistQuizSubmission> submissions = quizSubmissionService.selectWhitelistQuizSubmissionList(query);

        if (submissions == null || submissions.isEmpty()) {
            return null;
        }

        // 找到最新的提交
        WhitelistQuizSubmission latest = submissions.getFirst();
        for (WhitelistQuizSubmission sub : submissions) {
            if (sub.getSubmitTime() != null &&
                    (latest.getSubmitTime() == null || sub.getSubmitTime().after(latest.getSubmitTime()))) {
                latest = sub;
            }
        }
        return latest;
    }

    /**
     * 发送通知（异步）
     */
    private void sendNotifications(WhitelistInfo whitelistInfo, PlayerDetails details, String source, boolean autoApproved) {
        // 通知申请人
        asyncManager.execute(new TimerTask() {
            @Override
            public void run() {
                sendApplicantNotification(whitelistInfo, autoApproved);
            }
        });

        // 通知管理员
        asyncManager.execute(new TimerTask() {
            @Override
            public void run() {
                sendAdminNotification(whitelistInfo, autoApproved);
            }
        });
    }

    /**
     * 发送申请人通知
     */
    private void sendApplicantNotification(WhitelistInfo whitelistInfo, boolean autoApproved) {
        try {
            String emailContent;
            if (autoApproved) {
                emailContent = EmailTemplates.getWhitelistNotificationPending(
                        whitelistInfo.getQqNum(),
                        whitelistInfo.getUserName(),
                        DateUtils.getTime(),
                        true,
                        "default"
                ).replace("正在审核中", "已自动审核通过");
            } else {
                emailContent = EmailTemplates.getWhitelistNotificationPending(
                        whitelistInfo.getQqNum(),
                        whitelistInfo.getUserName(),
                        DateUtils.getTime(),
                        false,
                        "default"
                );
            }
            emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                    EmailTemplates.TITLE, emailContent);
        } catch (Exception e) {
            log.error("发送申请人通知失败", e);
        }
    }

    /**
     * 发送管理员通知
     */
    private void sendAdminNotification(WhitelistInfo whitelistInfo, boolean autoApproved) {
        try {
            String reviewTemplate = EmailTemplates.getReviewTemplate(
                    whitelistInfo.getQqNum(),
                    whitelistInfo.getUserName(),
                    DateUtils.getTime(),
                    autoApproved
            );

            String fallbackMessage = autoApproved ?
                    "用户 [" + whitelistInfo.getUserName() + "] 的白名单申请已被系统自动审核通过!" :
                    "用户 [" + whitelistInfo.getUserName() + "] 提交了白名单申请,请尽快审核!";

            emailService.push(ADMIN_EMAIL, EmailTemplates.TITLE,
                    reviewTemplate != null ? reviewTemplate : fallbackMessage);
        } catch (Exception e) {
            log.error("发送管理员通知失败", e);
        }
    }

}
