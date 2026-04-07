package cc.endmc.framework.concurrent;

import cc.endmc.common.utils.Threads;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * 可观测的定时线程池，支持追踪与中断超时任务。
 */
public class MonitoredScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    private final String poolName;
    private final TaskExecutionTracker taskExecutionTracker;

    public MonitoredScheduledThreadPoolExecutor(int corePoolSize,
                                                ThreadFactory threadFactory,
                                                RejectedExecutionHandler handler,
                                                String poolName,
                                                TaskExecutionTracker taskExecutionTracker) {
        super(corePoolSize, threadFactory, handler);
        this.poolName = poolName;
        this.taskExecutionTracker = taskExecutionTracker;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        taskExecutionTracker.onTaskStart(poolName, t, r == null ? "unknown" : r.getClass().getName());
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            Threads.printException(r, t);
        } finally {
            taskExecutionTracker.onTaskComplete(poolName, Thread.currentThread().getId());
            super.afterExecute(r, t);
        }
    }

    public int interruptTimedOutTasks(long timeoutMs, int maxInterruptPerCycle) {
        return taskExecutionTracker.interruptTimedOutTasks(poolName, timeoutMs, maxInterruptPerCycle);
    }
}
