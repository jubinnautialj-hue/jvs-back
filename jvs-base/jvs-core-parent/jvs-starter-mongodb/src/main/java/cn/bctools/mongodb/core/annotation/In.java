package cn.bctools.mongodb.core.annotation;

import java.lang.annotation.*;

/**
 * @author czy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConditionsAnnotation
public @interface In {
    // 字段的名称
    String value() default "";
}
