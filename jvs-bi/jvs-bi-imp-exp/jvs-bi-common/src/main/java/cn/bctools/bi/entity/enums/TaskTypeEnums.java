package cn.bctools.bi.entity.enums;

import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.service.impl.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum TaskTypeEnums {

    chart("chart", "", "图表", Boolean.TRUE, ChartDownOrUpServiceImpl.class),
    data_source("data_source", "", "数据源", Boolean.FALSE, DataSourceDownOrUpServiceImpl.class),
    data_factory("data_factory", "", "数据集", Boolean.FALSE, DataFactoryDownOrUpServiceImpl.class),
    screen("screen", "", "大屏", Boolean.TRUE, ScreenDownOrUpServiceImpl.class),
    report("report", "", "报表", Boolean.TRUE, ReportDownOrUpServiceImpl.class),
    ;

    @EnumValue
    String value;
    String queueName;
    String desc;
    /**
     * 是否需要获取名称 用于数据集作为菜单名称
     */
    Boolean isGetName;
    Class<? extends DownOrUpService> aClass;
}
