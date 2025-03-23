package cn.bctools.rule.annotations;

import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明 是否是一个执行规则，规则是否支持测试是否支持操作
 * 规则注解
 * 写在方法上面时,不支持{xxx} 变量
 * 逻辑的每一个节点规范,确定名称,地址,和是否启用功能
 * 此注解使用方式为两种，
 * 一种为在jvs-design 项目中实现{@linkplain  cn.bctools.rule.function.BaseCustomFunctionInterface}  接口做为实现类进行声明
 * 二在其它项目中，在同一个微服务中并在对应的Controller的方法上添加此注解
 * ~~~
 * <dependency>
 * <groupId>cn.bctools</groupId>
 * <artifactId>jvs-function-annotation</artifactId>
 * </dependency>
 * ~~~
 * <p>
 * 如果是依赖包引入 服务注册controller时， 需要和  ApiOperation注解一起使用  value值必须一致
 * 例如：
 * 实现类添加了事务注解会导致生成代理类，所以需要将事务功能分开，不能在 execute(T t, java.util.Map)  方法实现中添加事务
 *
 * @author Administrator
 * @ApiOperation(value = "测试demo")
 * @Rule(value = "测试demo", group = RuleGroup.常用插件)
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
@Order
@Component
public @interface Rule {

    /**
     * 方法的名称
     */
    @AliasFor(
            annotation = Component.class
    )
    String value();

    /**
     * 展示标签
     */
    String label() default "";

    /**
     * 方法返回的类型
     */
    ClassType returnType() default ClassType.对象;

    /**
     * 是否支持自定义结构class解析，通过返回的节点内容进行结构解析定义
     * 每一个节点，都可以自定义结构对象返回。
     * 是否支持自定义返回结构
     * 2.1.7 默认支持整体测试结构定义,如果有测试结果后.所有节点,完成动态化结构定义
     */
    boolean customStructure() default true;

    /**
     * 是否可用
     */
    boolean enable() default true;

    /**
     * 不可用状态的描述信息
     *
     * @return
     */
    String statsMsg() default "";

    /**
     * 是否支持方法测试
     */
    boolean test() default false;

    /**
     * 规则分组
     */
    RuleGroup group();

    /**
     * 规则的详细解释
     */
    String explain() default "http://www.bctools.cn";

    /**
     * 在线帮助的地址
     */
    String help() default "";

    /**
     * 方法的图片
     */
    String iconUrl() default "icon-jilianxuanze1";

    /**
     * 测试的结果预览格式
     */
    TestShowEnum testShowEnum() default TestShowEnum.JSON;

    /**
     * 展示排序
     *
     * @return
     */
    int order() default 2147483647;

    /**
     * 演示环境禁用此节点
     */
    boolean demoDisabled() default false;

}
