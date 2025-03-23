package cn.bctools.data.factory.service.impl;

import cn.bctools.data.factory.dto.StatisticsChartDto;
import cn.bctools.data.factory.dto.StatisticsQueryDto;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.mapper.JvsDataFactoryLogMapper;
import cn.bctools.data.factory.service.JvsDataFactoryLogService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据工厂-记录 服务实现类
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
@Service
@AllArgsConstructor
public class JvsDataFactoryLogServiceImpl extends ServiceImpl<JvsDataFactoryLogMapper, JvsDataFactoryLog> implements JvsDataFactoryLogService {

    private final static Map<String, Function<List<JvsDataFactoryLog>, StatisticsChartDto.IndexData>> STATIC_CHART_FUNCTION = new LinkedHashMap<>();

    /*
     * 统计页 不同图表的统计逻辑
     */
    static {
        STATIC_CHART_FUNCTION.put("总次数/日", e -> {
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(e.size());
        });
        STATIC_CHART_FUNCTION.put("成功次数/日", e -> {
            Long value = e.stream().filter(JvsDataFactoryLog::getExecutionResults).count();
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(value);
        });
        STATIC_CHART_FUNCTION.put("失败次数/日", e -> {
            Long value = e.stream().filter(v -> !v.getExecutionResults()).count();
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(value);
        });
        STATIC_CHART_FUNCTION.put("最高消耗时间(亳秒)/日", e -> {
            Long value = e.stream().map(JvsDataFactoryLog::getDuration).filter(ObjUtil::isNotNull).max(Long::compare).orElse(0L);
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(value);
        });
        STATIC_CHART_FUNCTION.put("最低消耗时问(亳秒)/日", e -> {
            Long value = e.stream().map(JvsDataFactoryLog::getDuration).filter(ObjUtil::isNotNull).min(Long::compare).orElse(0L);
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(value);
        });
        STATIC_CHART_FUNCTION.put("平均消耗时间(亳秒)/日", e -> {
            Long value = e.stream().map(JvsDataFactoryLog::getDuration).filter(ObjUtil::isNotNull).mapToLong(Long::longValue).sum();
            BigDecimal data = NumberUtil.div(value.toString(), String.valueOf(e.size()), 0, RoundingMode.DOWN);
            String x = DateUtil.format(e.get(0).getCreateTime(), DatePattern.NORM_DATE_PATTERN);
            return new StatisticsChartDto.IndexData().setX(x).setY(data);
        });
    }

    @Override
    public List<StatisticsChartDto> staticChart(StatisticsQueryDto statisticsQueryDto) {
        List<JvsDataFactoryLog> list = this.list(new LambdaQueryWrapper<JvsDataFactoryLog>()
                .between(StrUtil.isNotBlank(statisticsQueryDto.getEndTime()), JvsDataFactoryLog::getCreateTime, statisticsQueryDto.getStartTime(), statisticsQueryDto.getEndTime())
                .eq(JvsDataFactoryLog::getDataId, statisticsQueryDto.getId())
                .orderByAsc(JvsDataFactoryLog::getCreateTime));
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<StatisticsChartDto> statisticsChartDtoList = new ArrayList<>();
        STATIC_CHART_FUNCTION.forEach((k, v) -> {
            Map<String, StatisticsChartDto.IndexData> map = list.stream().collect(Collectors.groupingBy(e -> DateUtil.format(e.getCreateTime(), DatePattern.NORM_DATE_PATTERN), LinkedHashMap::new, Collectors.collectingAndThen(Collectors.toList(), v)));
            StatisticsChartDto statisticsChartDto = new StatisticsChartDto().setName(k).setIndex(new ArrayList<>(map.values()));
            statisticsChartDtoList.add(statisticsChartDto);
        });
        return statisticsChartDtoList;
    }

    @Override
    public BigDecimal meanConsumption(String id) {
        List<JvsDataFactoryLog> list = this.list(new LambdaQueryWrapper<JvsDataFactoryLog>().select(JvsDataFactoryLog::getDuration)
                .eq(JvsDataFactoryLog::getDataId, id)
                .isNotNull(JvsDataFactoryLog::getDuration)
        );
        if (list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Long duration = list.stream().map(JvsDataFactoryLog::getDuration).filter(ObjUtil::isNotNull).reduce(Long::sum).orElse(0L);
        return BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(list.size()), 0, RoundingMode.DOWN);
    }
}
