package cc.endmc.node.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 提供带指数退避的重试机制
 *
 * @author Memory
 */
@Slf4j
public class RetryUtil {

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 默认初始延迟（毫秒）
     */
    private static final long DEFAULT_INITIAL_DELAY = 1000;

    /**
     * 默认最大延迟（毫秒）
     */
    private static final long DEFAULT_MAX_DELAY = 30000;

    /**
     * 执行带重试的操作
     *
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟（毫秒）
     * @param maxDelay 最大延迟（毫秒）
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries,
                                          long initialDelay, long maxDelay) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, initialDelay, maxDelay);
                    log.warn("操作失败，将在 {}ms 后进行第 {} 次重试: {}", delay, attempt + 1, e.getMessage());
                    Thread.sleep(delay);
                }
            }
        }

        throw lastException;
    }

    /**
     * 执行带重试的操作（使用默认参数）
     *
     * @param operation 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation) throws Exception {
        return executeWithRetry(operation, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * 执行带重试的操作（无返回值）
     *
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param initialDelay 初始延迟（毫秒）
     * @param maxDelay 最大延迟（毫秒）
     * @throws Exception 如果所有重试都失败
     */
    public static void executeWithRetry(RunnableOperation operation, int maxRetries,
                                         long initialDelay, long maxDelay) throws Exception {
        executeWithRetry(() -> {
            operation.execute();
            return null;
        }, maxRetries, initialDelay, maxDelay);
    }

    /**
     * 计算延迟时间（指数退避）
     *
     * @param attempt 当前尝试次数
     * @param initialDelay 初始延迟
     * @param maxDelay 最大延迟
     * @return 延迟时间（毫秒）
     */
    private static long calculateDelay(int attempt, long initialDelay, long maxDelay) {
        // 指数退避：delay = initialDelay * 2^attempt
        long delay = initialDelay * (1L << attempt);

        // 添加随机抖动（±10%）
        double jitter = 0.9 + Math.random() * 0.2;
        delay = (long) (delay * jitter);

        // 限制最大延迟
        return Math.min(delay, maxDelay);
    }

    /**
     * 可重试的操作接口
     *
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    /**
     * 可重试的操作接口（无返回值）
     */
    @FunctionalInterface
    public interface RunnableOperation {
        void execute() throws Exception;
    }
}
