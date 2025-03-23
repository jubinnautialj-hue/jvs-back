package cn.bctools.data.factory.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.annotation.IgnoreTenant;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.JvsDataFactoryDto;
import cn.bctools.data.factory.api.dto.RequestDetailDto;
import cn.bctools.data.factory.api.dto.RowWhereDto;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataFactorySqlViewDto;
import cn.bctools.data.factory.entity.*;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.html.FHtmlGraph;
import cn.bctools.data.factory.html.FNodeType;
import cn.bctools.data.factory.html.node.params.FilterParams;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.query.po.DataSourceLinkInPo;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import cn.bctools.data.factory.service.JvsDataFactoryMenuService;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author xiaohui
 */
@Slf4j
@Component
@Api(tags = "数据仓库的数据获取")
@RestController
@AllArgsConstructor
public class DataFactoryController implements DataFactoryApi {

    private final JvsDataFactoryService jvsDataFactoryService;
    private final JvsDataFactoryOutService jvsDataFactoryOutService;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final DataSourceStructureService dataSourceStructureService;
    private final JvsDataFactoryMenuService jvsDataFactoryMenuService;
    private final DataSourceService dataSourceService;


    @Override
    public R<List> getTableNames() {
        //根据应用ID查询模型集
        List<DataSourceStructure> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getEnable, Boolean.TRUE))
                .stream()
                .map(e -> new DataSourceStructure()
                        .setId(e.getId())
                        .setName(e.getName())
                        .setExecuteName(e.getId())
                        .setTableNameDesc(e.getName()))
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @Override
    @ApiOperation("获取有输出的集合")
    public R<List<Dict>> getAll(String name) {
        Set<String> ids = jvsDataFactoryOutService.list(new LambdaQueryWrapper<JvsDataFactoryOut>().select(JvsDataFactoryOut::getDataId).groupBy(JvsDataFactoryOut::getDataId)).stream().map(JvsDataFactoryOut::getDataId).collect(Collectors.toSet());
        if (ObjectNull.isNull(ids)) {
            return R.ok();
        }
        //只包括启用的
        List<Dict> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getEnable, Boolean.TRUE).in(JvsDataFactory::getId, ids).like(StrUtil.isNotEmpty(name), JvsDataFactory::getName, name))
                .parallelStream()
                .map(e -> Dict.create().set("label", e.getName()).set("value", e.getId()))
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @Override
    public R<Integer> size() {
        TenantContextHolder.clear();
        Long count = jvsDataFactoryService.count();
        return R.ok(count.intValue());
    }

    @Override
    public R<RowWhereDto> getAuthRow(String dataId) {
        RowWhereDto javaObject = JSONObject.parseObject(JSONObject.toJSONString(jvsDataFactoryService.getRowWhere(dataId)), RowWhereDto.class);
        return R.ok(javaObject);
    }

    @Override
    @ApiOperation("通过设计id获取输出结构")
    public R<List<JSONObject>> getTable(String dataId) {
        if (StrUtil.isBlank(dataId)) {
            return R.ok();
        }
        JvsDataFactoryOut factoryOut = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataId).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        if (ObjectUtil.isNull(factoryOut)) {
            return R.ok(new ArrayList<>());
        }
        return R.ok(factoryOut.getFields());
    }

    @Override
    public R<String> getTableName(String dataFactoryId) {
        //验证数据集是否存在
        JvsDataFactory byId = jvsDataFactoryService.getById(dataFactoryId);
        if (byId == null) {
            return R.failed("此数据集未找到,请检查");
        }
        String sql = null;
        switch (byId.getTaskType()) {
            case sql:
                if (!byId.getEnable()) {
                    return R.failed("此数据集未启用");
                }
                sql = "(" + JSONObject.parseObject(byId.getViewJson(), DataFactorySqlViewDto.class).getSql() + ")";
                break;
            case ordinary:
                JvsDataFactoryOut one = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataFactoryId).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
                if (ObjUtil.isNull(one)) {
                    return R.failed("此数据集输出结果未找到,请重新执行");
                }
                boolean ifNotExistsTable = dorisJdbcTemplate.ifNotExistsTable(one.getDocumentName());
                if (!ifNotExistsTable) {
                    return R.failed("此数据集输出结果未找到,请重新执行");
                }
                sql = "( select * from " + one.getDocumentName() + ")";
                break;
            default:
        }
        sql = sql + " as `" + byId.getId() + "`";
        return R.ok(sql);
    }

    @Override
    public R<List<SourceFactoryGetIdDto>> getSourceFactory(List<String> ids) {
        //获取数据
        List<JvsDataFactory> list = jvsDataFactoryService.listByIds(ids);
        if (list.size() != ids.stream().distinct().count()) {
            throw new BusinessException("部分数据集未找到");
        }
        List<SourceFactoryGetIdDto> factoryGetIdDtos = list.stream().filter(e -> e.getTaskType().equals(TaskTypeEnum.ordinary)).flatMap(e -> {
            FHtmlGraph fHtmlGraph = JSONObject.parseObject(e.getViewJson(), FHtmlGraph.class);
            //获取输入源
            List<SourceFactoryGetIdDto> dtos = fHtmlGraph.getNodes().stream().filter(v -> v.getType().equals(FNodeType.Input))
                    .map(v -> {
                        InputParams inputParams = JSONObject.parseObject(JSONObject.toJSONString(v.getSourceData()), InputParams.class);
                        SourceFactoryGetIdDto sourceFactoryGetIdDto = new SourceFactoryGetIdDto();
                        sourceFactoryGetIdDto.setIsDataSource(Boolean.TRUE);
                        String id = inputParams.getFromSource().getId();
                        DataSourceStructure byId = dataSourceStructureService.getById(id);
                        if (byId == null) {
                            throw new BusinessException("此数据集的输入源未找到");
                        }
                        if (inputParams.getFromSource().getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource.getValue())) {
                            //获取数据集id
                            id = byId.getExecuteName();
                            sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                            sourceFactoryGetIdDto.setId(id);
                        } else {
                            //获取数据源id
                            String dataSourceId = byId.getDataSourceId();
                            sourceFactoryGetIdDto.setId(dataSourceId);
                        }
                        return sourceFactoryGetIdDto;
                    }).collect(Collectors.toList());
            //获取过滤节点存在不在其他数据源中的数据
            List<SourceFactoryGetIdDto> getIdDtos = fHtmlGraph.getNodes().stream().filter(v -> v.getType().equals(FNodeType.dataFilter))
                    .flatMap(v -> {
                        FilterParams filterParams = JSONObject.parseObject(JSONObject.toJSONString(v.getSourceData()), FilterParams.class);
                        return filterParams.getRuleObj().getNodeTwigs().stream().filter(x -> x.getMethod().equals(QueryEnums.dataSourceLikeIn) || x.getMethod().equals(QueryEnums.dataSourceNotLikeIn))
                                .map(x -> {
                                    DataSourceLinkInPo dataSourceLinkInPo = JSONObject.parseObject(x.getMethodValue(), DataSourceLinkInPo.class);
                                    String id = dataSourceLinkInPo.getId();
                                    SourceFactoryGetIdDto sourceFactoryGetIdDto = new SourceFactoryGetIdDto();
                                    sourceFactoryGetIdDto.setId(id);
                                    sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                                    return sourceFactoryGetIdDto;
                                });
                    }).collect(Collectors.toList());
            dtos.addAll(getIdDtos);
            //前置与后置
            List<SourceFactoryGetIdDto> prefixTask = e.getPrefixTaskId().stream().map(x -> {
                SourceFactoryGetIdDto sourceFactoryGetIdDto = new SourceFactoryGetIdDto();
                sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                sourceFactoryGetIdDto.setId(x);
                return sourceFactoryGetIdDto;
            }).collect(Collectors.toList());
            List<SourceFactoryGetIdDto> rearTask = e.getRearTaskId().stream().map(x -> {
                SourceFactoryGetIdDto sourceFactoryGetIdDto = new SourceFactoryGetIdDto();
                sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                sourceFactoryGetIdDto.setId(x);
                return sourceFactoryGetIdDto;
            }).collect(Collectors.toList());
            dtos.addAll(prefixTask);
            dtos.addAll(rearTask);
            return dtos.stream();
        }).collect(Collectors.toList());
        //获取当前数据集与数据源的绑定关系
        dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().in(DataSourceStructure::getExecuteName, ids))
                .forEach(e -> factoryGetIdDtos.add(new SourceFactoryGetIdDto().setIsDataSource(Boolean.TRUE).setId(e.getDataSourceId())));
        //判断是否存在数据集 如果存在需要 递归遍历
        List<String> listId = factoryGetIdDtos.stream().filter(e -> !e.getIsDataSource()).map(SourceFactoryGetIdDto::getId).collect(Collectors.toList());
        if (!listId.isEmpty()) {
            List<SourceFactoryGetIdDto> data = getSourceFactory(listId).getData();
            factoryGetIdDtos.addAll(data);
        }
        //需要区分不同类型数据集
        List<JvsDataFactory> factoryList = list.stream().filter(e -> !e.getTaskType().equals(TaskTypeEnum.ordinary)).collect(Collectors.toList());
        if (!factoryList.isEmpty()) {
            jvsDataFactoryService.getTimeTaskDataSourceId(factoryList).forEach(e -> {
                SourceFactoryGetIdDto sourceFactoryGetIdDto = new SourceFactoryGetIdDto();
                sourceFactoryGetIdDto.setIsDataSource(Boolean.TRUE);
                sourceFactoryGetIdDto.setId(e);
                factoryGetIdDtos.add(sourceFactoryGetIdDto);
            });
        }
        return R.ok(factoryGetIdDtos);
    }

    @Override
    public R<Long> getDataCount(String dataFactoryId) {
        //这里应该有个排序 然后获取最新的数据
        JvsDataFactoryOut factoryOut = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataFactoryId)
                .orderByDesc(JvsDataFactoryOut::getCreateTime)
                .last("limit 1"));
        if (ObjectUtil.isNull(factoryOut)) {
            return R.ok(0L);
        }
        long count = dorisJdbcTemplate.getCount(factoryOut.getDocumentName());
        return R.ok(count);
    }

    @Override
    @IgnoreTenant
    @ApiOperation("通过设计id获取输出结果")
    public R getData(RequestDetailDto requestDetailDto) {
        ConsanguinityAnalyse consanguinityAnalyse = BeanCopyUtil.copy(requestDetailDto, ConsanguinityAnalyse.class);
        Page<Map<String, Object>> data = jvsDataFactoryOutService.getData(requestDetailDto.getDataFactoryId(), requestDetailDto.getSize(), requestDetailDto.getCurrent(), consanguinityAnalyse);
        return R.ok(data);
    }

    @IgnoreTenant
    @Override
    public R page(String dataId, Long current, Long size) {
        JvsDataFactoryOut one = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataId).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        Page<Map<String, Object>> mapPage = new Page<>(current, size);
        if (one == null) {
            mapPage.setTotal(0).setRecords(new ArrayList<>());
            return R.ok(mapPage);
        }
        long total = dorisJdbcTemplate.getCount(one.getDocumentName());
        List<Map<String, Object>> list = dorisJdbcTemplate.page(size, current, one.getDocumentName());
        mapPage.setRecords(list);
        mapPage.setTotal(total);
        return R.ok(mapPage);
    }

    @Override
    public R<Boolean> up(JSONObject data, String menuName) {
        UserDto userDto = data.getJSONObject("userDto").toJavaObject(UserDto.class);
        String menuId = menuName;
        //通过menuName 确认是否为id
        JvsDataFactoryMenu dataFactoryMenu = jvsDataFactoryMenuService.getById(menuName);
        String tenantId = TenantContextHolder.getTenantId();
        if (dataFactoryMenu == null) {
            JvsDataFactoryMenu jvsDataFactoryMenu = new JvsDataFactoryMenu()
                    .setName(menuName);
            jvsDataFactoryMenu.setUpdateBy(userDto.getRealName());
            jvsDataFactoryMenu.setCreateBy(userDto.getRealName());
            jvsDataFactoryMenu.setCreateById(userDto.getId());
            //获取数量
            TenantContextHolder.clear();
            long i = jvsDataFactoryMenuService.count() + 1;
            TenantContextHolder.setTenantId(tenantId);
            jvsDataFactoryMenu.setSort(i);
            jvsDataFactoryMenuService.save(jvsDataFactoryMenu);
            menuId = jvsDataFactoryMenu.getId();
        }
        TenantContextHolder.clear();
        for (Object object : data.getJSONArray("data")) {
            JvsDataFactory dataFactory = JSONObject.parseObject(JSONObject.toJSONString(object), JvsDataFactory.class);
            dataFactory.setTenantId(tenantId);
            dataFactory.setEnable(false);
            dataFactory.setCreateBy(userDto.getRealName());
            dataFactory.setCreateById(userDto.getId());
            dataFactory.setCreateTime(null);
            dataFactory.setType(menuId);
            dataFactory.setUpdateBy(userDto.getRealName());
            dataFactory.setUpdateTime(null);
            dataFactory.setRowOtherUserVisible(null);
            dataFactory.setColumnOtherUserVisible(null);
            JvsDataFactory byId = jvsDataFactoryService.upGetOrDataRecover(dataFactory.getId());
            if (dataFactory.getTask() != null) {
                if (byId == null || byId.getTask() == null) {
                    TaskCronDto taskCronDto = new TaskCronDto().setId(null);
                    dataFactory.getTask().setId(taskCronDto.getId());
                }
            }
            if (byId == null) {
                jvsDataFactoryService.save(dataFactory);
            } else {
                jvsDataFactoryService.updateById(dataFactory);
            }
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<List<JSONObject>> getByIds(List<String> ids) {
        List<JSONObject> list = jvsDataFactoryService.listByIds(ids)
                .stream()
                .map(e -> JSONObject.parseObject(JSONObject.toJSONString(e)))
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @IgnoreTenant
    @Override
    public R<JvsDataFactoryDto> getById(String id) {
        JvsDataFactory factory = jvsDataFactoryService.getById(id);
        JvsDataFactoryDto copy = BeanCopyUtil.copy(factory, JvsDataFactoryDto.class);
        //查询输出结果
        JvsDataFactoryOut one = jvsDataFactoryOutService.getOne(Wrappers.lambdaQuery(JvsDataFactoryOut.class).eq(JvsDataFactoryOut::getDataId, id));
        copy.setCollectionId(one.getDocumentName());
        return R.ok(copy);
    }

    @Override
    public R<String> getDocumentName(String id) {
        JvsDataFactoryOut factoryOut = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, id).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        if (factoryOut == null) {
            throw new BusinessException("未找到输出数据");
        }
        return R.ok(factoryOut.getDocumentName());
    }
}
