package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.data.factory.dto.StatisticsChartDto;
import cn.bctools.data.factory.dto.StatisticsQueryDto;
import cn.bctools.data.factory.dto.StatisticsSumDto;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.service.JvsDataFactoryLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @author xiaohui
 */
@Api(tags = "统计数据")
@RestController
@AllArgsConstructor
@RequestMapping("/statistics")
@Slf4j
public class StatisticsController {
    private final JvsDataFactoryLogService jvsDataFactoryLogService;

    @ApiOperation("获取总统计数据")
    @GetMapping("/sum/{id}")
    public R<StatisticsSumDto> sum(@PathVariable String id) {
        //总次数
        long count = jvsDataFactoryLogService.count(new LambdaQueryWrapper<JvsDataFactoryLog>().eq(JvsDataFactoryLog::getDataId, id));
        //成功次数
        long succeedCount = jvsDataFactoryLogService.count(new LambdaQueryWrapper<JvsDataFactoryLog>()
                .eq(JvsDataFactoryLog::getExecutionResults, Boolean.TRUE)
                .eq(JvsDataFactoryLog::getDataId, id));
        //失败次数
        long failCount = jvsDataFactoryLogService.count(new LambdaQueryWrapper<JvsDataFactoryLog>()
                .eq(JvsDataFactoryLog::getExecutionResults, Boolean.FALSE)
                .eq(JvsDataFactoryLog::getDataId, id)
        );
        //最高消耗
        JvsDataFactoryLog maxData = jvsDataFactoryLogService.getOne(new QueryWrapper<JvsDataFactoryLog>()
                .select("max(duration) as duration")
                .lambda().eq(JvsDataFactoryLog::getDataId, id));
        Long max = Optional.ofNullable(maxData).orElse(new JvsDataFactoryLog().setDuration(0L)).getDuration();
        //最低消耗
        JvsDataFactoryLog minData = jvsDataFactoryLogService.getOne(new QueryWrapper<JvsDataFactoryLog>()
                .select("min(duration) as duration")
                .lambda().eq(JvsDataFactoryLog::getDataId, id));
        Long min = Optional.ofNullable(minData).orElse(new JvsDataFactoryLog().setDuration(0L)).getDuration();
        //平均消耗
        BigDecimal avgTime = jvsDataFactoryLogService.meanConsumption(id);
        StatisticsSumDto statisticsSumDto = new StatisticsSumDto()
                .setCount(count)
                .setSucceedCount(succeedCount)
                .setFailCount(failCount)
                .setAvgTime(avgTime)
                .setMaxTime(max)
                .setMinTime(min);
        return R.ok(statisticsSumDto);
    }

    @ApiOperation("获取总统计数据")
    @GetMapping("/chart")
    public R<List<StatisticsChartDto>> chart(StatisticsQueryDto statisticsQueryDto) {
        List<StatisticsChartDto> list = jvsDataFactoryLogService.staticChart(statisticsQueryDto);
        return R.ok(list);
    }
}
