package cn.bctools.chart.service;

import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.dto.MonomerDto;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
     * 发送血缘视图
     * @param consanguinityAnalyse 血缘关系
     */
    void send(ConsanguinityAnalyse consanguinityAnalyse);

    /**
     * 通过图表id获取数据id
     *
     * @param chartId 图表id
     * @return 数据集id集合
     */
    List<String> getDataFactoryId(String chartId);

    /**
     * 上传时数据获取与数据恢复
     *
     * @param id 数据id
     * @return 当前数据量
     */
    Long upGetOrDataRecover(String id);


}
