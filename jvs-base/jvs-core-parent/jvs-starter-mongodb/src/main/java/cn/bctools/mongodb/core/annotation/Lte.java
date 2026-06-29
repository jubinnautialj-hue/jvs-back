package cn.bctools.mongodb.core.annotation;

import java.lang.annotation.*;

/**
 * @author xh
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConditionsAnnotation
public @interface Lte {
    // 字段的名称
    String value() default "";
}
