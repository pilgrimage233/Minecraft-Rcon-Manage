package cc.endmc.web.controller.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 更新进度推送控制器
 * 使用 SSE (Server-Sent Events) 实时推送更新进度
 */
@Slf4j
@RestController
@RequestMapping("/system/update")
public class UpdateProgressController {

    // 存储所有活跃的 SSE 连接
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 发送进度消息到所有连接的客户端
     *
     * @param stage   阶段标识
     * @param message 消息内容
     * @param percent 进度百分比 (0-100)
     */
    public static void sendProgress(String stage, String message, int percent) {
        Map<String, Object> data = Map.of(
                "stage", stage,
                "message", message,
                "percent", percent,
                "timestamp", System.currentTimeMillis()
        );

        emitters.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(data));
            } catch (IOException e) {
                log.error("发送进度消息失败: {}", sessionId, e);
                emitters.remove(sessionId);
            }
        });
    }

    /**
     * 发送完成消息并关闭所有连接
     */
    public static void sendComplete(boolean success, String message) {
        Map<String, Object> data = Map.of(
                "stage", success ? "completed" : "failed",
                "message", message,
                "percent", 100,
                "timestamp", System.currentTimeMillis()
        );

        emitters.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(data));
                emitter.complete();
            } catch (IOException e) {
                log.error("发送完成消息失败: {}", sessionId, e);
            }
        });

        emitters.clear();
    }

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress() {
        String sessionId = generateSessionId();
        SseEmitter emitter = new SseEmitter(600000L); // 10分钟超时

        emitter.onCompletion(() -> {
            log.info("SSE 连接完成: {}", sessionId);
            emitters.remove(sessionId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时: {}", sessionId);
            emitters.remove(sessionId);
        });

        emitter.onError((ex) -> {
            log.error("SSE 连接错误: {}", sessionId, ex);
            emitters.remove(sessionId);
        });

        emitters.put(sessionId, emitter);
        log.info("新的 SSE 连接建立: {}, 当前连接数: {}", sessionId, emitters.size());

        // 发送初始连接成功消息
        sendProgress("connected", "连接已建立", 0);

        return emitter;
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + System.identityHashCode(Thread.currentThread());
    }
}
