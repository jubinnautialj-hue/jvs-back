package cn.bctools.design.data.controller;

import cn.bctools.ai.api.JvsAiDatasetApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.config.DesignConfig;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.crud.mapper.FormMapper;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataChangePo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.*;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.container.StepFieldHandler;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.*;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.project.mapper.JvsAppMapper;
import cn.bctools.design.project.service.JvsAppLogService;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Api(tags = "[data]动态数据-根据标识获取数据")
@RestController
@AllArgsConstructor
@RequestMapping("/app/identification/use/dynamic/data")
public class DynamicAppIdentificationUseController {
    /**
     * The Data field dynamic service.
     */
    DataFieldDynamicService dataFieldDynamicService;
    /**
     * The Rule run service.
     */
    RuleRunService ruleRunService;
    DesignConfig designConfig;
    JvsAiDatasetApi jvsAiDatasetApi;
    /**
     * The Data id service.
     */
    DataIdService dataIdService;
    /**
     * The Data log service.
     */
    DataLogService dataLogService;
    /**
     * The Data event service.
     */
    DataEventService dataEventService;
    /**
     * The Data field service.
     */
    DataFieldService dataFieldService;
    JvsAppMapper appMapper;
    /**
     * The Form mapper.
     */
    FormMapper formMapper;
    /**
     * The Data model service.
     */
    DataModelService dataModelService;
    /**
     * The Data field handler.
     */
    DataFieldHandler dataFieldHandler;
    /**
     * The Table form field handler.
     */
    TableFormFieldHandler tableFormFieldHandler;
    /**
     * The Tab field handler.
     */
    TabFieldHandler tabFieldHandler;
    /**
     * The Step field handler.
     */
    StepFieldHandler stepFieldHandler;
    /**
     * The Auth user service api.
     */
    AuthUserServiceApi authUserServiceApi;
    /**
     * The Data field handler.
     */
    Map<String, IDataFieldHandler> iDataFieldHandler;
    /**
     * The Jvs app log service.
     */
    JvsAppLogService jvsAppLogService;
    /**
     * The Design handler.
     */
    DesignHandler designHandler;
    /**
     * The Data model handler.
     */
    DataModelHandler dataModelHandler;
    DynamicDataService dynamicDataService;
    Map<String, IDataFieldHandler> fieldHandlerMap;
    IdentificationService identificationService;

    @Log
    @ApiOperation("分页查询数据")
    @PostMapping("/query/page/{modelIdentification}")
    public R queryPage(@PathVariable("modelIdentification") String modelIdentification, @RequestBody QueryPageDto queryPageDto) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        String appId = byIdentifierApp.getJvsAppId();
        Page<DynamicDataPo> page = new Page<>(queryPageDto.getCurrent(), queryPageDto.getSize());
        String modelId = byIdentifierApp.getDesignId();
        Set<String> queryField = new LinkedHashSet<>();
        //将查询条件的字段添加进去
        if (ObjectNull.isNotNull(queryPageDto.getFieldList())) {
            queryField.addAll(queryPageDto.getFieldList());
        }
        List<QueryConditionDto> queryConditions = new ArrayList<>();
        if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
            queryConditions = queryPageDto.getConditions().stream().filter(e -> {
                //过滤掉为空的数据
                if (e.getEnabledQueryTypes().equals(DataQueryType.between)) {
                    return ObjectNull.isNotNull(e.getValue());
                }
                if (!e.getEnabledQueryTypes().equals(DataQueryType.isNull) && ObjectNull.isNull(e.getValue())) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
        }
        List<List<QueryConditionDto>> queryGroupConditions = new ArrayList<>();
        if (ObjectNull.isNull(queryConditions)) {
            if (CollectionUtils.isNotEmpty(queryPageDto.getGroupConditions())) {
                queryGroupConditions = queryPageDto.getGroupConditions();
            } else {
                if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
                    queryGroupConditions = Collections.singletonList(queryPageDto.getConditions());
                }
            }
        } else {
            if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
                queryConditions.addAll(queryPageDto.getConditions());
                queryConditions = queryConditions.stream().distinct().collect(Collectors.toList());
            }
            queryGroupConditions = Collections.singletonList(queryConditions);
        }
        Map<String, FieldBasicsHtml> collectMap = dataFieldService.getAllField(appId, modelId, true, true, e -> false).stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));
        List<String> collect = new ArrayList<>(collectMap.keySet());
        Set<String> stringSet = new HashSet<>();
        Page<Map<String, Object>> pageResult = dynamicDataService.queryPage(appId, page, modelId, new HashMap<>(8), queryGroupConditions, queryPageDto.getSorts(), collect, true, true, ObjectNull.isNull(queryPageDto.getKeywords()), new ArrayList<>(collectMap.values()), stringSet);
        return R.ok(pageResult);
    }

    /**
     * 新增数据
     *
     * @param data 数据内容
     * @return 保存后的数据id r
     */
    @Log
    @ApiOperation("新增数据")
    @PostMapping("/save/{modelIdentification}")
    public R saveDynamicData(@PathVariable("modelIdentification") String modelIdentification, @RequestBody Map<String, Object> data) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        DynamicDataUtils.checkPermit();
        DynamicDataUtils.clearEcho(data);
        dynamicDataService.checkDataModel(data, byIdentifierApp.getDesignId());
        RuleExecuteDto executeDto = dynamicDataService.save(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), data);
        return R.ok().setMsg("保存成功");
    }

    @Log
    @ApiOperation("删除数据")
    @DeleteMapping("/delete/{modelIdentification}/{dataId}")
    public R deleteDynamicData(@PathVariable("modelIdentification") String modelIdentification, @PathVariable("dataId") String dataId) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        Map<String, Object> data = dynamicDataService.querySingle(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId);
        RuleExecuteDto executeDto = dynamicDataService.remove(byIdentifierApp.getDesignId(), dataId);
        return R.ok().setMsg("删除成功");
    }

    @Log
    @ApiOperation("批量删除数据")
    @PostMapping("/batch/delete/{modelIdentification}")
    public R batchDeleteDynamicData(@PathVariable("modelIdentification") String modelIdentification, @RequestBody List<String> ids) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        List<Map> maps = dynamicDataService.getByIds(byIdentifierApp.getDesignId(), ids);
        for (Map<String, Object> map : maps) {
            String id = String.valueOf(map.get("id"));
            dynamicDataService.remove(byIdentifierApp.getDesignId(), id);
        }
        return R.ok();
    }

    @Log
    @ApiOperation("修改数据")
    @PostMapping("/update/{modelIdentification}/{dataId}")
    public R updateDynamicData(@PathVariable("modelIdentification") String modelIdentification, @PathVariable("dataId") String dataId, @RequestBody Map<String, Object> data) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);

        DynamicDataUtils.checkPermit();
        DynamicDataUtils.clearEcho(data);
        String designId = DynamicDataUtils.getDesignId();
        Map<String, FieldBasicsHtml> fieldsMap =
                dataFieldService.getDesignIdFields(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), designId, true, true).stream().filter(e -> ObjectNull.isNotNull(e.getType())).collect(Collectors.toMap((FieldBasicsHtml::getFieldKey),
                        Function.identity(), (e, e2) -> e));
        //将字段判断是否存在树形关系，并避免回显死循环
        AtomicReference<QueryConditionDto> treeQuery = new AtomicReference<>();
        for (FieldBasicsHtml e : fieldsMap.values()) {
            IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(e.getType().getDesc());
            // 没有找到实现类，直接跳过
            if (ObjectNull.isNull(iDataFieldHandler)) {
                continue;
            }
            //判断为空，跳过，目前不知道为什么，偶然试出来的
            if (e.getDesignJson() == null) {
                continue;
            }
            FieldPublicHtml publicHtml = iDataFieldHandler.toHtml(e);
            //如果key不为空,并提交值为空,并允许值为空才设置为空占位
            boolean setEmpty = ObjectNull.isNotNull(publicHtml.getProp()) && Boolean.TRUE.equals(publicHtml.getEmptyEnable()) && ObjectNull.isNull(data.get(publicHtml.getProp()));
            if (setEmpty) {
                //处理分割线和小标题，不属于数据
                if (ObjectNull.isNotNull(publicHtml.getType().getAClass()) && !publicHtml.getType().equals(DataFieldType.tab) && !publicHtml.getType().equals(DataFieldType.cascader)) {
                    data.put(publicHtml.getProp(), DynamicDataConstant.getEmpty(publicHtml.getType().getAClass()));
                }
            }
            if (DataFieldType.select.equals(publicHtml.getType()) || DataFieldType.cascader.equals(publicHtml.getType())) {
                MultipleHtml html = (MultipleHtml) fieldHandlerMap.get(publicHtml.getType().getDesc()).toHtml(publicHtml.getDesignJson());
                if (ObjectNull.isNotNull(html)) {
                    if (byIdentifierApp.getDesignId().equals(html.getFormId()) && html.getDatatype().equals(FormDataTypeEnum.dataModel)) {
                        //树形结构，将对应的字段
                        if (ObjectNull.isNotNull(html.getProps().getSecTitle())) {
                            treeQuery.set(new QueryConditionDto().setFieldKey(html.getProps().getSecTitle()).setValue(null).setEnabledQueryTypes(DataQueryType.eq));
                        }
                    }
                }
            }
        }
        if (ObjectNull.isNotNull(treeQuery.get())) {
            //获取当前这条数据的 id
            String id = data.get("id").toString();
            //寻找当前这条数据的下级
            QueryConditionDto queryConditionDto = treeQuery.get();
            ArrayList<String> fields = new ArrayList<>();
            fields.add(queryConditionDto.getFieldKey());
            queryConditionDto.setValue(id);
            List<Map<String, Object>> mapList = dynamicDataService.queryList(byIdentifierApp.getDesignId(), fields, queryConditionDto);
            if (ObjectNull.isNotNull(mapList)) {
                //表示为树形关系
            }
        }
        Map<String, Object> oldData = dynamicDataService.querySingle(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId);
        dynamicDataService.clearSensitiveBody(byIdentifierApp.getDesignId(), data, oldData);

        // 修改数据
        RuleExecuteDto executeDto = dynamicDataService.update(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId, data);
        if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
            if (!executeDto.getStats()) {
                //成功直接返回
                return R.failed(executeDto.getSyncMessageTips());
            }
            return R.ok().setMsg(executeDto.getSyncMessageTips());
        }
        MapDifference<String, Object> difference = Maps.difference(oldData, data);
        List<DataChangePo> dataChange = null;
        if (!difference.areEqual()) {
            dataChange = dynamicDataService.saveDataChange(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), oldData, difference, data);
        }
        return R.ok().setMsg("修改成功");
    }

    @ApiOperation("查询所有数据")
    @GetMapping("/query/list/{modelIdentification}")
    public R<List<Map<String, Object>>> queryList(@PathVariable("modelIdentification") String modelIdentification, @ApiParam(name = "需要查询的字段", required = true) @RequestParam("fieldKey") String fieldKey) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);

        DataModelPo one = dataModelService.getOne(Wrappers.query(new DataModelPo().setAppId(byIdentifierApp.getJvsAppId()).setId(byIdentifierApp.getDesignId())));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        //判断模型
        DynamicDataUtils.dataModelScope(byIdentifierApp.getDesignId());
        List<String> fieldKey1 = null;
        try {
            fieldKey1 = JSONArray.parseArray(URLDecoder.decode(fieldKey, "UTF-8"), String.class);
            //兼容2.1.4的关联数据
        } catch (Exception e) {
            fieldKey1 = new ArrayList<String>() {{
                add(fieldKey);
            }};
        }
        List<Map<String, Object>> result = dynamicDataService.queryList(byIdentifierApp.getDesignId(), fieldKey1);
        //如果不为空,转树形
        return R.ok(result);
    }

    @ApiOperation("post查询所有数据")
    @PostMapping("/query/list/{modelIdentification}")
    @Transactional(rollbackFor = Exception.class)
    public R<List<Map<String, Object>>> postQueryList(@PathVariable("modelIdentification") String modelIdentification, @RequestBody QueryListDto queryPageDto) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        if (ObjectNull.isNull(queryPageDto.getFieldList())) {
            List<String> fieldKeys = dataFieldService.getFieldKeys(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId());
            queryPageDto.setFieldList(fieldKeys);
        }
        List<Map<String, Object>> data = dynamicDataService.postQueryList(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), queryPageDto);
        // 数据项禁用
        dynamicDataService.enableDataItem(data, queryPageDto.getEnableConditions());
        return R.ok(data);
    }

    @Log
    @ApiOperation("查询单条转换后的数据")
    @GetMapping("/query/single/transformation/{modelIdentification}/{dataId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> querySinglePrint(@PathVariable("modelIdentification") String modelIdentification, @PathVariable("dataId") String dataId,
                                                   @RequestParam(value = "override", required = false, defaultValue = "false") Boolean override) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        //设置数据权限
        DynamicDataUtils.dataModelScope(byIdentifierApp.getDesignId());
        //查询单条数据，并处理数据权限，并添加默认按钮,不做转换
        Map<String, Object> data = dynamicDataService.querySingle(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId, true);
        List<FieldBasicsHtml> allField = dataFieldService.getAllField(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), true, true);
        data = dynamicDataService.echo(data, allField, override);
        DataModelPo byId = dataModelService.getById(dataId);
        //处理表单中的脱敏
        dynamicDataService.encryptionData(data, byId);
        return R.ok(data);
    }

    @Log
    @ApiOperation("查询单条数据")
    @GetMapping("/query/single/{modelIdentification}/{dataId}")
    public R<Map<String, Object>> querySingle(@PathVariable("modelIdentification") String modelIdentification, @PathVariable("dataId") String dataId) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);

        //设置数据权限
        DynamicDataUtils.dataModelScope(byIdentifierApp.getDesignId());
        //查询单条数据，并处理数据权限，并添加默认按钮,不做转换
        Map<String, Object> data = dynamicDataService.querySingle(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId, true);
        //做数据转换操作
        List<FieldBasicsHtml> fields = dataFieldService.getFields(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), null, true, false);
        data = dynamicDataService.echo(data, fields, false);
        DataModelPo byId = dataModelService.getById(byIdentifierApp.getDesignId());
        //处理表单中的脱敏
        dynamicDataService.encryptionData(data, byId);
        // 扩展返回数据
        dynamicDataService.expandOtherData(data);
        return R.ok(data);
    }

    @ApiOperation("查询单个字段的值")
    @GetMapping("/query/field/{modelId}/{dataId}/{fieldId}")
    public R<Object> queryField(@PathVariable("modelIdentification") String modelIdentification, @PathVariable("dataId") String dataId, @PathVariable("fieldId") String fieldId) {
        Identification byIdentifierApp = identificationService.getByIdentifierApp(modelIdentification);
        DynamicDataUtils.dataModelScope(byIdentifierApp.getDesignId());
        return R.ok(dynamicDataService.queryField(byIdentifierApp.getJvsAppId(), byIdentifierApp.getDesignId(), dataId, fieldId));
    }


}
