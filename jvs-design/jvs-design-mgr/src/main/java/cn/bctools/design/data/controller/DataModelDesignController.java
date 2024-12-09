package cn.bctools.design.data.controller;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.auth.api.api.DictApi;
import cn.bctools.auth.api.dto.SysDictItemDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.crud.dto.AutoCreateCrudDesignDto;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.AutoCreateCrudDesignService;
import cn.bctools.design.crud.entity.IndexFields;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.*;
import cn.bctools.design.data.fields.dto.enums.DataConditionType;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.item.SelectItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[data]数据模型 design")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/data/model")
public class DataModelDesignController {

    DataModelService dataModelService;
    JvsAppService jvsAppService;
    CrudPageService crudPageService;
    FormService formService;
    DataFieldService dataFieldService;
    AuthRoleServiceApi roleServiceApi;
    DataModelHandler dataModelHandler;
    AutoCreateCrudDesignService autoCreateCrudDesignService;
    IdentificationService identificationService;

    /**
     * 查询所有模型
     *
     * @param appId 应用id
     * @return 模型基本数据集合
     */
    @ApiOperation("查询所有模型")
    @GetMapping("/all")
    public R<List<DataModelPo>> getAll(@PathVariable String appId) {
        // 通过appId查询数据模型
        List<DataModelPo> models = dataModelService.list(Wrappers.<DataModelPo>lambdaQuery()
                .select(DataModelPo::getId, DataModelPo::getAppId, DataModelPo::getName)
                .eq(DataModelPo::getAppId, appId));
        // 返回查询结果
        return R.ok(models);
    }

    @ApiOperation("删除模型")
    @DeleteMapping("/{id}")
    public R<List<DataModelPo>> delete(@PathVariable String appId, @PathVariable("id") String id) {
        dataModelService.remove(Wrappers.query(new DataModelPo().setAppId(appId).setId(id)));
        // 逻辑删除模型，不删除数据集，轻应用版本数据模型模式分库，开发模式删除了，测试模式模型不能直接删除可能会影响。
        return R.ok();
    }

    /**
     * 查询模型下所有可用于数据联动的字段
     *
     * @return 模型下所有字段
     */
    @ApiOperation("查询模型下所有可用于数据联动的字段")
    @GetMapping("/field/dataLinkage/{modelId}")
    public R<List> dataModelFieldAll(@PathVariable String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> allField = dataFieldService.getAllFieldDefault(appId, modelId, fieldBasicsHtml -> Objects.isNull(fieldBasicsHtml.getType()) || !fieldBasicsHtml.getType().isEnableDataLinkage());
        return R.ok(allField);
    }

    /**
     * 根据模型id查询是否
     *
     * @return 模型基本数据集合
     */
    @ApiOperation("查询模型下所有字段")
    @GetMapping("/list/{modelId}")
    public R<List> dataModel(@PathVariable String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> allField = dataFieldService.getAllField(appId, modelId);
        return R.ok(allField);
    }

    /**
     * 根据模型id查询是否
     *
     * @return 模型基本数据集合
     */
    @ApiOperation("查询模型下所有设计字段")
    @GetMapping("/design/fields/{modelId}")
    public R<List> dataModelDesignFields(@PathVariable String modelId, @PathVariable String appId) {
        return R.ok(dataModelService.getDesignField(appId, modelId));
    }

    /**
     * 分页查询模型
     *
     * @param appId 应用id
     * @param page  分页数据
     * @param dto   查询条件
     * @return 模型基本数据集合
     */
    @ApiOperation("分页查询模型")
    @GetMapping("/list")
    @Transactional(rollbackFor = Exception.class)
    public R<Page<DataModelDto>> getAll(Page<DataModelPo> page, DataModelPageReqDto dto, @PathVariable String appId) {
        return R.ok(dataModelService.findPage(page, dto, appId));
    }

    /**
     * 分页查询模型
     *
     * @param appId 应用id
     * @param page  分页数据
     * @return 模型基本数据集合
     */
    @ApiOperation("分页查询模型")
    @GetMapping("/page")
    @Transactional(rollbackFor = Exception.class)
    public R<Page<DataModelPo>> page(Page<DataModelPo> page, String name, @PathVariable String appId) {
        dataModelService.page(page, Wrappers.<DataModelPo>lambdaQuery()
                .eq(DataModelPo::getAppId, appId)
                .like(ObjectNull.isNotNull(name), DataModelPo::getName, name));
        return R.ok(page);
    }

    @ApiOperation("设置数据权限")
    @PostMapping("/data/filter/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Page<DataModelDto>> dataFilter(@PathVariable String modelId, @RequestBody List<JSONObject> role, @PathVariable String appId) {
        DataModelPo dataModelPo = dataModelService.getById(modelId);
        if (dataModelPo.getAppId().equals(appId)) {
            dataModelPo.setRole(role);
            dataModelService.updateById(dataModelPo);
        }
        return R.ok();
    }

    @ApiOperation("数据模型添加索引")
    @PostMapping("/indexField/{modelId}")
    public R<Page<DataModelDto>> indexField(@PathVariable String modelId, @PathVariable String appId, @RequestBody List<IndexFields> fields) {
        dataModelService.indexField(modelId, appId, fields);
        return R.ok();
    }

    @ApiOperation("获取这个模型的索引")
    @GetMapping("/indexField/{modelId}")
    public R<List<IndexFields>> getIndexField(@PathVariable String modelId, @PathVariable String appId) {
        List<IndexFields> indexFields = dataModelService.getById(modelId).getIndexFields();
        return R.ok(indexFields);
    }

    @ApiOperation("数据模型删除字段")
    @DeleteMapping("/{modelId}/{fieldKey}")
    @Transactional(rollbackFor = Exception.class)
    public R deleteField(@PathVariable String modelId, @PathVariable String fieldKey, @PathVariable String appId) {
        //校验字段是否被引用
        List<DataFieldPo> list = dataFieldService.list(Wrappers.<DataFieldPo>lambdaQuery()
                .select(DataFieldPo::getDesignId, DataFieldPo::getDataModelId, DataFieldPo::getDesignType, DataFieldPo::getFieldKey, DataFieldPo::getJvsAppId)
                .eq(DataFieldPo::getModelId, modelId)
                .eq(DataFieldPo::getFieldKey, fieldKey)
                .eq(DataFieldPo::getJvsAppId, appId));
        //判断设计里面是否还存在如果存在，则直接无法删除
        for (DataFieldPo dataFieldPo : list) {
            switch (dataFieldPo.getDesignType()) {
                case form:
                    List<FormPo> form = formService.list(new LambdaQueryWrapper<FormPo>().eq(FormPo::getJvsAppId, appId).like(FormPo::getViewJson, "\"" + dataFieldPo.getFieldKey() + "\""));
                    if (!form.isEmpty()) {
                        String collect = form.stream().map(FormPo::getName).collect(Collectors.joining(","));
                        throw new BusinessException("模型字段在使用中无法被删除", collect);
                    }
                    break;
                case page:
                    List<CrudPage> count = crudPageService.list(new LambdaQueryWrapper<CrudPage>().eq(CrudPage::getJvsAppId, appId).like(CrudPage::getViewJson, "\"" + dataFieldPo.getFieldKey() + "\""));
                    if (!count.isEmpty()) {
                        String collect = count.stream().map(CrudPage::getName).collect(Collectors.joining(","));
                        throw new BusinessException("模型字段在列表使用中无法被删除", collect);
                    }
                default:
            }
        }
        dataFieldService.remove(Wrappers.query(new DataFieldPo().setModelId(modelId).setJvsAppId(appId).setFieldKey(fieldKey)));
        //删除模型的，但不再删除真实字段，字段结构和类型必须完全保留避免重复导致的类型匹配失败
        return R.ok();
    }

    @ApiOperation("数据模型添加索引")
    @GetMapping("/data/filter/{modelId}")
    public R<List> dataFilter(@PathVariable String modelId, @PathVariable String appId) {
        DataModelPo dataModelPo = dataModelService.getById(modelId);
        if (ObjectNull.isNull(dataModelPo)) {
            return R.ok();
        }
        if (dataModelPo.getAppId().equals(appId)) {
            return R.ok(dataModelPo.getRole());
        }
        return R.ok();

    }

    /**
     * 获取列表页自定义数据权限类型枚举字下拉值
     *
     * @param modelId 数据模型
     * @param appId   应用 id
     * @return
     */
    @GetMapping("/DataConditionType/{modelId}")
    @ApiOperation("数据权限的自定义枚举类型")
    public R<List> dataConditionType(@PathVariable String modelId, @PathVariable String appId) {
        //根据模型获取字段，根据类型获取选择类型
        List<Object> collect = dataFieldService.getFields(appId, modelId, true, true)
                .stream()
                .filter(e -> ObjectNull.isNotNull(e.getType()) && ObjectNull.isNotNull(e.getFieldName()))
                .map(e -> {
                    DataConditionTypeDto dataConditionTypeDto = new DataConditionTypeDto();
                    switch (e.getType()) {
                        case inputNumber:
                            FieldBasicsHtml inputNumber = new FieldBasicsHtml();
                            inputNumber.setFieldKey(e.getFieldKey()).setFieldName(e.getFieldName()).setType(e.getType());
                            dataConditionTypeDto.setFieldDto(inputNumber).setTypes(DataQueryType.eq, DataQueryType.ne, DataQueryType.ge, DataQueryType.gt, DataQueryType.le, DataQueryType.lt);
                            return dataConditionTypeDto;
                        //单选、多选、
                        case radio:
                        case select:
                            List<DataConditionTypeDto.Values> list = get(e);
                            if (Objects.isNull(list)) {
                                return null;
                            }
                            FieldBasicsHtml fieldBasicsHtml = new FieldBasicsHtml();
                            fieldBasicsHtml.setFieldKey(e.getFieldKey()).setFieldName(e.getFieldName()).setType(e.getType());
                            return dataConditionTypeDto.setFieldDto(fieldBasicsHtml).setTypes(DataQueryType.eq, DataQueryType.ne, DataQueryType.in).setValues(list);
                        default:
                            break;
                    }
                    FieldBasicsHtml fieldBasicsHtml = new FieldBasicsHtml();
                    fieldBasicsHtml.setFieldKey(e.getFieldKey()).setFieldName(e.getFieldName()).setType(e.getType());
                    dataConditionTypeDto.setFieldDto(fieldBasicsHtml).setTypes(DataQueryType.eq, DataQueryType.ne, DataQueryType.in);
                    List<DataConditionTypeDto.Values> values =
                            Arrays.stream(DataConditionType.values())
                                    .map(s -> new DataConditionTypeDto.Values().setName(s.name()).setValue(s.name())).collect(Collectors.toList());
                    dataConditionTypeDto.setValues(values);
                    return dataConditionTypeDto;
                }).filter(Objects::nonNull).collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 根据字段类型解析下拉数据值有哪些，不同的下拉或多选，值会不一致
     *
     * @param e 字段对象
     * @return
     */
    public List<DataConditionTypeDto.Values> get(FieldBasicsHtml e) {
        SelectItemHtml selectItem = BeanCopyUtil.copy(SelectItemHtml.class, e.getDesignJson());
        if (ObjectNull.isNull(selectItem)) {
            return null;
        }
        FormDataTypeEnum dataType = selectItem.getDatatype();
        //如果类型为空，则返回空
        if (ObjectNull.isNull(dataType)) {
            return null;
        }
        // 配置数据
        if (FormDataTypeEnum.option.equals(dataType)) {
            List<FormValueHtml> dicData = selectItem.getDicData();
            if (ObjectUtils.isEmpty(dicData)) {
                return null;
            }
            Map<String, String> map = dicData.stream().collect(Collectors.toMap(FormValueHtml::getValue, FormValueHtml::getLabel, (s1, s2) -> s1));
            List<DataConditionTypeDto.Values> valuesList = map.keySet().stream().map(s -> new DataConditionTypeDto.Values().setName(map.get(s)).setValue(s)).collect(Collectors.toList());
            return valuesList;
        }
        // 接口数据
        if (FormDataTypeEnum.dataModel.equals(dataType)) {
            //获取 关联的模型
            String fromId = selectItem.getFormId();
            DynamicDataService bean = SpringContextUtil.getBean(DynamicDataService.class);
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(selectItem.getProps().getLabel());
            //跳过数据权限.
            DynamicDataUtils.freePermit();
            List<Map<String, Object>> list = bean.queryList(fromId, arrayList);
            if (ObjectNull.isNull(list)) {
                return null;
            }
            //一定要有ID值
            Map<String, String> map = list.stream().filter(s -> s.containsKey("id")).collect(Collectors.toMap(s -> String.valueOf(s.get("id")), s -> String.valueOf(s.get(selectItem.getProps().getLabel())), (k1, k2) -> k1));
            List<DataConditionTypeDto.Values> valuesList = map.keySet().stream().map(s -> new DataConditionTypeDto.Values().setName(map.get(s)).setValue(s)).collect(Collectors.toList());
            return valuesList;
        }
        // 系统字典
        if (FormDataTypeEnum.system.equals(dataType)) {
            String dictKey = selectItem.getSystemDict();
            List<SysDictItemDto> dictItems = SpringContextUtil.getBean(DictApi.class).listItems(dictKey).getData();
            Map<String, String> map = dictItems.stream().collect(Collectors.toMap(SysDictItemDto::getValue, SysDictItemDto::getLabel, (s1, s2) -> s1));
            List<DataConditionTypeDto.Values> valuesList = map.keySet().stream().map(s -> new DataConditionTypeDto.Values().setName(s).setValue(map.get(s))).collect(Collectors.toList());
            return valuesList;
        }
        if (FormDataTypeEnum.rule.equals(dataType)) {
            return Arrays.stream(DataConditionType.values())
                    .map(s -> new DataConditionTypeDto.Values().setName(s.name()).setValue(s.name())).collect(Collectors.toList());
        }
        return null;
    }

    @ApiOperation("获取数据模型")
    @GetMapping("/detail/{modelId}")
    public R<DataModelPo> getModel(@PathVariable String modelId, @PathVariable String appId) {
        return R.ok(dataModelService.getOne(Wrappers.query(new DataModelPo().setAppId(appId).setId(modelId))));
    }

    @ApiOperation("创建模型")
    @PostMapping()
    @Transactional(rollbackFor = Exception.class)
    public R<String> create(@PathVariable String appId, @Validated @RequestBody SaveDataModelDto saveDataModel) {
        // 创建模型
        String modelId = dataModelService.create(appId, null, DesignType.data, saveDataModel.getName());
        dataModelService.updateEnableModelField(modelId, true);
        // 保存模型字段
        if (ObjectNull.isNull(saveDataModel.getFields())) {
            return R.ok();
        }
        List<DataFieldPo> dataFields = new ArrayList<>();
        long offset = 0;
        for (SaveDataModelDto.Field field : saveDataModel.getFields()) {
            DataFieldPo dataFieldPo = new DataFieldPo()
                    .setDesignId(modelId)
                    .setModelId(modelId)
                    .setFieldKey(field.getFieldKey())
                    .setFieldName(field.getFieldName())
                    .setDesignType(DesignType.data)
                    .setFieldType(field.getFieldType())
                    .setJvsAppId(appId);
            LocalDateTime time = LocalDateTime.now().plusSeconds(offset);
            dataFieldPo.setCreateTime(time);
            dataFieldPo.setUpdateTime(time);
            dataFields.add(dataFieldPo);
            offset++;
        }
        dataFieldService.saveFields(appId, dataFields, modelId);
        // 生成crud设计
        if (Boolean.TRUE.equals(saveDataModel.getGenerateCrudDesign())) {
            generateModelCrudDesign(modelId, saveDataModel.getMenuId());
        }
        return R.ok();
    }

    @ApiOperation("修改模型")
    @PutMapping()
    @Transactional(rollbackFor = Exception.class)
    public R<String> update(@PathVariable String appId, @Validated @RequestBody UpdateDataModelDto updateDataModel) {
        // 修改模型
        String modelId = updateDataModel.getId();
        DataModelPo dataModel = dataModelService.getOne(Wrappers.query(new DataModelPo().setId(modelId).setAppId(appId)));
        if (ObjectNull.isNull(dataModel)) {
            R.ok();
        }
        dataModel.setName(updateDataModel.getName());
        dataModelService.updateById(dataModel);
        // 修改自定义标识冗余的设计名称
        identificationService.updateDesignName(modelId, dataModel.getName());
        dataModelService.updateEnableModelField(modelId, true);
        // 修改模型字段
        List<DataFieldPo> dataFields = new ArrayList<>();
        long offset = 0;
        for (UpdateDataModelDto.Field field : updateDataModel.getFields()) {
            DataFieldPo dataFieldPo = new DataFieldPo()
                    .setFieldKey(field.getFieldKey())
                    .setFieldName(field.getFieldName())
                    .setFieldType(field.getFieldType());
            LocalDateTime time = LocalDateTime.now().plusSeconds(offset);
            dataFieldPo.setCreateTime(time);
            dataFieldPo.setUpdateTime(time);
            dataFields.add(dataFieldPo);
            offset++;
        }
        dataFieldService.updateFields(appId, modelId, DesignType.data, modelId, dataFields);
        return R.ok();
    }

    @ApiOperation("依据模型字段自动生成列表和表单设计")
    @PostMapping("/generate/crud/design/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> generateCrudDesign(@PathVariable String appId, @PathVariable String modelId) {
        generateModelCrudDesign(modelId, appId);
        return R.ok();
    }

    @ApiOperation("获取模型可选字段类型")
    @GetMapping("/optional/field/type")
    public R<List<JSONObject>> optionalModelFieldType(@PathVariable String appId) {
        List<DataFieldType> notModelFieldTypeList = DataFieldType.notModelFieldTypes();
        // Map<字段类型, 字段类型名>
        List<JSONObject> fieldTypes = Arrays.stream(DataFieldType.values())
                .filter(type -> !notModelFieldTypeList.contains(type))
                .map(type -> {
                    JSONObject fieldType = new JSONObject();
                    fieldType.put(type.name(), type.getDesc());
                    return fieldType;
                })
                .collect(Collectors.toList());
        return R.ok(fieldTypes);
    }


    /**
     * 依据模型字段自动生成列表和表单设计
     *
     * @param modelId 模型id
     * @param menuId 目录id
     */
    private void generateModelCrudDesign(String modelId, String menuId) {
        DataModelPo model = dataModelService.getModel(modelId);
        String appId = model.getAppId();
        // 启用了模型字段，查询模型配置的字段
        List<FieldBasicsHtml> fields = null;
        if (Boolean.TRUE.equals(model.getEnableModelField())) {
            fields = dataFieldService.getModelFields(appId, model.getId());
        } else {
            // 未启用模型字段，则查询模型相关设计的字段
            fields = dataFieldService.getFields(appId, model.getId(), false, false);
        }
        if (ObjectNull.isNull(fields)) {
            throw new BusinessException("没有模型字段,不能生成设计");
        }
        // 生成crud设计
        List<AutoCreateCrudDesignDto.Field> autoFields = fields.stream()
                .map(field -> new AutoCreateCrudDesignDto.Field()
                        .setFieldKey(field.getFieldKey())
                        .setFieldName(field.getFieldName())
                        .setFieldType(field.getFieldType())
                        .setType(field.getFieldType().getDesc()))
                .collect(Collectors.toList());
        AutoCreateCrudDesignDto autoDto = new AutoCreateCrudDesignDto()
                .setAppId(appId)
                .setModelId(modelId)
                .setMenuId(ObjectNull.isNotNull(menuId) ? menuId : appId)
                .setDesignName(model.getName())
                .setFields(autoFields);
        autoCreateCrudDesignService.generate(autoDto);

        // 保存模型字段
        if (Boolean.FALSE.equals(model.getEnableModelField())) {
            dataModelService.updateEnableModelField(modelId, true);
            List<DataFieldPo> dataFields = autoFields.stream()
                    .map(field -> {
                        DataFieldPo fieldDto = new DataFieldPo();
                        fieldDto.setFieldKey(field.getFieldKey());
                        fieldDto.setFieldName(field.getFieldName());
                        fieldDto.setFieldType(field.getFieldType());
                        return fieldDto;
                    })
                    .collect(Collectors.toList());
            dataFieldService.updateFields(appId, modelId, DesignType.data, modelId, dataFields);
        }
    }
}
