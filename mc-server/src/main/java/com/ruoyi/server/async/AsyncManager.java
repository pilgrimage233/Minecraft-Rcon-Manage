package com.ruoyi.server.async;

import com.ruoyi.common.utils.Threads;
import com.ruoyi.common.utils.spring.SpringUtils;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 */
public class AsyncManager {
    private static final AsyncManager INSTANCE = new AsyncManager();
    private final ScheduledExecutorService executor;

    private AsyncManager() {
        // 获取Spring中的ScheduledExecutorService实例
        this.executor = SpringUtils.getBean("scheduledExecutorService");
    }

    public static AsyncManager getInstance() {
        return INSTANCE;
    }

    /**
     * 执行异步任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task) {
        // 操作延迟10毫秒
        int OPERATE_DELAY_TIME = 10;
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
