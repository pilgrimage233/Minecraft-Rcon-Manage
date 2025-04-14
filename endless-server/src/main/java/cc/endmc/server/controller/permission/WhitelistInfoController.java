package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Excel;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.async.AsyncManager;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.config.QuestionConfig;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.service.other.IIpLimitInfoService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import cc.endmc.server.utils.CodeUtil;
import cc.endmc.server.utils.MinecraftUUIDUtil;
import cc.endmc.server.utils.WhitelistUtils;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 白名单Controller
 *
 * @author ruoyi
 * @date 2023-12-26
 */
@RestController
@RequestMapping("/mc/whitelist")
public class WhitelistInfoController extends BaseController {

    private final SimpleDateFormat dateFormat;

    private final AsyncManager asyncManager = AsyncManager.getInstance();

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private IIpLimitInfoService iIpLimitInfoService;

    @Autowired
    private IWhitelistQuizConfigService quizConfigService;

    @Autowired
    private IWhitelistQuizSubmissionService quizSubmissionService;

    @Value("${whitelist.iplimit}")
    private String iplimit;

    @Value("${whitelist.email}")
    private String ADMIN_EMAIL;

    @Autowired
    private IPlayerDetailsService playerDetailsService;

    @Autowired
    private EmailService pushEmail;

    @Autowired
    private RedisCache redisCache;

    @Value("${app-url}")
    private String appUrl;
    @Autowired
    private EmailService emailService;

    public WhitelistInfoController() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @SneakyThrows
    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody WhitelistInfo whitelistInfo, @RequestHeader Map<String, String> header) {
        if (whitelistInfo == null || whitelistInfo.getUserName() == null || whitelistInfo.getQqNum() == null) {
            return error("申请信息不能为空!");
        }

        logger.info("申请信息:{}", whitelistInfo);
        logger.info("header:{}", header);

        // 获取IP地址
        String ip = WhitelistUtils.getIpFromHeader(header);

        // 获取UA头
        if (!header.containsKey("user-agent")) {
            return error("请勿使用爬虫提交申请!");
        }
        String userAgent = header.get("user-agent");
        String[] blackList = {"okhttp", "Postman", "curl", "python", "Go-http-client", "Java", "HttpClient", "Apache-HttpClient", "httpunit", "webclient", "webharvest", "wget", "libwww", "htmlunit", "pangolin"};
        for (String s : blackList) {
            if (userAgent.contains(s)) {
                return error("请勿使用爬虫提交申请!");
            }
        }
        // 判断是否为正确浏览器
        if (!userAgent.contains("Mozilla") && !userAgent.contains("Chrome") && !userAgent.contains("Safari") && !userAgent.contains("Edge") && !userAgent.contains("Opera") && !userAgent.contains("Firefox")) {
            return error("请使用浏览器提交申请!");
        }

        // 游戏ID正则匹配
        Pattern p = Pattern.compile("[a-zA-Z0-9_]{1,35}");
        if (!p.matcher(whitelistInfo.getUserName()).matches()) {
            return error("游戏ID不合法!");
        }

        // QQ号正则匹配
        Pattern p2 = Pattern.compile("[0-9]{5,11}");
        if (!p2.matcher(whitelistInfo.getQqNum()).matches()) {
            return error("QQ号不合法!");
        }

        if (!whitelistInfoService.checkRepeat(whitelistInfo).isEmpty()) {
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.checkRepeat(whitelistInfo);
            WhitelistInfo obj = whitelistInfos.get(0);
            switch (obj.getAddState()) {
                case "1":
                    return success("用户:[" + obj.getUserName() + "]的提交已于 [" + dateFormat.format(obj.getAddTime()) + "] 日通过审核,审核人:[" + obj.getReviewUsers() + "]");
                case "2":
                    return success("用户:[" + obj.getUserName() + "]的审核已于 [" + dateFormat.format(obj.getAddTime()) + "] 日被移除白名单,请规范游戏!如有疑问联系管理员");
                default:
                    return success("正在审核,请勿重复提交申请~ 如有纰漏或加急请联系管理员!");
            }
        }

        // IP限流检查
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String bodyParams = mapper.writeValueAsString(whitelistInfo);
        AjaxResult limitResult = WhitelistUtils.checkIpLimit(ip, iIpLimitInfoService, iplimit,
                whitelistInfo.getUserName(), userAgent, bodyParams);
        if (limitResult != null) {
            return limitResult;
        }

        final String code = CodeUtil.generateCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_KEY);

        if (StringUtils.isEmpty(code)) {
            return error("验证码生成失败,请稍后再试!");
        } else if (code != null && code.equals("isExist")) {
            return error("请勿重复申请！");
        }

        // 新增玩家详情
        PlayerDetails details = new PlayerDetails();
        details.setUserName(whitelistInfo.getUserName());
        details.setQq(whitelistInfo.getQqNum());
        details.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
        details.setCreateTime(new Date());
        details.setIdentity(Identity.PLAYER.getValue());
        details.setGameTime(0L);

        // 获取地理位置
        if (StringUtils.isNotEmpty(ip)) {
            String[] location = WhitelistUtils.getIpLocation(ip);
            if (location[0] != null) {
                details.setProvince(location[0]);
            }
            if (location[1] != null) {
                details.setCity(location[1]);
            }
        }

        // 缓存对象,30分钟
        Map<String, Object> data = new HashMap<>();
        data.put("whitelistInfo", whitelistInfo);
        data.put("details", details);
        redisCache.setCacheObject(CacheKey.VERIFY_KEY + code, data, 30, TimeUnit.MINUTES);

        // 获取前端地址，如果有路径就去掉
        if (header.containsKey("origon")) {
            appUrl = header.get("origon");
        } else if (header.containsKey("referer")) {
            appUrl = header.get("referer");
        }

        // 修改验证链接生成逻辑
        if (appUrl.endsWith("/")) {
            appUrl = appUrl.substring(0, appUrl.length() - 1);
        }
        // 改为前端验证页面的地址
        String url = appUrl + "/#/verify?code=" + code;

        // 发送邮件通知
        emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(url));

        return success("验证邮件已发送,请查收! 如果未收到邮件,请检查垃圾箱或联系管理员!");
    }

    /**
     * 验证白名单
     * 此接口不受权限控制！
     *
     * @param code
     * @return
     */
    @GetMapping("/verify")
    public AjaxResult verify(@RequestParam String code, @RequestHeader Map<String, String> header) {
        if (StringUtils.isEmpty(code)) {
            return error("验证失败,请勿直接访问此链接!");
        }

        // 检查验证码是否存在（区分来源）
        String webKey = CacheKey.VERIFY_KEY + code;
        String botKey = CacheKey.VERIFY_FOR_BOT_KEY + code;
        String batchKey = CacheKey.VERIFY_FOR_BATCH_KEY + code;
        String cacheKey = null;
        final boolean isFromBot;
        final boolean isFromBatch;

        if (redisCache.hasKey(webKey)) {
            cacheKey = webKey;
            isFromBot = false;
            isFromBatch = false;
        } else if (redisCache.hasKey(botKey)) {
            cacheKey = botKey;
            isFromBot = true;
            isFromBatch = false;
        } else if (redisCache.hasKey(batchKey)) {
            cacheKey = batchKey;
            isFromBot = false;
            isFromBatch = true;
        } else {
            return error("验证失败,验证码无效!");
        }

        // 获取缓存数据
        Object cacheData = redisCache.getCacheObject(cacheKey);
        if (cacheData == null) {
            return error("验证失败,数据为空!");
        }

        WhitelistInfo whitelistInfo;
        PlayerDetails details = null;

        try {
            // 根据来源处理不同的数据结构
            if (!isFromBot && !isFromBatch) {
                // Web端申请的数据处理
                Map<String, Object> data = (Map<String, Object>) cacheData;
                JSONObject whitelistInfoJson = (JSONObject) data.get("whitelistInfo");
                whitelistInfo = whitelistInfoJson.toJavaObject(WhitelistInfo.class);

                JSONObject detailsJson = (JSONObject) data.get("details");
                details = detailsJson.toJavaObject(PlayerDetails.class);
            } else {
                // QQ机器人+批量 申请的数据处理
                whitelistInfo = (WhitelistInfo) cacheData;

                // 为QQ机器人申请创建PlayerDetails
                details = new PlayerDetails();
                details.setUserName(whitelistInfo.getUserName());
                details.setQq(whitelistInfo.getQqNum());
                details.setCreateTime(new Date());
                details.setIdentity(Identity.PLAYER.getValue());
                details.setGameTime(0L);
                details.setCreateBy(isFromBatch ?
                        "BATCH::apply::" + whitelistInfo.getUserName() :
                        "BOT::apply::" + whitelistInfo.getUserName()
                );
            }

            // 获取IP地址
            String ip = WhitelistUtils.getIpFromHeader(header);

            // IP限流检查
            if (StringUtils.isNotEmpty(ip)) {
                AjaxResult limitResult = WhitelistUtils.checkIpLimit(ip, iIpLimitInfoService, iplimit,
                        whitelistInfo.getUserName(), header.get("user-agent"), null);
                if (limitResult != null) {
                    return limitResult;
                }
            }

            // 如果IP存在且details不为空，获取地理位置
            if (StringUtils.isNotEmpty(ip) && details != null) {
                String[] location = WhitelistUtils.getIpLocation(ip);
                if (location[0] != null) {
                    details.setProvince(location[0]);
                }
                if (location[1] != null) {
                    details.setCity(location[1]);
                }
            }

        } catch (Exception e) {
            logger.error("数据转换失败", e);
            return error("验证失败,数据格式错误!");
        }

        // 保存玩家详情
        if (details != null) {
            playerDetailsService.insertPlayerDetails(details);
        }

        // 补全基础申请信息 - 使用MinecraftUUIDUtil生成UUID
        boolean isOnline = whitelistInfo.getOnlineFlag() == 1;
        String uuid = MinecraftUUIDUtil.getPlayerUUID(whitelistInfo.getUserName(), isOnline);
        whitelistInfo.setUserUuid(uuid);

        String source;
        // 设置创建信息
        if (isFromBot) {
            source = "机器人";
            whitelistInfo.setCreateBy("BOT::apply::" + whitelistInfo.getUserName());
        } else if (isFromBatch) {
            source = "批量";
            whitelistInfo.setCreateBy("BATCH::apply::" + whitelistInfo.getUserName());
        } else {
            source = "网页";
            whitelistInfo.setCreateBy("WEB::apply::" + whitelistInfo.getUserName());
        }

        whitelistInfo.setCreateTime(new Date());
        whitelistInfo.setAddTime(new Date());
        whitelistInfo.setTime(new Date());
        whitelistInfo.setAddState("0"); // 添加状态：0-未添加，1-已添加
        whitelistInfo.setStatus("0"); // 审核状态 0-未审核，1-审核通过，2-审核不通过

        // 检查是否启用了自动通过功能，并检查答题情况
        boolean autoApproved = false;

        // 首先检查答题功能是否开启
        WhitelistQuizConfig quizStatusConfig = new WhitelistQuizConfig();
        quizStatusConfig.setConfigKey(QuestionConfig.STATUS);
        List<WhitelistQuizConfig> statusConfigs = quizConfigService.selectWhitelistQuizConfigList(quizStatusConfig);

        // 只有在答题功能开启的情况下才检查自动通过和答题记录
        if (!statusConfigs.isEmpty() && "true".equalsIgnoreCase(statusConfigs.get(0).getConfigValue())) {
            WhitelistQuizConfig autoPassedConfig = new WhitelistQuizConfig();
            autoPassedConfig.setConfigKey(QuestionConfig.AUTO_PASSED);
            List<WhitelistQuizConfig> autoPassedConfigs = quizConfigService.selectWhitelistQuizConfigList(autoPassedConfig);

            if (!autoPassedConfigs.isEmpty() && "true".equalsIgnoreCase(autoPassedConfigs.get(0).getConfigValue())) {
                // 自动通过功能已启用，检查此玩家的答题记录
                WhitelistQuizSubmission submission = new WhitelistQuizSubmission();
                submission.setPlayerName(whitelistInfo.getUserName());
                List<WhitelistQuizSubmission> submissions = quizSubmissionService.selectWhitelistQuizSubmissionList(submission);

                if (submissions != null && !submissions.isEmpty()) {
                    // 找到最新的一次提交
                    WhitelistQuizSubmission latestSubmission = submissions.get(0);
                    for (WhitelistQuizSubmission sub : submissions) {
                        if (sub.getSubmitTime() != null &&
                                (latestSubmission.getSubmitTime() == null ||
                                        sub.getSubmitTime().after(latestSubmission.getSubmitTime()))) {
                            latestSubmission = sub;
                        }
                    }

                    // 检查是否通过分数线
                    if (latestSubmission.getPassStatus() != null && latestSubmission.getPassStatus() == 1) {
                        // 已通过分数线，自动审核通过
                        whitelistInfo.setStatus("1"); // 审核状态改为通过
                        whitelistInfo.setReviewUsers("System(Auto)");
                        whitelistInfo.setUpdateTime(new Date());
                        autoApproved = true;
                        logger.info("用户[{}]的白名单申请已自动通过审核，答题分数：{}",
                                whitelistInfo.getUserName(), latestSubmission.getTotalScore());
                    }
                }
            }
        } else {
            logger.info("答题功能未开启，跳过自动审批检查");
        }

        // 保存最终的自动审核状态
        final boolean finalAutoApproved = autoApproved;

        if (whitelistInfoService.insertWhitelistInfo(whitelistInfo) != 0) {
            // 删除验证码
            redisCache.deleteObject(cacheKey);

            // 发送邮件通知申请人
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (finalAutoApproved) {
                            // 使用已有的模板，替换内容表明自动通过
                            String emailContent = EmailTemplates.getWhitelistNotificationPending(
                                    whitelistInfo.getQqNum(),
                                    whitelistInfo.getUserName(),
                                    DateUtils.getTime()
                            ).replace("正在审核中", "已自动审核通过");

                            pushEmail.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                                    EmailTemplates.TITLE,
                                    emailContent);
                        } else {
                            pushEmail.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                                    EmailTemplates.TITLE,
                                    EmailTemplates.getWhitelistNotificationPending(
                                            whitelistInfo.getQqNum(),
                                            whitelistInfo.getUserName(),
                                            DateUtils.getTime()
                                    ));
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            asyncManager.execute(timerTask);

            // 通知管理员
            TimerTask timerTask2 = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (finalAutoApproved) {
                            pushEmail.push(ADMIN_EMAIL, EmailTemplates.TITLE,
                                    "用户[" + whitelistInfo.getUserName() + "]通过" + source +
                                            "提交了白名单申请,并已被系统自动审核通过!");
                        } else {
                            pushEmail.push(ADMIN_EMAIL, EmailTemplates.TITLE,
                                    "用户[" + whitelistInfo.getUserName() + "]通过" + source +
                                            "提交了白名单申请,请尽快审核!");
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            asyncManager.execute(timerTask2);

            return success(finalAutoApproved ? "恭喜您！您的白名单申请已自动审核通过！" : EmailTemplates.APPLY_SUCCESS);
        } else {
            return error(EmailTemplates.APPLY_ERROR);
        }
    }

    /**
     * 批量申请白名单
     *
     * @param whitelistInfo 白名单信息
     * @return 结果
     */
    @SneakyThrows
    private boolean batchApply(WhitelistInfo whitelistInfo) {
        final String code = CodeUtil.generateCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BATCH_KEY);
        if (StringUtils.isEmpty(code)) {
            return false;
        } else if (code != null && code.equals("isExist")) {
            return false;
        }
        redisCache.setCacheObject(CacheKey.VERIFY_FOR_BATCH_KEY + code, whitelistInfo, 30, TimeUnit.MINUTES);

        String url = appUrl + "/#/verify?code=" + code;

        // 发送邮件通知
        emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(url));
        return true;
    }

    /**
     * 管理员添加白名单
     *
     * @return 结果
     */
    @PostMapping("/addWhiteListForAdmin")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult addWhiteListForAdmin(@RequestBody WhitelistInfo whitelistInfo) {

        if (whitelistInfo == null || whitelistInfo.getUserName() == null || whitelistInfo.getQqNum() == null) {
            return error("申请信息不能为空!");
        }

        // 补全基础申请信息 - 使用MinecraftUUIDUtil生成UUID
        boolean isOnline = whitelistInfo.getOnlineFlag() == 1;
        String uuid = MinecraftUUIDUtil.getPlayerUUID(whitelistInfo.getUserName(), isOnline);
        whitelistInfo.setUserUuid(uuid);

        // 设置创建信息
        whitelistInfo.setCreateBy(("ADMIN::apply::") + whitelistInfo.getUserName());
        whitelistInfo.setCreateTime(new Date());
        whitelistInfo.setAddTime(new Date());
        whitelistInfo.setTime(new Date());
        whitelistInfo.setAddState("0"); // 添加状态：0-未添加，1-已添加
        whitelistInfo.setStatus("0"); // 审核状态 0-未审核，1-审核通过，2-审核不通过

        final int i = whitelistInfoService.insertWhitelistInfo(whitelistInfo);

        if (i > 0) {
            final PlayerDetails playerDetails = new PlayerDetails();
            playerDetails.setUserName(whitelistInfo.getUserName());
            playerDetails.setQq(whitelistInfo.getQqNum());
            playerDetails.setCreateBy("ADMIN::apply::" + whitelistInfo.getUserName());
            playerDetails.setCreateTime(new Date());
            playerDetails.setIdentity(Identity.PLAYER.getValue());
            playerDetails.setGameTime(0L);
            playerDetailsService.insertPlayerDetails(playerDetails);
        }

        return success("添加成功!");
    }

    /**
     * 查询白名单列表
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:list')")
    @GetMapping("/list")
    public TableDataInfo list(WhitelistInfo whitelistInfo) {
        if (whitelistInfo.getUserName() != null) {
            whitelistInfo.setUserName(whitelistInfo.getUserName().toLowerCase().trim());
        }
        startPage();
        List<WhitelistInfo> list = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        return getDataTable(list);
    }

    /**
     * 导出白名单列表
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:export')")
    @Log(title = "白名单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WhitelistInfo whitelistInfo) {
        List<WhitelistInfo> list = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        ExcelUtil<WhitelistInfo> util = new ExcelUtil<WhitelistInfo>(WhitelistInfo.class);
        util.exportExcel(response, list, "白名单数据");
    }

    /**
     * 获取白名单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(whitelistInfoService.selectWhitelistInfoById(id));
    }

    /**
     * 新增白名单
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:add')")
    @Log(title = "白名单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WhitelistInfo whitelistInfo) {
        return toAjax(whitelistInfoService.insertWhitelistInfo(whitelistInfo));
    }

    /**
     * 修改白名单
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:edit')")
    @Log(title = "白名单", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody WhitelistInfo whitelistInfo) {
        return toAjax(whitelistInfoService.updateWhitelistInfo(whitelistInfo, getUsername()));
    }

    /**
     * 删除白名单
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:remove')")
    @Log(title = "白名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(whitelistInfoService.deleteWhitelistInfoByIds(ids));
    }

    @GetMapping("check")
    @CrossOrigin(origins = "https://app.yousb.sbs", maxAge = 3600)
    public AjaxResult cheack(@RequestParam Map<String, String> params) {

        if (params.isEmpty()) {
            return error("查询信息不能为空!");
        }

        return success(whitelistInfoService.check(params));
    }

    /**
     * 批量导入白名单数据
     */
    @PostMapping("/importTemplate")
    public AjaxResult importTemplate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("导入文件不能为空!");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
            return error("导入文件格式不正确,请使用xlsx格式的文件!");
        }

        // 读取Excel文件
        List<WhitelistInfo> whitelistInfos = new ArrayList<>();
        try {
            final InputStream inputStream = file.getInputStream();
            ExcelUtil<WhitelistImportTemplate> util = new ExcelUtil<>(WhitelistImportTemplate.class);
            final List<WhitelistImportTemplate> importList = util.importExcel(inputStream);

            if (importList == null || importList.isEmpty()) {
                return error("导入文件数据为空!");
            }

            for (WhitelistImportTemplate template : importList) {
                boolean flag = false;
                final String qq = template.getQqNum();
                final String userName = template.getUserName();
                final String isOnline = template.getIsOnline();
                final String remark = template.getRemark();

                // 正则校验数据合法性
                // 游戏ID正则匹配
                Pattern p = Pattern.compile("[a-zA-Z0-9_]{1,35}");
                if (!p.matcher(userName).matches()) {
                    flag = true;
                    logger.info("游戏ID不合法:{}", userName);
                }

                // QQ号正则匹配
                Pattern p2 = Pattern.compile("[0-9]{5,11}");
                if (!p2.matcher(qq).matches()) {
                    flag = true;
                    logger.info("QQ号不合法:{}", qq);
                }

                if (!flag) {
                    final WhitelistInfo whitelistInfo = new WhitelistInfo();
                    whitelistInfo.setQqNum(qq);
                    whitelistInfo.setUserName(userName);
                    whitelistInfo.setRemark(remark);
                    if (StringUtils.isNotBlank(isOnline)) {
                        if (isOnline.equals("是")) {
                            whitelistInfo.setOnlineFlag(1L);
                        } else if (isOnline.equals("否")) {
                            whitelistInfo.setOnlineFlag(0L);
                        }
                    }
                    whitelistInfos.add(whitelistInfo);
                }
            }

            // 异步申请
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        for (WhitelistInfo whitelistInfo : whitelistInfos) {
                            if (whitelistInfos.size() > 5) {
                                Thread.sleep(500); // 每次申请间隔500ms
                            }
                            // apply(whitelistInfo);
                            if (!batchApply(whitelistInfo)) {
                                logger.error("批量申请白名单失败,userName:{}", whitelistInfo.getUserName());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("批量导入白名单失败", e);
                    }
                }
            };
            asyncManager.execute(timerTask);
        } catch (IOException e) {
            logger.error("导入文件读取失败", e);
            return error("导入文件读取失败!");
        }
        return success("操作成功,共导入:" + whitelistInfos.size() + "条数据!");
    }

    /**
     * 下载白名单Excel模板
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            // 获取模板文件路径
            String templatePath = "template/template.xlsx";
            // 获取模板文件
            Resource resource = new ClassPathResource(templatePath);
            // 获取文件名
            String fileName = "白名单模板.xlsx";

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 将文件写入响应流
            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            logger.error("下载模板失败", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"下载模板失败\"}");
            } catch (IOException ex) {
                logger.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * Excel导入模板对象
     */
    @Data
    public static class WhitelistImportTemplate {
        @Excel(name = "QQ号")
        private String qqNum;

        @Excel(name = "游戏昵称")
        private String userName;

        @Excel(name = "是否正版")
        private String isOnline;

        @Excel(name = "备注")
        private String remark;
    }
}
