package cc.endmc.node.common.annotation;

import java.lang.annotation.*;

/**
 * 节点操作日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NodeLog {
    /**
     * 操作类型
     * 1新增节点 2修改节点 3删除节点 4下载日志 5启动游戏服务器 6停止游戏服务器 7重启游戏服务器 8强制终止游戏服务器 9新增游戏服务器 10修改游戏服务器 11删除游戏服务器
     */
    String operationType();

    /**
     * 操作目标类型（1节点服务器 2游戏服务器）
     */
    String operationTarget();

    /**
     * 操作名称
     */
    String operationName() default "";

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;
} 