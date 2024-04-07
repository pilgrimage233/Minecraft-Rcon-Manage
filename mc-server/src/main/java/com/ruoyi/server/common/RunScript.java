package com.ruoyi.server.common;

import com.ruoyi.server.async.AsyncManager;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class RunScript {

    @PreDestroy
    public void end() {
        // 程序关闭时断开所有Rcon连接
        MapCache.getMap().forEach((k, v) -> {
            try {
                v.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 关闭线程池
        AsyncManager.getInstance().shutdown();
    }
}
