package cn.bctools.rule.annotations;

import cn.bctools.rule.entity.enums.InputType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义选择属性的标识
 * 使用此注解的属性必须是public进行修饰
 * 此注解与{@linkplain SelectOption} 配合使用
 * @author Administrator
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface SelectOptionField {

    String value();

    /**
     * 目录 只会有，数字 ，字符，密码
     */
    InputType type() default InputType.input;

}
