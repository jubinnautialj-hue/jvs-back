package cn.bctools.data.factory.html.enums;

import cn.bctools.data.factory.html.fill.DataFillService;
import cn.bctools.data.factory.html.fill.impl.DataFillEnumServiceImpl;
import cn.bctools.data.factory.html.fill.impl.DataFillNumberServiceImpl;
import cn.bctools.data.factory.html.fill.impl.DataFillTimeServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据填充的方式
 *
 * @author xiaohui
 */
@Getter
@AllArgsConstructor
public enum DataFillTypeEnum {
    /**
     * 数据填充的方式
     */
    time("time", "动态时间", DataFillTimeServiceImpl.class),
    number("number", "范围数字", DataFillNumberServiceImpl.class),
    time_scope("time_scope", "时间范围", DataFillTimeServiceImpl.class),
    enums("enums", "枚举", DataFillEnumServiceImpl.class),
    ;

    private final String value;
    private final String desc;
    private final Class<? extends DataFillService> aClass;
}
