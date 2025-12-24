package cc.endmc.server.controller.server;


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
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.other.HistoryCommand;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.other.IHistoryCommandService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
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
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private IHistoryCommandService historyCommandService;

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
        if (redisCache.hasKey(CacheKey.SERVER_INFO_KEY)) {
            return success((List<ServerInfo>) redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY));
        } else {
            // 不存在则走数据库并缓存
            List<ServerInfo> list = serverInfoService.selectServerInfoList(new ServerInfo());
            redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, list, 1, TimeUnit.DAYS);
            return success(list);
        }
    }

    /**
     * 刷新缓存
     */
    @GetMapping("/refreshCache")
    public AjaxResult refreshCache() {
        // 刷新缓存前释放Rcon连接
        RconCache.getMap().forEach((k, v) -> {
            try {
                v.close();
            } catch (Exception ignored) {
            }
        });
        // 服务器信息缓存
        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfoService.selectServerInfoList(new ServerInfo()), 1, TimeUnit.DAYS);
        // 服务器信息缓存更新时间
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, new Date());
        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);
        RconCache.clear();
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
            RconClient client = RconCache.get(key);
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
            for (Map.Entry<String, RconClient> entry : RconCache.getMap().entrySet()) {
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
        if (RconCache.containsKey(id)) {
            RconClient rconClient = RconCache.get(id);
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
    public AjaxResult execute(@PathVariable String id, @RequestBody Map<String, String> request) {
        // 获取登录用户
        String name = null;
        try {
            name = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            name = getLoginUser().getUsername();
        }
        if (StringUtils.isEmpty(name)) {
            return error("用户未登录");
        }

        if (request == null || request.isEmpty()) {
            return error("指令不能为空");
        }

        if (id == null || id.isEmpty()) {
            return error("服务器标识不能为空");
        }

        final String command = request.get("command");
        log.info("执行指令->id: {}, command: {}", id, command);

        final HistoryCommand historyCommand = new HistoryCommand();
        historyCommand.setServerId(Long.parseLong(id));
        historyCommand.setCommand(command);
        final long l = System.currentTimeMillis();
        RconClient rconClient = RconCache.get(id);
        if (rconClient == null) {
            historyCommand.setStatus("NO");
            return error("服务器未连接");
        }
        Map<String, Object> result = new HashMap<>();
        try {
            final String msg = rconClient.sendCommand(command);
            historyCommand.setRunTime(System.currentTimeMillis() - l + "ms");
            historyCommand.setStatus("OK");
            historyCommand.setResponse(msg);
            log.info("指令执行成功, 返回消息: {}", msg);

            result.put("response", msg);
            return success(result);
        } catch (Exception e) {
            historyCommand.setStatus("NO");
            return error("指令发送失败");
        } finally {
            historyCommand.setUser(name);
            // 异步保存历史指令
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    historyCommandService.insertHistoryCommand(historyCommand);
                }
            });
        }
    }

    @SignVerify
    @GetMapping("/getServerInfoByGameId/{gameId}")
    public AjaxResult getServerInfoByGameId(@PathVariable String gameId) {
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setUserName(gameId);
        final List<WhitelistInfo> list = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (list == null || list.isEmpty()) {
            return error("抱歉，您未在白名单！");
        }
        whitelistInfo = list.get(0);
        if (!whitelistInfo.getStatus().equals("1")) {
            return error("抱歉，您未在白名单！");
        }
        if (whitelistInfo.getServers() == null || whitelistInfo.getServers().isEmpty()) {
            return error("抱歉，您未分配服务器！");
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
        return success(result);
    }

}
