package cn.bctools.design.data.aspect;

import java.lang.annotation.*;

/**
 * @author zhuxiaokang
 * 模型数据集名称注解
 * 使用了该注解的方法，将模型id转换为数据集名称
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModelCollectionName {
    /**
     * 模型id
     */
    String modelId();
}
