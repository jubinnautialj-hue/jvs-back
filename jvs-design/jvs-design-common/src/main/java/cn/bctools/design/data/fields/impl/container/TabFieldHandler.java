package cn.bctools.design.data.fields.impl.container;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表单字段: 选项卡
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "选项卡", type = DataFieldType.tab)
public class TabFieldHandler implements IDataFieldHandler<TabItemHtml> {

    @Override
    public void filterOrDataLinkage(String appId, Map<String, ? extends FieldBasicsHtml> fieldMap, String key, TabItemHtml e, Map<String, Object> map, Integer index, String... parentPath) {
        Map<String, IDataFieldHandler> fieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        //其它容器类型,如果内部设计结构里面包含此控件，需要进行递归处理
        if (ObjectNull.isNull(key)) {
            return;
        }
        if (ObjectNull.isNotNull(e.getDesignJson(), key) && e.getDesignJson().toString().contains(key)) {
            isNext(e);
            if (e.getNext()) {
                e.getDicData().forEach(v -> {
                    List<FieldBasicsHtml> fieldBasicsHtmls = e.getColumn().get(v.getName());
                    if (ObjectNull.isNotNull(fieldBasicsHtmls)) {
                        Map<String, FieldBasicsHtml> fieldPublicHtmls = fieldBasicsHtmls.stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));

                        for (FieldBasicsHtml publicHtml : fieldPublicHtmls.values()) {
                            IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(publicHtml.getType().getDesc());
                            if (ObjectNull.isNotNull(iDataFieldHandler)) {
                                //根据不同的tab，可能两个tab下的不同表格都会发生变化，所以路径，需要重置一下，就new 新数组
                                List<String> strings = new ArrayList<>(Arrays.asList(parentPath));
                                List<String> collect = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.toList());
                                String[] parentPath1;
                                if (e.getDetachData()) {
                                    if (ObjectNull.isNotNull(v.getProp())) {
                                        parentPath1 = collect.toArray(new String[]{v.getProp(), publicHtml.getProp()});
                                    } else {
                                        parentPath1 = new String[]{publicHtml.getProp()};
                                    }
                                } else {
                                    parentPath1 = collect.toArray(new String[0]);
                                }
                                iDataFieldHandler.filterOrDataLinkage(appId, fieldPublicHtmls, key, publicHtml, map, index, parentPath1);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void isNext(TabItemHtml tabItemHtml) {
        if (!tabItemHtml.getColumn().isEmpty()) {
            //如果有值，则进行返回下级
            tabItemHtml.setNext(true);
            Map<String, List<FieldBasicsHtml>> column = tabItemHtml.getColumn();
            if (ObjectNull.isNull(column)) {
                return;
            }
            Map<String, List<FieldBasicsHtml>> map = new HashMap<>(column.size());
            //多个选项卡
            for (Map.Entry<String, List<FieldBasicsHtml>> entry : column.entrySet()) {
                String s = entry.getKey();
                //多个字段
                JSONArray o = (((JSONObject) tabItemHtml.getDesignJson()).getJSONObject(Get.name(TabItemHtml::getColumn))).getJSONArray(s);
                if (ObjectNull.isNull(o)) {
                    continue;
                }
                List<FieldBasicsHtml> list = column.get(s);
                if (ObjectNull.isNull(list)) {
                    continue;
                }
                //判断同级是否有key重复
                Map<String, Integer> listMap = list.stream().map(FieldHtml::getProp).collect(Collectors.toMap(qe -> qe, qe -> 1, Integer::sum));
                for (Map.Entry<String, Integer> mapEnrty : listMap.entrySet()) {
                    if (mapEnrty.getValue() > 1) {
                        throw new BusinessException("下有重复的字段命名请修改", tabItemHtml.getLabel(), mapEnrty.getKey());
                    }
                }


                for (int i = 0; i < list.size(); i++) {
                    FieldPublicHtml fieldBasicsHtml = list.get(i);
                    if (ObjectNull.isNull(fieldBasicsHtml.getType()) && fieldBasicsHtml.getProp().startsWith(DataFieldType.SWITCH.toString().toLowerCase())) {
                        fieldBasicsHtml.setType(DataFieldType.SWITCH);
                    }
                    IDataFieldHandler bean = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class).get(fieldBasicsHtml.getType().getDesc());
                    if (ObjectNull.isNotNull(bean)) {
                        FieldBasicsHtml publicHtml = bean.toHtml(o.getJSONObject(i));
                        list.set(i, publicHtml);
                    }
                }
                map.put(s, list);
            }
            tabItemHtml.setColumn(map);
        }
    }

    @Override
    public void next(List<ElementVo> list, TabItemHtml html, Map<String, IDataFieldHandler> handlerMap, ElementVo e) {
        //判断是否开启了数据脱离，公式 会存在两种方式
        //选项卡的下级
        for (FormValueHtml dicDatum : html.getDicData()) {
            //获取下级的字段控件
            List<FieldBasicsHtml> publicHtmls = html.getColumn().get(dicDatum.getName());
            if (ObjectNull.isNull(publicHtmls)) {
                //如果为空直接跳过
                continue;
            }

            if (html.getDetachData()) {
                String prop = dicDatum.getProp();
                ElementVo elementVo;
                if (ObjectNull.isNotNull(prop)) {
                    elementVo = new ElementVo().setId(prop).setPath(e.getPath() + StrUtil.DOT + dicDatum.getName()).setName(dicDatum.getLabel() + StrUtil.DOT + prop).setShortName(prop).setInfo(dicDatum.getLabel() + StrUtil.DOT + prop).setJvsParamType(JvsParamType.object);
                    list.add(elementVo);
                }
                for (FieldBasicsHtml baseItemHtml : publicHtmls) {
                    IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
                    //如果是样式组件或基础用户组件则不向下递归处理
                    if (ObjectNull.isNull(iDataFieldHandler, baseItemHtml.getDesignJson())) {
                        continue;
                    }
                    ElementVo paramType;
                    //添加组件
                    if (ObjectNull.isNotNull(prop)) {
                        //如果不为空，公式数据为属性名加值
                        paramType = new ElementVo().setInfo(baseItemHtml.getProp() + "  " + baseItemHtml.getLabel() + "\n" + baseItemHtml.getType().getDesc()).setId(prop + StrUtil.DOT + baseItemHtml.getProp()).setPath(e.getPath() + StrUtil.DOT + dicDatum.getName() + StrUtil.DOT + baseItemHtml.getProp()).setType("请求入参").setName(prop + StrUtil.DOT + baseItemHtml.getLabel()).setFieldType(baseItemHtml.getType().name()).setShortName(dicDatum.getProp() + StrUtil.DOT + baseItemHtml.getLabel()).setJvsParamType(JvsParamType.getByClass(baseItemHtml.getType().getAClass()));
                        list.add(paramType);
                    } else {
                        paramType =
                                new ElementVo().setInfo(baseItemHtml.getProp() + "  " + baseItemHtml.getLabel() + "\n" + baseItemHtml.getType().getDesc()).setId(baseItemHtml.getProp()).setPath(e.getPath() + StrUtil.DOT + dicDatum.getName() + StrUtil.DOT + baseItemHtml.getProp()).setType("请求入参").setName(baseItemHtml.getLabel()).setFieldType(baseItemHtml.getType().name()).setShortName(baseItemHtml.getLabel()).setJvsParamType(JvsParamType.getByClass(baseItemHtml.getType().getAClass()));
                        list.add(paramType);
                    }
                    FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml);
                    if (publicHtml.getNext()) {
                        //判断有没有下级
                        iDataFieldHandler.next(list, publicHtml, handlerMap, paramType);
                    }
                }
                //需要将此字段删除掉，选项字段为另一个字段
                list.remove(e);
            } else {
                //添加tab级
                ElementVo elementVo = new ElementVo().setId(e.getId() + StrUtil.DOT + dicDatum.getName()).setPath(e.getPath() + StrUtil.DOT + dicDatum.getName()).setFieldType(html.getType().name()).setType("请求入参").setName(e.getName() + StrUtil.DOT + dicDatum.getLabel()).setShortName(e.getShortName() + StrUtil.DOT + dicDatum.getLabel()).setJvsParamType(JvsParamType.object);
                list.add(elementVo);
                for (FieldPublicHtml baseItemHtml : publicHtmls) {
                    //添加组件
                    ElementVo paramType = new ElementVo().setId(elementVo.getId() + StrUtil.DOT + baseItemHtml.getProp()).setPath(e.getPath() + StrUtil.DOT + baseItemHtml.getName()).setType("请求入参").setName(elementVo.getName() + StrUtil.DOT + baseItemHtml.getLabel()).setFieldType(baseItemHtml.getType().name()).setShortName(elementVo.getShortName() + StrUtil.DOT + baseItemHtml.getLabel()).setJvsParamType(JvsParamType.getByClass(baseItemHtml.getType().getAClass()));
                    list.add(paramType);

                    IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
                    FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml.getDesignJson());
                    //判断有没有下级
                    iDataFieldHandler.next(list, publicHtml, handlerMap, paramType);
                }
            }
        }
    }

    @Override
    public void next(List<ElementVo> list, TabItemHtml html, Function<FieldBasicsHtml, ElementVo> function, Map<String, IDataFieldHandler> handlerMap, String name, String prop) {
        //选项卡的下级
        Map<String, List<FieldBasicsHtml>> column = html.getColumn();

        List<FormValueHtml> dicData = html.getDicData();

        //循环tab
        for (FormValueHtml dicDatum : dicData) {
            List<FieldBasicsHtml> baseItemHtmls = column.get(dicDatum.getName());
            if (ObjectNull.isNull(baseItemHtmls)) {
                //如果为空直接跳过
                continue;
            }
            String label = dicDatum.getLabel();


            String tabProp = prop + StrUtil.DOT + dicDatum.getName();
            String talName = name + StrUtil.DOT + label;
            FieldBasicsHtml basicsHtml = new FieldBasicsHtml();
            basicsHtml.setFieldName(talName).setFieldKey(tabProp).setType(DataFieldType.tab);
            list.add(function.apply(basicsHtml));


            //一个tab下的所有字段

            if (ObjectNull.isNull(baseItemHtmls)) {
                continue;
            }
            for (FieldPublicHtml baseItemHtml : baseItemHtmls) {
                //多个控件
                String props = tabProp + StrUtil.DOT + baseItemHtml.getProp();
                String names = talName + StrUtil.DOT + baseItemHtml.getLabel();
                FieldBasicsHtml tableBasicsHtml = new FieldBasicsHtml();
                tableBasicsHtml.setFieldKey(props).setFieldName(names).setType(baseItemHtml.getType());
                list.add(function.apply(tableBasicsHtml));

                IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml.getDesignJson());
                //判断有没有下级
                iDataFieldHandler.next(list, publicHtml, function, handlerMap, names, props);
            }
        }
    }

    @Override
    public void tableSetData(Map<String, IDataFieldHandler> handlerMap, Map<String, FieldBasicsHtml> fieldsMap, TabItemHtml html, Map<String, Object> data, String... parentPath) {
        //选项卡的下级
        Map<String, List<FieldBasicsHtml>> column = html.getColumn();
        List<FormValueHtml> dicData = html.getDicData();
        //循环tab
        for (FormValueHtml dicDatum : dicData) {
            List<FieldBasicsHtml> baseItemHtmls = column.get(dicDatum.getName());
            if (ObjectNull.isNull(baseItemHtmls)) {
                //如果为空直接跳过
                continue;
            }
            for (FieldBasicsHtml baseItemHtml : baseItemHtmls) {
                if (baseItemHtml.getType().equals(DataFieldType.tableForm)) {
                    //下一层，其它的不处理
                    IDataFieldHandler iDataFieldHandler = handlerMap.get(baseItemHtml.getType().getDesc());
                    FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(baseItemHtml.getDesignJson());
                    String[] parentPath1 = null;
                    if (html.getDetachData()) {
                        List<String> strings = new ArrayList<>();
                        if (ObjectNull.isNotNull(dicDatum.getProp())) {
                            strings.add(dicDatum.getProp());
                        }
                        strings.add(baseItemHtml.getProp());
                        parentPath1 = strings.toArray(new String[0]);
                    } else {
                        List<String> strings = new ArrayList<>(Arrays.asList(parentPath));
                        strings.add(dicDatum.getName());
                        strings.add(baseItemHtml.getProp());
                        parentPath1 = strings.toArray(new String[0]);
                    }
                    //处理下级
                    iDataFieldHandler.tableSetData(handlerMap, fieldsMap, publicHtml, data, parentPath1);
                }
            }
        }

    }

    @Override
    public Object getEchoValue(TabItemHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        Map<String, IDataFieldHandler> dataFieldHandlerMap = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);
        //判断是否添加了数据脱离 如果打开了数据脱离,获取的数据将不根据属性判断
        Map<String, List<FieldBasicsHtml>> column = fieldDto.getColumn();
        if (fieldDto.getDetachData()) {
            //判断是否配置了 key
            for (FormValueHtml dicDatum : fieldDto.getDicData()) {
                if (ObjectNull.isNull(column.get(dicDatum.getName()))) {
                    continue;
                }
                //获取属性的 key
                String field = dicDatum.getProp();
                Map<String, FieldBasicsHtml> fieldPublicHtmlMap = column.get(dicDatum.getName()).stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
                if (ObjectNull.isNotNull(field)) {
                    //配置的有属性，直接返回为对象,这里做一个强制类型转换
                    LinkedHashMap<String, Object> fieldMap = new LinkedHashMap((LinkedHashMap) lineData.get(field));
                    //将返回的对象根据数据类型进行解析
                    for (Map.Entry<String, FieldBasicsHtml> entry : fieldPublicHtmlMap.entrySet()) {
                        String fieldKey = entry.getKey();
                        FieldBasicsHtml fieldPublicHtml = fieldPublicHtmlMap.get(fieldKey);
                        IDataFieldHandler iDataFieldHandler = dataFieldHandlerMap.get(fieldPublicHtml.getType().getDesc());
                        if (ObjectNull.isNotNull(iDataFieldHandler)) {
                            fieldPublicHtml = iDataFieldHandler.toHtml(fieldPublicHtml.getDesignJson());
                            //此处需要重新获取值，是需要在对象中获取数据值
                            Object echoValue = iDataFieldHandler.getEchoValue(fieldPublicHtml, fieldMap.get(fieldKey), override, lineData, paths);
                            setDataOverride(data, fieldKey, fieldPublicHtml, fieldKey, override, echoValue);
                        }
                    }
                    lineData.putAll(fieldMap);
                } else {
                    //没有配置属性时，获取同层级的所有数据即可
                    //获取数据结构
                    for (Map.Entry<String, FieldBasicsHtml> entry : fieldPublicHtmlMap.entrySet()) {
                        String fieldKey = entry.getKey();
                        FieldBasicsHtml fieldPublicHtml = entry.getValue();
                        IDataFieldHandler iDataFieldHandler = dataFieldHandlerMap.get(fieldPublicHtml.getType().getDesc());
                        if (ObjectNull.isNotNull(iDataFieldHandler)) {
                            fieldPublicHtml = iDataFieldHandler.toHtml(fieldPublicHtml.getDesignJson());
                            Object filedData = lineData.get(fieldKey);
                            if (ObjectNull.isNotNull(filedData)) {
                                Object echoValue = iDataFieldHandler.getEchoValue(fieldPublicHtml, filedData, override, lineData, paths);
                                setDataOverride(data, fieldKey, fieldPublicHtml, fieldKey, override, echoValue);
                            }
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<String, List<FieldBasicsHtml>> mapEntry : column.entrySet()) {
                String key = mapEntry.getKey();
                List<FieldBasicsHtml> value = mapEntry.getValue();
                if (ObjectNull.isNull(value)) {
                    continue;
                }
                Map<String, FieldPublicHtml> fieldPublicHtmls = value.stream().collect(Collectors.toMap(FieldPublicHtml::getProp, Function.identity()));
                for (Map.Entry<String, FieldPublicHtml> entry : fieldPublicHtmls.entrySet()) {
                    String filedKey = entry.getKey();
                    FieldPublicHtml fieldPublicHtml = fieldPublicHtmls.get(filedKey);
                    IDataFieldHandler iDataFieldHandler = dataFieldHandlerMap.get(fieldPublicHtml.getType().getDesc());
                    if (ObjectNull.isNotNull(iDataFieldHandler, fieldPublicHtml.getDesignJson())) {
                        FieldBasicsHtml html = iDataFieldHandler.toHtml(fieldPublicHtml.getDesignJson());
                        String path = DynamicDataUtils.getEchoFieldKey(html.getProp());
                        Object eval = JvsJsonPath.read(data, key + StrUtil.DOT + html.getProp());
                        //获取子集转换
                        Object echoValue = iDataFieldHandler.getEcho(html, eval, override, lineData, path);
                        //替换数据
                        setDataOverride(data, filedKey, html, path, override, echoValue);
                    }
                }
            }
        }
        lineData.put(fieldDto.getProp(), data);
        //将源数据返回,tab返回和不返回都是一样的
        return data;
    }


    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" + "    \"prop\": \"" + field + "\",\n" + "    \"type\": \"tab\",\n" + "    \"label\": \"" + name + "\",\n" + "    \"span\": 24,\n" + "    \"display\": true,\n" + "    \"status\": \"\",\n" + "    \"tips\": {\n" + "        \"text\": \"\",\n" + "        \"position\": \"right\"\n" + "    },\n" + "    \"showFrom\": [\n" + "        \"label\",\n" + "        \"span\",\n" + "        \"prop\",\n" + "        \"url\",\n" + "        \"datatype\",\n" + "        \"dicData\",\n" + "        \"jurisdiction\",\n" + "        \"sqlType\",\n" + "        \"option\"\n" + "    ],\n" + "    \"datatype\": \"option\",\n" + "    \"url\": \"\",\n" + "    \"dicData\": [\n" + "        {\n" + "            \"label\": \"选项一\",\n" + "            \"name\": \"first\"\n" + "        },\n" + "        {\n" + "            \"label\": \"选项二\",\n" + "            \"name\": \"second\"\n" + "        },\n" + "        {\n" + "            \"label\": \"选项三\",\n" + "            \"name\": \"third\"\n" + "        }\n" + "    ],\n" + "    \"activeName\": \"first\",\n" + "    \"column\": {\n" + "        \"undefined\": [\n" + "\n" + "        ]\n" + "    },\n" + "    \"rules\": [\n" + "\n" + "    ],\n" + "    \"showJurisdiction\": [\n" + "        \"所有用户\"\n" + "    ],\n" + "    \"sqlType\": \"object\",\n" + "    \"linkbind\": \"\",\n" + "    \"name\": \"" + DataFieldType.tab.getDesc() + "\",\n" + "    \"disabled\": false\n" + "}";
        return JSONObject.parseObject(str);
    }
}
