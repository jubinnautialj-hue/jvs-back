package cn.bctools.design.data.source.impl.sql;

import cn.bctools.design.data.source.service.DynamicDMLService;
import cn.bctools.design.data.source.aspect.DynamicDML;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: SQL 动态数据DML操作通用实现
 */
@Slf4j
@DynamicDML
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public abstract class BaseDynamicDMLImpl extends BaseDynamic implements DynamicDMLService {

    @Override
    public <T> T save(T data, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.INSERT_ONE, data);
        dynamicDataMapper.insert(statement.getSql(), statement.getData());
        return data;
    }

    @Override
    public <T> T insert(T data, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.INSERT_ONE, data);
        dynamicDataMapper.insert(statement.getSql(), statement.getData());
        return data;
    }

    @Override
    public <T> Collection<T> insertBatch(Collection<? extends T> data, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.INSERT_ONE, data);
        for (T element : data) {
            dynamicDataMapper.insert(statement.getSql(), element);
        }
        return (Collection)data;
    }

    @Override
    public Long remove(DynamicQuery dynamicQuery, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.DELETE, dynamicQuery);
        return dynamicDataMapper.delete(statement.getSql(), statement.getWhereParam()).longValue();
    }

    @Override
    public <T> T findAndRemove(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        // 先查询要删除的数据
        T data = findOne(dynamicQuery, entityClass, collectionName);
        // 删除数据
        remove(dynamicQuery, collectionName);
        return data;
    }

    @Override
    public <T> List<T> findAllAndRemove(DynamicQuery dynamicQuery, String collectionName) {
        // 先查询要删除的数据
        List<T> list = (List<T>) findList(dynamicQuery, Map.class, collectionName);
        // 删除数据
        remove(dynamicQuery, collectionName);
        return list;
    }

    @Override
    public Long updateMulti(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.UPDATE, dynamicQuery, data.getUpdateData());
        return  dynamicDataMapper.update(statement.getSql(), statement.getData(), statement.getWhereParam());
    }

    @Override
    public Long updateFirst(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.UPDATE, dynamicQuery, data.getUpdateData());
        return dynamicDataMapper.update(statement.getSql(), statement.getData(), statement.getWhereParam());
    }

    @Override
    public <T> T findOne(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.SELECT_LIST, dynamicQuery);
        JSONObject resultData = dynamicDataMapper.getOne(statement.getSql(), statement.getWhereParam());
        return processResult(resultData, entityClass);
    }

    @Override
    public <T> List<T> findList(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.SELECT_LIST, dynamicQuery);
        List<JSONObject> resultDatas = dynamicDataMapper.list(statement.getSql(), statement.getWhereParam());
        return processResult(resultDatas, entityClass);
    }

    @Override
    public Long count(DynamicQuery dynamicQuery, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.SELECT_COUNT, dynamicQuery);
        return dynamicDataMapper.count(statement.getSql(), statement.getWhereParam());
    }

    @Override
    public Long count(DynamicQuery dynamicQuery, Class<?> entityClass, String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.SELECT_COUNT, dynamicQuery);
        return dynamicDataMapper.count(statement.getSql(), statement.getWhereParam());
    }

    @Override
    public Long estimatedCount(String collectionName) {
        SqlStatement statement = SqlStatementUtil.getStatement(collectionName, DynamicSqlMethod.SELECT_COUNT, null);
        return dynamicDataMapper.count(statement.getSql(), statement.getWhereParam());
    }
}
