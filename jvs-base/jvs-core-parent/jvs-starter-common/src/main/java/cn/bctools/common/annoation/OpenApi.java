package cn.bctools.common.annoation;

import java.lang.annotation.*;

/**
 * @author jvs
 * 标记开放API 此注解只写入 controller中，表示此 controller下所有的 api都为开放 api, 添加此注解的服务，不能获取当前用户，会导致无法获取到用户信息而报错。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenApi {

}
