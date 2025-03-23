
package cn.bctools.document.message.aspect;


import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;

import java.lang.annotation.*;

/**
 * 消息推送的注解
 *
 * @author xiaohui
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessagePush {

    /**
     * 消息推送类型
     */
    DcLibraryLogOperationTypeEnum messagePushTye() default DcLibraryLogOperationTypeEnum.ADD;


    /**
     * 是否从返回值取参数 这种情况用于移到回收站
     */
    boolean returnValueIs() default false;


}
