package cn.bctools.design.data.source.impl.sql.methods.impl;

import cn.bctools.design.data.source.impl.sql.Constant;
import cn.bctools.design.data.source.impl.sql.SqlScriptUtil;
import cn.bctools.design.data.source.impl.sql.SqlStatement;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.bctools.design.data.source.impl.sql.methods.WhereParam;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 修改数据
 */
public class Update extends AbstractSqlMethod {

    @Override
    public SqlStatement injectSqlStatement(DynamicSqlInfo info) {
        DynamicSqlMethod sqlMethod = DynamicSqlMethod.UPDATE;
        List<DataColumnDto> fields = dataFields(info);
        WhereParam whereParam = whereSegmentSql(info);
        String sql = String.format(sqlMethod.getSql(), tableName(info), setSegment(fields, sqlFunction.updateSetPlaceholder()), whereParam.getSqlSegment());
        return new SqlStatement().setSql(sql).setData(data(info)).setWhereParam(whereParam.getParam());
    }

    /**
     * SET占位sql片段
     *
     * @param fields
     * @return
     */
    private String setSegment(List<DataColumnDto> fields, Function<DataColumnDto, DataColumnDto> function) {
        return fields.stream()
                // 移除主键（部分数据库不支持修改主键(如doris数据库)，所以将主键排除掉）
                .filter(field -> Boolean.FALSE.equals(Constant.PRIMARY_KEY.equals(field.getColumnName())))
                // 构造sql片段： #{data.字段}
                .map(field -> field.setSqlSegment(SqlScriptUtils.safeParam(Constant.DATA + StringPool.DOT + field.getColumnName())))
                // 特殊处理
                .map(function)
                // 构造sql片段：字段 = 字段sql片段
                .map(field -> SqlScriptUtil.backtick(field.getColumnName()) + StringPool.EQUALS + field.getSqlSegment())
                .collect(Collectors.joining(SqlScriptUtil.comma()));
    }

}
