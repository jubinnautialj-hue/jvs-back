package cn.bctools.design.data.fields.impl.container;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.QueryListDto;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.FilterHtml;
import cn.bctools.design.data.fields.dto.form.item.TypeHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.function.component.ExpressionComponent;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表单字段: 表格
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "表格", type = DataFieldType.tableForm)
public class TableFormFieldHandler implements IDataFieldHandler<TableFormItemHtml> {

    @Autowired
    DataFieldService dataFieldService;

    static final String TABLE_TYPE = "tableType";


    @Override
    public void isNext(TableFormItemHtml html) {
        Map<String, IDataFieldHandler> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        //解析内容判断是否有下级字段
        if (ObjectNull.isNotNull(html.getTableColumn())) {
            html.setNext(true);

            for (int i = 0; i < html.getTableColumn().size(); i++) {
                IDataFieldHandler iDataFieldHandler = beansOfType.get(html.getTableColumn().get(i).getType().getDesc());
                Class<? extends FieldBasicsHtml> aClass = getCls(iDataFieldHandler);

                List<Map> o = (List<Map>) html.getDesignJson().get(Get.name(TableFormItemHtml::getTableColumn));
                Map linkedHashMap = o.get(i);
                FieldBasicsHtml t = BeanCopyUtil.copy(aClass, linkedHashMap);
                List<FieldBasicsHtml> tableColumn = html.getTableColumn();

                //判断同级是否有key重复
                Map<String, Integer> listMap = tableColumn.stream().map(e -> e.getProp()).collect(Collectors.toMap(qe -> qe, qe -> 1, Integer::sum));
                for (String key : listMap.keySet()) {
                    if (listMap.get(key) > 1) {
                        throw new BusinessException(html.getLabel() + "下有重复的字段命名,请修改:" + key);
                    }
                }

                t.setDesignJson(linkedHashMap);
                tableColumn.set(i, t);
            }
        }
    }

    @Override
    public void addFields(List<String> fields, TableFormItemHtml v) {
        List<String> collect = v.getTableColumn().stream().map(e -> e.getProp()).collect(Collectors.toList());
        fields.addAll(collect);
    }

    /**
     * 进行数据解析自定义嵌套用于公式处理
     */
    @Override
    public void next(List<ElementVo> list, TableFormItemHtml html, Map<String, IDataFieldHandler> handlerMap, ElementVo e) {
        //如果是特殊容器类型，需要进行解析
        //遍历html的TableColumn
        for (FieldPublicHtml v : html.getTableColumn()) {
            //如果designJson不为空，并且handlerMap包含该字段名
            if (ObjectNull.isNotNull(handlerMap, v.getDesignJson()) && handlerMap.containsKey(v.getName())) {
                //获取对应的IDataFieldHandler
                IDataFieldHandler iDataFieldHandler = handlerMap.get(v.getName());
                //调用toHtml方法，将designJson转换为FieldPublicHtml
                FieldPublicHtml vo = iDataFieldHandler.toHtml(v.getDesignJson());
                vo.setFieldName(v.getLabel()).setFieldKey(v.getFieldKey()).setType(v.getType());
                //创建ElementVo
                ElementVo elementVo = new ElementVo()
                        //设置id
                        .setId(e.getId() + StrUtil.DOT + vo.getProp())
                        //设置真实路径
                        .setPath(e.getPath() + StrUtil.DOT + vo.getProp())
                        //设置name
                        .setName(e.getShortName() + StrUtil.DOT + vo.getFieldName())
                        .setFieldType(v.getType().name())
                        //设置shortName
                        .setShortName(e.getShortName() + StrUtil.DOT + vo.getFieldName())
                        //设置jvsParamType
                        .setJvsParamType(JvsParamType.getByClass(vo.getType().getAClass()));
                //添加控件属性
                list.add(elementVo);
                //解析具体字段
                //判断是否还存在有下级结构
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(v.getDesignJson());
                if (publicHtml.getNext()) {
                    //调用next方法，继续解析下级结构
                    iDataFieldHandler.next(list, publicHtml, handlerMap, elementVo);
                }
            }
        }
    }

    @Override
    public void next(List<ElementVo> list, TableFormItemHtml html, Function<FieldBasicsHtml, ElementVo> function, Map<String, IDataFieldHandler> handlerMap, String name, String prop) {
        //如果是特殊容器类型，需要进行解析
        List<ElementVo> tableList = html.getTableColumn().stream().map(v -> {
            FieldBasicsHtml basicsHtml = new FieldBasicsHtml();
            basicsHtml.setFieldName(name + StrUtil.DOT + v.getLabel()).setFieldKey(prop + StrUtil.DOT + v.getProp()).setType(v.getType());
            return basicsHtml;
        }).map(function).collect(Collectors.toList());
        if (ObjectNull.isNotNull(tableList)) {
            list.addAll(tableList);
        }
    }

    @Override
    public void filterOrDataLinkage(String appId, Map<String, ? extends FieldBasicsHtml> fieldMap, String key, TableFormItemHtml html, Map<String, Object> map, Integer index, String... parentPath) {
        //判断自己有没有这种数据联动
        if (SystemThreadLocal.get(TABLE_TYPE).equals(TableType.line)) {
            if (ObjectNull.isNotNull(html.getDataModelId())) {
                if (ObjectNull.isNotNull(html.getDataFilterList()) || ObjectNull.isNotNull(html.getDataFilterGroupList())) {
                    List<List<FilterHtml>> dataFilterGroupList = CollectionUtils.isNotEmpty(html.getDataFilterGroupList()) ? html.getDataFilterGroupList() : Collections.singletonList(html.getDataFilterList());
                    List<String> collect = new ArrayList<>();
                    //添加下级字段，目前只有表格才有这个操作
                    addFields(collect, html);
                    //获取查询条件
                    List<List<QueryConditionDto>> queryConditionDtos = dataFilterGroupList.stream().map(filterGroup -> filterGroup.stream()
                                    .peek(s -> {
                                        //添加查询字段
                                        collect.add(s.getFieldKey());
                                    }).map(s -> {
                                        try {
                                            TypeHtml type = s.getType();
                                            QueryConditionDto queryConditionDto = new QueryConditionDto().setEnabledQueryTypes(s.getEnabledQueryTypes()).setFieldKey(s.getFieldKey());
                                            switch (type) {
                                                case value:
                                                    return queryConditionDto.setValue(s.getValue());
                                                case prop:
                                                    //获取字段路径
                                                    String path = s.getValue().toString();
                                                    if (JSONUtil.isTypeJSONArray(s.getValue().toString())) {
                                                        path = ((JSONArray) s.getValue()).stream().map(String::valueOf).collect(Collectors.joining(StrUtil.DOT));
                                                    }
                                                    Object read = JvsJsonPath.read(JSONUtil.toJsonStr(map), path);
                                                    return queryConditionDto.setValue(read);
                                                default:
                                                    return null;
                                            }
                                        } catch (Exception exception) {
                                            return null;
                                        }
                                    }).filter(ObjectNull::isNotNull).collect(Collectors.toList()))
                            .collect(Collectors.toList());
                    //获取表格字段 判断逻辑为空不处理
                    if (ObjectNull.isNotNull(queryConditionDtos)) {
                        //如果有行数据，则直接退出， 则表示用户有操作表格数据变化
                        //需要修改字段一致的情况下，才能触发字段设置的值
                        if (html.getProp().equals(key)) {
                            if (ObjectNull.isNotNull(key, html.getProp())) {
                                setValue(appId, html.getDataModelId(), html, map, collect, queryConditionDtos, parentPath);
                            }
                        }
                    }
                }
            }
        }
        Map<String, IDataFieldHandler> fieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        List<FieldBasicsHtml> tableColumn = html.getTableColumn();

        //判断下级有没有数据联动
        if (ObjectNull.isNotNullOne(tableColumn)) {
            ExpressionComponent bean = SpringContextUtil.getBean(ExpressionComponent.class);
            //只触发表单的
            String designId = SystemThreadLocal.get("designId");
            Map<String, FieldPublicHtml> publicHtmlMap = tableColumn.stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
            forEachTableColumn(appId, key, html, map, index, fieldHandlerMap, tableColumn, bean, designId, publicHtmlMap, parentPath);
            execExpression(html, map, index, bean, designId, parentPath);
        }
    }

    private void forEachTableColumn(String appId, String key, TableFormItemHtml html, Map<String, Object> map, Integer index, Map<String, IDataFieldHandler> fieldHandlerMap, List<FieldBasicsHtml> tableColumn, ExpressionComponent bean, String designId, Map<String, FieldPublicHtml> publicHtmlMap, String[] parentPath) {
        for (int i = 0; i < tableColumn.size(); i++) {
            //获取表格的列，然后依次执行公式
            FieldPublicHtml e = tableColumn.get(i);
            IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(e.getType().getDesc());
            FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(BeanToMapUtils.beanToMap(e));
            //拼凑表格的数据获取路径
            List<String> strings = new ArrayList<String>(Arrays.asList(parentPath));
            String path = strings.stream().collect(Collectors.joining(StrUtil.DOT));
            //如果触发条件不匹配直接退出
            if (ObjectNull.isNotNull(key) && key.contains(",")) {
                if (key.contains(",")) {
                    if (Arrays.stream(key.split(",")).anyMatch(s -> !path.contains(s))) {
                        continue;
                    }
                }
            }
            //获取数据
            Object o = JvsJsonPath.read(map, path);
            if (ObjectNull.isNull(o)) {
                continue;
            }
            //上下文获取行级数据，然后触发公式
            List<String> collect = Arrays.stream(parentPath).collect(Collectors.toList());
            if (o instanceof Collection<?>) {
                List<LinkedHashMap<String, Object>> list = (List<LinkedHashMap<String, Object>>) o;
                if (ObjectNull.isNull(index)) {
                    continue;
                } else {
                    if (list.size() > index) {
                        iDataFieldHandler.filterOrDataLinkage(appId, publicHtmlMap, key, publicHtml, list.get(index), index, publicHtml.getProp());
                        JSONPath.set(map, path, list);
                    }
                }
            } else {
                JSONObject objMap = JSONObject.parseObject(JSON.toJSONString(o));
                iDataFieldHandler.filterOrDataLinkage(appId, publicHtmlMap, key, publicHtml, objMap, index, publicHtml.getProp());
                JSONPath.set(map, path, objMap);
            }
            //上下文获取行级数据，然后触发公式
            SystemThreadLocal.set("index", index);
            //如果是自己，则不处理，用户操作优先
            boolean tableTypeBoolean = ObjectNull.isNotNull(index) && SystemThreadLocal.get(TABLE_TYPE).equals(TableType.add);
            //如果只有一个上级，并且只是赋予表格整体字段的时候，则可以复制,不能是行内操作
            if (html.getProp().equals(key) || tableTypeBoolean) {
                if (ObjectNull.isNotNull(key, html.getProp())) {
                    HashMap<String, Object> params = new HashMap<>(map);
                    ExecDto execDto = new ExecDto().setIndex(index).setModifiedField(publicHtml.getProp()).setParams(params).setParentKey(new ArrayList<String>(collect));
                    bean.getExpression(designId, "formItemValue", execDto);
                    map.putAll(params);
                }
            }
        }
    }

    private void execExpression(TableFormItemHtml html, Map<String, Object> map, Integer index, ExpressionComponent bean, String designId, String[] parentPath) {
        //触发一下表格的公式， 触发表格的公式事件
        List<String> collect = Arrays.stream(parentPath).collect(Collectors.toList());
        //因为上一层的传递了值，所以需要添加一下这一次的
        collect.remove(collect.size() - 1);
        if (!collect.contains(html.getProp())) {
            collect.add(html.getProp());
        }
        ExecDto execDto = new ExecDto().setIndex(index).setModifiedField(html.getProp()).setParams(map).setParentKey(collect);
        bean.getExpression(designId, "formItemValue", execDto);
    }

    @Override
    public void tableSetData(Map<String, IDataFieldHandler> handlerMap, Map<String, FieldBasicsHtml> fieldsMap, TableFormItemHtml html, Map<String, Object> data, String... parentPath) {
        //数据初始化处理
        if (ObjectNull.isNull(html.getDataModelId())) {
            return;
        }
        List<String> field = html.getTableColumn().stream().map(e -> e.getProp()).collect(Collectors.toList());
        QueryListDto queryPageDto = new QueryListDto();
        queryPageDto.setFieldList(field);
        if (ObjectNull.isNotNull(html.getDataFilterList()) || ObjectNull.isNotNull(html.getDataFilterGroupList())) {
            List<List<FilterHtml>> dataFilterGroupList = CollectionUtils.isNotEmpty(html.getDataFilterGroupList()) ? html.getDataFilterGroupList() : Collections.singletonList(html.getDataFilterList());
            List<List<QueryConditionDto>> collections = dataFilterGroupList.stream().map(filterGroup -> filterGroup.stream()
                            .peek(e -> queryPageDto.getFieldList().add(e.getFieldKey()))
                            .map(e -> {
                                        if (e.getEnabledQueryTypes().equals(DataQueryType.isNull)) {
                                            return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(null).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                        } else if (TypeHtml.prop.equals(e.getType())) {
                                            Object read = JvsJsonPath.read((data), e.getValue().toString());
                                            if (e.getValue() instanceof List) {
                                                if (ObjectNull.isNotNull(e.getValue())) {
                                                    read = JvsJsonPath.read((data), ((List<?>) e.getValue()).stream().map(Object::toString).collect(Collectors.joining(".")));
                                                }
                                            }
                                            if (ObjectNull.isNull(read)) {
                                                return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(read).setEnabledQueryTypes(DataQueryType.isNull);
                                            }
                                            return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(read).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                        } else {
                                            return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(e.getValue()).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                        }
                                    }
                            ).filter(ObjectNull::isNotNull).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            //如果查询条件为空，则直接返回
            if (ObjectNull.isNull(collections) || Boolean.FALSE.equals(collections.stream().filter(ObjectNull::isNotNull).anyMatch(CollectionUtils::isNotEmpty))) {
                return;
            }
            queryPageDto.setGroupConditions(collections);
        }

        DynamicDataService bean = SpringContextUtil.getBean(DynamicDataService.class);
        List<Map<String, Object>> maps = bean.postQueryList(html.getDataModelId(), queryPageDto);
        String path = new ArrayList<String>(Arrays.asList(parentPath))
                .stream()
                .collect(Collectors.joining(StrUtil.DOT));
        Object echoValue = getEchoValue(html, maps, false, new HashMap<>(8), path);
        JSONPath.set(data, path, echoValue);
    }

    @Override
    public Object getEchoValue(TableFormItemHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        //获取表格里面的数据
        //进行数据遍历转换
        if (data instanceof List) {
            //获取表格的字段对象信息
            Map<String, FieldBasicsHtml> tableFieldMap = fieldDto.getTableColumn().stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
            //获取字段的类型处理器
            Map<String, IDataFieldHandler> iDataFieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
            //将数据转换为数组,需要使用一个新的对象，在转换数据时需要行级数据
            List<Map<String, Object>> list = (List<Map<String, Object>>) data;

            //遍历设计结构
            for (String filedKey : tableFieldMap.keySet()) {
                FieldBasicsHtml fieldPublicHtml = tableFieldMap.get(filedKey);
                //解析结构
                IDataFieldHandler handler = iDataFieldHandlerMap.get(fieldPublicHtml.getType().getDesc());
                if (ObjectNull.isNotNull(handler, fieldPublicHtml.getDesignJson())) {
                    fieldPublicHtml = handler.toHtml(fieldPublicHtml.getDesignJson());
                    for (Map<String, Object> body : list) {
                        //获取数据,这里需要创建一个新的对象，在做转换的时候，可能会存在 b下拉的数据来源于 a 下拉的选项属性，转换时将行级数据往下传递
                        Map<String, Object> oldData = new HashMap<>(body);
                        //获取属性值
                        Object data1 = oldData.get(fieldPublicHtml.getProp());
                        //获取数据并进行转换
                        Object echoValue = handler.getEcho(fieldPublicHtml, data1, override, body, paths);
                        String path = DynamicDataUtils.getEchoFieldKey(fieldPublicHtml.getProp());
                        //因为这里是表格里面没有改变原有数据值,所有直接修改 body的值即可
                        setDataOverride(body, filedKey, fieldPublicHtml, path, override, echoValue);
                    }
                }
            }
        }
        return data;

    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"tableForm\",\n" +
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
                "        \"border\",\n" +
                "        \"stripe\",\n" +
                "        \"sqlType\",\n" +
                "        \"editable\"\n" +
                "    ],\n" +
                "    \"border\": true,\n" +
                "    \"page\": false,\n" +
                "    \"editable\": true,\n" +
                "    \"addBtn\": true,\n" +
                "    \"addBtnFormCode\": \"\",\n" +
                "    \"editBtn\": true,\n" +
                "    \"editBtnFormCode\": \"\",\n" +
                "    \"viewBtn\": true,\n" +
                "    \"delBtn\": true,\n" +
                "    \"stripe\": false,\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"tableColumn\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"menuFix\": false,\n" +
                "    \"align\": \"left\",\n" +
                "    \"name\": \"" + DataFieldType.tableForm.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
