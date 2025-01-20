package com.ruoyi.server.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.async.AsyncManager;
import com.ruoyi.server.common.EmailService;
import com.ruoyi.server.common.EmailTemplates;
import com.ruoyi.server.domain.IpLimitInfo;
import com.ruoyi.server.domain.PlayerDetails;
import com.ruoyi.server.domain.WhitelistInfo;
import com.ruoyi.server.enums.Identity;
import com.ruoyi.server.sdk.SearchHttpAK;
import com.ruoyi.server.service.IIpLimitInfoService;
import com.ruoyi.server.service.IPlayerDetailsService;
import com.ruoyi.server.service.IWhitelistInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

    public WhitelistInfoController() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 提交白名单
     * 此接口不受权限控制！
     *
     * @param whitelistInfo
     * @return
     */
    @PostMapping("/apply")
    @CrossOrigin(origins = "https://app.yousb.sbs", maxAge = 3600)
    public AjaxResult apply(@RequestBody WhitelistInfo whitelistInfo, @RequestHeader Map<String, String> header) throws JsonProcessingException {

        if (whitelistInfo == null || whitelistInfo.getUserName() == null || whitelistInfo.getQqNum() == null) {
            return error("申请信息不能为空!");
        }

        logger.info("申请信息:{}", whitelistInfo);
        logger.info("header:{}", header);

        // 获取IP地址
        String ip = header.get("x-real-ip");
        if (ip == null) {
            ip = header.get("x-forwarded-for");
        }
        if (ip == null) {
            ip = header.get("proxy-client-ip");
        }
        if (ip == null) {
            ip = header.get("wl-proxy-client-ip");
        }
        if (ip == null) {
            ip = header.get("http_client_ip");
        }
        if (ip == null) {
            ip = header.get("http_x_forwarded_for");
        }

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
        IpLimitInfo ipLimitInfo = new IpLimitInfo();
        // IP限流
        if (StringUtils.isNotEmpty(ip) && !IpUtils.internalIp(ip)) {
            // 查询IP是否存在
            ipLimitInfo.setIp(ip);
            List<IpLimitInfo> ipLimitInfos = iIpLimitInfoService.selectIpLimitInfoList(ipLimitInfo);
            if (ipLimitInfos.isEmpty()) {
                ipLimitInfo.setCreateTime(new Date());
                ipLimitInfo.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
                ipLimitInfo.setCount(1L); // 第一次访问
                ipLimitInfo.setIp(ip); // IP地址
                ipLimitInfo.setUserAgent(userAgent); // 用户代理
                ipLimitInfo.setUuid(UUID.randomUUID().toString());
                // whitelistInfo序列化成json
                ObjectMapper mapper = new ObjectMapper();
                // 忽略null值
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                ipLimitInfo.setBodyParams(mapper.writeValueAsString(whitelistInfo));

                // 获取IP地理位置1.0
                SearchHttpAK searchHttpAK = new SearchHttpAK();
                Map<String, String> params = new HashMap<>();
                params.put("ip", ip);
                params.put("coor", "bd09ll");
                params.put("ak", SearchHttpAK.AK);
                try {
                    JSONObject json = searchHttpAK.requestGetAK(SearchHttpAK.URL, params);
                    if (json != null && json.containsKey("content") && json.getJSONObject("content").containsKey("address_detail")) {
                        JSONObject addressDetail = json.getJSONObject("content").getJSONObject("address_detail");
                        JSONObject point = json.getJSONObject("content").getJSONObject("point");
                        ipLimitInfo.setProvince(addressDetail.getString("province"));
                        ipLimitInfo.setCity(addressDetail.getString("city"));
                        ipLimitInfo.setLongitude(point.getString("x")); // 经度
                        ipLimitInfo.setLatitude(point.getString("y")); // 纬度
                    }
                } catch (Exception e) {
                    logger.error("获取IP地理位置失败", e);

                }

                if (ipLimitInfo.getCity() == null || ipLimitInfo.getCity().isEmpty()) {
                    // 获取IP地理位置2.0
                    try {
                        String result = HttpUtils.sendGet("http://ip-api.com/json/" + ip);
                        JSONObject json = JSONObject.parseObject(result);
                        if (json.containsKey("city")) {
                            ipLimitInfo.setCity(json.getString("city"));
                        }
                        if (json.containsKey("regionName")) {
                            ipLimitInfo.setProvince(json.getString("regionName"));
                        }
                        if (json.containsKey("lat")) {
                            ipLimitInfo.setLatitude(json.getString("lat"));
                        }
                        if (json.containsKey("lon")) {
                            ipLimitInfo.setLongitude(json.getString("lon"));
                        }
                    } catch (Exception e) {
                        logger.error("获取IP地理位置失败", e);
                    }
                }

                iIpLimitInfoService.insertIpLimitInfo(ipLimitInfo);
            } else {
                ipLimitInfo = ipLimitInfos.get(0);

                if (ipLimitInfo.getCount() >= Long.parseLong(iplimit)) {
                    return error("请求次数达到上限，请联系管理员!");
                }

                if (ipLimitInfo.getCreateBy() == null || ipLimitInfo.getCreateBy().isEmpty()) {
                    ipLimitInfo.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
                } else if (ipLimitInfo.getCount() == 1) {
                    ipLimitInfo.setUpdateBy(ipLimitInfo.getCreateBy() + "::" + whitelistInfo.getUserName());
                } else {
                    ipLimitInfo.setUpdateBy(ipLimitInfo.getUpdateBy() + "::" + whitelistInfo.getUserName());
                }
                ipLimitInfo.setCount(ipLimitInfo.getCount() + 1);
                ipLimitInfo.setUpdateTime(new Date());

                iIpLimitInfoService.updateIpLimitInfo(ipLimitInfo);
            }

            // 新增玩家详情
            PlayerDetails details = new PlayerDetails();
            details.setUserName(whitelistInfo.getUserName());
            details.setQq(whitelistInfo.getQqNum());
            details.setCity(ipLimitInfo.getCity());
            details.setProvince(ipLimitInfo.getProvince());
            details.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
            details.setCreateTime(new Date());
            details.setIdentity(Identity.PLAYER.getValue());
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
            // 盗版随机生成一个UUID,加了白名单修复mod的服务器不匹配ID
            whitelistInfo.setUserUuid(UUID.randomUUID().toString());
        }
        whitelistInfo.setCreateBy("AUTO::apply::" + whitelistInfo.getUserName());
        whitelistInfo.setCreateTime(new Date());
        whitelistInfo.setTime(new Date());
        whitelistInfo.setAddState("0"); // 添加状态：0-未添加，1-已添加
        whitelistInfo.setStatus("0"); // 审核状态 0-未审核，1-审核通过，2-审核不通过

        if (whitelistInfoService.insertWhitelistInfo(whitelistInfo) != 0) {
            // 发送邮件通知
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
                        pushEmail.push(ADMIN_EMAIL, EmailTemplates.TITLE, "用户[" + whitelistInfo.getUserName() + "]的白名单申请已提交,请尽快审核!");
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
        return toAjax(whitelistInfoService.updateWhitelistInfo(whitelistInfo));
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
            return error("申请信息不能为空!");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        if (params.containsKey("id") && !params.get("id").isEmpty()) {
            whitelistInfo.setUserName(params.get("id").toLowerCase());
        }
        if (params.containsKey("qq") && !params.get("qq").isEmpty()) {
            whitelistInfo.setQqNum(params.get("qq"));
        }

        if (!whitelistInfoService.checkRepeat(whitelistInfo).isEmpty()) {
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.checkRepeat(whitelistInfo);
            WhitelistInfo obj = whitelistInfos.get(0);

            map.put("游戏ID", obj.getUserName());
            map.put("QQ号", obj.getQqNum());
            // map.put("提交时间", dateFormat.format(obj.getAddTime()));  // 容余
            if (obj.getOnlineFlag() == 1) {
                map.put("账号类型", "正版");
            } else {
                map.put("账号类型", "离线");
            }

            PlayerDetails playerDetails = new PlayerDetails();
            playerDetails.setUserName(obj.getUserName().toLowerCase());
            final List<PlayerDetails> details = playerDetailsService.selectPlayerDetailsList(playerDetails);

            if (!details.isEmpty()) {
                playerDetails = details.get(0);
            }

            // if (playerDetails.getProvince() != null) {
            //     map.put("省份", playerDetails.getProvince());
            // }

            if (playerDetails.getCity() != null) {
                map.put("城市", playerDetails.getCity());
            }


            if (playerDetails.getIdentity() != null) {
                String identity;
                switch (playerDetails.getIdentity()) {
                    case "player":
                        identity = Identity.PLAYER.getDesc();
                        break;
                    case "operator":
                        identity = Identity.OPERATOR.getDesc();
                        break;
                    case "banned":
                        identity = Identity.BANNED.getDesc();
                        break;
                    default:
                        identity = Identity.OTHER.getDesc();
                        break;
                }
                map.put("身份", identity);
            }

            if (playerDetails.getLastOnlineTime() != null && playerDetails.getLastOfflineTime() != null) {
                // 在线时间和离线时间取最大的
                map.put("最后上线时间", playerDetails.getLastOnlineTime().getTime()
                        > playerDetails.getLastOfflineTime().getTime()
                        ? dateFormat.format(playerDetails.getLastOnlineTime())
                        : dateFormat.format(playerDetails.getLastOfflineTime()));
            } else if (playerDetails.getLastOnlineTime() != null) {
                map.put("最后上线时间", dateFormat.format(playerDetails.getLastOnlineTime()));
            }

            if (playerDetails.getGameTime() != null) {
                if (playerDetails.getGameTime() > 60) {
                    map.put("游戏时间", playerDetails.getGameTime() / 60 + "小时");
                } else {
                    map.put("游戏时间", playerDetails.getGameTime() + "分钟");
                }
            }

            if (StringUtils.isNotEmpty(playerDetails.getParameters())) {
                // 取历史名称
                final JSONObject jsonObject = JSONObject.parseObject(playerDetails.getParameters());
                if (jsonObject.containsKey("name_history")) {
                    map.put("历史名称", jsonObject.getJSONArray("name_history"));
                }
            }

            map.put("审核人", obj.getReviewUsers());
            // map.put("UUID", obj.getUserUuid());
            switch (obj.getAddState()) {
                case "1":
                    map.put("审核状态", "已通过");
                    map.put("审核时间", dateFormat.format(obj.getAddTime()));
                    break;
                case "2":
                    map.put("审核状态", "未通过/已移除");
                    map.put("移除时间", dateFormat.format(obj.getRemoveTime()));
                    map.put("移除原因", obj.getRemoveReason());
                    break;
                case "9":
                    map.put("审核状态", "已封禁");
                    map.put("封禁时间", dateFormat.format(obj.getRemoveTime()));
                    map.put("封禁原因", obj.getRemoveReason());
                    break;
                default:
                    map.put("审核状态", "待审核");
                    map.put("UUID", obj.getUserUuid());
                    break;
            }
        }
        return success(map);
    }

}
