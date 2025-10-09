package cc.endmc.server.annotation;

import java.lang.annotation.*;

/**
 * 签名验证注解
 * 用于标记需要进行签名验证的方法
 *
 * @author Memory
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignVerify {

    /**
     * 是否启用签名验证
     * 默认为true
     */
    boolean enabled() default true;

    /**
     * 自定义错误消息
     * 为空时使用默认消息
     */
    String message() default "";

    /**
     * 时间戳有效期（毫秒）
     * 默认5分钟
     */
    long timestampValidity() default 5 * 60 * 1000L;

    /**
     * 限流次数
     * 默认每分钟10次
     */
    long rateLimitCount() default 10L;

    /**
     * 限流时间窗口（秒）
     * 默认60秒
     */
    long rateLimitWindow() default 60L;
}
