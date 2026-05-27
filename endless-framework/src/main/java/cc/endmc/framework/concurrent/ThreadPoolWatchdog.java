package cc.endmc.framework.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池看门狗：监控线程池健康状态，并尝试中断超时任务。
 */
@Slf4j
@Component
public class ThreadPoolWatchdog {

    private static final String ASYNC_POOL_NAME = "threadPoolTaskExecutor";
    private static final String SCHEDULED_POOL_NAME = "scheduledExecutorService";
    private static final int MAX_DIAG_TASKS = 3;
    private static final int MAX_STACK_FRAMES = 6;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final TaskExecutionTracker taskExecutionTracker;

    @Value("${watchdog.thread-pool.enabled:true}")
    private boolean enabled;

    @Value("${watchdog.thread-pool.task-timeout-ms:120000}")
    private long taskTimeoutMs;

    @Value("${watchdog.thread-pool.warn-queue-usage-percent:80}")
    private int warnQueueUsagePercent;

    @Value("${watchdog.thread-pool.auto-interrupt-timeout-task:true}")
    private boolean autoInterruptTimeoutTask;

    @Value("${watchdog.thread-pool.max-interrupt-per-cycle:2}")
    private int maxInterruptPerCycle;

    public ThreadPoolWatchdog(@Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor threadPoolTaskExecutor,
                              @Qualifier("scheduledExecutorService") ScheduledExecutorService scheduledExecutorService,
                              TaskExecutionTracker taskExecutionTracker) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.scheduledExecutorService = scheduledExecutorService;
        this.taskExecutionTracker = taskExecutionTracker;
    }

    @Scheduled(
            fixedDelayString = "${watchdog.thread-pool.interval-ms:30000}",
            initialDelayString = "${watchdog.thread-pool.initial-delay-ms:30000}"
    )
    public void monitor() {
        if (!enabled) {
            return;
        }

        monitorSpringAsyncPool();
        monitorScheduledPool();
    }

    private void monitorSpringAsyncPool() {
        ThreadPoolExecutor executor = threadPoolTaskExecutor.getThreadPoolExecutor();
        if (executor == null) {
            return;
        }

        int active = executor.getActiveCount();
        int max = executor.getMaximumPoolSize();
        int queueSize = executor.getQueue().size();
        int queueTotal = queueSize + executor.getQueue().remainingCapacity();
        int queueUsage = queueTotal <= 0 ? 0 : (queueSize * 100 / queueTotal);

        List<TaskExecutionTracker.RunningTask> runningTasks = taskExecutionTracker.snapshot(ASYNC_POOL_NAME);
        long oldestRunningMs = runningTasks.isEmpty() ? 0L : runningTasks.get(0).runningMs();

        boolean isOverloaded = (active >= max && queueSize > 0) || queueUsage >= warnQueueUsagePercent;
        boolean hasTimedOutTask = oldestRunningMs >= taskTimeoutMs;

        if (isOverloaded || hasTimedOutTask) {
            log.warn("线程池告警[{}] active={}/{}, poolSize={}, queue={}%({}), completed={}, runningTasks={}, oldestRunning={}ms",
                    ASYNC_POOL_NAME,
                    active,
                    max,
                    executor.getPoolSize(),
                    queueUsage,
                    queueSize,
                    executor.getCompletedTaskCount(),
                    runningTasks.size(),
                    oldestRunningMs);
        }

        if (hasTimedOutTask) {
            logTimedOutTaskDetails(ASYNC_POOL_NAME, runningTasks);
        }

        if (autoInterruptTimeoutTask && hasTimedOutTask) {
            int interrupted = taskExecutionTracker.interruptTimedOutTasks(
                    ASYNC_POOL_NAME,
                    taskTimeoutMs,
                    maxInterruptPerCycle
            );
            if (interrupted > 0) {
                log.warn("线程池自愈[{}] 已中断超时任务线程 {} 个 (timeout={}ms)", ASYNC_POOL_NAME, interrupted, taskTimeoutMs);
            }
        }
    }

    private void monitorScheduledPool() {
        if (!(scheduledExecutorService instanceof MonitoredScheduledThreadPoolExecutor monitoredPool)) {
            return;
        }

        int active = monitoredPool.getActiveCount();
        int max = monitoredPool.getMaximumPoolSize();
        int queueSize = monitoredPool.getQueue().size();
        int queueTotal = queueSize + monitoredPool.getQueue().remainingCapacity();
        int queueUsage = queueTotal <= 0 ? 0 : (queueSize * 100 / queueTotal);

        boolean isOverloaded = (active >= max && queueSize > 0) || queueUsage >= warnQueueUsagePercent;
        if (isOverloaded) {
            log.warn("线程池告警[scheduledExecutorService] active={}/{}, poolSize={}, queue={}%({}), completed={}",
                    active,
                    max,
                    monitoredPool.getPoolSize(),
                    queueUsage,
                    queueSize,
                    monitoredPool.getCompletedTaskCount());
        }

        List<TaskExecutionTracker.RunningTask> runningTasks = taskExecutionTracker.snapshot(SCHEDULED_POOL_NAME);
        long oldestRunningMs = runningTasks.isEmpty() ? 0L : runningTasks.get(0).runningMs();
        if (oldestRunningMs >= taskTimeoutMs) {
            logTimedOutTaskDetails(SCHEDULED_POOL_NAME, runningTasks);
        }

        if (autoInterruptTimeoutTask) {
            int interrupted = monitoredPool.interruptTimedOutTasks(taskTimeoutMs, maxInterruptPerCycle);
            if (interrupted > 0) {
                log.warn("线程池自愈[scheduledExecutorService] 已中断超时任务线程 {} 个 (timeout={}ms)", interrupted, taskTimeoutMs);
            }
        }
    }

    private void logTimedOutTaskDetails(String poolName, List<TaskExecutionTracker.RunningTask> runningTasks) {
        if (runningTasks == null || runningTasks.isEmpty()) {
            return;
        }

        int limit = Math.min(MAX_DIAG_TASKS, runningTasks.size());
        for (int i = 0; i < limit; i++) {
            TaskExecutionTracker.RunningTask task = runningTasks.get(i);
            if (task.runningMs() < taskTimeoutMs) {
                continue;
            }

            Thread thread = task.thread();
            StackTraceElement[] stackTrace = thread == null ? new StackTraceElement[0] : thread.getStackTrace();
            String stackSummary = buildStackSummary(stackTrace);

            log.warn("线程池诊断[{}] taskId={}, taskName={}, thread={}, state={}, running={}ms, stackTop={}",
                    poolName,
                    task.id(),
                    task.taskName(),
                    thread == null ? "unknown" : thread.getName(),
                    thread == null ? "unknown" : thread.getState(),
                    task.runningMs(),
                    stackSummary);
        }
    }

    private String buildStackSummary(StackTraceElement[] stackTrace) {
        if (stackTrace == null || stackTrace.length == 0) {
            return "no-stack";
        }

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(MAX_STACK_FRAMES, stackTrace.length);
        for (int i = 0; i < limit; i++) {
            StackTraceElement frame = stackTrace[i];
            if (i > 0) {
                builder.append(" <- ");
            }
            builder.append(frame.getClassName())
                    .append(".")
                    .append(frame.getMethodName())
                    .append(":")
                    .append(frame.getLineNumber());
        }
        return builder.toString();
    }
}
