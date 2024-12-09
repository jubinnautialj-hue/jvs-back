package cn.bctools.design.data.source.impl.sql;

import cn.bctools.design.data.source.impl.sql.dto.DataColumnDto;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.function.Function;

/**
 * @Author: ZhuXiaoKang
 * @Description: 对sql做特殊处理的函数
 *
 * <p> 不同的数据库语法可能不同。可在此接口扩展
 */
public interface SqlFunction {

    /**
     * UPDATE语句SET字段占位符处理
     * @return
     */
    default Function<DataColumnDto, DataColumnDto> updateSetPlaceholder() {
        return dataColumnDto -> dataColumnDto;
    }

    /**
     * json字段模糊查询条件
     *
     * @return
     */
    default Function<String, String> jsonLike() {
        return null;
    }

    /**
     * json字段模糊查询 not like
     *
     * @return
     */
    default Function<String, String> notLike() {
        return null;
    }

    /**
     * EQ
     *
     * @return
     */
    default BiSqlFunction<String, Object, QueryWrapper<Object>, QueryWrapper<Object>> eq() {
       return (field, value, queryWrapper) -> queryWrapper.eq(SqlScriptUtil.backtick(field), value);
    }

    /**
     * NE
     *
     * @return
     */
    default BiSqlFunction<String, Object, QueryWrapper<Object>, QueryWrapper<Object>> ne() {
        return (field, value, queryWrapper) -> queryWrapper.ne(SqlScriptUtil.backtick(field), value);
    }
}
