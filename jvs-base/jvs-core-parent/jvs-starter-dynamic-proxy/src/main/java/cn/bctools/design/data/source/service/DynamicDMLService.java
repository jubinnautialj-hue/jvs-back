package cn.bctools.design.data.source.service;//package cn.bctools.design.data.source;


import cn.bctools.design.data.source.impl.sql.DynamicMethodConvention;
import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import cn.bctools.design.data.source.impl.sql.DynamicUpdate;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 动态数据DML操作统一接口
 *
 * <p> 本接口的方法及参数命名需遵守约定：{@link DynamicMethodConvention}
 */
public interface DynamicDMLService {

    /**
     * 新增 —— 保存单条数据
     *
     * @param data
     * @param collectionName
     * @return 保存的数据
     */
    <T> T save(T data, String collectionName);

    /**
     * 新增 —— 插入单条数据
     *
     * @param data
     * @param collectionName
     * @return 保存的数据
     */
    <T> T insert(T data, String collectionName);

    /**
     * 新增 —— 批量
     *
     * @param data
     * @param collectionName
     * @return 保存的数据
     */
    <T> Collection<T> insertBatch(Collection<? extends T> data, String collectionName);

    /**
     * 删除
     *
     * @param dynamicQuery
     * @param collectionName
     * @return 影响的行数
     */
    Long remove(DynamicQuery dynamicQuery, String collectionName);

    /**
     * 删除 —— 查询并删除一条数据
     *
     * @param dynamicQuery
     * @param entityClass
     * @param collectionName
     * @return 被删除的数据
     */
    <T> T findAndRemove(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName);

    /**
     * 删除 —— 查询数据集合并批量删除
     *
     * @param dynamicQuery
     * @param collectionName
     * @return 被删除的数据
     */
    <T> List<T> findAllAndRemove(DynamicQuery dynamicQuery, String collectionName);

    /**
     * 修改 —— 修改符合条件的所有数据
     *
     * @param dynamicQuery
     * @param data
     * @param collectionName
     * @return 影响的行数
     */
    Long updateMulti(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName);

    /**
     * 修改 —— 修改符合条件的第一条数据
     *
     * @param dynamicQuery
     * @param data
     * @param collectionName
     * @return 影响的行数
     */
    Long updateFirst(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName);

    /**
     * 查询 —— 单条数据
     *
     * @param dynamicQuery
     * @param entityClass
     * @param collectionName
     * @return 一条数据
     */
    <T> T findOne(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName);

    /**
     * 查询 —— 数据集合
     *
     * @param dynamicQuery
     * @param entityClass
     * @param collectionName
     * @return 数据集合
     */
    <T> List<T> findList(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName);

    /**
     * 统计数据量
     *
     * @param dynamicQuery
     * @param collectionName
     * @return 数据量
     */
    Long count(DynamicQuery dynamicQuery, String collectionName);

    /**
     * 统计数据量
     *
     * @param dynamicQuery
     * @param entityClass
     * @param collectionName
     * @return 数据量
     */
    Long count(DynamicQuery dynamicQuery, @Nullable Class<?> entityClass, String collectionName);

    /**
     * 统计表数据量 —— 估算
     *
     * @param collectionName
     * @return 估算数据量
     */
    Long estimatedCount(String collectionName);

    /**
     * 表数据占用空间大小
     *
     * @param collectionName
     * @return 数据存储大小（字节）
     */
    Long tableDataSize(String collectionName);
}
