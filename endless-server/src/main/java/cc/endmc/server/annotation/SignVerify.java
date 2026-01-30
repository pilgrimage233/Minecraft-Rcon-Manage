package cc.endmc.server.annotation;

import java.lang.annotation.*;

/**
 * 签名验证注解
 * 用于标记需要进行签名验证的方法或控制器
 *
 * 使用说明：
 * 1. 方法级别：在具体方法上使用，仅对该方法进行签名验证
 * 2. 控制器级别：在Controller类上使用，对该控制器的所有公开方法进行签名验证
 * 3. 方法级别的注解会覆盖类级别的配置
 * 4. 支持JWT兼容模式：当enableJwtCompatible=true时，如果请求包含有效的JWT token，则跳过签名验证
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
     *
     * 当在类级别使用时，可以通过在特定方法上设置enabled=false来跳过验证
     */
    boolean enabled() default true;

    /**
     * 是否启用JWT兼容模式
     * 默认为true
     * <p>
     * 当为true时，如果请求包含有效的JWT token，则跳过签名验证
     * 当为false时，即使有JWT token也必须进行签名验证
     */
    boolean enableJwtCompatible() default true;

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

    /**
     * 排除的方法名称
     * 当在类级别使用时，可以指定不需要签名验证的方法名
     * 仅在类级别注解时生效
     */
    String[] excludeMethods() default {};
}
