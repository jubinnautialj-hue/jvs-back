package cn.bctools.design.config;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.entity.DatabaseInfo;
import cn.bctools.database.getter.IDataSourceGetter;
import cn.bctools.database.getter.ITableFieldGetter;
import cn.bctools.design.util.CurrentAppUtils;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.inner.BaseMultiTableInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

/**
 * @author hoal
 *
 * 增加应用版本号字段查询条件
 * <p>
 *     只处理select类型的语句；
 *     只处理有应用版本号字段的表；
 *     指定id查询的sql，查询条件不自动添加应用版本号；
 *     上下文中应用版本号不存在，查询条件不自动添加应用版本号；
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JvsAppVersionInnerInterceptor extends BaseMultiTableInnerInterceptor implements InnerInterceptor {

    private ITableFieldGetter tableFieldGetter;
    private IDataSourceGetter dataSourceGetter;
    /**
     * 应用版本号字段
     */
    private static final String APP_VERSION = "app_version";

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(parserSingle(mpBs.sql(), null));
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        final String whereSegment = (String) obj;
        processSelectBody(select.getSelectBody(), whereSegment);

        List<WithItem> withItemsList = select.getWithItemsList();
        if (!CollectionUtils.isEmpty(withItemsList)) {
            withItemsList.forEach(withItem -> processSelectBody(withItem, whereSegment));
        }
    }

    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereSegment) {
        // 若没有应用版本号，则不处理
        String appVersion = CurrentAppUtils.getAppVersion();
        if (ObjectNull.isNull(appVersion)) {
            return null;
        }
        // 忽略没有应用版本号字段的表
        if (ignoreTable(table.getName())) {
            log.debug("数据表{}，跳过应用版本号拦截", table.getName());
            return null;
        }
        // 若查询条件中指定了主键id，则不增加版本号为查询条件
        if (getBooleanWhereId(where)) {
            return null;
        }
        return new EqualsTo(getAliasColumn(table),  new StringValue(appVersion));
    }

    /**
     * 轻应用版本号字段别名设置
     *
     * <p>appVersion 或 tableAlias.appVersion</p>
     *
     * @param table 表对象
     * @return 字段
     */
    protected Column getAliasColumn(Table table) {
        StringBuilder column = new StringBuilder();
        // 该起别名就要起别名,禁止修改此处逻辑
        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(StringPool.DOT);
        }
        column.append(APP_VERSION);
        return new Column(column.toString());
    }

    /**
     * 跳过没有应用版本号的表
     *
     * @param tableName 表名
     * @return true-跳过， false-不跳过
     */
    private boolean ignoreTable(String tableName) {
        tableName = tableName.replace("`", "");
        // 校验是否有应用版本号字段
        DatabaseInfo databaseInfo = dataSourceGetter.getCurrent();
        List<String> tableFieldNames = tableFieldGetter.getFieldNames(databaseInfo.getIp(), databaseInfo.getPort(), databaseInfo.getDatabaseName(), tableName);
        if (ObjectUtils.isEmpty(tableFieldNames)) {
            return true;
        }
        //其它默认都不跳过
        return Boolean.FALSE.equals(tableFieldNames.contains(APP_VERSION));
    }


    /**
     * 判断是否有id = ? 或 id in 的表字段条件
     * @param where
     * @return true-有id，false，无id
     */
    private boolean getBooleanWhereId(Expression where) {
        boolean bo = false;
        if (where instanceof Parenthesis) {
            Expression expression = ((Parenthesis) where).getExpression();
            return getBooleanWhereId(expression);
        }
        if (where instanceof AndExpression) {
            AndExpression andExpression = (AndExpression) where;
            bo = getBooleanWhereId(andExpression.getLeftExpression());
            if (bo) {
                return bo;
            }
            bo = getBooleanWhereId(andExpression.getRightExpression());
            if (bo) {
                return bo;
            }
        }
        if (where instanceof InExpression) {
            InExpression inExpression = (InExpression) where;
            bo = getBooleanWhereId(inExpression.getLeftExpression());
            if (bo) {
                return bo;
            }
            bo = getBooleanWhereId(inExpression.getRightExpression());
            if (bo) {
                return bo;
            }
        }
        if (where instanceof EqualsTo) {
            return getBooleanWhereId((((EqualsTo) where).getLeftExpression()));
        }
        if (where instanceof Column) {
            return "id".equals(((Column) where).getColumnName());
        }
        return false;
    }



}
