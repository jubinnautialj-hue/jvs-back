package cn.bctools.rule.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 是否需要自己校验，不使用通用校验结构
 * 逻辑引擎节点入参时,可以通过对象校验参数,或自定义业务校验参数处理
 *
 * @author: guojing
 * @return:
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
public @interface Inspect {

    /**
     * 加了此注解，默认就是
     */
    boolean value() default true;

}
