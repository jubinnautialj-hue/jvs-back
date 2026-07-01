package cn.bctools.design.sqlInjector;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * The type Delete physical.
 *
 * @author zhuxiaokang  自定义条件物理删除方法 <p>     此方法只执行物理删除，不判断是否配置了逻辑删除
 */
public class DeletePhysical extends CustomAbstractMethod {

    /**
     * The constant METHOD.
     */
    public static final String METHOD = "deletePhysical";

    /**
     * Instantiates a new Delete physical.
     */
    public DeletePhysical() {
        this(METHOD);
    }

    /**
     * Instantiates a new Delete physical.
     *
     * @param methodName the method name
     */
    protected DeletePhysical(String methodName) {
        super(methodName);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.DELETE;
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
        SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, this.methodName, sqlSource);
    }


}
