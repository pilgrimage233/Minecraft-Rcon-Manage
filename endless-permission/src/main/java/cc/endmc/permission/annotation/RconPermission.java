package cc.endmc.permission.annotation;

import java.lang.annotation.*;

/**
 * RCON权限检查注解
 * 用于标记需要RCON权限验证的方法
 *
 * @author Memory
 * @date 2025-12-25
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RconPermission {

    /**
     * 需要的权限类型
     * view: 查看日志权限
     * command: 执行命令权限
     * manage: 管理配置权限
     * admin: 管理员权限
     */
    String value() default "view";

    /**
     * 服务器ID参数名
     * 用于从请求参数中获取服务器ID
     */
    String serverIdParam() default "serverId";

    /**
     * 是否允许管理员绕过权限检查
     */
    boolean allowAdmin() default true;

    /**
     * 权限检查失败时的错误消息
     */
    String message() default "您没有访问该RCON服务器的权限";

    /**
     * 是否需要具体的服务器ID
     * false: 只检查用户是否有任意RCON服务器的对应权限
     * true: 需要检查具体服务器的权限
     */
    boolean requireServerId() default true;

    /**
     * 是否需要过滤数据
     * true: 在service层根据用户权限过滤返回的服务器数据
     * false: 不过滤数据
     */
    boolean filterData() default false;
}