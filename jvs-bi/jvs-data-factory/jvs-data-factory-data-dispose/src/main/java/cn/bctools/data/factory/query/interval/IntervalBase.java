package cn.bctools.data.factory.query.interval;


import cn.bctools.data.factory.enums.DataFieldTypeEnum;

import java.util.List;

/**
 * @author wl
 */
public interface IntervalBase {

    /**
     * 区间
     *
     * @param fieldType 字段类型
     * @param format    格式化
     * @param value     对比值
     * @param whereSql  执行的sql
     * @return
     */
    List<Object> exec(DataFieldTypeEnum fieldType, String format, String value, StringBuffer whereSql,String fieldKey);
}
