package cc.endmc.permission.aspect;

import cc.endmc.common.exception.ServiceException;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.permission.annotation.RconPermission;
import cc.endmc.permission.service.IResourcePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * RCON权限检查切面
 *
 * @author Memory
 * @date 2025-12-25
 */
@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class RconPermissionAspect {

    private final IResourcePermissionService resourcePermissionService;

    @Before("@annotation(rconPermission)")
    public void checkRconPermission(JoinPoint joinPoint, RconPermission rconPermission) {
        try {
            // 获取当前用户ID
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                throw new ServiceException("用户未登录");
            }

            // 如果允许管理员绕过且当前用户是管理员，直接通过
            if (rconPermission.allowAdmin() && resourcePermissionService.isAdmin(userId)) {
                return;
            }

            // 如果不需要具体的服务器ID，只检查用户是否有任意RCON服务器的对应权限
            if (!rconPermission.requireServerId()) {
                List<Long> userServerIds = resourcePermissionService.getUserRconServerIds(userId);
                if (userServerIds == null || !userServerIds.isEmpty()) {
                    // 管理员或有权限的服务器列表不为空，通过检查
                    return;
                }
                log.warn("用户[{}]尝试访问RCON功能但没有任何服务器权限", userId);
                throw new ServiceException(rconPermission.message());
            }

            // 获取服务器ID
            Long serverId = getServerIdFromRequest(joinPoint, rconPermission.serverIdParam());
            if (serverId == null) {
                throw new ServiceException("服务器ID参数缺失");
            }

            // 检查权限
            String permission = rconPermission.value();
            boolean hasPermission = resourcePermissionService.hasRconPermission(userId, serverId, permission);

            if (!hasPermission) {
                log.warn("用户[{}]尝试访问RCON服务器[{}]的[{}]权限被拒绝", userId, serverId, permission);
                throw new ServiceException(rconPermission.message());
            }

            log.debug("用户[{}]访问RCON服务器[{}]的[{}]权限检查通过", userId, serverId, permission);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("RCON权限检查异常", e);
            throw new ServiceException("权限检查失败");
        }
    }

    /**
     * 从请求中获取服务器ID
     */
    private Long getServerIdFromRequest(JoinPoint joinPoint, String serverIdParam) {
        // 1. 先从方法参数中获取
        Long serverId = getServerIdFromMethodParams(joinPoint, serverIdParam);
        if (serverId != null) {
            return serverId;
        }

        // 2. 从HTTP请求参数中获取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String serverIdStr = request.getParameter(serverIdParam);
            if (StringUtils.isNotEmpty(serverIdStr)) {
                try {
                    return Long.parseLong(serverIdStr);
                } catch (NumberFormatException e) {
                    log.warn("服务器ID参数格式错误: {}", serverIdStr);
                }
            }

            // 3. 从路径变量中获取
            String pathInfo = request.getPathInfo();
            if (StringUtils.isNotEmpty(pathInfo)) {
                String[] pathParts = pathInfo.split("/");
                for (int i = 0; i < pathParts.length - 1; i++) {
                    if (serverIdParam.equals(pathParts[i]) && i + 1 < pathParts.length) {
                        try {
                            return Long.parseLong(pathParts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("路径中服务器ID格式错误: {}", pathParts[i + 1]);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 从方法参数中获取服务器ID
     */
    private Long getServerIdFromMethodParams(JoinPoint joinPoint, String serverIdParam) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // 检查参数名是否匹配
            if (serverIdParam.equals(parameter.getName())) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        log.warn("方法参数中服务器ID格式错误: {}", arg);
                    }
                }
            }

            // 检查对象属性
            if (args[i] != null) {
                try {
                    java.lang.reflect.Field field = args[i].getClass().getDeclaredField(serverIdParam);
                    field.setAccessible(true);
                    Object value = field.get(args[i]);
                    if (value instanceof Long) {
                        return (Long) value;
                    }
                } catch (Exception e) {
                    // 忽略反射异常
                }
            }
        }

        return null;
    }
}