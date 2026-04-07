package cc.endmc.framework.concurrent;

import org.springframework.core.task.TaskDecorator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一追踪线程池中的运行中任务，支持超时线程中断尝试。
 */
public class TaskExecutionTracker {

    private final AtomicLong sequence = new AtomicLong(1L);
    private final Map<String, ConcurrentHashMap<Long, RunningTask>> runningTasksByPool = new ConcurrentHashMap<>();

    public TaskDecorator createTaskDecorator(String poolName) {
        return task -> () -> {
            Thread currentThread = Thread.currentThread();
            onTaskStart(poolName, currentThread, resolveTaskName(task));
            try {
                task.run();
            } finally {
                onTaskComplete(poolName, currentThread.getId());
            }
        };
    }

    public void onTaskStart(String poolName, Thread thread, String taskName) {
        if (thread == null || poolName == null) {
            return;
        }
        RunningTask runningTask = new RunningTask(
                sequence.getAndIncrement(),
                poolName,
                thread,
                taskName,
                System.currentTimeMillis()
        );
        getPoolTasks(poolName).put(thread.getId(), runningTask);
    }

    public void onTaskComplete(String poolName, long threadId) {
        if (poolName == null) {
            return;
        }
        getPoolTasks(poolName).remove(threadId);
    }

    public List<RunningTask> snapshot(String poolName) {
        List<RunningTask> result = new ArrayList<>();
        Map<Long, RunningTask> tasks = runningTasksByPool.get(poolName);
        if (tasks == null || tasks.isEmpty()) {
            return result;
        }

        long now = System.currentTimeMillis();
        for (RunningTask task : tasks.values()) {
            if (task == null) {
                continue;
            }
            Thread thread = task.thread();
            if (thread == null || !thread.isAlive()) {
                continue;
            }
            result.add(task.withRunningMs(now - task.startTimeMs()));
        }
        result.sort(Comparator.comparingLong(RunningTask::runningMs).reversed());
        return result;
    }

    public int interruptTimedOutTasks(String poolName, long timeoutMs, int maxInterruptPerCycle) {
        if (timeoutMs <= 0 || maxInterruptPerCycle <= 0) {
            return 0;
        }

        List<RunningTask> runningTasks = snapshot(poolName);
        int interruptedCount = 0;
        for (RunningTask task : runningTasks) {
            if (task.runningMs() < timeoutMs) {
                continue;
            }
            Thread thread = task.thread();
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                interruptedCount++;
            }
            if (interruptedCount >= maxInterruptPerCycle) {
                break;
            }
        }
        return interruptedCount;
    }

    private ConcurrentHashMap<Long, RunningTask> getPoolTasks(String poolName) {
        return runningTasksByPool.computeIfAbsent(poolName, ignored -> new ConcurrentHashMap<>());
    }

    private String resolveTaskName(Runnable runnable) {
        if (runnable == null) {
            return "unknown";
        }
        String simpleName = runnable.getClass().getSimpleName();
        if (simpleName == null || simpleName.isEmpty()) {
            return runnable.getClass().getName();
        }
        return simpleName;
    }

    public record RunningTask(
            long id,
            String poolName,
            Thread thread,
            String taskName,
            long startTimeMs,
            long runningMs
    ) {
        public RunningTask(long id, String poolName, Thread thread, String taskName, long startTimeMs) {
            this(id, poolName, thread, taskName, startTimeMs, 0L);
        }

        public RunningTask {
            Objects.requireNonNull(poolName, "poolName");
            Objects.requireNonNull(taskName, "taskName");
        }

        public RunningTask withRunningMs(long durationMs) {
            return new RunningTask(id, poolName, thread, taskName, startTimeMs, Math.max(0L, durationMs));
        }
    }
}
