package cc.endmc.node.common.interceptor;

import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.common.utils.ServletUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.node.common.annotation.NodeLog;
import cc.endmc.node.domain.NodeOperationLog;
import cc.endmc.node.service.INodeOperationLogService;
import com.alibaba.fastjson2.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 节点操作日志记录处理
 */
@Aspect
@Component
public class NodeLogInterceptor {
    private static final Logger log = LoggerFactory.getLogger(NodeLogInterceptor.class);

    @Autowired
    private INodeOperationLogService nodeOperationLogService;

    // 配置织入点
    @Pointcut("@annotation(cc.endmc.node.common.annotation.NodeLog)")
    public void logPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()")
    public void doAfterReturning(JoinPoint joinPoint) {
        handleLog(joinPoint, null);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e) {
        try {
            // 获得注解
            NodeLog controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }
            // 获取当前的用户
            String username = SecurityUtils.getLoginUser().getUsername();

            if (StringUtils.isEmpty(username)) {
                username = "unknown";
            }

            // 创建操作日志对象
            NodeOperationLog nodeLog = new NodeOperationLog();
            nodeLog.setStatus("0");
            nodeLog.setOperationIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
            nodeLog.setOperationType(controllerLog.operationType());
            nodeLog.setOperationTarget(controllerLog.operationTarget());
            nodeLog.setOperationName(controllerLog.operationName());
            nodeLog.setCreateBy(username);
            nodeLog.setCreateTime(new Date());

            if (e != null) {
                nodeLog.setStatus("1");
                nodeLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            nodeLog.setMethodName(className + "." + methodName + "()");

            // 设置请求参数
            if (controllerLog.isSaveRequestData()) {
                setRequestValue(joinPoint, nodeLog);
            }

            // 保存数据库
            nodeOperationLogService.insertNodeOperationLog(nodeLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解
     */
    private NodeLog getAnnotationLog(JoinPoint joinPoint) throws Exception {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(NodeLog.class);
        }
        return null;
    }

    /**
     * 设置请求参数
     */
    private void setRequestValue(JoinPoint joinPoint, NodeOperationLog nodeLog) {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            String method = request.getMethod();
            if (HttpMethod.PUT.name().equals(method) || HttpMethod.POST.name().equals(method)) {
                String params = argsArrayToString(joinPoint.getArgs());
                nodeLog.setOperationParam(StringUtils.substring(params, 0, 2000));
            }
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        params.append(JSON.toJSONString(o)).append(" ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象
     */
    @SuppressWarnings("rawtypes")
    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
} 