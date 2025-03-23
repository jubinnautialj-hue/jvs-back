package cn.bctools.screen.service;

import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.dto.MonomerDto;
import cn.bctools.screen.entity.ChartPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 页面配置
 *
 * @author zqs
 */
public interface ChartPageService extends IService<ChartPage> {

    /**
     * 通过json字符串进行数据的替换
     *
     * @param json       整个设计数据
     * @param monomerDto 查询条件  联动  下钻
     * @param chartName  名称
     * @return 替换后的json数据
     * @author xh
     * @Time 19:58
     **/
    ChartReturnObj dataTranslation(String json, MonomerDto monomerDto, String chartName);

    /**
     * 强制删除
     * @param id
     */
    void forcedDeletion(String id);

}
