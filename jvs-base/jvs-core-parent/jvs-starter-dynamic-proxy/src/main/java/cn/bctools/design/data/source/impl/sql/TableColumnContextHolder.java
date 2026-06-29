package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 当前线程表字段缓存
 */
public final class TableColumnContextHolder {

    private static final String TABLE_COLUMN = "dynamic_table_column";

    /**
     * 当前线程表字段
     *
     * @param columnCacheDtos
     */
    public static void setTableColumn(List<TableColumnCacheDto> columnCacheDtos) {
        SystemThreadLocal.set(TABLE_COLUMN, columnCacheDtos);
    }

    /**
     * 获取当前线程表字段
     *
     * @return
     */
    public static List<TableColumnCacheDto> getTableColumn() {
        return SystemThreadLocal.get(TABLE_COLUMN);
    }

    public static void clear() {
        SystemThreadLocal.remove(TABLE_COLUMN);
    }

    /**
     * 获取jdbcType
     *
     * @param column 字段
     * @return jdbcType
     */
    public static String getJdbcType(String column) {
        return Optional.ofNullable(getTableColumn()).orElse(Collections.emptyList())
                .stream()
                .filter(tableColumn -> tableColumn.getColumnName().equals(column))
                .map(TableColumnCacheDto::getJdbcType)
                .collect(Collectors.joining());
    }
}
