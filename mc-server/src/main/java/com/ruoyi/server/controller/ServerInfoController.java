package com.ruoyi.server.controller;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.RconService;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.service.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 服务器信息Controller
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@Slf4j
@RestController
@RequestMapping("/server/serverlist")
public class ServerInfoController extends BaseController {

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RconService rconService;

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
            rconService.init(serverInfo);
        }
        return success();
    }

    /**
     * 即时指令通讯
     *
     * @param command
     * @param key
     * @return AjaxResult
     */
    @GetMapping("/sendCommand")
    public AjaxResult sendCommand(@RequestParam String command, @RequestParam String key) {
        if (command == null || command.isEmpty()) {
            return error("指令不能为空");
        }
        if (key == null || key.isEmpty()) {
            return error("服务器标识不能为空");
        }
        Map<String, String> data = new HashMap<>();

        if (!key.equals("all")) {
            RconClient client = MapCache.get(key);
            if (client == null) {
                return error("服务器未连接");
            }
            // 根据key获取服务器nameTag
            final String nameTag = serverInfoService.selectServerInfoById(Long.parseLong(key)).getNameTag();
            data.put("time", String.valueOf(System.currentTimeMillis()));
            try {
                final String msg = client.sendCommand(command);
                data.put(nameTag, "指令发送成功, 返回消息: " + msg);
            } catch (Exception e) {
                data.put(nameTag, "指令发送失败");
                data.put("error", e.getMessage());
            }
        } else {
            for (Map.Entry<String, RconClient> entry : MapCache.getMap().entrySet()) {
                final String nameTag = serverInfoService.selectServerInfoById(Long.parseLong(entry.getKey())).getNameTag();
                data.put("time", String.valueOf(System.currentTimeMillis()));
                try {
                    final String msg = entry.getValue().sendCommand(command);
                    data.put(nameTag, "指令发送成功, 返回消息: " + msg);
                } catch (Exception e) {
                    data.put(nameTag, "指令发送失败");
                    data.put("error", e.getMessage());
                }
            }
        }
        return success(data);
    }

    /**
     * Web控制台连接服务器
     *
     * @param id
     * @return AjaxResult
     */
    @PostMapping("/rcon/connect/{id}")
    public AjaxResult connect(@PathVariable String id) {
        // 尝试在缓存中获取RconClient
        if (MapCache.containsKey(id)) {
            RconClient rconClient = MapCache.get(id);
            // 存活检测
            String testResponse = null;
            try {
                testResponse = rconClient.sendCommand("seed");
            } catch (Exception e) {
                if (StringUtils.isEmpty(testResponse)) {
                    log.error("服务器连接失败，尝试重连");
                    if (rconService.reconnect(id)) {
                        log.info("尝试重新测试连接");
                        try {
                            testResponse = rconClient.sendCommand("seed");
                        } catch (Exception noLuck) {
                            return error("服务器连接失败，请检查服务器状态");
                        }
                    }
                }
            }

            log.debug("测试连接返回: {}", testResponse);
            return success("服务器已连接");
        } else {
            // 未存在缓存，可能是未启用?  或者是未初始化
            ServerInfo serverInfo = serverInfoService.selectServerInfoById(Long.parseLong(id));
            if (serverInfo == null) {
                return error("服务器不存在");
            }
            if (serverInfo.getStatus() == 0) {
                return error("服务器未启用");
            }
            if (rconService.init(serverInfo)) {
                return success("服务器已连接");
            } else {
                return error("服务器连接失败，请检查服务器状态");
            }
        }
    }

    /**
     * Web控制台执行指令
     *
     * @param id
     * @return AjaxResult
     */
    @PostMapping("/rcon/execute/{id}")
    public AjaxResult execute(@PathVariable String id, @RequestBody Map<String, String> command) {
        if (command == null || command.isEmpty()) {
            return error("指令不能为空");
        }

        if (id == null || id.isEmpty()) {
            return error("服务器标识不能为空");
        }

        RconClient rconClient = MapCache.get(id);
        if (rconClient == null) {
            return error("服务器未连接");
        }
        Map<String, Object> result = new HashMap<>();
        try {
            final String msg = rconClient.sendCommand(command.get("command"));
            result.put("response", msg);
            return success(result);
        } catch (Exception e) {
            return error("指令发送失败");
        }

    }

}
