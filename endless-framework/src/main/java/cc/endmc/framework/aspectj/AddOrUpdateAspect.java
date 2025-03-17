package cc.endmc.framework.aspectj;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.core.domain.BaseEntity;
import cc.endmc.common.core.domain.entity.SysUser;
import cc.endmc.common.core.domain.model.LoginUser;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.common.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据过滤处理
 *
 * @author hhxx
 */
@Aspect
@Component
public class AddOrUpdateAspect {

    /**
     * 操作过滤
     *
     * @param joinPoint         切点
     * @param user              用户
     * @param addOrUpdateFilter 操作过滤
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, AddOrUpdateFilter addOrUpdateFilter) {
        String userName = user.getUserName();
        Date nowDate = DateUtils.getNowDate();
        Map<String, Object> paramsMap = new HashMap<>();
        boolean add = addOrUpdateFilter.add(); //新增
        boolean edit = addOrUpdateFilter.edit(); //修改

        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params)) {
            if (params instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) params;
                if (add) {
                    baseEntity.setCreateBy(userName);
                    baseEntity.setCreateTime(nowDate);
                    baseEntity.setUpdateBy(userName);
                    baseEntity.setUpdateTime(nowDate);
                } else if (edit) {
                    baseEntity.setUpdateBy(userName);
                    baseEntity.setUpdateTime(nowDate);
                }
            }
        }
    }

    @Before("@annotation(addOrUpdateFilter)")
    public void doBefore(JoinPoint point, AddOrUpdateFilter addOrUpdateFilter) throws Throwable {
        handleDataScope(point, addOrUpdateFilter);
    }

    protected void handleDataScope(final JoinPoint joinPoint, AddOrUpdateFilter addOrUpdateFilter) {
        // 获取当前的用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser)) {
            SysUser currentUser = loginUser.getUser();
            dataScopeFilter(joinPoint, currentUser, addOrUpdateFilter);
        }
    }
}