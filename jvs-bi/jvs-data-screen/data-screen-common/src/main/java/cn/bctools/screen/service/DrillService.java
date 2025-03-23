package cn.bctools.screen.service;


import cn.bctools.screen.chart.bo.DrillSetting;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.dto.DrillReturnDto;

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
