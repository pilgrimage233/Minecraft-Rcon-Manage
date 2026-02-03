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
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.config.QuestionConfig;
import cc.endmc.server.controller.permission.request.ApplyData;
import cc.endmc.server.controller.permission.request.VerifySource;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.other.IIpLimitInfoService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;
import cc.endmc.server.utils.*;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
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
@RequiredArgsConstructor
public class WhitelistInfoController extends BaseController {

    private final IWhitelistInfoService whitelistInfoService;
    private final AsyncManager asyncManager = AsyncManager.me();
    private final IIpLimitInfoService iIpLimitInfoService;
    private final IWhitelistQuizConfigService quizConfigService;
    private final IWhitelistQuizSubmissionService quizSubmissionService;
    private final IPlayerDetailsService playerDetailsService;
    private final IQqBotConfigService qqBotConfigService;
    private final EmailService emailService;
    private final RedisCache redisCache;
    private SimpleDateFormat dateFormat;
    @Value("${app-url}")
    private String appUrl;
    @Value("${whitelist.iplimit}")
    private String iplimit;
    @Value("${whitelist.email}")
    private String ADMIN_EMAIL;
    @Value("${app.ip-header-name:X-Real-IP}")
    private String ipHeaderName;

    @PostConstruct
    public void init() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 申请白名单
     *
     * @param request       请求
     * @param whitelistInfo 白名单信息
     * @param header        请求头
     * @return 结果
     */
    @SignVerify
    @SneakyThrows
    @PostMapping("/apply")
    public AjaxResult apply(HttpServletRequest request, @RequestBody WhitelistInfo whitelistInfo, @RequestHeader Map<String, String> header) {
        // 1. 基础参数校验
        AjaxResult validationResult = validateApplyParams(whitelistInfo);
        if (validationResult != null) {
            return validationResult;
        }

        logger.info("申请信息:{}", whitelistInfo);
        logger.info("header:{}", header);

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

        // 6. 生成验证码
        String code = CodeUtil.generateCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_KEY);
        AjaxResult codeCheckResult = validateVerifyCode(code);
        if (codeCheckResult != null) {
            return codeCheckResult;
        }

        // 7. 创建玩家详情并获取地理位置
        PlayerDetails details = createPlayerDetailsForApply(whitelistInfo, ip);

        // 8. 缓存申请数据
        cacheApplyData(code, whitelistInfo, details);

        // 9. 生成验证链接并发送邮件
        String verifyUrl = buildVerifyUrl(header, code);
        emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(verifyUrl));

        return success("验证邮件已发送,请查收! 如果未收到邮件,请检查垃圾箱或联系管理员!");
    }

    /**
     * 验证白名单
     * 此接口不受权限控制!
     *
     * @param request 请求
     * @param code    验证码
     * @param header  请求头
     * @return 结果
     */
    @SignVerify
    @GetMapping("/verify")
    public AjaxResult verify(HttpServletRequest request, @RequestParam String code, @RequestHeader Map<String, String> header) {
        if (StringUtils.isEmpty(code)) {
            return error("验证失败,请勿直接访问此链接!");
        }

        // 1. 检查验证码并获取申请来源
        VerifySource verifySource = checkVerifyCode(code);
        if (verifySource == null) {
            return error("验证失败,验证码无效!");
        }

        // 2. 解析申请数据
        ApplyData applyData;
        try {
            applyData = parseApplyData(verifySource);
        } catch (Exception e) {
            logger.error("数据转换失败", e);
            return error("验证失败,数据格式错误!");
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
            return error(EmailTemplates.APPLY_ERROR);
        }

        // 9. 删除验证码
        redisCache.deleteObject(verifySource.getCacheKey());

        // 10. 发送通知（异步）
        sendNotifications(applyData.getWhitelistInfo(), applyData.getDetails(), verifySource.getSource(), autoApproved);

        return success(autoApproved ? "恭喜您！您的白名单申请已自动审核通过！" : EmailTemplates.APPLY_SUCCESS);
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
                    java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));

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
            logger.info("答题功能未开启，跳过自动审批检查");
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
        logger.info("用户[{}]的白名单申请已自动通过审核，答题分数：{}",
                whitelistInfo.getUserName(), latestSubmission.getTotalScore());
        return true;
    }

    /**
     * 检查答题功能是否开启
     */
    private boolean isQuizEnabled() {
        WhitelistQuizConfig config = new WhitelistQuizConfig();
        config.setConfigKey(QuestionConfig.STATUS);
        List<WhitelistQuizConfig> configs = quizConfigService.selectWhitelistQuizConfigList(config);
        return !configs.isEmpty() && "true".equalsIgnoreCase(configs.getFirst().getConfigValue());
    }

    /**
     * 检查自动通过功能是否启用
     */
    private boolean isAutoPassEnabled() {
        WhitelistQuizConfig config = new WhitelistQuizConfig();
        config.setConfigKey(QuestionConfig.AUTO_PASSED);
        List<WhitelistQuizConfig> configs = quizConfigService.selectWhitelistQuizConfigList(config);
        return !configs.isEmpty() && "true".equalsIgnoreCase(configs.getFirst().getConfigValue());
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

        // QQ群通知
        asyncManager.execute(new TimerTask() {
            @Override
            public void run() {
                sendQQGroupNotification(whitelistInfo, details, source, autoApproved);
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
            logger.error("发送申请人通知失败", e);
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
            logger.error("发送管理员通知失败", e);
        }
    }

    /**
     * 发送QQ群通知
     */
    private void sendQQGroupNotification(WhitelistInfo whitelistInfo, PlayerDetails details, String source, boolean autoApproved) {
        QqBotConfig query = new QqBotConfig();
        query.setStatus(1L);
        List<QqBotConfig> qqBotConfigs = qqBotConfigService.selectQqBotConfigList(query);

        if (qqBotConfigs == null || qqBotConfigs.isEmpty()) {
            return;
        }

        for (QqBotConfig botConfig : qqBotConfigs) {
            if (botConfig.getStatus() != 1L) {
                continue;
            }

            String message = buildQQGroupMessage(whitelistInfo, details, source, autoApproved);
            BotUtil.sendMessage(message, botConfig.getGroupIds(), botConfig);
        }
    }

    /**
     * 构建QQ群通知消息
     */
    private String buildQQGroupMessage(WhitelistInfo whitelistInfo, PlayerDetails details, String source, boolean autoApproved) {
        StringBuilder message = new StringBuilder();
        message.append("【白名单申请】🎉 用户【").append(whitelistInfo.getUserName())
                .append("】通过 ").append(source).append(" 提交了白名单申请");

        if (autoApproved) {
            message.append("，已自动审核通过！🎉\n");
        } else {
            message.append("，快来审核吧！📝\n");
        }

        message.append("申请人QQ: ").append(whitelistInfo.getQqNum()).append("\n");

        if (details != null) {
            if (StringUtils.isNotEmpty(details.getProvince())) {
                message.append("📍省份: ").append(details.getProvince()).append("\n");
            }
            if (StringUtils.isNotEmpty(details.getCity())) {
                message.append("🏙️城市: ").append(details.getCity()).append("\n");
            }
        }

        if (!autoApproved) {
            String key = generateUniqueKey(whitelistInfo);
            message.append("管理员回复 【通过 ").append(key).append("】 可通过白名单审核 ✅\n");
            message.append("请在 30 分钟内回复此消息以完成审核。⏳\n");
        }

        return message.toString();
    }

    /**
     * 生成唯一审核key
     */
    private String generateUniqueKey(WhitelistInfo whitelistInfo) {
        String key;
        while (true) {
            key = RandomUtil.randomNumbers(4);
            if (!redisCache.hasKey(CacheKey.PASS_KEY + key)) {
                redisCache.setCacheObject(CacheKey.PASS_KEY + key, whitelistInfo, 30, TimeUnit.MINUTES);
                break;
            }
            logger.warn("生成的唯一key已存在，重新生成: {}", key);
        }
        return key;
    }

    /**
     * 验证申请参数
     */
    private AjaxResult validateApplyParams(WhitelistInfo whitelistInfo) {
        if (whitelistInfo == null || whitelistInfo.getUserName() == null || whitelistInfo.getQqNum() == null) {
            return error("申请信息不能为空!");
        }
        return null;
    }

    /**
     * 验证User-Agent
     */
    private AjaxResult validateUserAgent(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return error("请勿使用爬虫提交申请!");
        }

        // 黑名单检查
        String[] blackList = {
                "okhttp", "Postman", "curl", "python", "Go-http-client", "Java",
                "HttpClient", "Apache-HttpClient", "httpunit", "webclient",
                "webharvest", "wget", "libwww", "htmlunit", "pangolin"
        };
        for (String blocked : blackList) {
            if (userAgent.contains(blocked)) {
                return error("请勿使用爬虫提交申请!");
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
            return error("请使用浏览器提交申请!");
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
            return error("游戏ID不合法!");
        }

        // QQ号正则匹配
        Pattern qqPattern = Pattern.compile("[0-9]{5,11}");
        if (!qqPattern.matcher(whitelistInfo.getQqNum()).matches()) {
            return error("QQ号不合法!");
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
            case "1" -> success(String.format("用户:[%s]的提交已于 [%s] 日通过审核,审核人:[%s]",
                    existing.getUserName(),
                    dateFormat.format(existing.getAddTime()),
                    existing.getReviewUsers()));
            case "2" -> success(String.format("用户:[%s]的审核已于 [%s] 日被移除白名单,请规范游戏!如有疑问联系管理员",
                    existing.getUserName(),
                    dateFormat.format(existing.getAddTime())));
            default -> success("正在审核,请勿重复提交申请~ 如有纰漏或加急请联系管理员!");
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
            return error("验证码生成失败,请稍后再试!");
        }
        if ("isExist".equals(code)) {
            return error("请勿重复申请！");
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

        return baseUrl + "/#/verify?code=" + code;
    }

}
