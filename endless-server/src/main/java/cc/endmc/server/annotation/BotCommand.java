package cc.endmc.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 机器人命令注解
 * 用于标记处理QQ机器人命令的方法，便于AOP统一处理日志和响应
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {

    /**
     * 命令名称，用于日志记录
     * 如果不指定，则默认使用方法名
     */
    String name() default "";

    /**
     * 命令描述，简短描述命令功能
     */
    String description() default "";

    /**
     * 权限等级
     * 0 = 所有用户可用
     * 1 = 管理员可用
     * 2 = 超级管理员可用
     */
    int permissionLevel() default 0;
} 