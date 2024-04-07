package com.ruoyi.server.controller;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.common.DomainToIp;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.service.IServerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务器信息Controller
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@RestController
@RequestMapping("/server/serverlist")
public class ServerInfoController extends BaseController {
    @Autowired
    private IServerInfoService serverInfoService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询服务器信息列表
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:list')")
    @GetMapping("/list")
    public TableDataInfo list(ServerInfo serverInfo) {
        startPage();
        List<ServerInfo> list = serverInfoService.selectServerInfoList(serverInfo);
        return getDataTable(list);
    }

    /**
     * 导出服务器信息列表
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:export')")
    @Log(title = "服务器信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ServerInfo serverInfo) {
        List<ServerInfo> list = serverInfoService.selectServerInfoList(serverInfo);
        ExcelUtil<ServerInfo> util = new ExcelUtil<ServerInfo>(ServerInfo.class);
        util.exportExcel(response, list, "服务器信息数据");
    }

    /**
     * 获取服务器信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(serverInfoService.selectServerInfoById(id));
    }

    /**
     * 新增服务器信息
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:add')")
    @Log(title = "服务器信息", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody ServerInfo serverInfo) {
        return toAjax(serverInfoService.insertServerInfo(serverInfo));
    }

    /**
     * 修改服务器信息
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:edit')")
    @Log(title = "服务器信息", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody ServerInfo serverInfo) {
        return toAjax(serverInfoService.updateServerInfo(serverInfo));
    }

    /**
     * 删除服务器信息
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:remove')")
    @Log(title = "服务器信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(serverInfoService.deleteServerInfoByIds(ids));
    }

    /**
     * 从Redis缓存获取服务器列表
     */
    @PreAuthorize("@ss.hasPermi('server:serverlist:query')")
    @GetMapping("/getServerList")
    public AjaxResult getServerList() {
        // 判断Redis是否存在缓存
        if (redisCache.hasKey("serverInfo")) {
            return success((List<ServerInfo>) redisCache.getCacheObject("serverInfo"));
        } else {
            // 不存在则走数据库并缓存
            List<ServerInfo> list = serverInfoService.selectServerInfoList(new ServerInfo());
            redisCache.setCacheObject("serverInfo", list, 1, TimeUnit.DAYS);
            return success(list);
        }
    }

    /**
     * 刷新缓存
     */
    @GetMapping("/refreshCache")
    public AjaxResult refreshCache() {
        // 刷新缓存前释放Rcon连接
        MapCache.getMap().forEach((k, v) -> {
            try {
                v.close();
            } catch (Exception ignored) {
            }
        });
        // 服务器信息缓存
        redisCache.setCacheObject("serverInfo", serverInfoService.selectServerInfoList(new ServerInfo()), 1, TimeUnit.DAYS);
        // 服务器信息缓存更新时间
        redisCache.setCacheObject("serverInfoUpdateTime", new Date());
        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);
        MapCache.clear();
        for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
            try {
                logger.info("初始化Rcon连接：" + serverInfo.getNameTag());
                MapCache.put(serverInfo.getId().toString(), RconClient.open(DomainToIp.domainToIp(serverInfo.getIp()), serverInfo.getRconPort().intValue(), serverInfo.getRconPassword()));
                logger.info("初始化Rcon连接成功：" + serverInfo.getNameTag());
            } catch (Exception e) {
                logger.error("初始化Rcon连接失败：" + serverInfo.getNameTag() + " " + serverInfo.getIp() + " " + serverInfo.getRconPort() + " " + serverInfo.getRconPassword());
                logger.error("失败原因：" + e.getMessage());
            }
        }
        return success();
    }

    // 查询服务器在线人数
    @GetMapping("/getOnlinePlayer")
    public AjaxResult getOnlinePlayer() {
        return success(serverInfoService.getOnlinePlayer());
    }
}
