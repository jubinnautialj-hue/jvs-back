package cn.bctools.design.data.source.impl.sql.methods.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import cn.bctools.design.data.source.impl.sql.SqlScriptUtil;
import cn.bctools.design.data.source.impl.sql.SqlStatement;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.bctools.design.data.source.impl.sql.methods.WhereParam;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 查询多条数据
 */
public class SelectList extends AbstractSqlMethod {

    @Override
    public SqlStatement injectSqlStatement(DynamicSqlInfo info) {
        DynamicSqlMethod sqlMethod = DynamicSqlMethod.SELECT_LIST;
        WhereParam whereParam = segmentSql(info);
        String sql = String.format(sqlMethod.getSql(), sqlSelectColumns(info), tableName(info), whereParam.getSqlSegment());
        return new SqlStatement().setSql(sql).setWhereParam(whereParam.getParam());
    }

    /**
     * SQL 查询字段
     * @param info
     * @return
     */
    protected String sqlSelectColumns(DynamicSqlInfo info) {
        DynamicQuery query = info.getDynamicQuery();
        if (ObjectNull.isNull(query)) {
            return StringPool.ASTERISK;
        }
        List<String> selectColumns = query.getColumns();
        if (ObjectNull.isNotNull(query.getExclude())) {
            selectColumns.removeIf(column -> query.getExclude().contains(column));
        }
        if (ObjectNull.isNotNull(query.getInclude())) {
            selectColumns = query.getInclude().stream().filter(includeColumn -> query.getColumns().contains(includeColumn)).collect(Collectors.toList());
        }
        if (ObjectNull.isNull(selectColumns)) {
            return StringPool.ASTERISK;
        }
        return selectColumns.stream()
                // `字段`
                .map(SqlScriptUtil::backtick)
                .collect(Collectors.joining(SqlScriptUtil.comma()));
    }

    /**
     * 构造sql片段
     *    条件、排序、分页
     *
     * @param info
     * @return
     */
    private WhereParam segmentSql(DynamicSqlInfo info) {
        WhereParam whereParam = whereSegmentSql(info);
        DynamicQuery query = Optional.ofNullable(info.getDynamicQuery()).orElseGet(DynamicQuery::new);
        whereParam.setSqlSegment(SqlScriptUtil.appendSqlSegment(whereParam.getSqlSegment(), sort(query.getSort()), page(query.getOffset(), query.getLimit())));
        return whereParam;
    }

    /**
     * 排序
     *
     * @param sort
     */
    private static String sort(Sort sort) {
        if (ObjectNull.isNull(sort)) {
            return StringPool.EMPTY;
        }
        String sortSegment = sort.stream()
                .map(o -> SqlScriptUtil.appendSqlSegment(o.getProperty(), o.getDirection().name()))
                .collect(Collectors.joining(StringPool.COMMA));
        return SqlScriptUtil.appendSqlSegment(SqlKeyword.ORDER_BY.getSqlSegment(), sortSegment);
    }

    /**
     * 分页
     * @param skip
     * @param limit
     */
    private static String page(Long skip, Long limit) {
        if (ObjectNull.isNull(limit)) {
            return StringPool.EMPTY;
        }
        Long offset = Optional.ofNullable(skip).orElse(0L);
        return SqlScriptUtil.appendSqlSegment("limit ", offset, StringPool.COMMA, limit);
    }
}
