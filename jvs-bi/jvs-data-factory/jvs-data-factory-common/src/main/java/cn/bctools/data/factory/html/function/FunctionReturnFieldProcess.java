package cn.bctools.data.factory.html.function;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.SysFunction;

/**
 * 函数返回值加工
 *
 * @author Administrator
 */
public interface FunctionReturnFieldProcess {

    /**
     * 字段属性处理
     *
     * @param function        函数基本信息
     * @param functionStr     函数表达式
     * @param dataSourceField 字段属性
     */
    void setDataSourceField(SysFunction function, String functionStr, DataSourceField dataSourceField);

}
