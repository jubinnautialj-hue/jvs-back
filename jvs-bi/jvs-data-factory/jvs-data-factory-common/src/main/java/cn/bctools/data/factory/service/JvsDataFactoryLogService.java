package cn.bctools.data.factory.service;

import cn.bctools.data.factory.dto.StatisticsChartDto;
import cn.bctools.data.factory.dto.StatisticsQueryDto;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 数据工厂-记录 服务类
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
public interface JvsDataFactoryLogService extends IService<JvsDataFactoryLog> {

    /**
     * 平均消耗
     *
     * @param id 数据id
     * @return 消耗时间
     */
    BigDecimal meanConsumption(String id);

    /**
     * 统计图
     *
     * @param statisticsQueryDto 过滤条件
     * @return 统计数据
     */
    List<StatisticsChartDto> staticChart(StatisticsQueryDto statisticsQueryDto);

}
