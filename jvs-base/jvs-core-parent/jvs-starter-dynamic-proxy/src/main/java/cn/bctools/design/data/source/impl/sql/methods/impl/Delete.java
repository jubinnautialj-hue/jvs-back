package cn.bctools.design.data.source.impl.sql.methods.impl;

import cn.bctools.design.data.source.impl.sql.SqlStatement;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.bctools.design.data.source.impl.sql.methods.WhereParam;

/**
 * @Author: ZhuXiaoKang
 * @Description: 删除
 */
public class Delete extends AbstractSqlMethod {

    @Override
    public SqlStatement injectSqlStatement(DynamicSqlInfo info) {
        DynamicSqlMethod sqlMethod = DynamicSqlMethod.DELETE;
        WhereParam whereParam = whereSegmentSql(info);
        String sql = String.format(sqlMethod.getSql(), tableName(info), whereParam.getSqlSegment());
        return new SqlStatement().setSql(sql).setWhereParam(whereParam.getParam());
    }
}
