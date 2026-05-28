package cc.endmc.node.interceptor;

import cc.endmc.node.utils.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 日志拦截器
 * 自动为每个请求添加追踪ID
 *
 * @author Memory
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String NODE_ID_HEADER = "X-Node-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成或获取追踪ID
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = LogUtil.generateTraceId();
        } else {
            LogUtil.setTraceId(traceId);
        }

        // 获取节点ID
        String nodeId = request.getHeader(NODE_ID_HEADER);
        if (nodeId != null && !nodeId.isEmpty()) {
            try {
                LogUtil.setNodeId(Long.parseLong(nodeId));
            } catch (NumberFormatException e) {
                // 忽略无效的节点ID
            }
        }

        // 设置响应头
        response.setHeader(TRACE_ID_HEADER, traceId);

        log.debug("请求开始: {} {}, traceId={}", request.getMethod(), request.getRequestURI(), traceId);
        request.setAttribute("startTime", System.currentTimeMillis());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // 什么都不做
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        try {
            long startTime = (long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;

            log.debug("请求完成: {} {}, 状态码={}, 耗时={}ms, traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    LogUtil.getTraceId());

            // 如果请求耗时超过阈值，记录警告
            if (duration > 5000) {
                log.warn("请求耗时过长: {} {}, 耗时={}ms", request.getMethod(), request.getRequestURI(), duration);
            }
        } finally {
            // 清除 MDC 上下文
            LogUtil.clear();
        }
    }
}
