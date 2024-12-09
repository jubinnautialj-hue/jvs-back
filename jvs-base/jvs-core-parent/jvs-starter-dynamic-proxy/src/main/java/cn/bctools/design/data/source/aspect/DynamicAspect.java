package cn.bctools.design.data.source.aspect;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.data.source.impl.sql.DynamicMethodConvention;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
public class DynamicAspect {

    /**
     * 获取表名
     * @param args
     * @return
     */
    public String getTableName(Object[] args, MethodSignature methodSignature) {
        String[] parameters =  methodSignature.getParameterNames();
        for (int i = 0; i < parameters.length; i++) {
            if (DynamicMethodConvention.PARAM_TABLE_NAME.equals(parameters[i])) {
                return (String)args[i];
            }
        }
        throw new BusinessException("未指定表");
    }

}
