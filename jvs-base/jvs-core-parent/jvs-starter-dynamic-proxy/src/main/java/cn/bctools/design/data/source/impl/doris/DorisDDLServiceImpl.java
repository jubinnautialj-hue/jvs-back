package cn.bctools.design.data.source.impl.doris;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.BaseDynamicDDLImpl;
import cn.bctools.design.data.source.impl.sql.SqlScriptUtil;
import cn.bctools.design.data.source.impl.sql.TableCacheUtil;
import cn.bctools.design.data.source.aspect.DynamicDDL;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;
import cn.bctools.design.data.source.util.DataModelUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: Doris数据库DDL
 */
@Slf4j
@ConditionalOnProperty(name = "dynamic.data-source", havingValue = "doris")
@Service
@DynamicDDL
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class DorisDDLServiceImpl extends BaseDynamicDDLImpl {

    @Override
    public void createTable(String collectionName) {
        // 创建数据表
        String createDataTable = String.format(DorisSqlConstant.CREATE_TABLE_SQL, collectionName);
        dataSourceExecute(createDataTable);
        log.info("创建数据表：{}", collectionName);
        // 创建日志表
        String createLogTable = String.format(DorisSqlConstant.CREATE_LOG_TABLE_SQL, DataModelUtil.buildLogCollectionName(collectionName));
        dataSourceExecute(createLogTable);
        log.info("创建日志表：{}", collectionName);
        // 创建删除数据备份表
        String createDelTable = String.format(DorisSqlConstant.CREATE_TABLE_SQL, DataModelUtil.buildRemoveCollectionName(collectionName));
        dataSourceExecute(createDelTable);
        log.info("创建删除数据备份表：{}", collectionName);
    }

    @Override
    public void renameTable(String oldCollectionName, String collectionName) {
        String sql = String.format(DorisSqlConstant.RENAME_TABLE,oldCollectionName, collectionName);
        dataSourceExecute(sql);
        log.info("表{}重命名为{}", oldCollectionName, collectionName);
        // 更新表字段缓存
        String db = DynamicDataSourceContextHolder.peek();
        TableCacheUtil.clearCacheTableColumn(db, oldCollectionName);
        TableCacheUtil.cacheTableColumn(db, collectionName);
    }

    @Override
    public void createIndex(String collectionName, String column) {
        String sql = String.format(DorisSqlConstant.ADD_INVERTED_INDEX, collectionName, column, column);
        dataSourceExecute(sql);
        log.info("表{}列{}增加索引", collectionName, column);
    }

    @Override
    public void dropIndex(String collectionName, String column) {
        String sql = String.format(DorisSqlConstant.DROP_INDEX, collectionName, column);
        dataSourceExecute(sql);
        log.info("表{}列{}删除索引", collectionName, column);
    }

    @Override
    public List<TableColumnCacheDto> getTableColumn(String collectionName) {
        String sql = String.format(DorisSqlConstant.TABLE_COLUMN, collectionName);
        if (ObjectNull.isNull(sql)) {
            return null;
        }
        return dataSourceExecuteResult(sql, resultSet -> {
            List<TableColumnCacheDto> tableColumnCacheList = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    TableColumnCacheDto columnCache = new TableColumnCacheDto()
                            .setColumnName(resultSet.getString("Field"))
                            .setJdbcType(resultSet.getString("Type"));
                    tableColumnCacheList.add(columnCache);
                }
            } catch (Exception e) {
                log.error("获取表字段异常：", e);
                throw new BusinessException(e.getMessage());
            }
            return tableColumnCacheList;
        });
    }

    @Override
    public Boolean alterAddTableColumn(List<DataColumnDto> addColumns, String collectionName) {
        List<String> sqls = addColumns.stream()
                .map(column -> String.format(DorisSqlConstant.ADD_COLUMN, collectionName, SqlScriptUtil.backtick(column.getColumnName()), DorisDataTypeEnum.getType(column.getJavaType())))
                .collect(Collectors.toList());
        sqls.forEach(this::dataSourceExecute);
        return Boolean.TRUE;
    }
}
