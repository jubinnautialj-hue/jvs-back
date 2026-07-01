package cn.bctools.design.data.fields;

import cn.bctools.design.data.fields.enums.DataFieldType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 字段类型
 * 用于声明低代码表单引擎的组件解析器,注解,并实现接口{@linkplain IDataFieldHandler}, 表示此注解由此类进行处理
 * 接口中包含解析,获取,关联触发,值覆盖,联动处理,嵌套处理,查询条件约束,类型转换等
 *
 * @Author: GuoZi
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
@Component
public @interface DesignField {
    /**
     * 标记设计类型
     */
    @AliasFor(
            annotation = Component.class
    )
    String value();

    /**
     * 前端控件类型
     */
    DataFieldType type();


}
