package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.http.HttpUtils;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.async.AsyncManager;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.service.other.IIpLimitInfoService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.utils.WhitelistUtils;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody WhitelistInfo whitelistInfo, @RequestHeader Map<String, String> header) throws JsonProcessingException, ExecutionException, InterruptedException {
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

        // 使用QQ号生成验证码
        String code;
        try {
            // 基于QQ号生成固定验证码
            // 改为1800秒(30分钟)来匹配缓存过期时间
            String rawKey = whitelistInfo.getQqNum() + "_" + System.currentTimeMillis() / 1000 / 1800;
            // 使用MD5加密并取前8位作为验证码
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            code = sb.substring(0, 8);

            // 检查是否已存在该验证码
            if (redisCache.hasKey(CacheKey.VERIFY_KEY + code)) {
                return error("请勿重复提交申请!");
            }
        } catch (Exception e) {
            logger.error("生成验证码失败", e);
            return error("系统错误,请稍后重试!");
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
        String cacheKey = null;
        final boolean isFromBot;

        if (redisCache.hasKey(webKey)) {
            cacheKey = webKey;
            isFromBot = false;
        } else if (redisCache.hasKey(botKey)) {
            cacheKey = botKey;
            isFromBot = true;
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
            if (!isFromBot) {
                // Web端申请的数据处理
                Map<String, Object> data = (Map<String, Object>) cacheData;
                JSONObject whitelistInfoJson = (JSONObject) data.get("whitelistInfo");
                whitelistInfo = whitelistInfoJson.toJavaObject(WhitelistInfo.class);

                JSONObject detailsJson = (JSONObject) data.get("details");
                details = detailsJson.toJavaObject(PlayerDetails.class);
            } else {
                // QQ机器人申请的数据处理
                whitelistInfo = (WhitelistInfo) cacheData;

                // 为QQ机器人申请创建PlayerDetails
                details = new PlayerDetails();
                details.setUserName(whitelistInfo.getUserName());
                details.setQq(whitelistInfo.getQqNum());
                details.setCreateBy("BOT::apply::" + whitelistInfo.getUserName());
                details.setCreateTime(new Date());
                details.setIdentity(Identity.PLAYER.getValue());
                details.setGameTime(0L);
            }

            // 获取IP地址
            String ip = WhitelistUtils.getIpFromHeader(header);

            // 如果是网页请求，需要进行IP限流检查
            if (!isFromBot && StringUtils.isNotEmpty(ip)) {
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

        // 补全基础申请信息
        if (whitelistInfo.getOnlineFlag() == 1) {
            // 获取正版UUID
            String userName = whitelistInfo.getUserName();
            try {
                String result = HttpUtils.sendGet("https://api.mojang.com/users/profiles/minecraft/" + userName);
                if (result.isEmpty()) {
                    return error("信息不正确或非正版用户!");
                }
                JSONObject json = JSONObject.parseObject(result);
                if (json.containsKey("demo")) {
                    return error("该账号未购买游戏!");
                }
                if (!json.getString("id").isEmpty()) {
                    String uuid = json.getString("id");
                    // 格式化成带横杠的UUID
                    uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
                    whitelistInfo.setUserUuid(uuid);
                }
            } catch (Exception e) {
                logger.error("获取正版UUID失败", e);
            }
        } else {
            // 盗版随机生成一个UUID
            whitelistInfo.setUserUuid(UUID.randomUUID().toString());
        }

        // 设置创建信息
        whitelistInfo.setCreateBy((isFromBot ? "BOT::apply::" : "WEB::apply::") + whitelistInfo.getUserName());
        whitelistInfo.setCreateTime(new Date());
        whitelistInfo.setAddTime(new Date());
        whitelistInfo.setTime(new Date());
        whitelistInfo.setAddState("0"); // 添加状态：0-未添加，1-已添加
        whitelistInfo.setStatus("0"); // 审核状态 0-未审核，1-审核通过，2-审核不通过

        if (whitelistInfoService.insertWhitelistInfo(whitelistInfo) != 0) {
            // 删除验证码
            redisCache.deleteObject(cacheKey);

            // 发送邮件通知申请人
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        pushEmail.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                                EmailTemplates.TITLE,
                                EmailTemplates.getWhitelistNotificationPending(
                                        whitelistInfo.getQqNum(),
                                        whitelistInfo.getUserName(),
                                        DateUtils.getTime()
                                ));
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
                        pushEmail.push(ADMIN_EMAIL, EmailTemplates.TITLE,
                                "用户[" + whitelistInfo.getUserName() + "]通过" +
                                        (isFromBot ? "QQ机器人" : "网页") +
                                        "提交了白名单申请,请尽快审核!");
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            asyncManager.execute(timerTask2);

            return success(EmailTemplates.APPLY_SUCCESS);
        } else {
            return error(EmailTemplates.APPLY_ERROR);
        }
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

        String userName = whitelistInfo.getUserName();

        // 补全基础申请信息
        if (whitelistInfo.getOnlineFlag() == 1) {
            try {
                String result = HttpUtils.sendGet("https://api.mojang.com/users/profiles/minecraft/" + userName);
                if (result.isEmpty()) {
                    return error("信息不正确或非正版用户!");
                }
                JSONObject json = JSONObject.parseObject(result);
                if (json.containsKey("demo")) {
                    return error("该账号未购买游戏!");
                }
                if (!json.getString("id").isEmpty()) {
                    String uuid = json.getString("id");
                    // 格式化成带横杠的UUID
                    uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
                    whitelistInfo.setUserUuid(uuid);
                }
            } catch (Exception e) {
                logger.error("获取正版UUID失败", e);
                return error("获取正版UUID失败!");
            }
        } else {
            // 盗版随机生成一个UUID
            whitelistInfo.setUserUuid(UUID.randomUUID().toString());
        }

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

}
