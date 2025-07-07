package cn.bctools.rule.annotations;

import cn.bctools.rule.common.DefaultValueParameter;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.cons.CEEnum;
import cn.bctools.rule.cons.SHEnum;
import cn.bctools.rule.entity.enums.InputType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 属性的注解标记
 * 用于在声明了{@linkplain Rule} 注解节点的入参参数对象的属性标记.
 * 逻辑执行的每一个节点所属字段都需要进行声明,是否需要关联,是否需要扩展等
 *
 * @author: guojing
 * @return:
 */
@Target({FIELD})
@Retention(RUNTIME)
@Documented
public @interface ParameterValue {

    /**
     * 解释
     */
    String info();

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 默认值处理器，与默认值二选一
     */
    Class<? extends DefaultValueParameter> defaultValueHandle() default DefaultValueParameter.class;

    /**
     * 额外的参数提示说明
     */
    String explain() default "";

    /**
     * 类型
     */
    InputType type() default InputType.input;

    /**
     * 子类型,主要用于存在list map  listMap 三个类型的扩展
     *
     * @return
     */
    InputType subtype() default InputType.text;

    /**
     * 获取这个类型的值可选项，进行自定义操作, 这个属性，只针对 Type为  selected 的时候使用
     *
     * @author: guojing
     * @return: {@linkplain java.lang.Class<? extends  ParameterSelected > }
     */
    Class<? extends ParameterSelected> cls() default ParameterSelected.class;

    Class<? extends LinkFieldSelected> linkCls() default LinkFieldSelected.class;

    /**
     * 组件扩展属性
     *
     * @return
     */
    CEEnum[] extension() default {};

    SHEnum[] show() default {};

    /**
     * 是否必填写
     */
    boolean necessity() default true;

    /**
     * 字段关联 通过能用的url进行参数获取值，必须是select类型才能支持这个选择
     */
    String[] linkFields() default "";

}
