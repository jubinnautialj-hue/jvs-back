package cn.bctools.data.factory.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.MqttProperties;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.dto.DataFactoryMqttViewDto;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.TaskCronDto;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.po.ApiDataValuePo;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.service.MqttService;
import cn.bctools.data.factory.source.data.mqtt.MqttClientFactory;
import cn.bctools.data.factory.source.data.mqtt.OnMessageCallback;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.util.AuthUtil;
import cn.bctools.data.factory.util.JsonAnalysisUtil;
import cn.bctools.data.factory.util.MqttUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * mq
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Api(tags = "[jvs-data-factory]mq数据集相关的所有接口包括数据源")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/factory/mq")
@Slf4j
public class DataFactoryMqController {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final ConsanguinityViewService consanguinityViewService;
    private final ConsanguinityAnalyseConsumer consanguinityAnalyseConsumer;
    private final MqttClientFactory mqttClientFactory;
    private final MqttService mqttService;

    @ApiOperation("保存数据源获取修改")
    @PostMapping("/save/data/source")
    @Transactional(rollbackFor = Exception.class)
    public R<DataSource> saveDataSource(@RequestBody DataSource dataSource) {
        //校验是否存在使用
        if (ObjUtil.isNotNull(dataSource.getId())) {
            if (mqttService.checkDataSourceUse(dataSource.getId())) {
                return R.failed("目前数据源正在使用中,禁止修改请禁用数据集后重试!");
            }
        }
        R<Boolean> booleanR = this.checkDataSource(dataSource);
        if (!booleanR.getData()) {
            return R.failed("连接失败:" + booleanR.getMsg());
        }
        dataSourceService.saveOrUpdate(dataSource);
        return R.ok(dataSource);
    }

    @ApiOperation("校验mqtt是否连接正常")
    @PostMapping("/check/data/source")
    public R<Boolean> checkDataSource(@RequestBody DataSource dataSource) {
        MqttProperties mqttProperties = dataSource.getCustomJson().toJavaObject(MqttProperties.class);
        String check = MqttUtil.check(mqttProperties);
        if (StrUtil.isNotBlank(check)) {
            return R.failed(Boolean.FALSE, check);
        }
        return R.ok(Boolean.TRUE);
    }

    @ApiOperation("通过主题获取一条数据用于字段展示")
    @PostMapping("/data/source/get/field")
    public R<ApiDataValuePo> dataSourceGetField(@RequestBody DataFactoryMqttViewDto dataFactoryMqttViewDto) {
        OnMessageCallback onMessageCallback = new OnMessageCallback();
        DataSource dataSource = dataSourceService.getById(dataFactoryMqttViewDto.getInputDataSource().getDataSourceId());
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(dataFactoryMqttViewDto.getInputDataSource().getDataSourceStructureId());
        MqttProperties mqttProperties = dataSource.getCustomJson().toJavaObject(MqttProperties.class);
        mqttProperties.setSubTopic(dataSourceStructure.getExecuteName());
        String clientId = "get_field_" + dataFactoryMqttViewDto.getInputDataSource().getDataSourceId();
        MqttUtil mqttUtil = new MqttUtil();
        mqttUtil.build(mqttProperties, clientId, onMessageCallback);
        //获取结果
        //睡眠一秒 防止没有监听到数据
        ThreadUtil.sleep(1000);
        String dataValue = onMessageCallback.getDataValue();
        mqttUtil.close();
        try {
            List<JsonAnalysisPo> analysis = JsonAnalysisUtil.analysis(dataValue);
            ApiDataValuePo apiDataValuePo = new ApiDataValuePo();
            apiDataValuePo.setStructure(analysis);
            apiDataValuePo.setValue(dataValue);
            return R.ok(apiDataValuePo);
        } catch (Exception exception) {
            log.error("获取mq数据错误，", exception);
            return R.failed("获取数据错误," + exception.getMessage());

        }
    }

    @ApiOperation("执行一次")
    @PostMapping("/exec")
    public R<BigDecimal> exec(@RequestBody DataFactoryMqttViewDto dataFactoryMqttViewDto) {
        OnMessageCallback onMessageCallback = new OnMessageCallback();
        DataFactoryMqttViewDto.InputDataSource inputDataSource = dataFactoryMqttViewDto.getInputDataSource();
        DataSource dataSource = dataSourceService.getById(inputDataSource.getDataSourceId());
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(inputDataSource.getDataSourceStructureId());
        MqttProperties mqttProperties = dataSource.getCustomJson().toJavaObject(MqttProperties.class);
        mqttProperties.setSubTopic(dataSourceStructure.getExecuteName());
        String clientId = "get_value_" + inputDataSource.getDataSourceId();
        onMessageCallback.setRedisKey(clientId);
        MqttUtil mqttUtil = new MqttUtil();
        mqttUtil.build(mqttProperties, clientId, onMessageCallback);
        //获取结果
        //睡眠一秒 防止没有监听到数据
        ThreadUtil.sleep(1000);
        //执行一次
        try {
            BigDecimal bigDecimal = jvsDataFactoryService.execMqttTask(dataFactoryMqttViewDto, mqttUtil);
            return R.ok(bigDecimal);
        } catch (Exception exception) {
            return R.failed(exception.getMessage());
        } finally {
            mqttUtil.close();
        }
    }

    @ApiOperation("保存与修改主题")
    @PostMapping("/data/source/save/field")
    public R<DataSourceStructure> dataSourceSaveField(@RequestBody DataSourceStructure dataSourceStructure) {
        //校验是否存在使用
        if (ObjUtil.isNotNull(dataSourceStructure.getId())) {
            if (mqttService.checkSubTopicUse(dataSourceStructure.getId())) {
                return R.failed("目前主题正在使用中,禁止修改请禁用数据集后重试!");
            }
        }
        dataSourceStructureService.saveOrUpdate(dataSourceStructure);
        return R.ok(dataSourceStructure);
    }

    @ApiOperation("删除")
    @DeleteMapping("/data/source/delete/{id}")
    public R dataSourceDelete(@PathVariable String id) {
        //校验是否存在使用
        if (mqttService.checkSubTopicUse(id)) {
            return R.failed("目前主题正在使用中,禁止删除请禁用数据集后重试!");
        }
        dataSourceStructureService.removeById(id);
        return R.ok();
    }

    @ApiOperation("获取数据源")
    @GetMapping("/get/data/source")
    public R<List<DataSource>> getDataSource() {
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.mqttDataSource));
        if (list.isEmpty()) {
            return R.ok(list);
        }
        list = AuthUtil.auth(list);
        //获取数据
        List<String> ids = list.stream().map(DataSource::getId).collect(Collectors.toList());
        Map<String, List<DataSourceStructure>> map = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().in(DataSourceStructure::getDataSourceId, ids))
                .stream().collect(Collectors.groupingBy(DataSourceStructure::getDataSourceId, Collectors.toList()));
        list.stream().peek(e -> e.setChildren(map.get(e.getId()))).collect(Collectors.toList());
        return R.ok(list);
    }

    @ApiOperation("获取数据集")
    @GetMapping("/get/data/factory")
    public R<List<JvsDataFactory>> getDataFactory() {
        List<JvsDataFactory> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getTaskType, TaskTypeEnum.mqtt).eq(JvsDataFactory::getEnable, Boolean.TRUE));
        if (list.isEmpty()) {
            return R.ok(list);
        }
        list = list.stream().filter(e -> !jvsDataFactoryService.auth(e.getId()).getOperationList().isEmpty()).collect(Collectors.toList());
        return R.ok(list);
    }

    @ApiOperation("启用或者禁用")
    @PostMapping("/update/status")
    @Transactional(rollbackFor = Exception.class)
    public R<JvsDataFactory> updateStatus(@RequestBody JvsDataFactory jvsDataFactory) {
        JvsDataFactory byId = jvsDataFactoryService.getById(jvsDataFactory.getId());
        if (jvsDataFactory.getEnable()) {
            consanguinityViewService.deleteSource(jvsDataFactory.getId(), 1);
            DataFactoryMqttViewDto mqttViewDto = JSONObject.parseObject(byId.getViewJson(), DataFactoryMqttViewDto.class);
            DataFactoryMqttViewDto.InputDataSource inputDataSource = mqttViewDto.getInputDataSource();
            //这里需要重新获取 防止数据被修改
            DataSource dataSource = dataSourceService.getById(inputDataSource.getDataSourceId());
            if (dataSource == null) {
                return R.failed("数据源不存在!");
            }
            DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(inputDataSource.getDataSourceStructureId());
            if (dataSourceStructure == null) {
                return R.failed("主题信息不存在!");
            }
            ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                    .setDesignName(dataSource.getSourceName())
                    .setDataFactoryId(byId.getId())
                    .setDesignDetailId(inputDataSource.getDataSourceStructureId())
                    .setTenantId(TenantContextHolder.getTenantId())
                    .setDesignDetailName(dataSourceStructure.getName())
                    .setViewType(ConsanguinityViewTypeEnum.mqttDataSource)
                    .setType(1)
                    .setDesignId(inputDataSource.getDataSourceId());
            consanguinityAnalyseConsumer.send(consanguinityAnalyse);
            MqttProperties mqttProperties = dataSource.getCustomJson().toJavaObject(MqttProperties.class);
            mqttProperties.setSubTopic(dataSourceStructure.getExecuteName());
            //开启监听
            mqttClientFactory.openMonitor(mqttProperties, jvsDataFactory.getId(), mqttViewDto.getMaxNumber());
            //定时任务启动
            String cron;
            switch (mqttViewDto.getTimeUnit()) {
                case MINUTE:
                    cron = "0 0/{} * * * ?";
                    break;
                case SECOND:
                    cron = "0/{} * * * * ?";
                    break;
                default:
                    throw new BusinessException("时间跨度单位未知");
            }
            String format = StrUtil.format(cron, mqttViewDto.getTimeSpan());
            TaskCronDto taskCronDto = Optional.ofNullable(byId.getTask()).orElse(new TaskCronDto().setAuthor(UserCurrentUtils.getRealName()).setOnTask(Boolean.TRUE));
            taskCronDto.setCron(format);
            //使用hutool工具实现定时任务
            CronUtil.schedule(jvsDataFactory.getId(), taskCronDto.getCron(), () -> jvsDataFactoryService.execMqttTimedTask(jvsDataFactory.getId()));
            byId.setTask(taskCronDto);
        } else {
            //关闭监听
            mqttClientFactory.closeMonitor(jvsDataFactory.getId());
            CronUtil.remove(jvsDataFactory.getId());
        }
        byId.setEnable(jvsDataFactory.getEnable());
        jvsDataFactoryService.updateById(byId);
        return R.ok(jvsDataFactory);
    }


}
