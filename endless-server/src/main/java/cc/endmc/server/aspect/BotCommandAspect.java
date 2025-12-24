package cc.endmc.server.aspect;

import cc.endmc.server.annotation.BotCommand;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 机器人命令切面，用于统一处理命令日志和权限
 */
@Aspect
@Component
@Slf4j
public class BotCommandAspect {

    @PostConstruct
    public void init() {
        log.info("BotCommandAspect初始化成功，AOP切面已加载");
    }

    /**
     * 定义切入点
     */
    @Pointcut("@annotation(cc.endmc.server.annotation.BotCommand)")
    public void botCommandPointcut() {
    }

    /**
     * 前置通知，用于调试
     */
    @Before("botCommandPointcut()")
    public void beforeCommandExecution(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        // log.info("BotCommand Before通知: 方法[{}]被拦截", method.getName());
    }

    /**
     * 环绕通知，在命令方法执行前后进行日志记录
     *
     * @param point 连接点
     * @return 方法执行结果
     * @throws Throwable 如果方法执行出错
     */
    @Around("botCommandPointcut()")
    public Object aroundCommandExecution(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法上的注解
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        BotCommand annotation = method.getAnnotation(BotCommand.class);
        String methodName = method.getName();

        // 记录切面被调用
        log.info("BotCommand AOP环绕通知: 方法[{}]被拦截", methodName);

        // 如果注解的name为空，则使用方法名作为命令名
        String commandName = annotation.name().isEmpty() ? methodName : annotation.name();

        // 获取 QQMessage 参数
        QQMessage message = null;
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof QQMessage) {
                message = (QQMessage) arg;
                break;
            }
        }

        if (message == null) {
            log.warn("执行命令方法时未找到QQMessage参数: {}", methodName);
            return point.proceed();
        }

        // 生成方法参数记录
        String methodParams = formatMethodParams(message, args);

        // 记录方法执行结果
        Object result = null;
        String methodResult = commandName;
        String errorMessage = null;
        String stackTrace = null;

        try {
            result = point.proceed();
            // 方法执行后返回值可能为具体执行结果或消息发送结果
            if (result != null) {
                methodResult = JSON.toJSONString(result);
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            stackTrace = getStackTraceAsString(e);

            // 记录错误
            log.error("命令执行异常: {}", errorMessage, e);

            // 重新抛出异常，让调用方处理
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录方法调用信息 (调用BotClient中的日志方法)
            BotClient botClient = getBotClientFromPoint(point);
            if (botClient != null) {
                botClient.logCommandExecution(
                        methodName, methodParams, methodResult, executionTime,
                        errorMessage, stackTrace, message
                );
            } else {
                log.warn("无法获取BotClient实例，无法记录命令执行日志");
            }
        }

        return result;
    }

    /**
     * 格式化方法参数为JSON字符串
     */
    private String formatMethodParams(QQMessage message, Object[] args) {
        try {
            return String.format("{\"command\":\"%s\",\"groupId\":%d,\"userId\":%d}",
                    message.getMessage(), message.getGroupId(), message.getUserId());
        } catch (Exception e) {
            log.warn("格式化方法参数失败: {}", e.getMessage());
            return Arrays.toString(args);
        }
    }

    /**
     * 从连接点中获取BotClient实例
     */
    private BotClient getBotClientFromPoint(ProceedingJoinPoint point) {
        try {
            return (BotClient) point.getTarget();
        } catch (Exception e) {
            log.warn("获取BotClient实例失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将异常堆栈转换为字符串
     */
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 