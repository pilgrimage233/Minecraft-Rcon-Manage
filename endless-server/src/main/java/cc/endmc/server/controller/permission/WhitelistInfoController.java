package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.domain.permission.WhitelistIdChangeHistory;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.dto.WhitelistImportTemplate;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.service.permission.IWhitelistIdChangeHistoryService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.utils.MinecraftUUIDUtil;
import cc.endmc.server.utils.SecureCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
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

    private final RedisCache redisCache;
    private final AsyncManager asyncManager = AsyncManager.me();

    private final EmailService emailService;
    private final IWhitelistInfoService whitelistInfoService;
    private final IPlayerDetailsService playerDetailsService;
    private final IWhitelistIdChangeHistoryService whitelistIdChangeHistoryService;

    @Value("${app-url}")
    private String appUrl;

    /**
     * 批量申请白名单
     *
     * @param whitelistInfo 白名单信息
     * @return 结果
     */
    @SneakyThrows
    private boolean batchApply(WhitelistInfo whitelistInfo) {
        // 检查是否有活跃的验证码
        if (SecureCodeUtil.hasActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BATCH_KEY)) {
            return false;
        }

        // 生成安全的验证码（8位字母数字组合）
        final String code = SecureCodeUtil.generateSecureCode(
                whitelistInfo.getQqNum(),
                CacheKey.VERIFY_FOR_BATCH_KEY,
                8,
                30
        );

        if (StringUtils.isEmpty(code)) {
            return false;
        }

        // 缓存白名单信息
        redisCache.setCacheObject(CacheKey.VERIFY_FOR_BATCH_KEY + code, whitelistInfo, 30, TimeUnit.MINUTES);

        // 标记活跃验证码
        SecureCodeUtil.markActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BATCH_KEY, 30);

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

    /**
     * 查询ID更改历史列表（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:changeHistory')")
    @GetMapping("/changeHistory/list")
    public TableDataInfo listChangeHistory(WhitelistIdChangeHistory history) {
        startPage();
        List<WhitelistIdChangeHistory> list = whitelistIdChangeHistoryService.selectWhitelistIdChangeHistoryList(history);
        return getDataTable(list);
    }

    /**
     * 导出ID更改历史列表
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:changeHistory')")
    @Log(title = "白名单ID更改历史", businessType = BusinessType.EXPORT)
    @PostMapping("/changeHistory/export")
    public void exportChangeHistory(HttpServletResponse response, WhitelistIdChangeHistory history) {
        List<WhitelistIdChangeHistory> list = whitelistIdChangeHistoryService.selectWhitelistIdChangeHistoryList(history);
        ExcelUtil<WhitelistIdChangeHistory> util = new ExcelUtil<>(WhitelistIdChangeHistory.class);
        util.exportExcel(response, list, "白名单ID更改历史");
    }

    /**
     * 根据白名单ID查询ID更改历史
     */
    @PreAuthorize("@ss.hasPermi('mc:whitelist:query')")
    @GetMapping("/changeHistory/byWhitelistId")
    public AjaxResult getChangeHistoryByWhitelistId(@RequestParam Long whitelistId) {
        if (whitelistId == null) {
            return error("白名单ID不能为空");
        }
        List<WhitelistIdChangeHistory> list = whitelistIdChangeHistoryService.selectChangeHistoryByWhitelistId(whitelistId);
        return success(list);
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
}
