package cn.bctools.design.data.source.impl.doris;

import cn.bctools.design.data.source.impl.sql.BiSqlFunction;
import cn.bctools.design.data.source.impl.sql.SqlFunction;
import cn.bctools.design.data.source.impl.sql.SqlScriptUtil;
import cn.bctools.design.data.source.impl.sql.TableColumnContextHolder;
import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: ZhuXiaoKang
 * @Description: doris对sql做特殊处理的函数
 */
@ConditionalOnProperty(name = "dynamic.data-source", havingValue = "doris")
@Service
public class DorisSqlFunction implements SqlFunction {

    @Override
    public Function<DataColumnDto, DataColumnDto> updateSetPlaceholder() {
        return field -> {
            // doris修改JSONB格式的类型不能直接 key=value，需要特殊处理
            if (Collection.class.isAssignableFrom(field.getJavaType()) || Map.class.isAssignableFrom(field.getJavaType())) {
                field.setSqlSegment("jsonb_parse" + SqlScriptUtil.bracket(field.getSqlSegment()));
            }
            return field;
        };
    }

    /**
     * 构造doris json模糊查询
     * 例：jsonb_extract(`column`,'$.person') like "%{0}%"
     * @return
     */
    @Override
    public Function<String, String> jsonLike() {
        return field -> {
            String column = field;
            String jsonPath = StringPool.DOLLAR;
            if (field.contains(StringPool.DOT)) {
                int dotIndex = field.indexOf(StringPool.DOT);
                column = field.substring(0, dotIndex);
                jsonPath = StringPool.DOLLAR + field.substring(dotIndex);
            }
            String segmentBracket = SqlScriptUtil.bracket(SqlScriptUtil.backtick(column) + SqlScriptUtil.comma() + SqlScriptUtil.singleQuote(jsonPath));
            return SqlScriptUtil.appendSqlSegment("jsonb_extract", segmentBracket, "like concat('%',{0}, '%')");
        };
    }

    @Override
    public Function<String, String> notLike() {
        return field -> {
            String column = field;
            String jsonPath = StringPool.DOLLAR;
            if (field.contains(StringPool.DOT)) {
                int dotIndex = field.indexOf(StringPool.DOT);
                column = field.substring(0, dotIndex);
                jsonPath = StringPool.DOLLAR + field.substring(dotIndex);
            }
            String segmentBracket = SqlScriptUtil.bracket(SqlScriptUtil.backtick(column) + SqlScriptUtil.comma() + SqlScriptUtil.singleQuote(jsonPath));
            return SqlScriptUtil.appendSqlSegment("jsonb_extract", segmentBracket, "not like concat('%',{0}, '%')");
        };
    }

    @Override
    public BiSqlFunction<String, Object, QueryWrapper<Object>, QueryWrapper<Object>> eq() {
        return (field, value, queryWrapper) -> {
            String jdbcType = TableColumnContextHolder.getJdbcType(field);
            if ("JSONB".equals(jdbcType)) {
                queryWrapper.apply(jsonLike().apply(field), value);
            } else {
                queryWrapper.eq(SqlScriptUtil.backtick(field), value);
            }
          return queryWrapper;
        };
    }

    @Override
    public BiSqlFunction<String, Object, QueryWrapper<Object>, QueryWrapper<Object>> ne() {
        return (field, value, queryWrapper) -> {
            String jdbcType = TableColumnContextHolder.getJdbcType(field);
            // 存在null值的列作为条件查询，符合条件的列doris也不会返回。所以需要增加条件isNUll
            if ("JSONB".equals(jdbcType)) {
                queryWrapper.isNull(SqlScriptUtil.backtick(field)).or().apply(notLike().apply(field), value);
            } else {
                queryWrapper.isNull(SqlScriptUtil.backtick(field)).or().ne(SqlScriptUtil.backtick(field), value);
            }
            return queryWrapper;
        };
    }
}
