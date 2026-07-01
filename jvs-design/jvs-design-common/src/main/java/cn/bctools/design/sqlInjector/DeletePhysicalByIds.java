package cn.bctools.design.sqlInjector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * The type Delete physical by ids.
 *
 * @author zhuxiaokang  自定义根据id批量物理删除方法 <p>     此方法只执行物理删除，不判断是否配置了逻辑删除
 */
public class DeletePhysicalByIds extends CustomAbstractMethod {

    /**
     * The constant METHOD.
     */
    public static final String METHOD = "deletePhysicalByIds";

    /**
     * Instantiates a new Delete physical by ids.
     */
    public DeletePhysicalByIds() {
        this(METHOD);
    }

    /**
     * Instantiates a new Delete physical by ids.
     *
     * @param methodName the method name
     */
    protected DeletePhysicalByIds(String methodName) {
        super(methodName);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.DELETE_BATCH_BY_IDS;
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(), SqlScriptUtils.convertForeach(SqlScriptUtils.convertChoose("@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(item.getClass())", "#{item}", "#{item." + tableInfo.getKeyProperty() + "}"), "coll", (String) null, "item", ","));
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);
        return this.addDeleteMappedStatement(mapperClass, METHOD, sqlSource);
    }
}
