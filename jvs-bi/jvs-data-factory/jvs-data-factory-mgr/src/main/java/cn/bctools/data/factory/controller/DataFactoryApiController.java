package cn.bctools.data.factory.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.dto.DataFactoryApiViewDto;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.TaskCronDto;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.po.ApiDataValuePo;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.service.ApiService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.data.factory.source.util.AuthUtil;
import cn.bctools.data.factory.source.util.JarExecUtil;
import cn.bctools.data.factory.util.JsonAnalysisUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
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

import java.io.File;
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
@Api(tags = "[jvs-data-factory]api数据集相关的所有接口包括数据源")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/factory/api")
@Slf4j
public class DataFactoryApiController {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final ConsanguinityViewService consanguinityViewService;
    private final ConsanguinityAnalyseConsumer consanguinityAnalyseConsumer;
    private final ApiService apiService;
    private final SysJarService sysJarService;

    @ApiOperation("保存数据源获取修改")
    @PostMapping("/save/data/source")
    @Transactional(rollbackFor = Exception.class)
    public R<DataSource> saveDataSource(@RequestBody DataSource dataSource) {
        //校验是否存在使用
        if (ObjUtil.isNotNull(dataSource.getId())) {
            if (apiService.checkDataSourceUse(dataSource.getId())) {
                return R.failed("目前数据源正在使用中,禁止修改请禁用数据集后重试!");
            }
        }
        dataSourceService.saveOrUpdate(dataSource);
        return R.ok(dataSource);
    }

    @ApiOperation("校验api是否连接正常并获取返回值")
    @PostMapping("/check/data/source")
    public R<ApiDataValuePo> checkDataSource(@RequestBody DataFactoryApiViewDto dataFactoryApiViewDto) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(dataFactoryApiViewDto.getDataSourceStructureId());
        ApiDataSourceExecDto apiDataSourceExecDto = com.alibaba.fastjson2.JSONObject.parseObject(dataSourceStructure.getCustomJson(), ApiDataSourceExecDto.class);
        //获取数据源 需要判断是否存在jar包
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        apiDataSourceExecDto.setJarId(dataSource.getCustomJson().toJavaObject(ApiDataSourceExecDto.class).getJarId());
        try {
            String exec;
            if (StrUtil.isNotBlank(apiDataSourceExecDto.getJarId())) {
                File jarFile = sysJarService.getJarFile(apiDataSourceExecDto.getJarId());
                exec = JarExecUtil.exec(jarFile, com.alibaba.fastjson2.JSONObject.toJSONString(apiDataSourceExecDto));
            } else {
                exec = JarExecUtil.exec(apiDataSourceExecDto);
            }
            List<JsonAnalysisPo> analysis = JsonAnalysisUtil.analysis(exec);
            ApiDataValuePo apiDataValuePo = new ApiDataValuePo();
            apiDataValuePo.setStructure(analysis);
            apiDataValuePo.setValue(exec);
            return R.ok(apiDataValuePo);
        } catch (Exception exception) {
            log.info("请求url错误", exception);
            return R.failed(exception.getMessage());
        }
    }

    @ApiOperation("校验api是否连接正常并获取返回值")
    @PostMapping("/check")
    public R<String> check(@RequestBody ApiDataSourceExecDto apiDataSourceExecDto) {
        try {
            //获取数据源 需要判断是否存在jar包
            DataSource dataSource = dataSourceService.getById(apiDataSourceExecDto.getDataSourceId());
            apiDataSourceExecDto.setJarId(dataSource.getCustomJson().toJavaObject(ApiDataSourceExecDto.class).getJarId());
            String value;
            if (StrUtil.isNotBlank(apiDataSourceExecDto.getJarId())) {
                File jarFile = sysJarService.getJarFile(apiDataSourceExecDto.getJarId());
                value = JarExecUtil.exec(jarFile, com.alibaba.fastjson2.JSONObject.toJSONString(apiDataSourceExecDto));
            } else {
                value = JarExecUtil.exec(apiDataSourceExecDto);
            }
            return R.ok(value);
        } catch (Exception exception) {
            log.info("请求url错误", exception);
            return R.failed(exception.getMessage());
        }
    }

    @ApiOperation("执行一次")
    @PostMapping("/exec")
    public R<BigDecimal> exec(@RequestBody DataFactoryApiViewDto dataFactoryApiViewDto) {
        try {
            BigDecimal value = jvsDataFactoryService.execApi(dataFactoryApiViewDto);
            return R.ok(value);
        } catch (Exception exception) {
            log.info("请求url错误", exception);
            return R.failed(exception.getMessage());
        }
    }


    @ApiOperation("保存api")
    @PostMapping("/data/source/save/field")
    public R<DataSourceStructure> dataSourceSaveField(@RequestBody DataSourceStructure dataSourceStructure) {
        //校验是否存在使用
        if (ObjUtil.isNotNull(dataSourceStructure.getId())) {
            if (apiService.checkSubTopicUse(dataSourceStructure.getId())) {
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
        if (apiService.checkSubTopicUse(id)) {
            return R.failed("目前主题正在使用中,禁止删除请禁用数据集后重试!");
        }
        dataSourceStructureService.removeById(id);
        return R.ok();
    }

    @ApiOperation("获取数据源")
    @GetMapping("/get/data/source")
    public R<List<DataSource>> getDataSource() {
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.apiDataSource));
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
        List<JvsDataFactory> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getTaskType, TaskTypeEnum.api).eq(JvsDataFactory::getEnable, Boolean.TRUE));
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
            DataFactoryApiViewDto dataFactoryApiViewDto = JSONObject.parseObject(byId.getViewJson(), DataFactoryApiViewDto.class);
            DataFactoryApiViewDto.InputDataSource inputDataSource = dataFactoryApiViewDto.getInputDataSource();
            //这里需要重新获取 防止数据被修改
            DataSource dataSource = dataSourceService.getById(inputDataSource.getDataSourceId());
            if (dataSource == null) {
                return R.failed("数据源不存在!");
            }
            DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(inputDataSource.getDataSourceStructureId());
            if (dataSourceStructure == null) {
                return R.failed("api地址信息不存在!");
            }
            ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                    .setDesignName(dataSource.getSourceName())
                    .setDataFactoryId(byId.getId())
                    .setDesignDetailId(inputDataSource.getDataSourceStructureId())
                    .setTenantId(TenantContextHolder.getTenantId())
                    .setDesignDetailName(dataSourceStructure.getName())
                    .setViewType(ConsanguinityViewTypeEnum.apiDataSource)
                    .setType(1)
                    .setDesignId(inputDataSource.getDataSourceId());
            consanguinityAnalyseConsumer.send(consanguinityAnalyse);
            //定时任务启动
            String cron;
            switch (dataFactoryApiViewDto.getTimeUnit()) {
                case MINUTE:
                    cron = "0 0/{} * * * ?";
                    break;
                case SECOND:
                    cron = "0/{} * * * * ?";
                    break;
                default:
                    throw new BusinessException("时间跨度未知");
            }
            String format = StrUtil.format(cron, dataFactoryApiViewDto.getTimeSpan());
            TaskCronDto taskCronDto = Optional.ofNullable(byId.getTask()).orElse(new TaskCronDto().setAuthor(UserCurrentUtils.getRealName()).setOnTask(Boolean.TRUE));
            taskCronDto.setCron(format);
            byId.setTask(taskCronDto);
            //使用hutool工具实现定时任务
            CronUtil.schedule(jvsDataFactory.getId(), taskCronDto.getCron(), () -> jvsDataFactoryService.execApiTimedTask(jvsDataFactory.getId()));
        } else {
            CronUtil.remove(jvsDataFactory.getId());
        }
        byId.setEnable(jvsDataFactory.getEnable());
        jvsDataFactoryService.updateById(byId);
        return R.ok(jvsDataFactory);
    }

}
