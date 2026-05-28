package cc.endmc.server.common.rconclient;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RconClient是一个用于与RCON服务器通信的客户端实现，支持发送命令、批量命令以及自动重连功能。
 * 它使用Java NIO的SocketChannel进行非阻塞I/O操作，并通过线程池管理异步任务。
 * 该类还实现了缓冲区池以优化内存使用，并提供了详细的日志记录以便于调试和监控。
 */
public class RconClient implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(RconClient.class.getName());
    private static final int AUTHENTICATION_FAILURE_ID = -1;
    // RCON协议规定每个数据包至少包含请求ID（4字节）和类型（4字节），加上至少两个空字节作为结束符
    private static final int RCON_HEADER_SIZE = 2 * Integer.BYTES;
    // 最小数据包大小为请求ID + 类型 + 2个空字节（结束符），不包含负载
    private static final int MIN_PACKET_SIZE = RCON_HEADER_SIZE + 2;
    // 读取空闲时的睡眠时间，避免CPU过高占用
    private static final int READ_IDLE_SLEEP_MS = 10;
    // 在默认超时时间的基础上，动态调整每个数据包之间的等待时间，避免过早超时但又能快速响应
    private static final int DEFAULT_INTER_PACKET_TIMEOUT_MS = 80;
    // 协议常量，不可变
    public static final int TYPE_COMMAND = 2;
    public static final int TYPE_AUTH = 3;

    // 配置字段，通过 RconConfig.init() 初始化，使用 volatile 保证可见性
    private static volatile Charset payloadCharset = StandardCharsets.UTF_8;
    private static volatile int maxReconnectAttempts = 3;
    private static volatile int bufferPoolSize = 10;
    private static volatile long reconnectDelayMs; // 重连延迟时间
    private static volatile int defaultTimeoutMs;  // 超时时间
    private static volatile int defaultBufferSize; // 缓冲区大小
    private static volatile int maxResponseSize;  // 最大响应大小
    private static final int SHARED_EXECUTOR_CORE_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors());
    private static final int SHARED_EXECUTOR_MAX_SIZE = SHARED_EXECUTOR_CORE_SIZE * 2;
    private static final int SHARED_EXECUTOR_QUEUE_CAPACITY = 2000;
    private static final ExecutorService SHARED_EXECUTOR = new ThreadPoolExecutor(
            SHARED_EXECUTOR_CORE_SIZE,
            SHARED_EXECUTOR_MAX_SIZE,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(SHARED_EXECUTOR_QUEUE_CAPACITY),
            new ThreadFactory() {
                private final ThreadFactory delegate = Executors.defaultThreadFactory();

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = delegate.newThread(r);
                    thread.setName("rcon-client-async-" + thread.threadId());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private final String host;
    private final int port;
    private final String password;
    private final AtomicInteger currentRequestId;
    private final AtomicBoolean isConnected;
    private final BlockingQueue<ByteBuffer> bufferPool;
    private final boolean useDirectBuffers;
    private final ReentrantLock sendLock = new ReentrantLock(true); // 公平锁，避免饥饿
    private volatile SocketChannel socketChannel;

    /**
     * 打开RconClient
     *
     * @param host             主机
     * @param port             端口
     * @param password         密码
     * @param timeoutMs        超时时间
     * @param useDirectBuffers 是否使用直接缓冲区
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password, int timeoutMs, boolean useDirectBuffers) {
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            applySocketOptions(socketChannel, timeoutMs);

            LOGGER.info(String.format("正在尝试连接RCON服务器 %s:%d，超时时间 %dms", host, port, timeoutMs));
            socketChannel.socket().connect(new InetSocketAddress(host, port), timeoutMs);
            socketChannel.configureBlocking(false);

            LOGGER.info("成功建立Socket连接");
        } catch (IOException e) {
            String errorMsg = String.format("无法打开到 %s:%d 的socket连接 - %s", host, port, e.getMessage());
            LOGGER.severe(errorMsg);
            throw new RconClientException(errorMsg, e);
        }

        RconClient rconClient = new RconClient(host, port, password, socketChannel, useDirectBuffers);
        try {
            LOGGER.info("正在尝试认证RCON服务器");
            rconClient.authenticate(password);
            LOGGER.info("成功认证RCON服务器");
        } catch (Exception authException) {
            String errorMsg = String.format("RCON服务器认证失败 %s:%d - %s (连接超时: %dms)",
                    host, port, authException.getMessage(), timeoutMs);
            LOGGER.severe(errorMsg);
            try {
                rconClient.close();
            } catch (Exception closingException) {
                authException.addSuppressed(closingException);
            }
            throw new RconClientException(errorMsg, authException);
        }
        return rconClient;
    }

    private RconClient(String host, int port, String password, SocketChannel socketChannel, boolean useDirectBuffers) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.currentRequestId = new AtomicInteger(1);
        this.isConnected = new AtomicBoolean(true);
        this.useDirectBuffers = useDirectBuffers;
        this.bufferPool = new ArrayBlockingQueue<>(bufferPoolSize);

        // Pre-allocate buffers
        for (int i = 0; i < bufferPoolSize; i++) {
            bufferPool.offer(createBuffer(defaultBufferSize));
        }
    }

    /**
     * 打开RconClient
     *
     * @param host     主机
     * @param port     端口
     * @param password 密码
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password) {
        return open(host, port, password, defaultTimeoutMs);
    }

    /**
     * 打开RconClient
     *
     * @param host      主机
     * @param port      端口
     * @param password  密码
     * @param timeoutMs 超时时间
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password, int timeoutMs) {
        return open(host, port, password, timeoutMs, true);
    }

    private static void sleepQuietly(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("线程被中断", e);
        }
    }

    /**
     * 创建缓冲区
     *
     * @param capacity 容量
     * @return 缓冲区
     */
    private ByteBuffer createBuffer(int capacity) {
        return useDirectBuffers ?
                ByteBuffer.allocateDirect(capacity) :
                ByteBuffer.allocate(capacity);
    }

    /**
     * 获取缓冲区
     *
     * @param minCapacity 最小容量
     * @return 缓冲区
     */
    private ByteBuffer getBuffer(int minCapacity) {
        ByteBuffer buffer = bufferPool.poll();
        if (buffer == null || buffer.capacity() < minCapacity) {
            // If no buffer available or too small, create a new one
            return createBuffer(Math.max(minCapacity, defaultBufferSize));
        }
        buffer.clear(); // Reset position and limit
        return buffer;
    }

    /**
     * 释放缓冲区
     *
     * @param buffer 缓冲区
     */
    private void returnBuffer(ByteBuffer buffer) {
        if (buffer != null && buffer.capacity() <= defaultBufferSize * 2) {
            // Clear buffer before returning to pool
            buffer.clear();
            bufferPool.offer(buffer);
        }
    }

    /**
     * 发送命令
     *
     * @param command 命令
     * @return 响应
     */
    public String sendCommand(String command) {
        checkConnection();
        return send(TYPE_COMMAND, command);
    }

    /**
     * 异步发送命令
     *
     * @param command 命令
     * @return 响应
     */
    public Future<String> sendCommandAsync(String command) {
        return SHARED_EXECUTOR.submit(() -> sendCommand(command));
    }

    /**
     * 发送批量命令
     *
     * @param commands 命令列表
     * @return 响应列表
     */
    public List<String> sendBatchCommands(List<String> commands) {
        List<String> responses = new ArrayList<>(commands.size());
        for (String command : commands) {
            try {
                responses.add(sendCommand(command));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "命令执行失败: " + command, e);
                responses.add("Error: " + e.getMessage());
            }
        }
        return responses;
    }

    /**
     * 并行发送批量命令
     *
     * @param commands 命令列表
     * @return 响应列表
     */
    public List<CompletableFuture<String>> sendBatchCommandsParallel(List<String> commands) {
        List<CompletableFuture<String>> futures = new ArrayList<>(commands.size());
        for (String command : commands) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendCommand(command);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "命令执行失败: " + command, e);
                    return "Error: " + e.getMessage();
                }
            }, SHARED_EXECUTOR);
            futures.add(future);
        }
        return futures;
    }

    /**
     * 异步发送批量命令
     *
     * @param commands 命令列表
     * @return 响应列表
     */
    public Future<List<String>> sendBatchCommandsAsync(List<String> commands) {
        return SHARED_EXECUTOR.submit(() -> sendBatchCommands(commands));
    }

    @Override
    public void close() {
        isConnected.set(false);
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            LOGGER.warning("关闭socket通道失败: " + e.getMessage());
        } finally {
            // 清空缓冲区池，对于 direct buffer 需要显式清理
            if (useDirectBuffers) {
                // 对于 direct buffer，遍历并清空以帮助 GC 回收
                ByteBuffer buffer;
                while ((buffer = bufferPool.poll()) != null) {
                    // direct buffer 无法直接释放，但清空引用有助于 GC 通过 Cleaner 回收
                    buffer.clear();
                }
            } else {
                bufferPool.clear();
            }
        }
    }

    private static void applySocketOptions(SocketChannel channel, int timeoutMs) throws IOException {
        channel.socket().setTcpNoDelay(true);
        channel.socket().setKeepAlive(true);
        channel.socket().setSoTimeout(timeoutMs);
        channel.socket().setReceiveBufferSize(defaultBufferSize);
        channel.socket().setSendBufferSize(defaultBufferSize);
    }

    /**
     * 认证
     *
     * @param password 密码
     */
    private void authenticate(String password) {
        LOGGER.info("开始RCON认证流程");
        try {
            authenticateWithCurrentConnection(password);
            LOGGER.info("RCON认证成功");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "RCON认证失败: {0}", e.getMessage());
            throw new RconClientException("RCON认证失败: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "RCON认证失败: {0}", e.getMessage());
            throw e;
        }
    }

    private void authenticateWithCurrentConnection(String password) throws IOException {
        sendInternal(TYPE_AUTH, password);
    }

    /**
     * 发送命令，使用 ReentrantLock 替代 synchronized 以获得更细粒度的锁控制
     *
     * @param type    类型
     * @param payload 负载
     * @return 响应
     */
    private String send(int type, String payload) {
        sendLock.lock();
        try {
            int attempts = 0;
            while (attempts < maxReconnectAttempts) {
                try {
                    return sendInternal(type, payload);
                } catch (IOException e) {
                    attempts++;
                    LOGGER.log(Level.WARNING, String.format("连接失败（第 %d 次尝试，共 %d 次）: %s",
                            attempts, maxReconnectAttempts, e.getMessage()));

                    if (attempts < maxReconnectAttempts) {
                        try {
                            waitBeforeReconnect();
                            reconnect();
                        } catch (IOException ioException) {
                            throw new RconClientException("重连等待被中断", ioException);
                        }
                    } else {
                        LOGGER.severe(String.format("在 %d 次尝试后仍然无法发送命令: %s",
                                maxReconnectAttempts, e.getMessage()));
                        throw new RconClientException("在 " + maxReconnectAttempts + " 次尝试后仍然无法发送命令", e);
                    }
                }
            }
            throw new RconClientException("在 " + maxReconnectAttempts + " 次尝试后仍然无法发送命令");
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * 发送内部
     *
     * @param type    类型
     * @param payload 负载
     * @return 响应
     */
    private String sendInternal(int type, String payload) throws IOException {
        int requestId = currentRequestId.getAndIncrement();

        byte[] payloadBytes = payload.getBytes(payloadCharset);
        ByteBuffer buffer = null;
        try {
            buffer = toByteBuffer(requestId, type, payloadBytes);
            writeFully(buffer);

            ResponsePacket firstPacket = readResponsePacket(defaultTimeoutMs, false);
            validateResponsePacket(requestId, firstPacket);

            if (type == TYPE_AUTH) {
                return decodePayload(firstPacket.payload).trim();
            }

            StringBuilder allPayload = new StringBuilder(decodePayload(firstPacket.payload));
            int interPacketTimeoutMs = resolveInterPacketTimeoutMs();

            while (true) {
                ResponsePacket packet = readResponsePacket(interPacketTimeoutMs, true);
                if (packet == null) {
                    break;
                }
                validateResponsePacket(requestId, packet);
                allPayload.append(decodePayload(packet.payload));

                if (isTerminalPayload(packet.payload)) {
                    break;
                }
            }

            return allPayload.toString().trim();
        } finally {
            if (buffer != null) {
                returnBuffer(buffer);
            }
        }
    }

    /**
     * 重连
     */
    private void reconnect() {
        try {
            LOGGER.info(String.format("正在关闭与 %s:%d 的现有连接", host, port));
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "关闭现有连接时出错: " + e.getMessage(), e);
        }

        try {
            LOGGER.info(String.format("正在尝试重新连接到 %s:%d", host, port));
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            applySocketOptions(socketChannel, defaultTimeoutMs);
            socketChannel.socket().connect(new InetSocketAddress(host, port), defaultTimeoutMs);
            socketChannel.configureBlocking(false);
            LOGGER.info("Socket连接已重新建立，正在尝试认证");
            authenticateWithCurrentConnection(password);
            isConnected.set(true);
            LOGGER.info("成功重新连接并认证到 " + host + ":" + port);
        } catch (IOException e) {
            isConnected.set(false);
            String errorMsg = String.format("无法重新连接到 %s:%d - %s", host, port, e.getMessage());
            LOGGER.severe(errorMsg);
            throw new RconClientException(errorMsg, e);
        }
    }

    private void waitBeforeReconnect() throws IOException {
        if (reconnectDelayMs <= 0) {
            return;
        }
        LOGGER.info(String.format("等待 %dms 后进行重连尝试", reconnectDelayMs));
        sleepQuietly(reconnectDelayMs);
    }

    private ResponsePacket readResponsePacket(int timeoutMs, boolean allowTimeoutWithoutData) throws IOException {
        ByteBuffer sizeBuffer = null;
        ByteBuffer dataBuffer = null;

        try {
            sizeBuffer = getBuffer(Integer.BYTES);
            sizeBuffer.limit(Integer.BYTES);
            boolean hasHeader = readFully(sizeBuffer, timeoutMs, allowTimeoutWithoutData);
            if (!hasHeader) {
                return null;
            }

            sizeBuffer.flip();
            sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int size = sizeBuffer.getInt();

            if (size < MIN_PACKET_SIZE || size > maxResponseSize) {
                throw new IOException(String.format("无效的响应大小: %d（允许范围: %d-%d）", size, MIN_PACKET_SIZE, maxResponseSize));
            }

            dataBuffer = getBuffer(size);
            dataBuffer.limit(size);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            readFully(dataBuffer, timeoutMs, false);
            dataBuffer.flip();

            int responseId = dataBuffer.getInt();
            int responseType = dataBuffer.getInt();
            byte[] bodyBytes = new byte[dataBuffer.remaining()];
            dataBuffer.get(bodyBytes);

            return new ResponsePacket(responseId, responseType, bodyBytes);
        } finally {
            returnBuffer(sizeBuffer);
            returnBuffer(dataBuffer);
        }
    }

    /**
     * 检查连接
     */
    private void checkConnection() {
        if (!isConnected.get() || socketChannel == null || !socketChannel.isOpen() || !socketChannel.isConnected()) {
            throw new RconClientException("未连接到RCON服务器");
        }
    }

    /**
     * 读取完整
     *
     * @param buffer 缓冲区
     * @throws IOException IO异常
     */
    private boolean readFully(ByteBuffer buffer, int timeoutMs, boolean allowTimeoutWithoutData) throws IOException {
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(1, timeoutMs));
        long deadline = System.nanoTime() + timeoutNanos;
        int totalBytesRead = 0;

        while (buffer.hasRemaining()) {
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead > 0) {
                totalBytesRead += bytesRead;
                continue;
            }

            if (bytesRead < 0) {
                throw new IOException("读取到流结束，已读取 " + totalBytesRead + " 字节");
            }

            if (System.nanoTime() >= deadline) {
                if (allowTimeoutWithoutData && totalBytesRead == 0) {
                    return false;
                }
                throw new IOException("读取超时，已读取 " + totalBytesRead + " 字节");
            }

            sleepQuietly(READ_IDLE_SLEEP_MS);
        }
        return true;
    }

    private void writeFully(ByteBuffer buffer) throws IOException {
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(1, defaultTimeoutMs));
        long deadline = System.nanoTime() + timeoutNanos;

        while (buffer.hasRemaining()) {
            int bytesWritten = socketChannel.write(buffer);
            if (bytesWritten > 0) {
                continue;
            }

            if (bytesWritten < 0) {
                throw new IOException("写入失败：连接已关闭");
            }

            if (System.nanoTime() >= deadline) {
                throw new IOException("写入超时");
            }
            sleepQuietly(READ_IDLE_SLEEP_MS);
        }
    }

    private void validateResponsePacket(int requestId, ResponsePacket packet) {
        if (packet.requestId == AUTHENTICATION_FAILURE_ID) {
            throw new AuthFailureException();
        }

        if (packet.requestId != requestId) {
            throw new RconClientException("无效的响应ID: 期望 " + requestId + "，但收到 " + packet.requestId);
        }
    }

    private String decodePayload(byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return "";
        }

        int payloadLength = bodyBytes.length;
        while (payloadLength > 0 && bodyBytes[payloadLength - 1] == 0) {
            payloadLength--;
        }
        return new String(bodyBytes, 0, payloadLength, payloadCharset);
    }

    private boolean isTerminalPayload(byte[] bodyBytes) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return true;
        }

        for (byte bodyByte : bodyBytes) {
            if (bodyByte != 0) {
                return false;
            }
        }
        return true;
    }

    private int resolveInterPacketTimeoutMs() {
        int candidate = defaultTimeoutMs / 4;
        if (candidate <= 0) {
            return DEFAULT_INTER_PACKET_TIMEOUT_MS;
        }
        return Math.max(20, Math.min(200, candidate));
    }

    // 响应数据包记录类，包含请求ID、类型和负载字节数组
    private record ResponsePacket(int requestId, int type, byte[] payload) {

    }

    /**
     * 转换为字节缓冲区
     *
     * @param requestId    请求ID
     * @param type         类型
     * @param payloadBytes 负载字节数组
     * @return 字节缓冲区
     */
    private ByteBuffer toByteBuffer(int requestId, int type, byte[] payloadBytes) {
        int totalSize = (3 * Integer.BYTES) + payloadBytes.length + (2 * Byte.BYTES);
        ByteBuffer buffer = getBuffer(totalSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt((2 * Integer.BYTES) + payloadBytes.length + (2 * Byte.BYTES));
        buffer.putInt(requestId);
        buffer.putInt(type);
        buffer.put(payloadBytes);
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        buffer.flip();
        return buffer;
    }

    /**
     * 检查SocketChannel是否打开
     *
     * @return 是否打开
     */
    public Boolean isSocketChannelOpen() {
        return socketChannel != null && socketChannel.isOpen();
    }

    /**
     * 关闭共享线程池，应在应用关闭时调用
     */
    public static void shutdownSharedExecutor() {
        SHARED_EXECUTOR.shutdown();
        try {
            if (!SHARED_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                SHARED_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SHARED_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ========== 配置 setter 方法，由 RconConfig.init() 调用 ==========

    public static void setPayloadCharset(Charset charset) {
        payloadCharset = charset;
    }

    public static void setMaxReconnectAttempts(int attempts) {
        maxReconnectAttempts = attempts;
    }

    public static void setBufferPoolSize(int size) {
        bufferPoolSize = size;
    }

    public static void setReconnectDelayMs(long delayMs) {
        reconnectDelayMs = delayMs;
    }

    public static void setDefaultTimeoutMs(int timeoutMs) {
        defaultTimeoutMs = timeoutMs;
    }

    public static void setDefaultBufferSize(int bufferSize) {
        defaultBufferSize = bufferSize;
    }

    public static void setMaxResponseSize(int responseSize) {
        maxResponseSize = responseSize;
    }

    // ========== 配置 getter 方法 ==========

    public static Charset getPayloadCharset() {
        return payloadCharset;
    }

    public static int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public static int getBufferPoolSize() {
        return bufferPoolSize;
    }

    public static long getReconnectDelayMs() {
        return reconnectDelayMs;
    }

    public static int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public static int getDefaultBufferSize() {
        return defaultBufferSize;
    }

    public static int getMaxResponseSize() {
        return maxResponseSize;
    }
}


