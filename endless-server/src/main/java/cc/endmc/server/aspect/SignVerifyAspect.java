package cc.endmc.server.aspect;

import cc.endmc.common.constant.HttpStatus;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.domain.model.LoginUser;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.ServletUtils;
import cc.endmc.framework.web.service.TokenService;
import cc.endmc.server.annotation.SignVerify;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 签名验证AOP切面
 * 通过注解方式实现签名验证
 *
 * 支持方法级别和类级别的签名验证：
 * 1. 方法级别注解优先级最高
 * 2. 类级别注解作用于所有公开方法，但可通过excludeMethods排除特定方法
 * 3. 方法级别的enabled=false可以覆盖类级别的验证
 * 4. 兼容RuoYi JWT验证：如果请求包含有效的JWT token，则跳过签名验证
 *
 * @author Memory
 */
@Slf4j
@Aspect
@Order(0) // 设置最高优先级，确保签名验证最先执行
@Component
@SuppressWarnings("unchecked")
public class SignVerifyAspect {

    @Value("${app.secret-key:}")
    private String secretKey;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TokenService tokenService;

    /**
     * 环绕通知，处理方法级别的签名验证
     */
    @Around("@annotation(signVerify)")
    public Object around(ProceedingJoinPoint joinPoint, SignVerify signVerify) throws Throwable {
        return processSignVerify(joinPoint, signVerify);
    }

    /**
     * 环绕通知，处理类级别的签名验证
     * 只有当方法没有 @SignVerify 注解时才会执行，避免重复验证
     */
    @Around("@within(signVerify) && execution(public * *(..)) && !@annotation(cc.endmc.server.annotation.SignVerify)")
    public Object aroundClass(ProceedingJoinPoint joinPoint, SignVerify signVerify) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // 检查方法是否在排除列表中
        if (Arrays.asList(signVerify.excludeMethods()).contains(methodName)) {
            log.debug("方法 {} 在排除列表中，跳过签名验证", methodName);
            return joinPoint.proceed();
        }

        // 使用类级别的配置
        log.debug("使用类级别的SignVerify配置验证方法: {}", methodName);
        return processSignVerify(joinPoint, signVerify);
    }

    /**
     * 处理签名验证的核心逻辑
     */
    private Object processSignVerify(ProceedingJoinPoint joinPoint, SignVerify signVerify) throws Throwable {
        // 如果注解禁用，直接执行原方法
        if (!signVerify.enabled()) {
            log.debug("签名验证已禁用，跳过验证");
            return joinPoint.proceed();
        }

        try {
            // 获取HTTP请求和响应
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                throw new RuntimeException("无法获取HTTP请求上下文");
            }

            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            // 检查是否启用JWT兼容模式，如果启用且有有效的JWT token，则跳过签名验证
            if (signVerify.enableJwtCompatible() && hasValidJwtToken(request)) {
                log.debug("JWT兼容模式已启用且检测到有效的JWT token，跳过签名验证");
                return joinPoint.proceed();
            }

            // 执行签名验证
            if (!validateSign(request, response, signVerify)) {
                return null; // 验证失败，直接返回
            }

            // 验证通过，继续执行原方法
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("签名验证异常", e);
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getResponse() != null) {
                writeErrorResponse(attributes.getResponse(), HttpStatus.ERROR,
                        StringUtils.hasText(signVerify.message()) ? signVerify.message() : "签名验证异常");
            }
            return null;
        }
    }

    /**
     * 检查请求是否包含有效的JWT token
     */
    private boolean hasValidJwtToken(HttpServletRequest request) {
        try {
            // 尝试从请求中获取登录用户信息
            LoginUser loginUser = tokenService.getLoginUser(request);
            if (loginUser != null) {
                // 验证token是否有效
                tokenService.verifyToken(loginUser);
                log.debug("JWT token验证成功，用户: {}", loginUser.getUsername());
                return true;
            }
        } catch (Exception e) {
            log.debug("JWT token验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证签名
     */
    private boolean validateSign(HttpServletRequest request, HttpServletResponse response, SignVerify signVerify) throws Exception {
        // 1. 验证请求头
        if (!validateHeaders(request, response, signVerify)) {
            return false;
        }

        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String sign = request.getHeader("X-Sign");

        // 2. 校验时间戳
        if (!validateTimestamp(timestamp, response, signVerify)) {
            return false;
        }

        // 3. 防重放验证
        if (!validateNonce(nonce, response, signVerify)) {
            return false;
        }

        // 4. 校验签名
        if (!validateSignature(timestamp, nonce, sign, response, signVerify)) {
            return false;
        }

        // 5. 限流验证
        return validateRateLimit(request, response, signVerify);
    }

    /**
     * 验证请求头
     */
    private boolean validateHeaders(HttpServletRequest request, HttpServletResponse response, SignVerify signVerify) throws Exception {
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String sign = request.getHeader("X-Sign");

        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "缺少必要的请求头：X-Timestamp、X-Nonce、X-Sign";
            writeErrorResponse(response, HttpStatus.BAD_REQUEST, message);
            return false;
        }
        return true;
    }

    /**
     * 校验时间戳
     */
    private boolean validateTimestamp(String timestamp, HttpServletResponse response, SignVerify signVerify) throws Exception {
        try {
            long now = System.currentTimeMillis();
            long ts = Long.parseLong(timestamp);
            long validity = signVerify.timestampValidity();

            if (Math.abs(now - ts) > validity) {
                String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "请求时间戳已过期，请重新发起请求";
                writeErrorResponse(response, HttpStatus.FORBIDDEN, message);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "时间戳格式错误";
            writeErrorResponse(response, HttpStatus.BAD_REQUEST, message);
            return false;
        }
    }

    /**
     * 防重放验证
     */
    private boolean validateNonce(String nonce, HttpServletResponse response, SignVerify signVerify) throws Exception {
        String nonceKey = "nonce:" + nonce;
        Boolean exists = redisCache.hasKey(nonceKey);

        if (Boolean.TRUE.equals(exists)) {
            String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "请求重复，请检查nonce值";
            writeErrorResponse(response, HttpStatus.FORBIDDEN, message);
            return false;
        }

        // 设置nonce缓存，有效期根据注解配置
        long validityMinutes = signVerify.timestampValidity() / (60 * 1000);
        redisCache.setCacheObject(nonceKey, "1", (int) validityMinutes, TimeUnit.MINUTES);
        return true;
    }

    /**
     * 校验签名
     */
    private boolean validateSignature(String timestamp, String nonce, String sign, HttpServletResponse response, SignVerify signVerify) throws Exception {
        if (!StringUtils.hasText(secretKey)) {
            log.warn("未配置签名密钥，跳过签名验证");
            return true;
        }

        String expectedSign = DigestUtils.sha256Hex(timestamp + nonce + secretKey);
        if (!expectedSign.equals(sign)) {
            String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "签名验证失败，请检查签名算法";
            writeErrorResponse(response, HttpStatus.FORBIDDEN, message);
            return false;
        }
        return true;
    }

    /**
     * 限流验证
     */
    private boolean validateRateLimit(HttpServletRequest request, HttpServletResponse response, SignVerify signVerify) throws Exception {
        String ip = getClientIpAddress(request);
        String rateLimitKey = "rate:" + ip;

        Long count = redisCache.redisTemplate.opsForValue().increment(rateLimitKey);
        if (count != null && count == 1) {
            redisCache.expire(rateLimitKey, signVerify.rateLimitWindow(), TimeUnit.SECONDS);
        }

        if (count != null && count > signVerify.rateLimitCount()) {
            String message = StringUtils.hasText(signVerify.message()) ? signVerify.message() : "请求过于频繁，请稍后再试";
            writeErrorResponse(response, 429, message);
            return false;
        }

        return true;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        AjaxResult result = new AjaxResult(status, message);
        String jsonResult = JSON.toJSONString(result);

        ServletUtils.renderString(response, jsonResult);
    }
}
