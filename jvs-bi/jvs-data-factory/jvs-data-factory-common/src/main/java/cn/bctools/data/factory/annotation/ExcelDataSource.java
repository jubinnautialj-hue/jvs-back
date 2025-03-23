package cn.bctools.data.factory.annotation;

import cn.bctools.data.factory.enums.ExcelInterceptType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Documented
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDataSource {

    ExcelInterceptType type() default ExcelInterceptType.新增;

}
