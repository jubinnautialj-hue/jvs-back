package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.dto.DataFactorySqlViewDto;
import cn.bctools.data.factory.dto.FieldTypeFunction;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.po.DataFactoryExecSql;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelApiService;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelService;
import cn.bctools.data.factory.source.dto.RealTimeSettingDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.RealTimeLog;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.enums.StateEnums;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.RealTimeLogService;
import cn.bctools.data.factory.source.util.AuthUtil;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 实时数据库
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Api(tags = "[jvs-data-factory]sql数据集相关的所有接口包括数据源")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/factory/sql")
@Slf4j
public class DataFactorySqlController {
    private final DataSourceStructureService dataSourceStructureService;
    private final SeaTunnelService seaTunnelService;
    private final RealTimeLogService realTimeLogService;
    private final SeaTunnelApiService seaTunnelApiService;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final ConsanguinityAnalyseConsumer consanguinityAnalyseConsumer;
    private final DataSourceService dataSourceService;
    private final ConsanguinityViewService consanguinityViewService;

    @Log(back = false)
    @ApiOperation("执行sql")
    @PostMapping("/exec")
    public R<DataFactoryExecSql> execSql(@RequestBody DataFactoryExecSql dataFactoryExecSql) {
        //判断是否为select 开头的sql
        String startStr = dataFactoryExecSql.getSql().trim().substring(0, 6);
        if (!"select".equalsIgnoreCase(startStr)) {
            return R.failed("只能执行查询语句");
        }
        TimeInterval timer = DateUtil.timer();
        Page<Map<String, Object>> dataPage = dorisJdbcTemplate.getDataPage(dataFactoryExecSql.getSize(), dataFactoryExecSql.getCurrent(), dataFactoryExecSql.getSql());
        //花费毫秒数
        dataFactoryExecSql.setExecTime(timer.interval());
        dataFactoryExecSql.setRecords(dataPage.getRecords());
        dataFactoryExecSql.setTotal(dataPage.getTotal());
        if (!dataPage.getRecords().isEmpty()) {
            Set<String> set = dataPage.getRecords().get(0).keySet();
            dataFactoryExecSql.setKey(set);
        }
        return R.ok(dataFactoryExecSql);
    }

    @Log(back = false)
    @ApiOperation("获取字段映射")
    @GetMapping("/field/type")
    public R<List<FieldTypeFunction>> getFieldType() {
        Map<DataFieldTypeClassifyEnum, List<DataFieldTypeEnum>> map = Arrays.stream(DataFieldTypeEnum.values()).collect(Collectors.groupingBy(DataFieldTypeEnum::getClassifyEnum, Collectors.toList()));
        List<FieldTypeFunction> functions = map.keySet().stream().map(e -> {
            List<FieldTypeFunction> list = map.get(e)
                    .stream().map(v -> new FieldTypeFunction().setLabel(v.name())
                            .setValue(v.name())
                            .setFormat(v.getFormat())).collect(Collectors.toList());
            return new FieldTypeFunction().setLabel(e.name())
                    .setValue(e.name())
                    .setChildren(list);
        }).collect(Collectors.toList());
        return R.ok(functions);
    }

    @Log(back = false)
    @ApiOperation("获取开启的数据集")
    @GetMapping("/get/data/factory")
    public R<Map<String, List<DataSource>>> getDataFactory() {
        DataSource dataSourceServiceOne = dataSourceService.getOne(new LambdaQueryWrapper<DataSource>()
                .select(DataSource::getSourceType, DataSource::getRole, DataSource::getRoleType, DataSource::getSourceName, DataSource::getId, BasalPo::getCreateById)
                .eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        if (ObjUtil.isNull(dataSourceServiceOne)) {
            return R.ok(new HashMap<>());
        }
        List<DataSourceStructure> list = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, dataSourceServiceOne.getId()));
        list = list.stream().filter(v -> !jvsDataFactoryService.auth(v.getExecuteName()).getOperationList().isEmpty())
                .filter(v -> {
                    JvsDataFactory jvsDataFactory = jvsDataFactoryService.getById(v.getExecuteName());
                    return jvsDataFactory.getEnable() && jvsDataFactory.getTaskType().equals(TaskTypeEnum.sql);
                }).collect(Collectors.toList());
        dataSourceServiceOne.setChildren(list);
        Map<String, List<DataSource>> map = new HashMap<>();
        map.put(DataSourceTypeEnum.dataFactoryDataSource.desc, Arrays.asList(dataSourceServiceOne));
        return R.ok(map);
    }


    @Log(back = false)
    @ApiOperation("获取实时同步任务的验证日志")
    @GetMapping("/get/log")
    public R<Page<RealTimeLog>> getLog(Page<RealTimeLog> page, RealTimeLog realTimeLog) {
        LambdaQueryWrapper<RealTimeLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(realTimeLog.getTaskStatus()), RealTimeLog::getTaskStatus, realTimeLog.getTaskStatus());
        queryWrapper.eq(StrUtil.isNotBlank(realTimeLog.getTableName()), RealTimeLog::getTableName, realTimeLog.getTableName());
        queryWrapper.orderByDesc(RealTimeLog::getCreateTime);
        page = realTimeLogService.page(page, queryWrapper);
        return R.ok(page);
    }


    @Log(back = false)
    @ApiOperation("启用或者禁用")
    @PostMapping("/update/status")
    public R<JvsDataFactory> updateStatus(@RequestBody JvsDataFactory jvsDataFactory) {
        JvsDataFactory byId = jvsDataFactoryService.getById(jvsDataFactory.getId());
        if (jvsDataFactory.getEnable()) {
            consanguinityViewService.deleteSource(jvsDataFactory.getId(), 1);
            DataFactorySqlViewDto dataFactorySqlViewDto = JSONObject.parseObject(byId.getViewJson(), DataFactorySqlViewDto.class);
            dataFactorySqlViewDto.getInputDataSource().forEach(e -> {
                ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                        .setDesignName(e.getSourceName())
                        .setDataFactoryId(byId.getId())
                        .setDesignDetailId(e.getDataSourceStructureId())
                        .setTenantId(TenantContextHolder.getTenantId())
                        .setDesignDetailName(e.getTableNameDesc())
                        .setViewType(ConsanguinityViewTypeEnum.valueOf(e.getSourceType().value))
                        .setType(1)
                        .setDesignId(e.getDataSourceId());
                consanguinityAnalyseConsumer.send(consanguinityAnalyse);
            });
            //通知数据源同步结构
            jvsDataFactoryService.syncTableStructure(byId);
        }
        jvsDataFactoryService.update(new UpdateWrapper<JvsDataFactory>().lambda().set(JvsDataFactory::getEnable, jvsDataFactory.getEnable())
                .eq(JvsDataFactory::getId, jvsDataFactory.getId()));
        return R.ok(jvsDataFactory);
    }

    @ApiOperation("获取支持实时数据的数据库信息")
    @GetMapping("/get/data/source")
    public R<List<DataSource>> getDataSource() {
        //获取支持实时同步的数据源
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>().eq(DataSource::getRealTimeOpen, Boolean.TRUE));
        if (!list.isEmpty()) {
            List<String> ids = list.stream().map(DataSource::getId).collect(Collectors.toList());
            List<DataSourceStructure> dataSourceStructureList = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().in(DataSourceStructure::getDataSourceId, ids)
                    .eq(DataSourceStructure::getRealTimeSettingIsOpen, Boolean.TRUE).eq(DataSourceStructure::getRealTimeIsOpen, Boolean.TRUE));
            if (!dataSourceStructureList.isEmpty()) {
                Map<String, List<DataSourceStructure>> map = dataSourceStructureList.stream().collect(Collectors.groupingBy(DataSourceStructure::getDataSourceId, Collectors.toList()));
                list = list.stream().filter(e -> map.containsKey(e.getId()))
                        .peek(e -> e.setChildren(map.get(e.getId())))
                        .collect(Collectors.toList());
                //判断权限
                list = AuthUtil.auth(list);
                return R.ok(list);
            }
        }
        return R.ok(new ArrayList<>());
    }

    @Log(back = false)
    @ApiOperation("实时数据配置项保存")
    @PostMapping("/update/real/time/setting/{structureId}")
    public R<DataSourceStructure> updateRealTimeSetting(@RequestBody RealTimeSettingDto realTimeSettingDto, @PathVariable String structureId) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        dataSourceStructure.setRealTimeSettingIsOpen(Boolean.TRUE)
                .setRealTimeSetting(realTimeSettingDto);
        dataSourceStructureService.updateById(dataSourceStructure);
        return R.ok(dataSourceStructure);
    }

    @Log(back = false)
    @ApiOperation("实时任务启动或者关闭")
    @GetMapping("/update/real/time/status/{status}/{structureId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> updateRealTimeStatus(@PathVariable Boolean status, @PathVariable String structureId) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        String odsTableName = "ods_" + dataSourceStructure.getExecuteName() + "_" + structureId;
        String seaTunnelId;
        if (status) {
            seaTunnelId = seaTunnelService.syncDataCDC(structureId, odsTableName);
            RealTimeLog realTimeLog = new RealTimeLog().setCreateTime(LocalDateTime.now())
                    .setCheckTime(LocalDateTime.now())
                    .setDataStructureId(structureId)
                    .setTaskStatus(StateEnums.succeed)
                    .setSeaTunnelId(seaTunnelId)
                    .setOdsTableName(odsTableName)
                    .setTableName(dataSourceStructure.getExecuteName());
            realTimeLogService.save(realTimeLog);
        } else {
            seaTunnelId = dataSourceStructure.getSeaTunnelId();
            //关闭任务时 需要停止上次的任务数据
            if (dataSourceStructure.getRealTimeIsOpen()) {
                realTimeLogService.update(new UpdateWrapper<RealTimeLog>()
                        .lambda().eq(RealTimeLog::getDataStructureId, structureId)
                        .eq(RealTimeLog::getTaskStatus, StateEnums.succeed)
                        .set(RealTimeLog::getTaskStatus, StateEnums.fail));
                //停止seaTunnel 任务
                seaTunnelApiService.stopJob(seaTunnelId);
            }
            //删除ods表
            dorisJdbcTemplate.dropForce(odsTableName);
        }
        dataSourceStructureService.update(new UpdateWrapper<DataSourceStructure>().lambda()
                .set(DataSourceStructure::getRealTimeIsOpen, status)
                .set(status, DataSourceStructure::getOdsTableName, odsTableName)
                .set(status, DataSourceStructure::getSeaTunnelId, seaTunnelId)
                .eq(DataSourceStructure::getId, structureId));
        return R.ok(Boolean.TRUE);
    }

}
