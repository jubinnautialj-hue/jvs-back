package cn.bctools.document.log;

import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;

import java.lang.annotation.*;

/**
 * 环绕日志注解 自定义的
 * @author admin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocumentLog {
    /**
     * 记录操作类型
     */
    DcLibraryLogOperationTypeEnum operationType() default DcLibraryLogOperationTypeEnum.UPDATE;
}
