package cn.bctools.index.annotation;

import cn.bctools.index.design.enums.FormAttributeTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jvs 用于描述FormQueryParamsBase 基类下的属性
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormFormQuery {
    /**
     * 值
     *
     * @return string
     */
    String prop();

    /**
     * 描述
     *
     * @return the string
     */
    String label();

    /**
     * 详细解释
     */
    String desc();


    /**
     * 类型
     *
     * @return the form attribute type enum
     */
    FormAttributeTypeEnum type() default FormAttributeTypeEnum.input;

    /**
     * 关联的 Key,a属性控制 B属性时，在 b属性中写 a 属性的field key
     *
     * @return the string [ ]
     */
    String[] link() default {};
}
