package cn.bctools.data.factory.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.service.ApiService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.service.MqttService;
import cn.bctools.data.factory.source.data.api.ApiDataSourceImpl;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.dto.ExecApiPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.util.AuthUtil;
import cn.bctools.data.factory.source.util.JsonAnalysisUtil;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Api(tags = "[jvs-data-source]数据源配置")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/data/source")
public class DataSourceFieldController {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final MqttService mqttService;
    private final ApiService apiService;
    private final Map<String, DataSourceExecuteInterface> interfaceMap;


    @Log(back = false)
    @ApiOperation("保存数据源获取修改")
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public R<DataSource> save(@RequestBody DataSource dataSource) {
        //判断名称是否重复
        dataSourceService.duplicateName(dataSource.getSourceName(), dataSource.getId());
        dataSource.setCheckIs(Optional.ofNullable(dataSource.getCheckIs()).orElse(Boolean.FALSE));
        boolean b = !dataSource.getSourceType().equals(DataSourceTypeEnum.apiDataSource);
        if (b) {
            try {
                dataSourceService.check(JSONObject.toJSONString(dataSource), dataSource.getSourceType());
                dataSource.setCheckIs(Boolean.TRUE);
            } catch (Exception exception) {
                log.error("验证数据源是否正常发生错误", exception);
                return R.failed("数据源连接失败,失败原因:" + exception.getMessage());
            }
        }
        dataSource.setSyncStructure(2);
        dataSourceService.saveOrUpdate(dataSource);
        if (b) {
            dataSourceService.syncTableStructure(dataSource, TenantContextHolder.getTenantId(), UserCurrentUtils.getCurrentUser());
        }
        return R.ok(dataSource);
    }


    @Log(back = false)
    @ApiOperation("api保存数据源与修改")
    @PostMapping("/save/api")
    @Transactional(rollbackFor = Exception.class)
    public R<DataSource> saveApi(@RequestBody DataSource dataSource) {
        dataSource.setSyncStructure(BigDecimal.ROUND_DOWN);
        dataSourceService.saveOrUpdate(dataSource);
        return R.ok(dataSource);
    }

    @Log(back = false)
    @ApiOperation("api保存数据源与修改")
    @PostMapping("/save/structure/api")
    @Transactional(rollbackFor = Exception.class)
    public R<DataSourceStructure> saveOrUpdateStructure(@RequestBody DataSourceStructure dataSourceStructure) {
        dataSourceStructure.setFieldCount(Optional.ofNullable(dataSourceStructure).map(DataSourceStructure::getStructure).map(List::size).orElse(0));
        dataSourceStructureService.saveOrUpdateStructure(dataSourceStructure);
        return R.ok(dataSourceStructure);
    }


    @Log(back = false)
    @ApiOperation("重新同步")
    @PostMapping("/sync/structure")
    public R<DataSource> syncStructure(@RequestBody DataSource dataSource) {
        DataSource byId = dataSourceService.getById(dataSource.getId());
        if (byId.getSyncStructure() == BigDecimal.ROUND_CEILING) {
            return R.failed("同步中请稍等");
        }
        dataSourceService.update(new UpdateWrapper<DataSource>().lambda().eq(DataSource::getId, dataSource.getId()).set(DataSource::getSyncStructure, BigDecimal.ROUND_CEILING));
        dataSourceService.syncTableStructure(byId, TenantContextHolder.getTenantId(), UserCurrentUtils.getCurrentUser());
        return R.ok(dataSource);
    }

    @Log(back = false)
    @ApiOperation("修改表结构的数据类型")
    @PutMapping("/update/structure")
    public R updateStructure(@RequestBody DataSourceStructure dataSourceStructure) {
        dataSourceStructureService.update(new UpdateWrapper<DataSourceStructure>().lambda()
                .eq(DataSourceStructure::getId, dataSourceStructure.getId())
                .set(DataSourceStructure::getStructure, JSONObject.toJSONString(dataSourceStructure.getStructure())));
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("解析json结构")
    @PutMapping("/analysis/json")
    public R<List<JsonAnalysisPo>> updateStructure(@RequestBody String jsonStr) {
        try {
            List<JsonAnalysisPo> analysis = JsonAnalysisUtil.analysis(jsonStr);
            return R.ok(analysis);
        } catch (BusinessException businessException) {
            log.error("解析错误", businessException);
            return R.failed(businessException.getMessage());
        } catch (Exception e) {
            log.error("解析错误", e);
            return R.failed("解析json错误");
        }
    }

    @Log(back = false)
    @ApiOperation("执行api并返回json")
    @PutMapping("/exec/api")
    public R<List<JsonAnalysisPo>> execApi(@RequestBody ExecApiPo execApiPo) {
        ApiDataSourceImpl bean = SpringContextUtil.getBean(ApiDataSourceImpl.class);
        try {
            String jsonStr = bean.test(execApiPo);
            log.info("api返回数据：【{}】", jsonStr);
            List<JsonAnalysisPo> analysis = JsonAnalysisUtil.analysis(jsonStr);
            return R.ok(analysis);
        } catch (BusinessException businessException) {
            log.error("解析错误", businessException);
            return R.failed(businessException.getMessage());
        } catch (Exception e) {
            log.error("解析错误", e);
            return R.failed("解析json错误");
        }
    }

    @Log(back = false)
    @ApiOperation("测试当前数据源是否正常")
    @PutMapping("/check")
    public R<Boolean> check(@RequestBody DataSource dataSource) {
        Boolean isCheck = Boolean.TRUE;
        String errStr = "";
        try {
            dataSourceService.check(JSONObject.toJSONString(dataSource), dataSource.getSourceType());
        } catch (Exception e) {
            isCheck = Boolean.FALSE;
            errStr = e.getMessage();
        }
        if (StrUtil.isNotBlank(dataSource.getId())) {
            dataSourceStructureService.update(new UpdateWrapper<DataSourceStructure>().lambda().set(DataSourceStructure::getCheckIs, isCheck)
                    .eq(DataSourceStructure::getDataSourceId, dataSource.getId()));
        }
        if (!isCheck) {
            errStr = "数据源连接失败,失败原因:" + errStr;
            return R.failed(Boolean.FALSE, errStr);
        }
        return R.ok(Boolean.TRUE);
    }

    @Log(back = false)
    @ApiOperation("api测试当前数据源是否正常")
    @PutMapping("/check/api")
    public R<Boolean> checkApi(@RequestBody DataSourceStructure dataSource) {
        Boolean isCheck = Boolean.TRUE;
        String errStr = "";
        try {
            dataSourceService.check(JSONObject.toJSONString(dataSource), DataSourceTypeEnum.apiDataSource);
        } catch (Exception e) {
            isCheck = Boolean.FALSE;
            errStr = e.getMessage();
        }
        if (StrUtil.isNotBlank(dataSource.getId())) {
            dataSourceStructureService.update(new UpdateWrapper<DataSourceStructure>().lambda()
                    .set(DataSourceStructure::getCheckIs, isCheck)
                    .eq(DataSourceStructure::getId, dataSource.getId()));
        }
        if (!isCheck) {
            errStr = "数据源连接失败,失败原因:" + errStr;
            return R.failed(Boolean.FALSE, errStr);
        }
        return R.ok(Boolean.TRUE);
    }

    @Log(back = false)
    @ApiOperation("获取字段所有类型")
    @GetMapping("/get/field/type")
    public R<List<DataFieldTypeEnum>> getFieldType() {
        DataFieldTypeEnum[] values = DataFieldTypeEnum.values();
        return R.ok(Arrays.asList(values));
    }

    @Log(back = false)
    @ApiOperation("获取所有数据源")
    @GetMapping("/list")
    public R<List<DataSource>> getAll(DataSource dataSource) {
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<DataSource>()
                .like(StrUtil.isNotBlank(dataSource.getSourceName()), DataSource::getSourceName, dataSource.getSourceName())
                .eq(ObjectUtil.isNotNull(dataSource.getSourceType()), DataSource::getSourceType, dataSource.getSourceType())
                .eq(ObjectUtil.isNotNull(dataSource.getSyncStructure()), DataSource::getSyncStructure, BigDecimal.ROUND_DOWN)
                .orderByDesc(DataSource::getCreateTime);
        //todo 需要把密码制空
        List<DataSource> list = dataSourceService.list(queryWrapper);
        return R.ok(list);
    }


    @Log(back = false)
    @ApiOperation("1、获取一级数据库和类型树")
    @PostMapping("/select")
    public R<Map<String, List<DataSource>>> getSelect(@RequestBody List<DataSourceTypeEnum> list) {
        if (list.isEmpty()) {
            list = Arrays.asList(DataSourceTypeEnum.values());
        }
        //获取有权限的数据
        Map<String, List<DataSource>> map = new HashMap<>(list.size());
        for (DataSourceTypeEnum e : list) {
            //排除智仓数据源
            try {
                List<DataSource> dataBase = interfaceMap.get(e.value).getDataBase(e);
                dataBase = AuthUtil.auth(dataBase);
                map.put(e.desc, dataBase);
            } catch (Exception exception) {
                log.info("获取{}数据源错误", e.desc, exception);
            }
        }
        return R.ok(map);
    }

    @Log(back = false)
    @ApiOperation("获取数据库,类型树,表名称")
    @PostMapping("/select/all/{users}")
    public R<Map<String, List<DataSource>>> getSelectTable(@RequestBody List<DataSourceTypeEnum> list, @PathVariable ConsanguinityViewTypeEnum users) {
        if (list.isEmpty()) {
            list = Arrays.asList(DataSourceTypeEnum.values());
        }
        //获取有权限的数据
        Map<String, List<DataSource>> map = new HashMap<>(list.size());
        //如果是sql 数据集只有部分数据源可以使用
        if (users.equals(ConsanguinityViewTypeEnum.dataFactorySqlDataSource)) {
            list = list.stream().filter(DataSourceTypeEnum::getRealTime).collect(Collectors.toList());
        }
        if (users.equals(ConsanguinityViewTypeEnum.dataFactoryMqttDataSource)) {
            list = list.stream().filter(e -> e.equals(DataSourceTypeEnum.mqttDataSource)).collect(Collectors.toList());
        }
        for (DataSourceTypeEnum e : list) {
            //排除智仓数据源
            try {
                List<DataSource> dataBase = interfaceMap.get(e.value).getDataBase(e);
                if (dataBase.isEmpty()) {
                    map.put(e.desc, new ArrayList<>());
                    continue;
                }
                dataBase = AuthUtil.auth(dataBase);
                //设置下级 表名称
                List<String> ids = dataBase.stream().map(DataSource::getId).collect(Collectors.toList());
                List<DataSourceStructure> dataSourceStructures = dataSourceStructureService
                        .list(new LambdaQueryWrapper<DataSourceStructure>()
                                .in(DataSourceStructure::getDataSourceId, ids)
                                .eq(DataSourceStructure::getCheckIs, Boolean.TRUE)
                                .eq(users.equals(ConsanguinityViewTypeEnum.dataFactorySqlDataSource), DataSourceStructure::getRealTimeIsOpen, Boolean.TRUE)
                                .select(DataSourceStructure::getId, DataSourceStructure::getOdsTableName, DataSourceStructure::getTableNameDesc, DataSourceStructure::getExecuteName, DataSourceStructure::getName, DataSourceStructure::getDataSourceId)
                                .orderByDesc(DataSourceStructure::getCreateTime));
                //数据集权限过滤 数据集权限需要走数据集本身的权限
                if (e.equals(DataSourceTypeEnum.dataFactoryDataSource)) {
                    dataSourceStructures = dataSourceStructures.stream()
                            .filter(v -> !jvsDataFactoryService.auth(v.getExecuteName()).getOperationList().isEmpty())
                            .filter(v -> jvsDataFactoryService.getById(v.getExecuteName()).getTaskType().equals(TaskTypeEnum.ordinary))
                            //如果是普通数据集 可以使用普通数据集
                            .filter(v -> {
                                if (users.equals(ConsanguinityViewTypeEnum.dataFactoryDataSource)) {
                                    JvsDataFactory jvsDataFactory = jvsDataFactoryService.getById(v.getExecuteName());
                                    return Optional.ofNullable(jvsDataFactory.getTaskType()).orElse(TaskTypeEnum.ordinary).equals(TaskTypeEnum.ordinary);
                                }
                                return true;
                            })
                            .collect(Collectors.toList());
                }
                Map<String, List<DataSourceStructure>> stringListMap = dataSourceStructures
                        .stream()
                        .collect(Collectors.groupingBy(DataSourceStructure::getDataSourceId, LinkedHashMap::new, Collectors.toList()));
                //设置下级
                dataBase.stream().peek(v -> v.setChildren(stringListMap.getOrDefault(v.getId(), new ArrayList<>()))).collect(Collectors.toList());
                map.put(e.desc, dataBase);
            } catch (Exception exception) {
                log.info("获取{}数据源错误", e.desc, exception);
            }
        }
        //获取子级数据
        return R.ok(map);
    }

    @Log(back = false)
    @ApiOperation("获取数据库只有数据库没有表信息")
    @PostMapping("/select/all/two")
    public R<Map<String, List<DataSource>>> getSelectTable() {
        List<DataSourceTypeEnum> dataSourceTypeEnums = Arrays.asList(DataSourceTypeEnum.values())
                .stream().filter(DataSourceTypeEnum::getExecSql).collect(Collectors.toList());
        Map<String, List<DataSource>> map = new HashMap<>(dataSourceTypeEnums.size());
        for (DataSourceTypeEnum e : dataSourceTypeEnums) {
            List<DataSource> dataBase = interfaceMap.get(e.value).getDataBase(e);
            dataBase = AuthUtil.auth(dataBase);
            map.put(e.desc, dataBase);
        }
        return R.ok(map);
    }

    @Log(back = false)
    @ApiOperation("获取所有分类")
    @PostMapping("/get/type/all")
    public R<List<Dict>> getTypeAll() {
        List<Dict> list = Arrays.stream(DataSourceTypeEnum.values())
                .map(e -> new Dict()
                        .set("label", e.getDesc())
                        .set("value", e.getValue()))
                .collect(Collectors.toList());
        return R.ok(list);
    }


    @Log
    @ApiOperation("分页获取")
    @GetMapping("/page")
    public R<Page<DataSourceStructure>> page(Page<DataSourceStructure> page, DataSourceStructure dataSourceStructure) {
        page = dataSourceStructureService.page(page, new LambdaQueryWrapper<DataSourceStructure>()
                .like(StrUtil.isNotBlank(dataSourceStructure.getTableNameDesc()), DataSourceStructure::getTableNameDesc, dataSourceStructure.getTableNameDesc()));
        if (page.getTotal() == 0) {
            return R.ok(page);
        }
        List<String> ids = page.getRecords().stream().map(DataSourceStructure::getDataSourceId).distinct().collect(Collectors.toList());
        Map<String, DataSource> map = dataSourceService.listByIds(ids).stream().collect(Collectors.toMap(DataSource::getId, Function.identity()));
        page.getRecords().stream().peek(e -> {
            DataSource dataSource = map.get(e.getDataSourceId());
            e.setLibraryName(dataSource.getSourceName()).setDataSourceTypeEnum(dataSource.getSourceType());
        }).collect(Collectors.toList());
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("2、根据树结构id和类型,获取表树形结构")
    @GetMapping("/select/structure/{dataSourceId}/{sourceType}")
    public R<List<DataSourceStructure>> getSelectStructure(@PathVariable String dataSourceId, @PathVariable DataSourceTypeEnum sourceType) {
        List<DataSourceStructure> tableNames = (List<DataSourceStructure>) interfaceMap.get(sourceType.value).getTableNames(dataSourceId).stream().sorted(Comparator.comparing(DataSourceStructure::getName)).collect(Collectors.toList());
        return R.ok(tableNames);
    }

    @Log(back = false)
    @ApiOperation("3、获取数据源表结构")
    @GetMapping("/select/structure/table/{sourceType}")
    public R<List<DataSourceStructure.Structure>> getStructure(@PathVariable("sourceType") DataSourceTypeEnum sourceType, DataSourceStructure dataSourceStructure) {
        List<DataSourceStructure.Structure> tableStructure = interfaceMap.get(sourceType.value).getTableStructure(dataSourceStructure);
        return R.ok(tableStructure);
    }

    @Log(back = false)
    @ApiOperation("3.获取数据源表结构-通过字段类型分组")
    @GetMapping("/select/structure/table/group/{sourceType}")
    public R<Map> getStructureGroup(@PathVariable("sourceType") DataSourceTypeEnum sourceType, DataSourceStructure dataSourceStructure) {
        List<DataSourceStructure.Structure> tableStructure = interfaceMap.get(sourceType.value).getTableStructure(dataSourceStructure);
        Map<String, List<DataSourceStructure.Structure>> map = tableStructure.stream().collect(Collectors.groupingBy(e -> e.getDataFieldTypeClassify().name(), Collectors.toList()));
        return R.ok(map);
    }

    @Log
    @ApiOperation("删除数据源表结构")
    @DeleteMapping("/structure/{id}")
    public R<Boolean> delete(@PathVariable("id") String id) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(id);
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        interfaceMap.get(dataSource.getSourceType().value).removeDataSourceStructure(id);
        return R.ok(Boolean.TRUE);
    }

    @Log
    @ApiOperation("删除数据源")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteStructure(@PathVariable("id") String id) {
        DataSource dataSource = dataSourceService.getById(id);
        if (dataSource.getSourceType().equals(DataSourceTypeEnum.mqttDataSource)) {
            if (mqttService.checkDataSourceUse(id)) {
                return R.failed("目前数据源正在使用中,禁止修改请禁用数据集后重试!");
            }
        }
        if (dataSource.getSourceType().equals(DataSourceTypeEnum.apiDataSource)) {
            if (apiService.checkDataSourceUse(id)) {
                return R.failed("目前数据源正在使用中,禁止修改请禁用数据集后重试!");
            }
        }
        interfaceMap.get(dataSource.getSourceType().value).removeDataSource(id);
        return R.ok(Boolean.TRUE);
    }
}
