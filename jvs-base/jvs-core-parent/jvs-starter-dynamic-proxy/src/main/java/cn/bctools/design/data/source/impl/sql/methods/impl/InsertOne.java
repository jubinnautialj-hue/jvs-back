package cn.bctools.design.data.source.impl.sql.methods.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.Constant;
import cn.bctools.design.data.source.impl.sql.SqlScriptUtil;
import cn.bctools.design.data.source.impl.sql.SqlStatement;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 新增单条数据
 */
public class InsertOne extends AbstractSqlMethod {

    @Override
    public SqlStatement injectSqlStatement(DynamicSqlInfo info) {
        DynamicSqlMethod sqlMethod = DynamicSqlMethod.INSERT_ONE;
        List<DataColumnDto> fields = dataFields(info);
        String sql = String.format(sqlMethod.getSql(), tableName(info), columnSegment(fields), valueSegment(fields));
        // 新增的数据
        Map<String, Object> data = data(info);
        // 若主键为空，则生成主键id
        if (ObjectNull.isNotNull(data) && ObjectNull.isNull(data.get(Constant.PRIMARY_KEY))) {
            data.put(Constant.PRIMARY_KEY, IdUtil.getSnowflake().nextIdStr());
        }
        return new SqlStatement().setSql(sql).setData(data);
    }

    /**
     * 待插入数据字段sql片段
     *
     * @param fields
     * @return (`字段1`, `字段2`,... , '字段n')
     */
    private String columnSegment(List<DataColumnDto> fields) {
        String fieldStr = fields.stream()
                // `字段`
                .map(field -> SqlScriptUtil.backtick(field.getColumnName()))
                .collect(Collectors.joining(SqlScriptUtil.comma()));
        return SqlScriptUtil.bracket(fieldStr);
    }

    /**
     * VALUES占位sql片段
     *
     * @param fields
     * @return (#{data.字段1}, #{data.字段2}, ..., #{data.字段n})
     */
    private String valueSegment(List<DataColumnDto> fields) {
        String segment = fields.stream()
                // #{data.字段}
                .map(field -> SqlScriptUtils.safeParam(Constant.DATA + StringPool.DOT + field.getColumnName()))
                .collect(Collectors.joining(SqlScriptUtil.comma()));
        return SqlScriptUtil.bracket(segment);
    }
}
