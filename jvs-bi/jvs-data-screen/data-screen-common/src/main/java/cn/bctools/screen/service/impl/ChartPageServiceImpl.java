package cn.bctools.screen.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.screen.chart.ChartElementInterface;
import cn.bctools.screen.chart.bo.ChartSettingBo;
import cn.bctools.screen.chart.po.ChartDesignInParameter;
import cn.bctools.screen.chart.po.ChartReturnObj;
import cn.bctools.screen.chart.po.GetDataParameter;
import cn.bctools.screen.chart.po.StatisticsDataPo;
import cn.bctools.screen.dto.DrillReturnDto;
import cn.bctools.screen.dto.MonomerDto;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.mapper.ChartPageMapper;
import cn.bctools.screen.query.QueryExecuteFactory;
import cn.bctools.screen.service.ChartPageService;
import cn.bctools.screen.service.DrillService;
import cn.bctools.screen.service.LinkService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 页面配置
 *
 * @author zqs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChartPageServiceImpl extends ServiceImpl<ChartPageMapper, ChartPage> implements ChartPageService {

    private final QueryExecuteFactory queryExecuteFactory;
    private final LinkService linkService;
    private final DrillService drillService;
    private final DataFactoryApi dataFactoryApi;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ChartPageMapper chartPageMapper;

    @Override
    @SneakyThrows
    public ChartReturnObj dataTranslation(String json, MonomerDto monomerDto, String chartName) {
        TimeInterval timer = DateUtil.timer();
        StatisticsDataPo statisticsDataPo = new StatisticsDataPo();
        com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(json);
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
                //生成筛选器与组内条件
                List<Object> parameter = queryExecuteFactory.execute(chartSettingBo, monomerDto.getQueryData(), whereSql);
                //进行数据联动 条件生成
                List<Object> link = linkService.link(monomerDto.getLinkData(), whereSql);
                parameter.addAll(link);
                if (monomerDto.getDrillSetting() != null && monomerDto.getDrillSetting().check()) {
                    //数据钻取
                    DrillReturnDto drill = drillService.drill(monomerDto.getDrillSetting(), logicSetting, whereSql);
                    parameter.addAll(drill.getParameter());
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
                    if (!Optional.ofNullable(chartSettingBo.getIsTable()).orElse(Boolean.FALSE)) {
                        chartReturnObj = elementInterface.exec(getDataParameter);
                        Long count = dorisJdbcTemplate.getCountSql(tableName);
                        statisticsDataPo.setNumber(count);
                    } else {
                        chartReturnObj = elementInterface.getTable(getDataParameter, chartSettingBo.getCurrent(), chartSettingBo.getSize());
                    }
                } else {
                    throw new BusinessException("获取表名称 发生错误");
                }
                //查询总条数
            } catch (Exception exception) {
                String errorMsg = "数据处理异常";
                if (exception instanceof BusinessException || exception.getCause() instanceof BusinessException) {
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
                        .setDesignDetailId(jsonObject.getString("id"))
                        .setDesignDetailName(jsonObject.getJSONObject("setting").getString("name"));
                this.send(consanguinityAnalyse);
            }
        }
        return chartReturnObj;
    }

    @Override
    public void forcedDeletion(String id) {
        chartPageMapper.forcedDeletion(id);
    }

    public void send(ConsanguinityAnalyse consanguinityAnalyse) {
        consanguinityAnalyse.setType(3)
                .setViewType(ConsanguinityViewTypeEnum.screen);
        rabbitTemplate.convertAndSend("consanguinity_analyse_task", consanguinityAnalyse);
    }

}
