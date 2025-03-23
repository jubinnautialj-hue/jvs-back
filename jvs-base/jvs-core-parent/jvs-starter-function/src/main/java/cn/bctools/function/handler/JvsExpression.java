package cn.bctools.function.handler;

import cn.bctools.function.controller.ExpressionController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jvs表达式元素, 包含变量与函数
 * 公式场景
 * 用于在不同业务模块中扩展公式服务
 * 根据动态groovy脚本实现了代码可编辑, 业务灵活扩展和业务场景数据进行对接
 * 目前工作流,表单,列表页,数据模型,规则,BI,都已接入此模块功能,
 * 通过实现{@linkplain IJvsParam}接口不同的场景扩展业务  并通过{@linkplain ExpressionHandler#calculate(String, java.util.Map, String)}执行动态脚本
 * <p>
 * <p>
 * 通过界面操作编写动态脚本进行执行
 *
 * @Author: GuoZi
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JvsExpression {

    /**
     * 分组名称
     * <p>
     * 所有的表达式元素都会按照该名称进行分组.
     * 该值不能为空, 且该值重名时会放在同一分组下, 不会被覆盖.
     * <p>
     * 详情见{@link ExpressionController#getAllFunctions(String, String, String, String)}
     */
    String groupName();

    /**
     * 表达式元素名称的前缀
     * <p>
     * 除了基础函数, 该值不能为空.
     * 所以为避免与基础函数重名, 请尽量设置一个有个性的字符串.
     * 详情见{@link cn.bctools.function.handler.impl.BaseFunctionImpl}
     */
    String prefix() default "";

    /**
     * 该元素的适用场景(如:表单,逻辑引擎等)
     * <p>
     * 该值为空时, 表示适用所有场景.
     */
    String[] useCase() default "";

}
