package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.service.DynamicDDLService;
import cn.bctools.design.data.source.aspect.DynamicDDL;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: SQL 动态数据DDL操作通用实现
 */
@Slf4j
@DynamicDDL
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public abstract class BaseDynamicDDLImpl extends BaseDynamic implements DynamicDDLService {

    /**
     * 默认表前缀
     * - 默认增加表前缀"jvs_"（默认生成的模型id(纯数字)作为表名。但有些数据库(如：doris)不支持纯数字作为表名，所以加前缀）
     */
    public static final String DEFAULT_TABLE_PREFIX = "jvs_";

    @Override
    public String tableNamePrefix() {
        return DEFAULT_TABLE_PREFIX;
    }

    /**
     * 获取表字段
     *
     * @param addColumns 待添加的字段
     * @param collectionName 表名
     * @return TRUE-修改表结构成功，FALSE-修改表结构失败
     */
    public abstract Boolean alterAddTableColumn(List<DataColumnDto> addColumns, String collectionName);


    @Override
    public void addColumn(List<DataColumnDto> addColumnList, String collectionName) {
        if (ObjectNull.isNull(addColumnList)) {
            return;
        }
        // 获取表结构
        List<TableColumnCacheDto> tableColumnCacheList = TableCacheUtil.cacheTableColumn(DynamicDataSourceContextHolder.peek(), collectionName);
        // 得到不在表结构中的字段
        List<DataColumnDto> addColumns = addColumnList.stream()
                .filter(column -> Boolean.FALSE.equals(tableColumnCacheList.stream().anyMatch(tableColumn -> tableColumn.getColumnName().equals(column.getColumnName()))))
                .collect(Collectors.toList());
        if (ObjectNull.isNull(addColumns)) {
            return;
        }
        // 修改表结构：增加字段
        if (alterAddTableColumn(addColumns, collectionName)) {
            TableCacheUtil.updateTableColumnCache(DynamicDataSourceContextHolder.peek(), collectionName);
        }
    }
}
