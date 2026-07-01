package cn.bctools.design.sqlInjector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

/**
 * The type Custom abstract method.
 *
 * @author zhuxiaokang
 */
public abstract class CustomAbstractMethod extends AbstractMethod {

    /**
     * Instantiates a new Custom abstract method.
     *
     * @param methodName the method name
     */
    public CustomAbstractMethod(String methodName) {
        super(methodName);
    }

    @Override
    protected String sqlWhereEntityWrapper(boolean newLine, TableInfo table) {
        String sqlScript;
        sqlScript = table.getAllSqlWhere(false, true, "ew.entity.");
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
        sqlScript = sqlScript + "\n";
        sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", "ew.nonEmptyOfEntity", "ew.nonEmptyOfNormal"), false) + " ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfWhere"), true);
        sqlScript = SqlScriptUtils.convertWhere(sqlScript) + "\n";
        sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfWhere"), true);
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew"), true);
        return newLine ? "\n" + sqlScript : sqlScript;
    }
}
