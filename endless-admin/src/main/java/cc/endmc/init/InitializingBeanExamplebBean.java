package cc.endmc.init;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.common.MapCache;
import cc.endmc.server.common.ObjectCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.constant.RconMsg;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.config.RconConfig;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.sdk.SearchHttpAK;
import cc.endmc.server.service.server.IServerCommandInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitializingBeanExamplebBean implements InitializingBean {

    final private RedisCache redisCache;

    final private IServerInfoService serverInfoService;

    // final private INodeServerService nodeServerService;

    final private IServerCommandInfoService commandInfoService;

    final private RconService rconService;

    final private RconConfig rconConfig;

    final private Environment env;

    /**
     * InitializingBean afterPropertiesSet
     * 在bean初始化后执行一些操作
     * 例如：初始化服务器信息缓存、Rcon连接、Node初始化等
     * 作者：Memory
     */
    @Override
    public void afterPropertiesSet() {
        log.info("🔧 ENDLESS INIT: 初始化开始...");
        // 判断Redis缓存是否存在
        // if (redisCache.hasKey("serverInfo") && redisCache.hasKey("serverInfoUpdateTime")) {
        //     // 判断上次缓存时间是否超过一天
        //     if (DateUtils.getNowDate().getTime() - ((Date) redisCache.getCacheObject("serverInfoUpdateTime")).getTime() < 86400000) {
        //         log.debug("服务器信息缓存存在且未过期");
        //         return;
        //     }
        // }
        rconConfig.init();

        // 初始化必要配置变量
        if (env.getProperty("baidu.key") != null) {
            SearchHttpAK.AK = env.getProperty("baidu.key");
            log.info("百度IP定位API秘钥: {}", SearchHttpAK.AK);
        } else {
            log.error("百度IP定位API秘钥为空，请检查配置文件！");
        }

        if (env.getProperty("app-url") == null) log.error("白名单申请网站地址为空，请检查配置文件！");

        if (env.getProperty("whitelist.email") == null) {
            log.error("白名单管理员邮件通知地址为空，请检查配置文件！");
            System.exit(1);
        }


        // 服务器信息缓存
        final List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(new ServerInfo());
        if (serverInfos == null || serverInfos.isEmpty()) {
            log.error("❌ ENDLESS ERROR: " + RconMsg.SERVER_EMPTY);
        }
        Map<String, ServerInfo> map = new HashMap<>();
        if (serverInfos != null) {
            for (ServerInfo serverInfo : serverInfos) {
                map.put(serverInfo.getId().toString(), serverInfo);
            }
        }

        redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, map);

        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);
        if (serverInfos != null) {
            log.info("📊 ENDLESS INIT: 缓存服务器数量: {}", serverInfos.size());
        }

        // 服务器信息缓存更新时间
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());

        // 初始化缓存服务器指令
        commandInfoService.initServerCommandInfo();

        RconService.COMMAND_INFO = ObjectCache.getCommandInfo();
        if (RconService.COMMAND_INFO != null && !RconService.COMMAND_INFO.isEmpty()) {
            log.info("📝 ENDLESS INIT: 缓存指令数量: {}", RconService.COMMAND_INFO.size());
        }

        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);
        for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
            rconService.init(serverInfo);
        }
        log.info("🔌 ENDLESS INIT: 初始化Rcon连接完成... 共有 {} 个服务器", MapCache.size());

        // 初始化Node节点服务器
        // NodeServer nodeServer = new NodeServer();
        // nodeServer.setStatus("0");
        // final List<NodeServer> list = nodeServerService.selectNodeServerList(nodeServer);
        // if (list != null) {
        //     for (NodeServer server : list) {
        //         NodeCache.put(server.getId(), server);
        //     }
        // }
        // log.info("🖥️ ENDLESS INIT: 缓存节点服务器数量: {}", NodeCache.size());

        // Thread.sleep(5000);

        // 发送广播
        rconService.sendCommand("all", "say Rcon ready! Time: " + DateUtils.getNowDate(), false);

        log.info("✅ ENDLESS INIT: 初始化完成...");
    }
}
