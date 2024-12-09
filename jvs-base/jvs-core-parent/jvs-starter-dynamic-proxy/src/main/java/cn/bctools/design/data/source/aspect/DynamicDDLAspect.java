package cn.bctools.design.data.source.aspect;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
@Slf4j
@Aspect
@Data
@Component
@Order(-2)
public class DynamicDDLAspect extends DynamicAspect{
    @Value("${dynamic.data-source}")
    private String ds;

    @SneakyThrows
    @Around("@within(cn.bctools.design.data.source.aspect.DynamicDDL)")
    public Object around(ProceedingJoinPoint point) {
        if (ObjectNull.isNull(ds)) {
            throw new BusinessException("未配置动态数据源");
        }
        log.debug("动态数据源为：{}", ds);
        Object result;
        try {
            DynamicDataSourceContextHolder.push(ds);
            result = point.proceed();
        } catch (Throwable throwable) {
            log.error("AOP拦截到错误", throwable);
            throw new BusinessException(throwable.getMessage());
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
        return result;
    }
}
