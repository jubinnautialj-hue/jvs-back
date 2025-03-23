package cn.bctools.chart.service.impl;

import cn.bctools.chart.chart.ChartElementInterface;
import cn.bctools.chart.chart.bo.ChartSettingBo;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.chart.po.StatisticsDataPo;
import cn.bctools.chart.dto.DrillReturnDto;
import cn.bctools.chart.dto.MonomerDto;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.mapper.ChartPageMapper;
import cn.bctools.chart.query.QueryExecuteFactory;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.chart.service.DrillService;
import cn.bctools.chart.service.LinkService;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 页面配置
 *
 * @author zqs
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChartPageServiceImpl extends ServiceImpl<ChartPageMapper, ChartPage> implements ChartPageService {
    private final LinkService linkService;
    private final DrillService drillService;
    private final DataFactoryApi dataFactoryApi;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ChartPageMapper chartPageMapper;

    @Override
    public Long upGetOrDataRecover(String id) {
        chartPageMapper.retrieve(id);
        return this.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getId, id));
    }

    @Override
    public List<String> getDataFactoryId(String chartId) {
        ChartPage byId = this.getById(chartId);
        if (byId == null) {
            throw new BusinessException("此图表数据不存在");
        }
        List<ChartSettingBo> settingBos = JSONArray.parseArray(byId.getDataJson(), ChartSettingBo.class);
        return settingBos.stream().filter(e -> (e.getDataSource() != null && StrUtil.isNotBlank(e.getDataSource().getString("executeName")))
                        || e.getLogicSetting().getDataFactoryObj() != null
                        && ("mqtt".equals(e.getLogicSetting().getDataFactoryObj().getDataFactoryType()) || "api".equals(e.getLogicSetting().getDataFactoryObj().getDataFactoryType())))
                .map(e -> {
                    if (e.getDataSource() == null) {
                        return e.getLogicSetting().getDataFactoryObj().getDataFactoryId();
                    } else {
                        return e.getDataSource().getString("executeName");
                    }
                }).distinct().collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public ChartReturnObj dataTranslation(String json, MonomerDto monomerDto, String chartName) {
        TimeInterval timer = DateUtil.timer();
        StatisticsDataPo statisticsDataPo = new StatisticsDataPo();
        JSONObject jsonObject = JSONObject.parseObject(json);
        //获取公共参数
        ChartSettingBo chartSettingBo = jsonObject.toJavaObject(ChartSettingBo.class);
        ChartReturnObj chartReturnObj = new ChartReturnObj();
        if (ObjectUtil.isNotNull(chartSettingBo.getDataSource())) {
            //初始化数据来源
            //获取接口的返回数据
            try {
                StringBuffer whereSql = new StringBuffer();
                ChartElementInterface elementInterface = SpringContextUtil.getBean(chartSettingBo.getType().getCls());
                //获取数据集id
                String executeName = chartSettingBo.getDataSource().getString("executeName");
                //配置信息
                ChartDesignInParameter logicSetting = chartSettingBo.getLogicSetting();
                //过滤数据
                QueryExecuteFactory queryExecuteFactory = SpringContextUtil.getBean(QueryExecuteFactory.class);
                //生成筛选器与组内条件
                List<Object> parameter = queryExecuteFactory.execute(chartSettingBo, monomerDto.getQueryData(), whereSql);
                //进行数据联动 条件生成
                List<Object> link = linkService.link(monomerDto.getLinkData(), whereSql);
                parameter.addAll(link);
                if (monomerDto.getDrillSetting() != null && monomerDto.getDrillSetting().check()) {
                    //数据钻取
                    DrillReturnDto drill = drillService.drill(monomerDto.getDrillSetting(), logicSetting, whereSql);
                    parameter.addAll(drill.getParameter());
                    //数据下钻 需要清空同环比与排序
                    if (!logicSetting.getYAxis().isEmpty()) {
                        logicSetting.getYAxis().stream().peek(e -> e.setChildren(new ArrayList<>())).collect(Collectors.toList());
                    }
                    chartSettingBo.setSortList(new ArrayList<>());
                    if (drill.getSort() != null) {
                        chartSettingBo.setSortList(Collections.singletonList(drill.getSort()));
                    }
                }
                //获取表名称
                R<String> r = dataFactoryApi.getTableName(executeName);
                if (r.is()) {
                    String tableName = r.getData();
                    //设置默认值防止为null
                    logicSetting.setClassifyField(Optional.ofNullable(logicSetting.getClassifyField()).orElse(new ArrayList<>(1))).setYAxis(Optional.ofNullable(logicSetting.getYAxis()).orElse(new ArrayList<>(1))).setXAxis(Optional.ofNullable(logicSetting.getXAxis()).orElse(new ArrayList<>(1)));
                    GetDataParameter getDataParameter = new GetDataParameter()
                            .setParameter(parameter)
                            .setWhere(whereSql.toString())
                            .setShowNumber(Optional.ofNullable(chartSettingBo.getSize()).orElse(-1L))
                            .setSortFields(chartSettingBo.getSortList())
                            .setTableName(tableName)
                            .setDataFactoryId(executeName)
                            .setFileName(chartSettingBo.getFileName())
                            .setLogicSetting(logicSetting);
                    Integer getDataType = Optional.ofNullable(chartSettingBo.getGetDataType()).orElse(0);
                    switch (getDataType) {
                        case 0:
                            chartReturnObj = elementInterface.exec(getDataParameter);
                            Long count = dorisJdbcTemplate.getCountSql(tableName);
                            statisticsDataPo.setNumber(count);
                            break;
                        case 1:
                            chartReturnObj = elementInterface.getTable(getDataParameter, chartSettingBo.getCurrent(), chartSettingBo.getSize());
                            break;
                        case 2:
                            chartReturnObj = elementInterface.outFile(getDataParameter, chartSettingBo.getSize());
                            break;
                        default:
                            new BusinessException("获取数据的类型未知");
                    }
                } else {
                    throw new BusinessException(r.getMsg());
                }
                //查询总条数
            } catch (Exception exception) {
                String errorMsg = "数据处理异常";
                if (exception.getCause() instanceof BusinessException) {
                    errorMsg = exception.getMessage();
                }
                chartReturnObj.setError(errorMsg);
                log.error("数据处理返回异常", exception);
            } finally {
                statisticsDataPo.setTime(timer.interval());
                chartReturnObj.setStatisticsDataPo(statisticsDataPo);
                //发送消息 用于血缘视图记录
                ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                        .setDataFactoryId(chartSettingBo.getDataSource().getString("executeName"))
                        .setDesignName(chartName)
                        .setTenantId(monomerDto.getTenantId())
                        .setDesignId(monomerDto.getDesignId())
                        .setDesignDetailId(jsonObject.getString("i"))
                        .setDesignDetailName(jsonObject.getJSONObject("setting").getString("name"));
                this.send(consanguinityAnalyse);
            }
        }
        return chartReturnObj;
    }

    @Override
    public void send(ConsanguinityAnalyse consanguinityAnalyse) {
        consanguinityAnalyse.setType(3)
                .setViewType(ConsanguinityViewTypeEnum.chart);
        rabbitTemplate.convertAndSend("consanguinity_analyse_task", consanguinityAnalyse);
    }

}
