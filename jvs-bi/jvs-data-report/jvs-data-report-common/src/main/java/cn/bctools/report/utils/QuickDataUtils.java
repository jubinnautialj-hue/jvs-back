package cn.bctools.report.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.bctools.data.factory.query.dynamic.enums.DynamicTimeTypeEnum;

/**
 * 快捷方式获取值
 */
public class QuickDataUtils {

    public static DynamicTimeValue getValue(String quickType,String format){
        DynamicTimeTypeEnum dynamicTimeTypeEnum;
        try {
            dynamicTimeTypeEnum = DynamicTimeTypeEnum.valueOf(quickType);
        } catch (Exception e) {
            throw new BusinessException("快捷方式不支持");
        }
        return SpringContextUtil.getBean(dynamicTimeTypeEnum.getCls()).getValue(new DynamicTimeDto().setFormat(format).setType(dynamicTimeTypeEnum));
    }
}
