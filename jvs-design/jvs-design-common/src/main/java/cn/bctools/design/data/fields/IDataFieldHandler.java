package cn.bctools.design.data.fields;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.function.component.ExpressionComponent;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.vo.ElementVo;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.validation.constraints.NotNull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字段处理接口
 * 所有字段的解析转换和数据处理，都需要实现此接口类
 *
 * @param <T> the type parameter
 * @Author: GuoZi
 */
public interface IDataFieldHandler<T extends FieldBasicsHtml> {
    
    Logger log = LoggerFactory.getLogger(IDataFieldHandler.class);

    /**
     * The constant TYPE.
     * 基础常量
     */
    String TYPE = "type";

    /**
     * 获取回显数据
     * 如  1>男
     * 0>女
     *
     * @param fieldDto 回显字段的设计数据
     * @param data     数据值
     * @param override 是否覆盖子数据的原始值
     * @param lineData 原始行级数据，用于逻辑的字典转换，如， 省市区县， 多个字段进行联合转换时
     * @param paths    the paths  路径，某些情况是组件存在嵌套，需要嵌套路径
     * @return 显示值 echo
     */
    default Object getEcho(@NotNull T fieldDto, @NotNull Object data, boolean override, Map<String, Object> lineData, String... paths) {
        if (ObjectNull.isNull(data)) {
            return fieldDto.getDefaultValue();
        } else {
            return getEchoValue(fieldDto, data, override, lineData, paths);
        }
    }

    /**
     * Gets echo value.
     *
     * @param fieldDto the field dto  字段对象
     * @param data     the data   当前这个字段的数据
     * @param override the override
     * @param lineData the line data
     * @param paths    the paths
     * @return the echo value
     */
    default Object getEchoValue(@NotNull T fieldDto, @NotNull Object data, boolean override, Map<String, Object> lineData, String... paths) {
        return data;
    }

    /**
     * 通过显示转换为数据，  默认组件不转换
     * 与getEchoValue 正好相反
     * 如  男>1
     * 女>0
     *
     * @param dto      the dto  字段对象
     * @param o        the o  当前字段的数据
     * @param lineData the line 当前行级数据
     * @return conversion key
     */
    default Object getConversionKey(T dto, Object o, Map<String, Object> lineData) {
        return o;
    }

    /**
     * 通过显示转换为数据，  默认组件不转换
     * 与getEchoValue 正好相反
     * 如  男>1
     * 女>0
     *
     * @param cascaderFieldPathIdsMap 处理的 id 值如果转换过程中 Id不存在，则需要将其对象放到这对象里面
     * @param dto                     the dto  字段对象
     * @param o                       the o  当前字段的数据
     * @param lineData                the line 当前行级数据
     * @param generateCascaderList    如果是树形结构，存在中间数据时会直接将产生的中间数据添加到此对象中，   当数据库中一条数据都没有，新增时   /1/2/3， 将会新增三条数据
     * @return conversion key
     */
    default String getConversionKey(T dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        return o.toString();
    }

    /**
     * 回显数据是否覆盖值
     *
     * @return TRUE -覆盖，FALSE-不覆盖
     */
    default Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    /**
     * 获取支持的查询类型
     *
     * @param html 字段设计数据
     * @return 查询类型集合 enabled query types
     */
    default List<DataQueryType> getEnabledQueryTypes(T html) {
        ArrayList<DataQueryType> dataQueryTypes = new ArrayList<>();
        dataQueryTypes.add(DataQueryType.like);
        dataQueryTypes.add(DataQueryType.isNull);
        dataQueryTypes.add(DataQueryType.eq);
        dataQueryTypes.add(DataQueryType.ne);
        return dataQueryTypes;
    }

    /**
     * 将组件的设计数据直接转换为Html对象
     *
     * @param fieldJson 字段设计结构转换的map
     * @return 返回字段泛型对象 t
     */
    default T toHtml(Map<String, Object> fieldJson) {
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(fieldJson));
        if (ObjectNull.isNull(jsonObject)) {
            return null;
        }
        Class<? extends FieldPublicHtml> cls = getCls(this);
        //选择类型特殊处理解析
        if (jsonObject.containsKey(TYPE) && jsonObject.getString(TYPE).equals(DataFieldType.SWITCH.toString().toLowerCase())) {
            jsonObject.put(TYPE, DataFieldType.SWITCH);
        }
        if (DataFieldType.tableForm.name().equals(jsonObject.getString(TYPE)) || DataFieldType.tab.name().equals(jsonObject.getString(TYPE))) {
            String fieldJsonStr = JSON.toJSONString(jsonObject).replace("\"type\":\"switch\"", "\"type\":\"" + DataFieldType.SWITCH.name() + "\"");
            jsonObject = JSON.parseObject(fieldJsonStr);
        }
        FieldPublicHtml t = BeanCopyUtil.copy(cls, jsonObject);
        t.setDataFilterEnable(jsonObject.getBooleanValue("dataFilterEnable", false));
        t.setDesignJson(jsonObject);
        isNext((T) t);
        return (T) t;
    }


    /**
     * 根据处理类获取字段对象信息
     *
     * @param a the a  当前这个组件的解析处理类
     * @return cls  返回当前这个组件的数据结构实体类 cls
     */
    default Class<? extends FieldPublicHtml> getCls(IDataFieldHandler<T> a) {
        Type obj = a.getClass().getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) obj;
        Class<? extends FieldPublicHtml> cls = (Class) parameterizedType.getActualTypeArguments()[0];
        return cls;
    }

    /**
     * 是否还有下级嵌套，如果有直接保存
     * 目前只有表格和选项卡才重写此方法参数
     *
     * @param t 根据泛型判断是否存在下集类型
     */
    default void isNext(T t) {
    }

    /**
     * 解析下级字段
     * 于用公式逻辑处理
     * 此方法已经摒弃,使用 {@link #next(List, FieldBasicsHtml, Map, ElementVo) }代替
     *
     * @param list       公式数据
     * @param publicHtml 字段对象信息
     * @param function   字段转换器
     * @param handlerMap 字段处理器
     * @param name       名
     * @param prop       父级
     */
    default void next(List<ElementVo> list, T publicHtml, Function<FieldBasicsHtml, ElementVo> function, Map<String, IDataFieldHandler> handlerMap, String name, String prop) {
    }

    /**
     * 重载next方法
     * 于用公式逻辑处理  当表单或逻辑在取表单设计时可以在公式中直接取到所有组件的公式变量值。
     *
     * @param list       the list    所有的公式变量
     * @param publicHtml the public html  当前组件的 html
     * @param handlerMap the handler map 所有属性对应的组件处理类对象
     * @param e          the e  上一级的公式对象，在下级使用时需要根据上级的 Id 进行依次替换添加
     */
    default void next(List<ElementVo> list, T publicHtml, Map<String, IDataFieldHandler> handlerMap, ElementVo e) {

    }

    /**
     * 数据联动
     *
     * @param appId      the app id  应用的 id
     * @param fieldMap   the field map  所有属性对应的组件处理类对象
     * @param key        the key  当前触发联动的 key
     * @param e          the e    当前组件的对象的 html
     * @param map        the map   当前组件对象
     * @param index      the index   如果存在行操作的行号，默认以 0 开始 ， 此属性在方法中示使用到
     * @param parentPath the parent path   对应的父级路径信息，嵌套关系中依次排列为数组
     */
    default void dataLinkage(String appId, Map<String, ? extends FieldPublicHtml> fieldMap, String key, T e, Map<String, Object> map, Integer index, String... parentPath) {
        if (ObjectNull.isNotNull(e.getDataLinkageList())) {
            if (ObjectNull.isNotNull(e.getDataLinkageEnable())) {
                if (!e.getDataLinkageEnable()) {
                    return;
                }
            }
            DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);

            //数据联动，获取当前控件的联动
            List<String> collect = e.getDataLinkageList().stream().map(s -> String.valueOf(s.getValue())).collect(Collectors.toList());
            //判断里面
            if (map.keySet().containsAll(collect)) {
                collect.add(e.getLinkageFieldKey());
                //判断是否是时间段类型,如果为空数组则设置为空串,避免设置不成功
                if (e.getType().equals(DataFieldType.timePicker)) {
                    //判断是否有值
                    if (ObjectNull.isNull(map.get(e.getProp()))) {
                        map.put(e.getProp(), "");
                    }
                }
                // 排除不参与数据联动的数据
                if (DataFieldType.flowNode.equals(e.getType())) {
                    map.remove(e.getProp());
                }

                //查询条件字段值
                List<QueryConditionDto> queryConditionDtos = e.getDataLinkageList().stream().filter(s -> fieldMap.containsKey(s.getValue())).map(s -> {
                    //获取数据  如果是下拉框，或者是文本框的选择方式不一样
                    Object value = map.get(s.getValue());
                    //判断是不是特殊类型的值

                    Map<String, IDataFieldHandler> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType(IDataFieldHandler.class);

                    //判断值是什么类型
                    //如果是下拉类型
                    switch (fieldMap.get(s.getValue()).getType()) {
                        case select:
                            MultipleHtml publicHtml = (MultipleHtml) beansOfType.get(fieldMap.get(s.getValue()).getType().getDesc()).toHtml(fieldMap.get(s.getValue()).getDesignJson());
                            publicHtml.setLinkageFieldKey(s.getFieldKey());
                            // 下拉框存数据id，表格里的下拉框联动不只存了数据id。如果只有数据id，则查询数据
                            Object selectDataId = map.get(Get.name(DynamicDataPo::getId));
                            if (value.equals(selectDataId)) {
                                Map<String, Object> dataMap = dynamicDataService.querySingle(appId, e.getDataLinkageModelId(), selectDataId.toString());
                                value = dataMap.get(s.getFieldKey());
                            }
                            break;
                        default:

                    }
                    QueryConditionDto queryConditionDto = s.setFieldKey(String.valueOf(s.getFieldKey())).setValue(value);
                    return queryConditionDto;
                }).collect(Collectors.toList());
                // 需要有查询条件查询数据进行替换
                if (ObjectNull.isNotNull(queryConditionDtos) && e.getDataLinkageList().size() == queryConditionDtos.size() && ObjectNull.isNotNull(e.getDataLinkageModelId())) {
                    //如果是动态流程，并不存在这个值时
                    if ((!map.containsKey(e.getProp())) && DataFieldType.flowNode.equals(e.getType())) {
                        setValue(appId, e.getDataLinkageModelId(), e, map, collect, Collections.singletonList(queryConditionDtos), parentPath);
                    } else {
                        boolean o = false;
                        try {
                            o = SystemThreadLocal.get("designSkip");
                        } catch (Exception ex) {

                        }
                        if (!o) {
                            setValue(appId, e.getDataLinkageModelId(), e, map, collect, Collections.singletonList(queryConditionDtos), parentPath);
                        }
                    }
                } else {
                    //如果触发器为空,则关联值为默认值
                    if (ObjectNull.isNull(map.get(key))) {
                        List<String> strings = new ArrayList<>(Arrays.asList(parentPath));
                        //如果是容器类型才加，如果不是就不加
                        if (DataFieldType.CONTAINER.contains(e.getType())) {
                            strings.add(e.getProp());
                            strings = strings.stream().distinct().collect(Collectors.toList());
                        }
                        String paths = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining(StrUtil.DOT));
                        Object obj = e.getDefaultValue();
                        //如果默认值不为空的时候才进行设置值
                        if (ObjectNull.isNotNull(obj)) {
                            switch (e.getType()) {
                                case SWITCH:
                                    obj = Boolean.valueOf(e.getDefaultValue().toString());
                                    break;
                                case inputNumber:
                                    obj = Integer.valueOf(e.getDefaultValue().toString());
                                    break;
                                default:
                            }
                        }
                        if (ObjectNull.isNotNull(paths, obj)) {
                            JSONPath.set(map, paths, obj);
                        }
                    }
                }
            }
            ExpressionComponent bean = SpringContextUtil.getBean(ExpressionComponent.class);
            String designId = SystemThreadLocal.get("designId");
            ExecDto execDto = new ExecDto().setIndex(index).setModifiedField(e.getProp()).setParams(map).setParentKey(collect);
            bean.getExpression(designId, "formItemValue", execDto);
        }
    }


    /**
     * 数据筛选或数据联动,根据类型直接操作下一层
     * 联动是不关心key的。 筛选才关心Key
     * 联动与筛选两者不冲突
     *
     * @param appId      应用id
     * @param fieldMap   字段map对象
     * @param key        关联触发的key  前端发生改变的key的值,注意，此key是带有jsonpath路径的
     * @param e          泛型对象信息
     * @param map        前端的数据
     * @param index      表格操作时的行级索引
     * @param parentPath 如果存在内嵌时的内嵌路径
     */
    default void filterOrDataLinkage(String appId, Map<String, ? extends FieldBasicsHtml> fieldMap, String key, T e, Map<String, Object> map, Integer index, String... parentPath) {
        dataLinkage(appId, fieldMap, key, e, map, index, parentPath);
    }

    /**
     * 添加下级字段
     *
     * @param fields 需要添加的字段类型
     * @param e      泛型数据对象
     */
    default void addFields(List<String> fields, T e) {
    }

    /**
     * 触发了联动后再触发公式
     */
    /**
     * 根据查询条件和数据,将查询数据放到对象中去替换原有的数据值
     *
     * @param appId              应用id
     * @param modelId            模型id
     * @param e                  数据泛型对象
     * @param map                原始数据对象
     * @param collect            数据字段
     * @param queryConditionDtos 查询条件
     * @param parentPath         操作的数据路径
     */
    default void setValue(String appId, String modelId, T e, Map<String, Object> map, List<String> collect, List<List<QueryConditionDto>> queryConditionDtos, String... parentPath) {
        collect.add("id");
        queryConditionDtos = queryConditionDtos.stream().map(condition -> condition.stream().filter(o -> o.getEnabledQueryTypes().equals(DataQueryType.isNull) || ObjectNull.isNotNull(o.getValue())).collect(Collectors.toList())).collect(Collectors.toList());
        if (ObjectNull.isNull(queryConditionDtos)) {
            return;
        }
        Criteria criteria = DynamicDataUtils.buildDynamicGroupCriteria(queryConditionDtos);
        criteria = DynamicDataUtils.initCriteria(criteria);
        //只返回一条，如果有多条，只返回第一条
        DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);
        Sort sort = Sort.by(Get.name(DynamicDataPo::getCreateTime)).descending();
        List<Map> maps = dynamicDataService.queryList(appId, modelId, criteria, sort, new ArrayList<>(), collect);
        //获取返回的数据
        List<String> strings = new ArrayList<>(Arrays.asList(parentPath));
        //如果是容器类型才加，如果不是就不加
        if (DataFieldType.CONTAINER.contains(e.getType())) {
            strings.add(e.getProp());
            strings = strings.stream().distinct().collect(Collectors.toList());
        }
        String paths = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining(StrUtil.DOT));
        if (ObjectNull.isNull(paths)) {
            return;
        }
        switch (e.getType()) {
            case file:
            case fileUpload:
            case image:
            case imageUpload:
                //只取第一个数据进行处理
                if (!map.isEmpty() && !maps.isEmpty()) {
                    JSONPath.set(map, paths, maps.get(0).getOrDefault(e.getLinkageFieldKey(), ""));
                }
                return;
            default:
                setValue(maps, e, map, paths);
        }

    }

    /**
     * 数据模型查询出来的值进行数据回填写
     *
     * @param maps  数据集
     * @param e     泛型数据对象
     * @param map   原始数据对象信息
     * @param paths 操作的数据路径
     */
    default void setValue(List<Map> maps, T e, Map<String, Object> map, String paths) {
        //如果设置值为空，则直接返回
        if (ObjectNull.isNull(paths)) {
            return;
        }
        log.info("[数据联动setValue] 字段: {}, 类型: {}, linkageFieldKey: {}, paths: {}, 查询结果数量: {}", 
                e.getProp(), e.getType(), e.getLinkageFieldKey(), paths, maps == null ? "null" : maps.size());
        if (ObjectNull.isNotNull(maps) && !maps.isEmpty()) {
            log.info("[数据联动setValue] 查询结果第一条数据: {}", maps.get(0));
        }
        //如果是表格组件才处理
        if (e.getType().equals(DataFieldType.tableForm)) {
            if (ObjectNull.isNull(maps)) {
                if (ObjectNull.isNotNull(paths)) {
                    //联动筛选后，查询的数据可能为空，需要将空值，也存放进去
                    JSONPath.set(map, paths, maps);
                }
            }
        }
        Object read = "";
        try {//如果前端是数组， 则设置数组，如果是对象，就设置对象。根据前端 的数据结构定
            read = JvsJsonPath.read(JSON.toJSONString(map), paths);
        } catch (Exception exception) {
            //有可能获取不了数据
            return;
        }
        /**
         * 特殊类型的关联数据值设置关系
         */
        switch (e.getType()) {
            case file:
            case image:
            case signature:
            case imageUpload:
            case fileUpload:
                if (ObjectNull.isNotNull(maps)) {
                    Map<String, Object> stringObjectMap = maps.get(0);
                    JSONPath.set(map, paths, stringObjectMap.getOrDefault(e.getLinkageFieldKey(), ""));
                    //同时触发一下公式， 看是否有其它数据执行
                } else {
                    //联动筛选为空,直接设置为空
                    if (ObjectNull.isNotNull(paths)) {
                        JSONPath.set(map, paths, "");
                    }
                }
                break;
            case htmlEditor:
                if (ObjectNull.isNotNull(maps)) {
                    Map<String, Object> stringObjectMap = maps.get(0);
                    JSONPath.set(map, paths, stringObjectMap.getOrDefault(e.getLinkageFieldKey(), "<p></p>"));
                    //同时触发一下公式， 看是否有其它数据执行
                } else {
                    JSONPath.set(map, paths, "<p></p>");
                }
                break;
            case user:
            case department:
            case role:
                if (read instanceof List) {
                    log.info("[数据联动setValue] user类型字段 {} 为数组,从查询结果提取linkageFieldKey", e.getProp());
                    Object o = maps.stream().map(a -> JvsJsonPath.read(a, e.getLinkageFieldKey())).collect(Collectors.toList()).get(0);
                    log.info("[数据联动setValue] user类型字段 {} 提取结果: {}", e.getProp(), o);
                    JSONPath.set(map, paths, o);
                } else {
                    if (ObjectNull.isNotNull(maps)) {
                        Object o = maps.get(0).get(e.getLinkageFieldKey());
                        log.info("[数据联动setValue] user类型字段 {} 单值模式,提取linkageFieldKey值: {}", e.getProp(), o);
                        JSONPath.set(map, paths, o);
                    }
                }
                break;
            default:
                if (read instanceof List) {
                    log.info("[数据联动setValue] 字段 {} 为数组类型,设置联动结果数组", e.getProp());
                    JSONPath.set(map, paths, maps);
                } else {
                    if (ObjectNull.isNotNull(maps)) {
                        Map<String, Object> stringObjectMap = maps.get(0);
                        Object linkageValue = stringObjectMap.getOrDefault(e.getLinkageFieldKey(), "");
                        Object originalValue = map.get(e.getProp());
                        log.info("[数据联动setValue] 字段 {} 设置联动值 - 原始值: {}, 联动键: {}, 联动值: {}", 
                                e.getProp(), originalValue, e.getLinkageFieldKey(), linkageValue);
                        JSONPath.set(map, paths, linkageValue);
                        //同时触发一下公式， 看是否有其它数据执行
                    } else {
                        //联动筛选为空,直接设置为空
                        log.warn("[数据联动setValue] 字段 {} 联动查询结果为空,设置为空值", e.getProp());
                        JSONPath.set(map, paths, "");
                    }
                }
        }

    }

    /**
     * 初始化的时候，将设计里面的表格做一次数据筛选
     *
     * @param handlerMap 数据处理类
     * @param fieldsMap  数据转换器
     * @param html       泛型数据对象
     * @param data       原始数据
     * @param parentPath 数据操作路径
     */
    default void tableSetData(Map<String, IDataFieldHandler> handlerMap, Map<String, FieldBasicsHtml> fieldsMap, T html, Map<String, Object> data, String... parentPath) {
        //只有表格和徐选项卡才有这个功能
    }

    /**
     * To html t.
     *
     * @param e the e
     * @return the t
     */
    default T toHtml(T e) {
        //部分类型不需要解析处理
        if (e.getType().equals(DataFieldType.tabGenerate)) {
            return e;
        }
        return toHtml(e.getDesignJson());
    }

    /**
     * 将数据值设置到对象中
     *
     * @param data            数据
     * @param fieldKey        属性 Key
     * @param fieldPublicHtml 组件类型
     * @param path            key的路径
     * @param override        是否覆盖
     * @param echoValue       转换后的值
     */
    default void setDataOverride(Object data, String fieldKey, FieldBasicsHtml fieldPublicHtml, String path, boolean override, Object echoValue) {
        switch (fieldPublicHtml.getType()) {
            case tab:
                //以上类型不处理替换
                //在节点处理类进行处理了
                break;
            case select:
            case radio:
            case cascader:
            case checkbox:
            case user:
            case department:
            case SWITCH:
            case role:
            case datePicker:
            case timeSelect:
            case timePicker:
            case job:
                //根据传递的类型处理是否转换或替换
                if (override) {
                    //直接覆盖
                    JSONPath.set(data, fieldKey, echoValue);
                } else {
                    JSONPath.set(data, path, echoValue);
                }
                break;
            default:
                //其它类型需要进行直接替换，不用判断是否需要替换
                JSONPath.set(data, fieldKey, echoValue);
                break;
        }
    }

    /**
     * 检查数据类型是否符合规范
     *
     * @param t 设计结构
     * @param o 数据
     * @throws Exception the exception
     */
    default Object checkDataFieldType(T t, Object o) throws Exception {
        //默认不处理数据类型格式
        return o;
    }

    /**
     * 用户修改表单时判断对比字段属性值是否发生变更，给于变更提示
     *
     * @param html   当前这次用户提交的设计
     * @param dbHtml 数据库上一次用户的设计
     * @throws Exception the exception
     */
    default void checkFieldTypeAttributeChanged(T html, T dbHtml) throws Exception {

    }

    /**
     * 生成一个组件
     *
     * @param name    组件名称
     * @param field
     * @param dicData 多选 组件的属性
     * @return
     */
    default Map<String, Object> generate(String name, String field, List<String> dicData) {
        throw new BusinessException(name + "类型不支持");
    }
}
