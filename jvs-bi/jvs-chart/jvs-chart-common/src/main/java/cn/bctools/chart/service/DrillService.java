package cn.bctools.chart.service;


import cn.bctools.chart.chart.ChartElementInterface;
import cn.bctools.chart.chart.bo.DrillSetting;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.dto.DrillReturnDto;

import java.util.List;
import java.util.Map;

/**
 * 数据钻取
 *
 * @author zqs
 */
public interface DrillService {
    /**
     * 数据钻取
     *
     * @param drillSetting     钻取设计数据
     * @param chartDesignInParameter 配置信息
     * @return 过滤后的数据
     */
   DrillReturnDto drill(DrillSetting drillSetting, ChartDesignInParameter chartDesignInParameter, StringBuffer whereSql);
}
