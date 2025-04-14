package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.MapCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.config.QuestionConfig;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.quiz.*;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import cc.endmc.server.service.quiz.IWhitelistQuizQuestionService;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.MinecraftUUIDUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 公共接口
 * 用于提供一些公共的接口
 * 例如: 聚合查询, 获取白名单列表等
 */
@RestController
@RequestMapping("/api/v1")
public class PublicInterfaceController extends BaseController {

    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 每秒最多10个请求

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private IWhitelistQuizConfigService quizConfigService;

    @Autowired
    private IWhitelistQuizQuestionService quizQuestionService;

    @Autowired
    private IWhitelistQuizSubmissionService quizSubmissionService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 聚合查询
     *
     * @return AjaxResult
     */
    @GetMapping("/aggregateQuery")
    public AjaxResult aggregateQuery() {
        final Map<String, Object> result = serverInfoService.aggregateQuery();
        if (!result.isEmpty()) {
            return success(result);
        } else {
            return error("服务器为空");
        }

    }

    @GetMapping("getWhiteListForServer")
    public AjaxResult getWhiteListForServer() {
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            return error("服务器繁忙,请稍后再试");
        }

        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                // logger.info("获取白名单列表缓存");
                return success(cacheObject);
            }

            Map<String, String> map = new HashMap<>();
            MapCache.getMap().forEach((k, v) -> {
                final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                try {
                    final String list = v.sendCommand("whitelist list");
                    String[] split = new String[0];
                    if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
                        split = list.split("whitelisted player\\(s\\):")[1].trim().split(", ");
                    }
                    map.put(nameTag, Arrays.toString(split));
                } catch (Exception e) {
                    logger.error("获取白名单列表失败, serverId: {}", k, e);
                    // map.put(nameTag, "获取失败"); // 不要因为单个服务器失败影响整体
                }
            });

            // 更新缓存
            if (!map.isEmpty()) {
                logger.info("更新白名单列表缓存");
                redisCache.setCacheObject(CacheKey.WHITE_LIST_KEY, map, 5, TimeUnit.MINUTES);
            }
            return success(map);

        } catch (Exception e) {
            logger.error("获取白名单列表发生异常", e);
            return error("系统繁忙,请稍后重试");
        }
    }

    // 查询服务器在线人数
    @GetMapping("/getOnlinePlayer")
    public AjaxResult getOnlinePlayer() {
        return success(serverInfoService.getOnlinePlayer());
    }

    /**
     * 从数据库获取白名单列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getWhiteList")
    public AjaxResult getWhiteList() {
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            return error("服务器繁忙,请稍后再试");
        }
        Map<String, String> result = new HashMap<>();

        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                return success(cacheObject);
            }

            // 查询已通过审核且已添加的白名单用户
            WhitelistInfo query = new WhitelistInfo();
            query.setStatus("1"); // 已审核通过
            query.setAddState("1"); // 已添加
            final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);

            if (whitelistInfos.isEmpty()) {
                return error("服务器白名单为空");
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
                return success(result);
            }

            cache.forEach((String k, Set<String> v) -> {
                if (MapCache.containsKey(k)) {  // 只查询活跃服务器
                    final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                    result.put(nameTag, Arrays.toString(v.toArray()));
                }
            });

        } catch (Exception e) {
            logger.error("获取白名单列表发生异常", e);
            return error("系统繁忙,请稍后重试");
        }
        return success(result);
    }

    /**
     * 获取白名单答题列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getQuestions")
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
            }
        }

        return success(questions);
    }

    /**
     * 检查答题状态
     *
     * @param code 验证码
     * @return AjaxResult
     */
    @GetMapping("/checkQuizStatus")
    public AjaxResult checkQuizStatus(@RequestParam String code) {
        // 从缓存中获取验证信息
        Map<String, Object> cache = redisCache.getCacheObject(CacheKey.VERIFY_KEY + code);
        if (cache == null) {
            cache = redisCache.getCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code);
        }

        // 验证码不存在或已失效
        if (cache == null || cache.isEmpty()) {
            return error("验证码已失效");
        }

        // 获取白名单信息
        JSONObject whitelistInfoJson = (JSONObject) cache.get("whitelistInfo");
        if (whitelistInfoJson == null) {
            return error("验证信息不完整");
        }

        final WhitelistInfo whitelistInfo = whitelistInfoJson.toJavaObject(WhitelistInfo.class);

        // 查询答题记录
        final WhitelistQuizSubmission whitelistQuizSubmission = new WhitelistQuizSubmission();
        whitelistQuizSubmission.setPlayerName(whitelistInfo.getUserName());
        final List<WhitelistQuizSubmission> submissions = quizSubmissionService.selectWhitelistQuizSubmissionList(whitelistQuizSubmission);

        // 判断是否完成问卷
        if (submissions == null || submissions.isEmpty()) {
            return success("未完成问卷");
        }

        // 已完成问卷
        return success("已完成问卷");
    }

    /**
     * 提交答题
     *
     * @param param 答题信息
     * @return AjaxResult
     */
    @PostMapping("/submitQuiz")
    public AjaxResult submitQuiz(@RequestBody JSONObject param) {

        if (param.isEmpty()) {
            return error("参数不能为空");
        }
        final String code = param.getString("code");
        final JSONArray answers = param.getJSONArray("answers");

        if (StringUtils.isEmpty(code)) {
            return error("验证码不能为空");
        }

        // 从缓存中获取验证信息
        Map<String, Object> cache = redisCache.getCacheObject(CacheKey.VERIFY_KEY + code);
        if (cache == null) {
            cache = redisCache.getCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code);
        }

        // 验证码不存在或已失效
        if (cache == null || cache.isEmpty()) {
            return error("验证码已失效");
        }

        // 获取白名单信息
        JSONObject whitelistInfoJson = (JSONObject) cache.get("whitelistInfo");
        if (whitelistInfoJson == null) {
            return error("验证信息不完整");
        }

        final WhitelistInfo whitelistInfo = whitelistInfoJson.toJavaObject(WhitelistInfo.class);

        // 检查该用户是否已经提交过问卷
        WhitelistQuizSubmission existingSubmission = new WhitelistQuizSubmission();
        existingSubmission.setPlayerName(whitelistInfo.getUserName());
        List<WhitelistQuizSubmission> existingSubmissions = quizSubmissionService.selectWhitelistQuizSubmissionList(existingSubmission);
        if (existingSubmissions != null && !existingSubmissions.isEmpty()) {
            return error("您已经提交过问卷");
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
            return error("没有有效的答题记录");
        }

        // 查询所有题目信息
        final List<WhitelistQuizQuestion> questions = quizQuestionService.selectWhitelistQuizQuestionByIds(questionIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList()));
        if (questions.isEmpty()) {
            return error("未找到有效题目信息");
        }

        // 创建提交记录
        WhitelistQuizSubmission submission = new WhitelistQuizSubmission();
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

        // 遍历所有答案
        for (int i = 0; i < answers.size(); i++) {
            JSONObject answerObj = answers.getJSONObject(i);
            Long questionId = answerObj.getLong("questionId");
            String playerAnswer = answerObj.getString("answer");

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
            if (question.getWhitelistQuizAnswerList() != null && !question.getWhitelistQuizAnswerList().isEmpty()) {
                // 处理单选题和多选题
                if (question.getQuestionType() == 1 || question.getQuestionType() == 2) {
                    String[] selectedAnswers = playerAnswer.split(",");
                    boolean isCorrect = true;
                    double score = 0.0;

                    // 获取所有正确答案
                    List<WhitelistQuizAnswer> correctAnswers = question.getWhitelistQuizAnswerList().stream()
                            .filter(a -> a.getIsCorrect() == 1)
                            .collect(Collectors.toList());

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
            long passScore = Long.parseLong(passConfigs.get(0).getConfigValue());
            if (totalScore >= passScore) {
                submission.setPassStatus(1); // 已通过
                submission.setReviewer("System(Auto)"); // 自动审核
            }
        }

        // 设置详情列表
        submission.setWhitelistQuizSubmissionDetailList(detailList);

        // 保存提交记录
        int result = quizSubmissionService.insertWhitelistQuizSubmission(submission);

        if (result > 0) {
            return success("提交成功");
        } else {
            return error("提交失败");
        }
    }
}