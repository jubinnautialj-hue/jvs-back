package cn.bctools.design.data.source.service;

import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;

import java.util.Collections;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 动态数据DDL操作统一接口
 */
public interface DynamicDDLService {

    /**
     * 表名前缀
     *
     * @return
     */
    String tableNamePrefix();

    /**
     * 创建表
     *
     * @param collectionName
     */
    void createTable(String collectionName);

    /**
     * 修改数据集名称
     *
     * @param oldCollectionName 旧名称
     * @param collectionName    新名称
     */
    void renameTable(String oldCollectionName, String collectionName);

    /**
     * 创建索引
     *
     * @param collectionName 表名
     * @param column 索引字段
     */
    void createIndex(String collectionName, String column);

    /**
     * 删除索引
     *
     * @param collectionName 表名
     * @param column 索引字段
     */
    void dropIndex(String collectionName, String column);

    /**
     * 获取表字段
     *
     * @param collectionName 表名
     * @return
     */
    default List<TableColumnCacheDto> getTableColumn(String collectionName) {
        return Collections.emptyList();
    }

    /**
     * 新增列
     *
     * @param addColumnList 字段集合
     * @param collectionName 表名
     */
    default void addColumn(List<DataColumnDto> addColumnList, String collectionName) {

    }
}
