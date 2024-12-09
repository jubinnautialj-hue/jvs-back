package cn.bctools.design.data.source.aspect;

import java.lang.annotation.*;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicDML {
}
