package cn.bctools.data.factory.html.function.impl;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.SysFunction;
import cn.bctools.data.factory.html.function.FunctionReturnFieldProcess;
import org.springframework.stereotype.Component;

/**
 * now 函数
 */
@Component
public class NowFunction implements FunctionReturnFieldProcess {
    @Override
    public void setDataSourceField(SysFunction function, String functionStr,DataSourceField dataSourceField) {
        StringBuilder format = new StringBuilder("yyyy-MM-dd HH:mm:ss");
        String substring = functionStr.substring(functionStr.indexOf("(") + 1, functionStr.indexOf(")"));
        int length = !substring.isEmpty() ? Integer.parseInt(substring) : 0;
        if (length > 0) {
            format.append(".");
        }
        for (int i = 0; i < length; i++) {
            format.append("S");
        }
        dataSourceField.setLength(length);
        dataSourceField.setFormat(format.toString());
    }
}
