package cn.bctools.design.data.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.html.DatePickerHtml;
import cn.bctools.design.data.fields.dto.form.item.SelectItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.mapper.DataFieldMapper;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.CurrentAppUtils;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.impl.basic.DatePickerFieldHandler.DateType.datetime;


/**
 * 数据字段
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DataFieldServiceImpl extends ServiceImpl<DataFieldMapper, DataFieldPo> implements DataFieldService, IJvsDesigner {

    private List<DataFieldPo> defaultFieldCache;
    private List<DataFieldPo> doNotShowFieldCache;
    private final MapperMethodHandler mapperMethodHandler;

    static final String[] STR = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default",
            "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof"
            , "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};
    static final List<String> RETAIN_FIELDS = Arrays.asList(STR.clone());

    @Data
    @Accessors(chain = true)
    public static class FieldCacheKey {
        private String appId;
        private String modelId;
        private String designId;
        List<String> fieldKeys;
        DataFieldType fieldType;
        Boolean getDesignJson;
        Boolean addDefaultFields;
        Predicate<DataFieldPo> fieldPredicate = e -> true;
    }

    @Override
    public boolean saveFields(String appId, List<DataFieldPo> fields, String modelId) {
        if (ObjectUtils.isEmpty(fields)) {
            return false;
        }
        List<DataFieldPo> fieldList = BeanCopyUtil.copys(fields, DataFieldPo.class);
        DesignUtils.checkField(fieldList);
        List<String> fieldKeys = new ArrayList<>();
        fieldList.forEach(e -> {
            e.setModelId(modelId);
            if (RETAIN_FIELDS.contains(e.getFieldKey())) {
                fieldKeys.add(e.getFieldName() + ":" + e.getFieldKey());
            }
        });
        if (ObjectNull.isNotNull(fieldKeys)) {
            throw new BusinessException("字段名起名冲突请修改" + JSONObject.toJSONString(fieldKeys));
        }
        return this.saveBatch(fieldList);
    }

    /**
     * 保存数据模型字段信息
     * <p>
     * 删除后统一新增
     *
     * @param designId   设计套件id
     * @param designType 设计套件类型
     * @param modelId    数据模型id
     * @param fieldList  字段信息集合
     * @return 操作结果
     */
    @Override
    public boolean updateFields(String appId, String designId, DesignType designType, String modelId, List<DataFieldPo> fieldList) {
        this.remove(Wrappers.<DataFieldPo>lambdaQuery()
                .eq(DataFieldPo::getModelId, modelId)
                .eq(DataFieldPo::getDesignId, designId)
                .eq(DataFieldPo::getDesignType, designType));
        if (ObjectUtils.isEmpty(fieldList)) {
            return true;
        }
        DesignUtils.checkField(fieldList);
        List<String> fieldKeys = new ArrayList<>();
        String appVersion = CurrentAppUtils.getAppVersion();
        for (DataFieldPo fieldPo : fieldList) {
            fieldPo.setModelId(modelId);
            fieldPo.setJvsAppId(appId);
            fieldPo.setDesignId(designId);
            fieldPo.setDesignType(designType);
            fieldPo.setAppVersion(appVersion);
            if (RETAIN_FIELDS.contains(fieldPo.getFieldKey())) {
                fieldKeys.add(fieldPo.getFieldName() + ":" + fieldPo.getFieldKey());
            }
        }
        if (ObjectNull.isNotNull(fieldKeys)) {
            throw new BusinessException("字段名起名冲突或是保留字请修改" + JSONObject.toJSONString(fieldKeys));
        }
        // 不是模型数据字段的类型不保存
        fieldList.removeIf(field -> DataFieldType.notModelFieldTypes().contains(field.getFieldType()));
        return this.saveBatch(fieldList);
    }

    @Override
    public FieldBasicsHtml getField(String appId, String modelId, String fieldKey, boolean getDesignJson) {
        if (StringUtils.isBlank(fieldKey)) {
            return null;
        }
        List<FieldBasicsHtml> fields = this.getFields(appId, modelId, null, Collections.singletonList(fieldKey), null, getDesignJson, false);
        if (ObjectUtils.isEmpty(fields)) {
            return null;
        }
        return fields.get(0);
    }

    @Override
    public List<FieldBasicsHtml> getFields(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields) {
        return this.getFields(appId, modelId, null, null, null, getDesignJson, addDefaultFields);
    }

    @Override
    public List<FieldBasicsHtml> getFields(String appId, String modelId, String designId, boolean getDesignJson, boolean addDefaultFields) {
        return this.getFields(appId, modelId, designId, null, null, getDesignJson, addDefaultFields);
    }

    @Override
    public List<String> getFieldKeys(String appId, String modelId) {
        List<DataFieldPo> fields = this.list(Wrappers.<DataFieldPo>lambdaQuery()
                .select(DataFieldPo::getFieldKey)
                .eq(ObjectNull.isNotNull(appId), DataFieldPo::getJvsAppId, appId)
                .eq(DataFieldPo::getModelId, modelId)
                .groupBy(DataFieldPo::getFieldKey));
        return fields.stream().map(DataFieldPo::getFieldKey).collect(Collectors.toList());
    }

    @Override
    public List<FieldBasicsHtml> getIncreasedIdFields(String appId, String modelId) {
        return this.getFields(appId, modelId, null, null, DataFieldType.serialNumber, true, false);
    }

    @Override
    public List<FieldBasicsHtml> getAllField(String appId, String modelId) {
        return getAllField(appId, modelId, true, true);
    }

    @Override
    public List<FieldBasicsHtml> getAllField(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields) {
        List<FieldBasicsHtml> fields = getFields(appId, modelId, getDesignJson, addDefaultFields);
        fields.removeIf(field -> Objects.isNull(field.getType()) || !field.getType().isEnableExport());
        getSourceFieldId(fields);
        if (!fields.isEmpty() && fields.stream().anyMatch(e -> e.getFieldKey().equals("id"))) {
            return fields;
        }
        FieldBasicsHtml fieldBasicsHtml = new FieldBasicsHtml();
        fieldBasicsHtml.setFieldKey(Get.name(DynamicDataPo::getId)).setFieldName("数据id").setType(DataFieldType.input);
        fieldBasicsHtml.setEnabledQueryTypes(Collections.singletonList(DataQueryType.eq));
        fields.add(0, fieldBasicsHtml);
        return fields;
    }

    @Override
    public List<FieldBasicsHtml> getAllField(String appId, String modelId, Predicate<FieldBasicsHtml> predicate) {
        return getAllField(appId, modelId, true, false, predicate);
    }

    @Override
    public List<FieldBasicsHtml> getAllField(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields, Predicate<FieldBasicsHtml> predicate) {
        List<FieldBasicsHtml> fields = getFields(appId, modelId, getDesignJson, addDefaultFields);
        if (ObjectNull.isNotNull(predicate)) {
            fields.removeIf(predicate);
        }
        getSourceFieldId(fields);
        if (!fields.stream().anyMatch(e -> e.getFieldKey().equals("id"))) {
            FieldBasicsHtml fieldBasicsHtml = new FieldBasicsHtml();
            fieldBasicsHtml.setFieldKey(Get.name(DynamicDataPo::getId)).setFieldName("数据id").setType(DataFieldType.input);
            fieldBasicsHtml.setEnabledQueryTypes(Collections.singletonList(DataQueryType.eq));
            fields.add(0, fieldBasicsHtml);
        }
        return fields;
    }

    @Override
    public List<FieldBasicsHtml> getAllFieldDefault(String appId, String modelId, Predicate<FieldBasicsHtml> predicate) {
        List<FieldBasicsHtml> fields = getFields(appId, modelId, true, true);
        if (ObjectNull.isNotNull(predicate)) {
            fields.removeIf(predicate);
        }
        if (!fields.isEmpty() && "id".equals(fields.get(0).getFieldKey())) {
            return fields;
        }
        getSourceFieldId(fields);
        FieldBasicsHtml fieldBasicsHtml = new FieldBasicsHtml();
        fieldBasicsHtml.setFieldKey(Get.name(DynamicDataPo::getId)).setFieldName("数据id").setType(DataFieldType.input);
        fieldBasicsHtml.setEnabledQueryTypes(Collections.singletonList(DataQueryType.eq));
        fields.add(0, fieldBasicsHtml);
        return fields;
    }

    @Override
    public List<DataFieldPo> getDefaultAllFields() {
        return getDefaultFields(null).values().stream().collect(Collectors.toList());
    }

    /**
     * 解析字段设计得到显示来源字段
     *
     * @param fields
     */
    private void getSourceFieldId(List<FieldBasicsHtml> fields) {
        fields.stream()
                // 字段类型为下拉
                .filter(field -> ObjectNull.isNotNull(field.getType()))
                .filter(field -> DataFieldType.select.equals(field.getType()))
                .forEach(selectField -> {
                    SelectItemHtml selectItemHtml = BeanCopyUtil.copy(selectField.getDesignJson(), SelectItemHtml.class);
                    if (ObjectNull.isNotNull(selectItemHtml)) {
                        String sourceFieldId = null;
                        // 数据类型为数据模型，得到显示来源字段id
                        if (FormDataTypeEnum.dataModel.equals(selectItemHtml.getDatatype())) {
                            sourceFieldId = StringUtils.isNotBlank(selectItemHtml.getProps().getSourceFieldId()) ? selectItemHtml.getProps().getSourceFieldId() : selectField.getId();
                        }
                        selectField.setSourceFieldId(sourceFieldId);
                    }
                });
    }

    /**
     * 获取模型字段
     *
     * @param modelId          模型id
     * @param designId         设计id
     * @param fieldKeys        字段key集合
     * @param fieldType        字段类型
     * @param getDesignJson    是否查询设计json
     * @param addDefaultFields 是否添加系统默认字段
     * @return 字段数据集合
     */
    @SneakyThrows
    @Override
    public List<FieldBasicsHtml> getFields(String appId, String modelId, String designId, List<String> fieldKeys, DataFieldType fieldType, boolean getDesignJson, boolean addDefaultFields) {
        if (StringUtils.isBlank(modelId)) {
            return new ArrayList<>();
        }
        List<FieldBasicsHtml> fieldBasicsHtmls = getFieldsCache(new FieldCacheKey()
                .setAppId(appId)
                .setModelId(modelId)
                .setDesignId(designId)
                .setFieldKeys(fieldKeys)
                .setFieldType(fieldType)
                .setGetDesignJson(getDesignJson)
                .setAddDefaultFields(addDefaultFields));
        return JSON.parseArray(JSON.toJSONString(fieldBasicsHtmls), FieldBasicsHtml.class);
    }

    @Override
    public List<FieldBasicsHtml> getDesignIdFields(String appId, String modelId, String designId, boolean getDesignJson, boolean addDefaultFields) {
        List<DataFieldPo> fieldPoList = list(Wrappers.query(new DataFieldPo().setJvsAppId(appId).setModelId(modelId).setDesignId(designId)));
        if (addDefaultFields) {
            Map<String, DataFieldPo> defaultFields = this.getDefaultFields(modelId);
            fieldPoList.addAll(defaultFields.values().stream().collect(Collectors.toList()));
        }
        List<FieldBasicsHtml> collect = fieldPoList.stream()
                .map(e -> {
                            FieldBasicsHtml fieldPublicHtml = BeanCopyUtil.copy(e, FieldBasicsHtml.class);
                            fieldPublicHtml.setProp(e.getFieldKey());
                            fieldPublicHtml.setType(e.getFieldType());
                            fieldPublicHtml.setFieldType(e.getFieldType());
                            fieldPublicHtml.setDesignJson(e.getDesignJson());
                            return fieldPublicHtml;
                        }
                ).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<FieldBasicsHtml> getFieldsCache(FieldCacheKey fieldCacheKey) {
        List<DataFieldPo> fieldPoList = this.list(Wrappers.<DataFieldPo>lambdaQuery()
                .select(DataFieldPo::getId, DataFieldPo::getFieldKey, DataFieldPo::getLinkageFieldKey, DataFieldPo::getDataLinkageModelId, DataFieldPo::getDataLinkageList, DataFieldPo::getIsExport, DataFieldPo::getEncryptionExpress,
                        DataFieldPo::getEncryption,
                        DataFieldPo::getDesignType,
                        DataFieldPo::getFieldName,
                        DataFieldPo::getDesignId,
                        DataFieldPo::getFieldType,
                        DataFieldPo::getEnabledQueryTypes, DataFieldPo::getDesignJson,
                        DataFieldPo::getDataModelId)
                .eq(DataFieldPo::getModelId, fieldCacheKey.getModelId())
                .eq(ObjectNull.isNotNull(fieldCacheKey.getAppId()), DataFieldPo::getJvsAppId, fieldCacheKey.getAppId())
                //新增字段类型排序保证有类型的排序在最后面
                .orderByDesc(DataFieldPo::getDesignJson)
                .orderByAsc(DataFieldPo::getEnabledQueryTypes));
        return getFields(fieldPoList, fieldCacheKey.getModelId(), fieldCacheKey.getDesignId(), fieldCacheKey.getFieldKeys(), fieldCacheKey.getFieldType(), fieldCacheKey.getGetDesignJson(),
                fieldCacheKey.getAddDefaultFields(), fieldCacheKey.getFieldPredicate());
    }

    private List<FieldBasicsHtml> getFields(List<DataFieldPo> fieldPoList, String modelId, String designId, List<String> fieldKeys, DataFieldType fieldType, boolean getDesignJson, boolean addDefaultFields,
                                            Predicate<DataFieldPo> fieldPredicate) {
        fieldPoList = fieldPoList.stream().filter(field -> {
            boolean filterFieldType = Boolean.TRUE;
            boolean filterDesignId = Boolean.TRUE;
            boolean filterFieldKeys = Boolean.TRUE;
            if (ObjectNull.isNotNull(fieldType)) {
                filterFieldType = fieldType.equals(field.getFieldType());
            }
            if (ObjectNull.isNotNull(designId)) {
                filterDesignId = designId.equals(field.getDesignId());
            }
            if (ObjectNull.isNotNull(fieldKeys)) {
                filterFieldKeys = fieldKeys.contains(field.getFieldKey());
            }
            return filterFieldType && filterDesignId && filterFieldKeys;
        }).peek(field -> {
            if (Boolean.FALSE.equals(getDesignJson)) {
                field.setDesignJson(null);
            }
        }).collect(Collectors.toList());


        Map<String, DataFieldPo> defaultFields = new HashMap<>(1);
        if (addDefaultFields) {
            defaultFields = this.getDefaultFields(modelId);
            fieldPoList.addAll(defaultFields.values().stream().collect(Collectors.toList()));
        }
        // 尽量与设计的字段顺序一致, 这样导出的Excel会好看一些
        List<String> fieldKeyList = fieldPoList.stream().map(DataFieldPo::getFieldKey).distinct().collect(Collectors.toList());
        Map<String, List<DataFieldPo>> fieldMap = fieldPoList.stream().collect(Collectors.groupingBy(DataFieldPo::getFieldKey));
        List<FieldBasicsHtml> result = new ArrayList<>(fieldPoList.size());
        for (String fieldKey : fieldKeyList) {
            List<DataFieldPo> fields = fieldMap.get(fieldKey);
            FieldBasicsHtml fieldDto = new FieldBasicsHtml();
            fieldDto.setModelId(modelId);
            fieldDto.setFieldKey(fieldKey);
            // 优先获取表单的设计字段
            List<DataFieldPo> validFields = fields.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getFieldName()))
                    .filter(e -> Objects.nonNull(e.getFieldType()))
                    .filter(fieldPredicate)
                    .filter(e -> Objects.nonNull(e.getDesignJson()))
                    .collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(validFields)) {
                fields = validFields;
            }
            DataFieldPo fieldPon = fields.get(fields.size() - 1);
            DataFieldPo fieldPo = fields.stream().filter(e -> !e.getFieldType().equals(DataFieldType.input)).findFirst().orElse(fieldPon);
            // TODO 默认获取最新的设计字段 会有错误，两个不一样的表单，会显示另一个的数据联动关系
            fieldDto.setId(fieldPo.getId());
            fieldDto.setFieldName(fieldPo.getFieldName());
            fieldDto.setType(fieldPo.getFieldType());
            fieldDto.setProp(fieldPo.getFieldKey());
            fieldDto.setLabel(fieldPo.getFieldName());
            fieldDto.setDesignId(fieldPo.getDesignId());
            fieldDto.setDesignType(fieldPo.getDesignType());
            fieldDto.setDesignJson(fieldPo.getDesignJson());
            fieldDto.setDataModelId(fieldPo.getDataModelId());
            fieldDto.setIsExport(fieldPo.getIsExport());
            fieldDto.setEncryptionExpress(fieldPo.getEncryptionExpress());
            fieldDto.setEncryption(fieldPo.getEncryption());
            fieldDto.setDataLinkageModelId(fieldPo.getDataLinkageModelId());
            if (ObjectNull.isNotNull(fieldPo.getDataLinkageList())) {
                List<QueryConditionDto> dataLinkageList = JSONUtil.toList(fieldPo.getDataLinkageList().toJSONString(), QueryConditionDto.class);
                fieldDto.setDataLinkageList(dataLinkageList);
                fieldDto.setDataLinkageEnable(fieldPo.getDataLinkageEnable());
                fieldDto.setDataLinkageModelId(fieldPo.getDataLinkageModelId());
                fieldDto.setLinkageFieldKey(fieldPo.getLinkageFieldKey());
            }
            JSONArray enabledQueryTypes = fieldPo.getEnabledQueryTypes();
            if (ObjectUtils.isEmpty(enabledQueryTypes)) {
                fieldDto.setEnabledQueryTypes(Collections.singletonList(DataQueryType.eq));
            } else {
                List<DataQueryType> collect = (List<DataQueryType>) enabledQueryTypes.stream().map(e -> DataQueryType.valueOf(e.toString())).collect(Collectors.toList());
                fieldDto.setEnabledQueryTypes(collect.stream().distinct().collect(Collectors.toList()));
                fieldDto.getEnabledQueryTypes().add(DataQueryType.isNull);
            }
            if (defaultFields.containsKey(fieldDto.getFieldKey())) {
                //如果是默认字段不能使用用户设计的字段类型
                fieldDto.setType(defaultFields.get(fieldKey).getFieldType());
            }
            result.add(fieldDto);
        }
        return result;
    }

    /**
     * 获取默认字段
     *
     * @param modelId 数据模型id
     * @return 默认字段集合
     */
    @Override
    public Map<String, DataFieldPo> getDefaultFields(final String modelId) {
        if (ObjectNull.isNull(this.defaultFieldCache)) {
            List<DataFieldPo> fields = new ArrayList<>();
            JSONArray eqArr = new JSONArray();
            eqArr.add(DataQueryType.eq);
            JSONArray comparableArr = new JSONArray();
            comparableArr.add(DataQueryType.eq);
            comparableArr.add(DataQueryType.gt);
            comparableArr.add(DataQueryType.ge);
            comparableArr.add(DataQueryType.lt);
            comparableArr.add(DataQueryType.le);
            comparableArr.add(DataQueryType.between);
            //审批人员信息也是保存在jvsFlowTask字段下面
            fields.add(new DataFieldPo().setFieldKey("jvsFlowTaskState").setFieldName("工作流状态").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey("jvsFlowTaskProgress").setFieldName("工作流进度").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey(Get.name(DynamicDataPo::getCreateBy)).setFieldName("创建人").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey(Get.name(DynamicDataPo::getCreateById)).setFieldName("创建人ID").setFieldType(DataFieldType.user).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey(Get.name(DynamicDataPo::getCreateTime)).setFieldName("创建时间").setFieldType(DataFieldType.datePicker).setEnabledQueryTypes(comparableArr).setDesignJson(BeanCopyUtil.beanToMap(new DatePickerHtml().setDatetype(datetime).setType(DataFieldType.datePicker).setName("创建时间"))));
            fields.add(new DataFieldPo().setFieldKey(Get.name(DynamicDataPo::getUpdateBy)).setFieldName("修改人").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey("updateById").setFieldName("修改人Id").setFieldType(DataFieldType.user).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey(Get.name(DynamicDataPo::getUpdateTime)).setFieldName("修改时间").setFieldType(DataFieldType.datePicker).setEnabledQueryTypes(comparableArr).setDesignJson(BeanCopyUtil.beanToMap(new DatePickerHtml().setDatetype(datetime).setType(DataFieldType.datePicker).setName("修改时间"))));
            fields.add(new DataFieldPo().setFieldKey("id").setFieldName("数据id").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.forEach(field -> field.setModelId(modelId));
            this.defaultFieldCache = fields;
        }
        return this.defaultFieldCache.stream().collect(Collectors.toMap(DataFieldPo::getFieldKey, Function.identity()));
    }

    @Override
    public List<DataFieldPo> getDoNotShowFields() {
        if (ObjectNull.isNull(this.doNotShowFieldCache)) {
            List<DataFieldPo> fields = new ArrayList<>();
            JSONArray eqArr = new JSONArray();
            eqArr.add(DataQueryType.eq);
            //审批人员信息也是保存在jvsFlowTask字段下面
            fields.add(new DataFieldPo().setFieldKey("jvsFlowTask").setFieldName("工作流任务信息").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            fields.add(new DataFieldPo().setFieldKey(FIELD_RELATION_TAG).setFieldName("关联数据id默认字段").setFieldType(DataFieldType.input).setEnabledQueryTypes(eqArr));
            this.doNotShowFieldCache = fields;
        }
        return this.doNotShowFieldCache;
    }


    @Override
    public String templateReplacement(String appId, String modelId, Map<String, Object> data, String conent) {
        Binding binding = new Binding();
        getFields(appId, modelId, false, true)
                .forEach(e -> {
                    binding.setVariable(e.getFieldName(), data.get(e.getFieldKey()));
                });
        GroovyShell shell = new GroovyShell(binding);
        Object evaluate = shell.evaluate("\"" + conent + "\"");
        return evaluate.toString();
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<DataFieldPo>lambdaQuery().eq(DataFieldPo::getJvsAppId, appId));
    }

    @Override
    public List<FieldBasicsHtml> getModelFields(String appId, String modelId) {
        List<DataFieldPo> fieldPoList = this.list(Wrappers.<DataFieldPo>lambdaQuery()
                .select(DataFieldPo::getId,
                        DataFieldPo::getFieldKey,
                        DataFieldPo::getLinkageFieldKey,
                        DataFieldPo::getDataLinkageModelId,
                        DataFieldPo::getDataLinkageList,
                        DataFieldPo::getIsExport,
                        DataFieldPo::getEncryptionExpress,
                        DataFieldPo::getEncryption,
                        DataFieldPo::getDesignType,
                        DataFieldPo::getFieldName,
                        DataFieldPo::getDesignId,
                        DataFieldPo::getFieldType,
                        DataFieldPo::getEnabledQueryTypes, DataFieldPo::getDesignJson,
                        DataFieldPo::getDataModelId)
                .eq(DataFieldPo::getModelId, modelId)
                .eq(DataFieldPo::getDesignId, modelId)
                .eq(DataFieldPo::getJvsAppId, appId)
                .orderByAsc(DataFieldPo::getCreateTime));
        return getFields(fieldPoList, modelId, modelId, null, null, false, false, e -> true);
    }
}
