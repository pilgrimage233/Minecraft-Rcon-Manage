package cc.endmc.server.service.open.impl.v1;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.config.QuestionConfig;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.player.vo.PlayerDetailsVo;
import cc.endmc.server.domain.quiz.*;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;
import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.model.MinecraftServerInfo;
import cc.endmc.server.service.message.AsyncMessagePushService;
import cc.endmc.server.service.open.IOpenApiService;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import cc.endmc.server.service.quiz.IWhitelistQuizQuestionService;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import cc.endmc.server.service.relation.IRconNodeInstanceRelationService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.MinecraftUUIDUtil;
import cc.endmc.server.utils.NetWorkUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Quiz相关服务
    private final IWhitelistQuizSubmissionService quizSubmissionService;
    private final IWhitelistQuizQuestionService quizQuestionService;
    private final IWhitelistQuizConfigService quizConfigService;

    // 权限相关服务
    private final IWhitelistInfoService whitelistInfoService;
    private final IOperatorListService operatorListService;
    private final IBanlistInfoService banlistInfoService;

    // 服务器相关服务
    private final IServerInfoService serverInfoService;
    private final INodeServerService nodeServerService;
    private final INodeMinecraftServerService nodeMinecraftServerService;
    private final IRconNodeInstanceRelationService rconNodeInstanceRelationService;

    // 异步消息推送服务
    private final AsyncMessagePushService asyncMessagePushService;

    // Mapper
    private final WhitelistInfoMapper whitelistInfoMapper;
    private final PlayerDetailsMapper playerDetailsMapper;
    private final ServerInfoMapper serverInfoMapper;

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

        // 检查是否通过及格线
        WhitelistQuizConfig passScoreConfig = new WhitelistQuizConfig();
        passScoreConfig.setConfigKey(QuestionConfig.PASS_SCORE);
        List<WhitelistQuizConfig> passConfigs = quizConfigService.selectWhitelistQuizConfigList(passScoreConfig);
        if (!passConfigs.isEmpty()) {
            long passScore = Long.parseLong(passConfigs.getFirst().getConfigValue());
            if (totalScore >= passScore) {
                submission.setPassStatus(1); // 已通过
                submission.setReviewer("System(Auto_Quiz_Pass)"); // 自动审核
            }
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
        final WhitelistQuizConfig whitelistQuizConfig = new WhitelistQuizConfig();
        // 答题功能开启才能查询
        whitelistQuizConfig.setConfigKey(QuestionConfig.STATUS);
        whitelistQuizConfig.setConfigValue(Boolean.TRUE.toString());
        AtomicBoolean random = new AtomicBoolean(false);
        AtomicInteger questionCount = new AtomicInteger(0);
        List<WhitelistQuizQuestionVo> questions = new ArrayList<>();


        if (!quizConfigService.selectWhitelistQuizConfigList(whitelistQuizConfig).isEmpty()) {
            final WhitelistQuizQuestion question = new WhitelistQuizQuestion();
            question.setStatus(1);

            // 查询配置
            final List<WhitelistQuizConfig> configs = quizConfigService.selectWhitelistQuizConfigList(new WhitelistQuizConfig());
            configs.forEach(config -> {
                if (config.getConfigKey().equals(QuestionConfig.RANDOM)) {
                    random.set(Boolean.parseBoolean(config.getConfigValue()));
                }
                if (config.getConfigKey().equals(QuestionConfig.QUESTION_COUNT)) {
                    questionCount.set(Integer.parseInt(config.getConfigValue()));
                }
            });

            // 随机抽取问题 - 使用VO查询
            if (random.get()) {
                questions = quizQuestionService.selectWhitelistQuizQuestionVoList(question);
                if (questionCount.get() < questions.size()) {
                    Collections.shuffle(questions);
                    questions = questions.subList(0, questionCount.get());
                }
            }

            if (!random.get() && questionCount.get() > 0) {
                questions = quizQuestionService.selectWhitelistQuizQuestionVoList(question);
                if (questionCount.get() < questions.size()) {
                    questions = questions.subList(0, questionCount.get());
                }
            } else {
                questions = quizQuestionService.selectWhitelistQuizQuestionVoList(question);
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
        List<Map<String, Object>> data = new ArrayList<>();

        for (ServerInfo serverInfo : serverInfos) {
            String cacheKey = CacheKey.MINECRAFT_SERVER_INFO + serverInfo.getId();
            if (redisCache.hasKey(cacheKey)) {
                final Map<String, Object> cacheObject = redisCache.getCacheMap(cacheKey);
                data.add(cacheObject);
                continue;
            }
            Map<String, Object> statusMap = new HashMap<>();
            String nameTag = serverInfo.getNameTag();
            statusMap.put("id", serverInfo.getId());
            statusMap.put("服务器名称", nameTag);
            statusMap.put("连接地址", serverInfo.getPlayAddress());
            statusMap.put("连接端口", String.valueOf(serverInfo.getPlayAddressPort()));
            statusMap.put("版本", serverInfo.getServerVersion());
            statusMap.put("核心", serverInfo.getServerCore());

            final boolean rconConnection = NetWorkUtil.testRconConnection(String.valueOf(serverInfo.getId()));
            statusMap.put("Rcon连接", rconConnection ? "成功" : "失败");

            final MinecraftServerInfo minecraftServerLatency = NetWorkUtil.getMinecraftServerLatency(serverInfo.getPlayAddress(), serverInfo.getPlayAddressPort());
            statusMap.put("在线状态", minecraftServerLatency.isReachable() ? "在线" : "离线");
            statusMap.put("在线人数", String.valueOf(minecraftServerLatency.getOnlinePlayers()));
            statusMap.put("最大人数", String.valueOf(minecraftServerLatency.getMaxPlayers()));
            statusMap.put("延迟(ms)", String.valueOf(minecraftServerLatency.getLatency()));

            final boolean offline = statusMap.get("在线状态").equals("离线");
            if (offline && !rconConnection) {
                statusMap.put("指标", "服务熔断");
            } else if (offline || !rconConnection) {
                statusMap.put("指标", "服务降级");
            } else {
                statusMap.put("指标", "服务正常");
            }
            data.add(statusMap);

            redisCache.setCacheMap(cacheKey, statusMap, 1, TimeUnit.MINUTES);
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

}
