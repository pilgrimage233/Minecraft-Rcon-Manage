package cc.endmc.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AddOrUpdateFilter {

    /**
     * 新增
     */
    public boolean add() default false;

    /**
     * 修改
     */
    public boolean edit() default false;
}
