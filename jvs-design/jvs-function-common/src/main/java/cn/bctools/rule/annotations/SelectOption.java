package cn.bctools.rule.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明此类为自定义对象选择类
 * 用于逻辑引擎设计时需要存储的值
 * 如mysql 地址,外部api地址. 需要长期存储的数据值,此值可在逻辑的任何节点获取
 * 此注解与{@linkplain SelectOptionField} 配合使用
 *
 * @author Administrator
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
public @interface SelectOption {
    /**
     * 如果为selected 类型，值为英文 ，但不能重复
     * 如果为动态扩展，需要为扩展分类名称,如天眼查
     *
     * @return
     */
    String value();
}
