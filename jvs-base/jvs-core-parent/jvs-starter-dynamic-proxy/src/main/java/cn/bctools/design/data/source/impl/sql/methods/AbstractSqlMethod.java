package cn.bctools.design.data.source.impl.sql.methods;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.data.source.impl.sql.DynamicCriteria;
import cn.bctools.design.data.source.impl.sql.*;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.dto.TableColumnCacheDto;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Abstract sql method.
 *
 * @Author: ZhuXiaoKang
 * @Description: sql方法抽象类
 */
public abstract class AbstractSqlMethod {
    /**
     * The constant METHOD.
     */
    public static final String METHOD = "injectSqlStatement";
    /**
     * The constant sqlFunction.
     */
    protected static final SqlFunction sqlFunction = SpringContextUtil.getBean(SqlFunction.class);

    /**
     *
     * @param info
     * @return
     */
    public abstract SqlStatement injectSqlStatement(DynamicSqlInfo info);

    /**
     * SQL 得到表名
     *
     * @param info the info
     * @return string string
     */
    protected String tableName(DynamicSqlInfo info) {
        if (ObjectNull.isNull(info.getTableName())) {
            throw new BusinessException("未指定TableName");
        }
        return info.getTableName();
    }

    /**
     * 得到数据字段
     *
     * @param info the info
     * @return list list
     */
    protected List<DataColumnDto> dataFields(DynamicSqlInfo info) {
        List<DataColumnDto> fields = DataUtil.getDataColumns(info.getData());
        if (CollectionUtils.isEmpty(fields)) {
            throw new BusinessException("无数据字段");
        }
        return fields;
    }


    /**
     * 得到数据（新增、修改）
     *
     * @param info the info
     * @return map map
     */
    protected Map<String, Object> data(DynamicSqlInfo info) {
        if (ObjectNull.isNull(info.getData())) {
            return Collections.emptyMap();
        }
        return DataUtil.data(info.getData());
    }


    /**
     * where条件sql片段
     *
     * @param info the info
     * @return where param
     */
    protected WhereParam whereSegmentSql(DynamicSqlInfo info) {
        if (ObjectNull.isNull(info) || ObjectNull.isNull(info.getDynamicQuery())) {
            return WhereParam.empty();
        }
        // 由于表字段是新增、修改数据时，动态插入的。 可能存在查询条件中的字段在表中不存在的情况，导致执行sql异常。
        // 判断若条件字段在表中不存在，则构造非正常条件
        DynamicCriteria dynamicCriteria = info.getDynamicQuery().getCriteria();
        if (ObjectNull.isNotNull(dynamicCriteria)) {
            List<String> tableColumns = Optional.ofNullable(TableColumnContextHolder.getTableColumn()).orElseGet(ArrayList::new)
                    .stream().map(TableColumnCacheDto::getColumnName).collect(Collectors.toList());
            if (Boolean.FALSE.equals(tableColumns.containsAll(dynamicCriteria.getAllKey()))) {
                dynamicCriteria = DynamicCriteria.falseCriteria();
            }
        }
        // 构造条件
        WhereParam whereParam = SqlStatementUtil.where(dynamicCriteria);
        String whereSql = Optional.ofNullable(whereParam.getSqlSegment()).orElse(StringPool.EMPTY);
        if (StringUtils.isNotBlank(whereSql)) {
            whereSql = SqlScriptUtil.appendSqlSegment("WHERE", whereSql);
            whereParam.setSqlSegment(whereSql);
        }
        return whereParam;
    }
}
