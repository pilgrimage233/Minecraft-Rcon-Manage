package cc.endmc.server.common.rconclient;

import lombok.SneakyThrows;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class RconClient implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(RconClient.class.getName());
    private static final int AUTHENTICATION_FAILURE_ID = -1;
    private static final Charset PAYLOAD_CHARSET = StandardCharsets.UTF_8;
    private static final int DEFAULT_TIMEOUT_MS = 5000;  // 超时时间
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long RECONNECT_DELAY_MS = 1000; // 重连延迟时间
    private static final int BUFFER_POOL_SIZE = 10;
    private static final int DEFAULT_BUFFER_SIZE = 8192; // 缓冲区大小
    private static final int MAX_RESPONSE_SIZE = 4096;  // 最大响应大小

    private static final int TYPE_COMMAND = 2;
    private static final int TYPE_AUTH = 3;

    private final String host;
    private final int port;
    private final String password;
    private final AtomicInteger currentRequestId;
    private final AtomicBoolean isConnected;
    private final ExecutorService executorService;
    private final BlockingQueue<ByteBuffer> bufferPool;
    private final boolean useDirectBuffers;
    private SocketChannel socketChannel;

    private RconClient(String host, int port, String password, SocketChannel socketChannel, boolean useDirectBuffers) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.currentRequestId = new AtomicInteger(1);
        this.isConnected = new AtomicBoolean(true);
        this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = defaultFactory.newThread(r);
                thread.setDaemon(true); // Make threads daemon so they don't prevent JVM shutdown
                return thread;
            }
        });
        this.useDirectBuffers = useDirectBuffers;
        this.bufferPool = new ArrayBlockingQueue<>(BUFFER_POOL_SIZE);

        // Pre-allocate buffers
        for (int i = 0; i < BUFFER_POOL_SIZE; i++) {
            bufferPool.offer(createBuffer(DEFAULT_BUFFER_SIZE));
        }
    }

    /**
     * 打开RconClient
     *
     * @param host     // 主机
     * @param port     // 端口
     * @param password // 密码
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password) {
        return open(host, port, password, DEFAULT_TIMEOUT_MS);
    }

    /**
     * 打开RconClient
     *
     * @param host      // 主机
     * @param port      // 端口
     * @param password  // 密码
     * @param timeoutMs // 超时时间
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password, int timeoutMs) {
        return open(host, port, password, timeoutMs, true);
    }

    /**
     * 打开RconClient
     *
     * @param host             // 主机
     * @param port             // 端口
     * @param password         // 密码
     * @param timeoutMs        // 超时时间
     * @param useDirectBuffers // 是否使用直接缓冲区
     * @return // RconClient
     */
    public static RconClient open(String host, int port, String password, int timeoutMs, boolean useDirectBuffers) {
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.socket().setTcpNoDelay(true);
            socketChannel.socket().setKeepAlive(true);
            socketChannel.socket().setSoTimeout(timeoutMs);
            socketChannel.socket().setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
            socketChannel.socket().setSendBufferSize(DEFAULT_BUFFER_SIZE);

            // Set connection timeout
            socketChannel.configureBlocking(true);

            LOGGER.info(String.format("正在尝试连接RCON服务器 %s:%d，超时时间 %dms", host, port, timeoutMs));

            // Use a separate thread with timeout for connection
            CompletableFuture<Boolean> connectionFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    socketChannel.socket().connect(new InetSocketAddress(host, port), timeoutMs);
                    return true;
                } catch (IOException e) {
                    LOGGER.severe("连接失败: " + e.getMessage());
                    return false;
                }
            });

            try {
                if (!connectionFuture.get(timeoutMs, TimeUnit.MILLISECONDS)) {
                    throw new RconClientException("连接超时");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RconClientException("连接被中断", e);
            } catch (ExecutionException e) {
                throw new RconClientException("连接执行错误", e.getCause());
            } catch (TimeoutException e) {
                throw new RconClientException("连接在 " + timeoutMs + "ms 后超时", e);
            }

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

    /**
     * 创建缓冲区
     *
     * @param capacity // 容量
     * @return // 缓冲区
     */
    private ByteBuffer createBuffer(int capacity) {
        return useDirectBuffers ?
                ByteBuffer.allocateDirect(capacity) :
                ByteBuffer.allocate(capacity);
    }

    /**
     * 获取缓冲区
     *
     * @param minCapacity // 最小容量
     * @return // 缓冲区
     */
    private ByteBuffer getBuffer(int minCapacity) {
        ByteBuffer buffer = bufferPool.poll();
        if (buffer == null || buffer.capacity() < minCapacity) {
            // If no buffer available or too small, create a new one
            return createBuffer(Math.max(minCapacity, DEFAULT_BUFFER_SIZE));
        }
        buffer.clear(); // Reset position and limit
        return buffer;
    }

    /**
     * 释放缓冲区
     *
     * @param buffer // 缓冲区
     */
    private void returnBuffer(ByteBuffer buffer) {
        if (buffer != null && buffer.capacity() <= DEFAULT_BUFFER_SIZE * 2) {
            // Clear buffer before returning to pool
            buffer.clear();
            bufferPool.offer(buffer);
        }
    }

    /**
     * 发送命令
     *
     * @param command // 命令
     * @return // 响应
     */
    public String sendCommand(String command) {
        checkConnection();
        return send(TYPE_COMMAND, command);
    }

    /**
     * 异步发送命令
     *
     * @param command // 命令
     * @return // 响应
     */
    public Future<String> sendCommandAsync(String command) {
        return executorService.submit(() -> sendCommand(command));
    }

    /**
     * 发送批量命令
     *
     * @param commands // 命令列表
     * @return // 响应列表
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
     * @param commands // 命令列表
     * @return // 响应列表
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
            }, executorService);
            futures.add(future);
        }
        return futures;
    }

    /**
     * 异步发送批量命令
     *
     * @param commands // 命令列表
     * @return // 响应列表
     */
    public Future<List<String>> sendBatchCommandsAsync(List<String> commands) {
        return executorService.submit(() -> sendBatchCommands(commands));
    }

    @Override
    public void close() {
        isConnected.set(false);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RconClientException("关闭socket通道失败", e);
        } finally {
            // Clear buffer pool
            bufferPool.clear();
        }
    }

    /**
     * 认证
     *
     * @param password // 密码
     */
    private void authenticate(String password) {
        LOGGER.info("开始RCON认证流程");
        try {
            send(TYPE_AUTH, password);
            LOGGER.info("RCON认证成功");
        } catch (Exception e) {
            LOGGER.severe("RCON认证失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 发送
     *
     * @param type    // 类型
     * @param payload // 负载
     * @return // 响应
     */
    private synchronized String send(int type, String payload) {
        int attempts = 0;
        while (attempts < MAX_RECONNECT_ATTEMPTS) {
            try {
                return sendInternal(type, payload);
            } catch (IOException e) {
                attempts++;
                LOGGER.log(Level.WARNING, String.format("连接失败（第 %d 次尝试，共 %d 次）: %s",
                        attempts, MAX_RECONNECT_ATTEMPTS, e.getMessage()));

                if (attempts < MAX_RECONNECT_ATTEMPTS) {
                    try {
                        LOGGER.info(String.format("等待 %dms 后进行重连尝试", RECONNECT_DELAY_MS));
                        Thread.sleep(RECONNECT_DELAY_MS);
                        reconnect();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        LOGGER.severe("重连被中断: " + ie.getMessage());
                        throw new RconClientException("重连被中断", ie);
                    }
                } else {
                    LOGGER.severe(String.format("在 %d 次尝试后仍然无法发送命令: %s",
                            MAX_RECONNECT_ATTEMPTS, e.getMessage()));
                    throw new RconClientException("在 " + MAX_RECONNECT_ATTEMPTS + " 次尝试后仍然无法发送命令", e);
                }
            }
        }
        throw new RconClientException("在 " + MAX_RECONNECT_ATTEMPTS + " 次尝试后仍然无法发送命令");
    }

    /**
     * 发送内部
     *
     * @param type    // 类型
     * @param payload // 负载
     * @return // 响应
     */
    private String sendInternal(int type, String payload) throws IOException {
        int requestId = currentRequestId.getAndIncrement();

        byte[] payloadBytes = payload.getBytes(PAYLOAD_CHARSET);
        ByteBuffer buffer = null;
        try {
            buffer = toByteBuffer(requestId, type, payloadBytes);
            socketChannel.write(buffer);

            ByteBuffer responseBuffer = readResponse();
            int responseId = responseBuffer.getInt();

            if (responseId == AUTHENTICATION_FAILURE_ID) {
                throw new AuthFailureException();
            }

            if (responseId != requestId) {
                throw new RconClientException("无效的响应ID: 期望 " + requestId + "，但收到 " + responseId);
            }

            int responseType = responseBuffer.getInt();

            byte[] bodyBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(bodyBytes);

            if (bodyBytes.length >= 2 && bodyBytes[bodyBytes.length - 1] == 0 && bodyBytes[bodyBytes.length - 2] == 0) {
                return new String(bodyBytes, 0, bodyBytes.length - 2, PAYLOAD_CHARSET).trim();
            } else {
                return new String(bodyBytes, PAYLOAD_CHARSET).trim();
            }
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
            socketChannel.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "关闭现有连接时出错: " + e.getMessage(), e);
        }

        try {
            LOGGER.info(String.format("正在尝试重新连接到 %s:%d", host, port));
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
            socketChannel.socket().setTcpNoDelay(true);
            socketChannel.socket().setKeepAlive(true);
            socketChannel.configureBlocking(true);
            LOGGER.info("Socket连接已重新建立，正在尝试认证");
            authenticate(password);
            isConnected.set(true);
            LOGGER.info("成功重新连接并认证到 " + host + ":" + port);
        } catch (IOException e) {
            isConnected.set(false);
            String errorMsg = String.format("无法重新连接到 %s:%d - %s", host, port, e.getMessage());
            LOGGER.severe(errorMsg);
            throw new RconClientException(errorMsg, e);
        }
    }

    @SneakyThrows
    private ByteBuffer readResponse() {
        ByteBuffer sizeBuffer = null;
        ByteBuffer dataBuffer = null;
        ByteBuffer nullsBuffer = null;

        try {
            // LOGGER.fine("正在读取响应大小");
            sizeBuffer = getBuffer(Integer.BYTES);
            sizeBuffer.limit(Integer.BYTES);
            readFully(sizeBuffer);
            sizeBuffer.flip();
            // LOGGER.fine("响应大小读取成功");
            sizeBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int size = sizeBuffer.getInt();

            // LOGGER.fine("正在检查响应大小");
            if (size <= 0 || size > MAX_RESPONSE_SIZE) {
                String errorMsg = String.format("无效的响应大小: %d 字节（最大允许: %d 字节）。这可能表示协议不匹配或服务器配置错误。", size, MAX_RESPONSE_SIZE);
                LOGGER.severe(errorMsg);

                // LOGGER.severe("无效的响应大小: " + size + " 字节（最大允许: " + MAX_RESPONSE_SIZE + " 字节）。这可能表示协议不匹配或服务器配置错误。");
                if (size <= 0 || size > 1000000) {
                    LOGGER.warning("尝试使用默认缓冲区大小恢复");
                    size = DEFAULT_BUFFER_SIZE;
                }
            }

            LOGGER.fine(String.format("响应大小: %d 字节", size));

            // 确保在分配缓冲区前大小合理
            // 注意：这里不再减去2个字节，而是读取完整数据
            int dataSize = Math.min(size, MAX_RESPONSE_SIZE);

            // LOGGER.fine("正在读取响应数据");
            dataBuffer = getBuffer(dataSize);
            dataBuffer.limit(dataSize);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

            try {
                readFully(dataBuffer);
            } catch (IOException e) {
                LOGGER.severe("读取数据时出错: " + e.getMessage());
                throw new RconClientException("读取响应数据失败: " + e.getMessage(), e);
            }

            dataBuffer.flip();

            // 不再单独读取空字节，而是作为数据的一部分
            // 在sendInternal中处理

            ByteBuffer result = ByteBuffer.allocate(dataBuffer.remaining());
            result.order(ByteOrder.LITTLE_ENDIAN);
            result.put(dataBuffer);
            result.flip();
            // LOGGER.fine("响应读取成功");
            return result;
        } catch (Exception e) {
            LOGGER.severe("读取响应时出错: " + e.getMessage());
            throw new RconClientException("读取响应失败: " + e.getMessage(), e);
        } finally {
            returnBuffer(sizeBuffer);
            returnBuffer(dataBuffer);
            returnBuffer(nullsBuffer);
        }
    }

    /**
     * 检查连接
     */
    private void checkConnection() {
        if (!isConnected.get() || !socketChannel.isConnected()) {
            throw new RconClientException("未连接到RCON服务器");
        }
    }

    /**
     * 读取完整
     *
     * @param buffer // 缓冲区
     * @throws IOException // IO异常
     */
    private void readFully(ByteBuffer buffer) throws IOException {
        int totalBytesRead = 0;
        int maxAttempts = 10;
        int attempts = 0;

        while (buffer.hasRemaining() && attempts < maxAttempts) {
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead == 0) {
                // No data available yet, wait a bit
                attempts++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("读取被中断");
                }
                continue;
            }

            if (bytesRead == -1) {
                throw new IOException("读取到流结束，已读取 " + totalBytesRead + " 字节");
            }

            totalBytesRead += bytesRead;
            attempts = 0; // Reset attempts counter on successful read
        }

        if (buffer.hasRemaining()) {
            throw new IOException("在 " + maxAttempts + " 次尝试后仍无法读取完整数据");
        }
    }

    /**
     * 转换为字节缓冲区
     *
     * @param requestId    // 请求ID
     * @param type         // 类型
     * @param payloadBytes // 负载字节数组
     * @return // 字节缓冲区
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
}


