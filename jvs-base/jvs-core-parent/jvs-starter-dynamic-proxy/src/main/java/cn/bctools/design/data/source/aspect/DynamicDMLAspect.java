package cn.bctools.design.data.source.aspect;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.service.DynamicDDLService;
import cn.bctools.design.data.source.impl.sql.DynamicMethodConvention;
import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import cn.bctools.design.data.source.impl.sql.DataUtil;
import cn.bctools.design.data.source.impl.sql.TableCacheUtil;
import cn.bctools.design.data.source.impl.sql.TableColumnContextHolder;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
@Slf4j
@Aspect
@Data
@Component
@Order(-2)
public class DynamicDMLAspect extends DynamicAspect {

    @Resource
    Map<String, DataSource> dataSourceMap;
    @Resource
    private DynamicDDLService service;

    @SneakyThrows
    @Around("@within(cn.bctools.design.data.source.aspect.DynamicDML)")
    public Object around(ProceedingJoinPoint point) {
        if (ObjectNull.isNull(dataSourceMap.keySet())) {
            throw new BusinessException("未配置动态数据源");
        }
        Object[] args = point.getArgs();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Object result;
        try {
            String ds = Optional.ofNullable(DynamicDataSourceContextHolder.peek()).orElse("default");
            //如果没有，则直接使用默认的数据源
            String tableName = getTableName(args, methodSignature);
            // 缓存表字段
            List<TableColumnCacheDto> tableColumnCacheList = TableCacheUtil.cacheTableColumn(ds, tableName);
            TableColumnContextHolder.setTableColumn(tableColumnCacheList);
            // 加工入参
            processReq(args, methodSignature, tableName, tableColumnCacheList);

            // 切换数据源
            DynamicDataSourceContextHolder.push(ds);
            // 执行
            result = point.proceed();
        } catch (Throwable throwable) {
            log.error("AOP拦截到错误", throwable);
            throw new BusinessException(throwable.getMessage());
        } finally {
            // 清除当前线程数据源
            DynamicDataSourceContextHolder.clear();
            TableColumnContextHolder.clear();
        }
        return result;
    }


    /**
     * 加工入参
     *
     * @param args
     * @param methodSignature
     * @param tableName
     */
    private void processReq(Object[] args, MethodSignature methodSignature, String tableName, List<TableColumnCacheDto> tableColumnCacheList) {
        String[] parameters = methodSignature.getParameterNames();
        // 新增、修改数据时，若有新字段，则往当前表结构中增加字段
        if (DynamicMethodConvention.EDIT_METHOD_PREFIX.stream().anyMatch(prefix -> methodSignature.getName().startsWith(prefix))) {
            List<DataColumnDto> columnList = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
                if (DynamicMethodConvention.PARAM_DATA.equals(parameters[i])) {
                    columnList = DataUtil.getDataColumns(args[i]);
                }
            }
            // 添加字段
            service.addColumn(columnList, tableName);
        }

        // 查询数据，得到表所有字段
        if (DynamicMethodConvention.FIND_METHOD_PREFIX.stream().anyMatch(prefix -> methodSignature.getName().startsWith(prefix))) {
            for (int i = 0; i < parameters.length; i++) {
                if (DynamicMethodConvention.PARAM_QUERY.equals(parameters[i])) {
                    DynamicQuery query = (DynamicQuery) args[i];
                    List<String> columns = tableColumnCacheList.stream().map(TableColumnCacheDto::getColumnName).collect(Collectors.toList());
                    query.columns(columns);
                    args[i] = query;
                }
            }
        }
    }
}
