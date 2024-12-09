package cn.bctools.design.data.source.impl.sql.methods.impl;

import cn.bctools.design.data.source.impl.sql.Constant;
import cn.bctools.design.data.source.impl.sql.SqlStatement;
import cn.bctools.design.data.source.impl.sql.enums.DynamicSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.AbstractSqlMethod;
import cn.bctools.design.data.source.impl.sql.methods.DynamicSqlInfo;
import cn.bctools.design.data.source.impl.sql.methods.WhereParam;

/**
 * @Author: ZhuXiaoKang
 * @Description: 统计数据量
 */
public class SelectCount extends AbstractSqlMethod {

    @Override
    public SqlStatement injectSqlStatement(DynamicSqlInfo info) {
        DynamicSqlMethod sqlMethod = DynamicSqlMethod.SELECT_COUNT;
        WhereParam whereParam = whereSegmentSql(info);
        String sql = String.format(sqlMethod.getSql(), Constant.PRIMARY_KEY, tableName(info), whereParam.getSqlSegment());
        return new SqlStatement().setSql(sql).setWhereParam(whereParam.getParam());
    }

}
