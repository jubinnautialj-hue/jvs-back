package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.mapper.DynamicDataMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @Author: ZhuXiaoKang
 * @Description: SQL动态数据顶级类
 */
@Slf4j
public abstract class BaseDynamic {
    @Resource
    public DynamicDataMapper dynamicDataMapper;
    @Resource
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    /**
     * 执行sql
     *
     * <p> 无返回
     * <p> 当遇到mybatis不支持的sql时，使用DataSource执行sql
     * @param sql
     */
    protected void dataSourceExecute(String sql) {
        if (ObjectNull.isNull(sql)) {
            return;
        }
        log.info("执行SQL：{}", sql);
        DataSource dataSource = getCurrentDataSource();
        try (Connection connection =  dataSource.getConnection()) {
            connection.createStatement().execute(sql);
        } catch (Exception e) {
            log.error("执行{}失败：{}", sql, e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 执行sql并返回结果集
     *
     * <p> 有返回
     * <p> 当遇到mybatis不支持的sql时，使用DataSource执行sql
     * @param sql
     */
    protected <T> T dataSourceExecuteResult(String sql, Function<ResultSet, T> function) {
        if (ObjectNull.isNull(sql)) {
            return null;
        }
        DataSource dataSource = getCurrentDataSource();
        try (Connection connection =  dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            return function.apply(resultSet);
        } catch (Exception e) {
            log.error("执行{}失败：{}", sql, e);
            throw new BusinessException(e.getMessage());
        }
    }


    /**
     * 得到当前数据源
     *
     * @return
     */
    protected DataSource getCurrentDataSource() {
        return dynamicRoutingDataSource.determineDataSource();
    }


    /**
     * 加工返回数据
     *
     * @param resultData
     * @param entityClass
     * @return
     */
    protected <T> T processResult(JSONObject resultData, Class<T> entityClass) {
        if (ObjectNull.isNull(resultData)) {
            return null;
        }
        DynamicDataTypeHandler.dataTypeHandler(resultData);
        return JSON.toJavaObject(resultData, entityClass);
    }

    /**
     * 加工返回数据
     *
     * @param resultDatas
     * @param entityClass
     * @return
     */
    protected <T> List<T> processResult(List<JSONObject> resultDatas, Class<T> entityClass) {
        if (ObjectNull.isNull(resultDatas)) {
            return Collections.emptyList();
        }
        resultDatas.forEach(DynamicDataTypeHandler::dataTypeHandler);
        return JSON.parseArray(JSON.toJSONString(resultDatas), entityClass);
    }

}
