package com.xxl.job.core.config.annotation;

import cn.bctools.xxl.job.api.XxlAdminApi;
import com.xxl.job.core.config.XxlJobConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * @author My_gj
 * 开启执行器
 * 对接了xxl-job 开启此注解,自动注册为执行器,名称为微服务名称作为执行器名称
 * 并可以通过feign {@linkplain XxlAdminApi}创建,修改,删除执行器任务
 * xxl-job-admin 执行管理帐号密码默认为admin  123456
 * 在spring boot 项目启动类中添加此类后
 * 执行器添加{@linkplain XxlJob} 注解编写执行规则
 * 获取执行参数方式 {@linkplain XxlJobHelper#getJobParam()}
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({XxlJobConfig.class})
public @interface EnableXxlJobExecutor {
}
