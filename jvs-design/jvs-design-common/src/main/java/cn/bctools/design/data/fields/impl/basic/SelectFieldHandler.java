package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.dto.form.item.FilterHtml;
import cn.bctools.design.data.fields.dto.form.item.TypeHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.fields.impl.ISelectorDataHandler;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单字段: 下拉框
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@AllArgsConstructor
@DesignField(value = "下拉框", type = DataFieldType.select)
public class SelectFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<MultipleHtml>, ISelectorDataHandler {

    @Override
    public void filterOrDataLinkage(String appId, Map<String, ? extends FieldBasicsHtml> fieldMap, String key, MultipleHtml e, Map<String, Object> map, Integer index, String... parentPath) {
        if (ObjectNull.isNull(e.getDataLinkageList())) {
            return;
        }
        if (ObjectNull.isNotNull(e.getDataFilterList()) || ObjectNull.isNotNull(e.getDataFilterGroupList())) {
            List<List<FilterHtml>> dataFilterGroupList = CollectionUtils.isNotEmpty(e.getDataFilterGroupList()) ? e.getDataFilterGroupList() : Collections.singletonList(e.getDataFilterList());
            //如果查询条件与触发级件相关,则执行,如果不相关则退出关联,处理下拉关联问题
            if (dataFilterGroupList.stream().flatMap(Collection::stream).anyMatch(a -> a.getFieldKey().equals(key))) {
                //需要查询的字段，是关联字段和显示字段
                List<String> collect = new ArrayList<>(1);
                collect.add(e.getProps().getValue());
                collect.add(e.getProps().getLabel());
                //添加下级字段，目前只有表格才有这个操作
                addFields(collect, e);
                //获取查询条件
                List<List<QueryConditionDto>> queryConditionDtos = dataFilterGroupList.stream().map(filterGroup ->
                        filterGroup.stream()
                                .peek(s -> {
                                    //添加查询字段
                                    collect.add(s.getFieldKey());
                                }).map(s -> {
                                    try {
                                        TypeHtml type = s.getType();
                                        if (ObjectNull.isNull(type)) {
                                            type = TypeHtml.value;
                                        }
                                        QueryConditionDto queryConditionDto = new QueryConditionDto().setEnabledQueryTypes(s.getEnabledQueryTypes()).setFieldKey(s.getFieldKey());
                                        switch (type) {
                                            case prop:
                                                //获取字段路径
                                                List<String> strings = new ArrayList<>(Arrays.asList(parentPath));
                                                strings.add(e.getProp());
                                                String paths = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining(StrUtil.DOT));
                                                Object read = JvsJsonPath.read(JSONUtil.toJsonStr(map), paths);
                                                return queryConditionDto.setValue(read);
                                            case value:
                                            default:
                                                return queryConditionDto.setValue(s.getValue());
                                        }
                                    } catch (Exception exception) {
                                        return null;
                                    }
                                }).filter(ObjectNull::isNotNull).collect(Collectors.toList())
                ).collect(Collectors.toList());
                //获取表格字段
                setValue(appId, e.getDataModelId(), e, map, collect, queryConditionDtos, parentPath);
            }
        }
        // 数据联动处理
        dataLinkage(appId, fieldMap, key, e, map, index, parentPath);
    }

    @Override
    public List<DataQueryType> getEnabledQueryTypes(MultipleHtml html) {
        return super.getEnabledQueryTypes(html);
    }

    @Override
    public Object getEchoValue(MultipleHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        return echoValue(fieldDto, data, lineData);
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        return conversionKey(dto, o, lineData, cascaderFieldPathIdsMap, generateCascaderList);
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        return conversionKey(dto, o, oldData);
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public void checkFieldTypeAttributeChanged(MultipleHtml html, MultipleHtml dbHtml) {
        super.checkFieldTypeAttributeChanged(html, dbHtml);
    }

    @Override
    public Object checkDataFieldType(MultipleHtml multipleHtml, Object o) throws Exception {
        if (multipleHtml.getMultiple()) {
            if (!(o instanceof List)) {
                if (o instanceof String) {
                    o = Arrays.stream(((String) o).split(",")).collect(Collectors.toList());
                } else {
                    throw new RuntimeException("正确格式为数组");
                }
            }
        } else {
            if (!FormDataTypeEnum.flowable.equals(multipleHtml.getDatatype())) {
                if(o instanceof Number){
                    return o.toString();
                }
                if (!(o instanceof String)) {
                    throw new RuntimeException("正确格式为字符串");
                }
            }
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {

        if (ObjectNull.isNull(dicData)) {
            dicData = new ArrayList<>();
            dicData.add("未配置选择");
        }
        String jsonString = JSONObject.toJSONString(dicData.stream().map(e -> Dict.create().set("value", e).set("label", e)).collect(Collectors.toList()));

        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"select\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"prop\",\n" +
                "        \"multiple\",\n" +
                "        \"collapsetags\",\n" +
                "        \"placeholder\",\n" +
                "        \"defaultUrl\",\n" +
                "        \"clearable\",\n" +
                "        \"disabled\",\n" +
                "        \"filterable\",\n" +
                "        \"allowcreate\",\n" +
                "        \"datatype\",\n" +
                "        \"option\",\n" +
                "        \"url\",\n" +
                "        \"defaultValue\",\n" +
                "        \"sqlType\",\n" +
                "        \"hasChildren\"\n" +
                "    ],\n" +
                "    \"hasChildren\": false,\n" +
                "    \"children\": [],\n" +
                "    \"multiple\": false,\n" +
                "    \"collapsetags\": false,\n" +
                "    \"disabled\": false,\n" +
                "    \"filterable\": true,\n" +
                "    \"allowcreate\": false,\n" +
                "    \"placeholder\": \"请输入" + name + "\",\n" +
                "    \"clearable\": true,\n" +
                "    \"defaultValue\": \"\",\n" +
                "    \"defaultUrl\": \"\",\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"parentKey\": \"\",\n" +
                "    \"datatype\": \"option\",\n" +
                "    \"url\": \"\",\n" +
                "    \"dicData\": " + jsonString + ",\n " +
                "    \"text\": \"\",\n" +
                "    \"currVal\": \"\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"props\": {\n" +
                "        \"label\": \"label\",\n" +
                "        \"value\": \"value\"\n" +
                "    },\n" +
                "    \"name\": \"" + DataFieldType.select.getDesc() + "\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
