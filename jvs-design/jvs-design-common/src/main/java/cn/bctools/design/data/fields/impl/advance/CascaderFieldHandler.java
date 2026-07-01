package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.constant.AppConstant;
import cn.bctools.design.crud.entity.JvsTree;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.QueryListDto;
import cn.bctools.design.data.fields.dto.TreeDictDto;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.item.CascaderItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单字段: 级联选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "级联选择", type = DataFieldType.cascader)
@AllArgsConstructor
public class CascaderFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<CascaderItemHtml> {

    JvsTreeService jvsTreeService;

    @Override
    public List<DataQueryType> getEnabledQueryTypes(CascaderItemHtml html) {
        return super.getEnabledQueryTypes(html);
    }

    @Override
    public Object getEchoValue(CascaderItemHtml cascaderItem, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        String dictName = cascaderItem.getDictName();
        boolean isMulti = Boolean.TRUE.equals(cascaderItem.getMultiple());
        boolean showPath = Boolean.TRUE.equals(cascaderItem.getShowalllevels());
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);
        if (ObjectNull.isNull(cascaderItem.getDatatype())) {
            return data;
        }
        switch (cascaderItem.getDatatype()) {
            case option:

            case dataModel:
                DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);
                //此处直接跳过数据模型
                Object o = null;
                if (showPath) {
                    // 尝试从预加载缓存中获取数据
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Map<String, Object>>> preloadedCache = (Map<String, Map<String, Map<String, Object>>>) SystemThreadLocal.get("PRELOADED_DATA_CACHE");
                    
                    List<Map<String, Object>> dictList = null;
                    
                    // 获取formId - 使用与预加载相同的逻辑：从Desi gnJson中读取
                    String formId = null;
                    try {
                        formId = (String) JvsJsonPath.read(cascaderItem.getDesignJson(), "$.formId");
                    } catch (Exception e) {
                        formId = cascaderItem.getFormId();
                        log.debug("[级联选择-回显优化] 字段从DesignJson读取formId失败，回退使用getFormId()={}", formId);
                    }
                    if (ObjectNull.isNull(formId)) {
                        formId = cascaderItem.getFormId();
                        log.warn("[级联选择-回显优化] 字段DesignJson中的formId为null，使用getFormId()={}", formId);
                    }
                    
                    if (ObjectNull.isNotNull(preloadedCache)) {
                        // 使用与预加载相同的key逻辑：优先使用prop，为null时回退使用fieldKey
                        String cacheKey = cascaderItem.getProp();
                        if (ObjectNull.isNull(cacheKey)) {
                            cacheKey = cascaderItem.getFieldKey();
                            log.debug("[级联选择-回显优化] 字段prop为null，回退使用fieldKey={}", cacheKey);
                        }
                        
                        Map<String, Map<String, Object>> fieldCache = preloadedCache.get(cacheKey);
                        if (ObjectNull.isNotNull(fieldCache)) {
                            Map<String, Object> modelCache = fieldCache.get(formId);
                            if (ObjectNull.isNotNull(modelCache) && !modelCache.isEmpty()) {
                                // 从缓存中构建字典列表
                                dictList = new ArrayList<>();
                                for (Object value : modelCache.values()) {
                                    if (value instanceof Map) {
                                        dictList.add((Map<String, Object>) value);
                                    }
                                }
                                log.info("[级联选择-回显优化] 字段[{}]使用预加载缓存，命中{}条数据", cacheKey, dictList.size());
                            } else {
                                log.debug("[级联选择-回显优化] 字段[{}]modelCache为空，fieldCache中的formId: {}", 
                                    cacheKey, fieldCache.keySet());
                            }
                        } else {
                            log.debug("[级联选择-回显优化] 字段[{}]fieldCache为null，preloadedCache中的key: {}", 
                                cacheKey, preloadedCache.keySet());
                        }
                    }
                    
                    // 如果预加载缓存未命中，尝试使用旧的ThreadLocal缓存
                    if (ObjectNull.isNull(dictList)) {
                        Object o1 = SystemThreadLocal.get(formId + "_" + cascaderItem.getProps().getLabel() + "_" + cascaderItem.getProps().getSecTitle());
                        if (ObjectNull.isNotNull(o1)) {
                            dictList = (List<Map<String, Object>>) o1;
                            log.debug("[级联选择-回显优化] 字段[{}]使用旧ThreadLocal缓存", cascaderItem.getProp());
                        }
                    }
                    
                    // 如果都没命中，查询数据库
                    if (ObjectNull.isNull(dictList)) {
                        log.warn("[级联选择-回显优化] 字段[{}]缓存未命中，执行数据库查询", cascaderItem.getProp());
                        List<Criteria> authCriteria = DynamicDataUtils.getAuthCriteria();
                        SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, null);
                        List<String> fields = new ArrayList<>();
                        fields.add(cascaderItem.getProps().getLabel());
                        fields.add(cascaderItem.getProps().getSecTitle());
                        dictList = dynamicDataService.queryList(formId, fields, new QueryConditionDto());
                        SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, authCriteria);
                        // 存入旧的ThreadLocal缓存，兼容其他代码
                        SystemThreadLocal.set(formId + "_" + cascaderItem.getProps().getLabel() + "_" + cascaderItem.getProps().getSecTitle(), dictList);
                    }
                    
                    Map<String, Object> map = dictList
                            .stream()
                            .filter(e -> ObjectNull.isNotNull(e.get(cascaderItem.getProps().getLabel())))
                            .collect(Collectors.toMap(e -> e.get("id").toString(), e -> e.get(cascaderItem.getProps().getLabel())));
                    try {
                        data = dataFieldHandler.handlePathId(data, isMulti, showPath, dictList, e -> e.get("id").toString(), e -> e.getOrDefault(cascaderItem.getProps().getSecTitle(), "-1").toString());
                    } catch (Exception e) {
                        return null;
                    }
                    return dataFieldHandler.joinFormItems(map, data, isMulti, showPath);
                } else {
                    // 非showPath场景：获取formId - 使用与预加载相同的逻辑：从Desi gnJson中读取
                    String formId = null;
                    try {
                        formId = (String) JvsJsonPath.read(cascaderItem.getDesignJson(), "$.formId");
                    } catch (Exception e) {
                        formId = cascaderItem.getFormId();
                        log.debug("[级联选择-回显优化] 字段从DesignJson读取formId失败，回退使用getFormId()={}", formId);
                    }
                    if (ObjectNull.isNull(formId)) {
                        formId = cascaderItem.getFormId();
                    }
                    
                    try {
                        o = dynamicDataService.getById(formId, data.toString()).getJsonData().get(cascaderItem.getProps().getLabel());
                    } catch (Exception e) {
                        log.error("找不到数据");
                    }
                }
                return o;
            //根据模型获取字段
            case system:
                List<TreeDictDto> dictList = this.getTreeDict(dictName);
                dictList = dictList.stream().filter(e -> ObjectNull.isNotNull(e.getParentId())).collect(Collectors.toList());
                if (ObjectNull.isNotNull(dictList)) {
                    Map<String, Object> map = dictList.stream().collect(Collectors.toMap(TreeDictDto::getId, TreeDictDto::getName, (s1, s2) -> s1));
                    try {
                        data = dataFieldHandler.handlePathId(data, isMulti, showPath, dictList, TreeDictDto::getId, TreeDictDto::getParentId);
                    } catch (Exception e) {
                        return null;
                    }
                    return dataFieldHandler.joinFormItems(map, data, isMulti, showPath);
                } else {
                    return data;
                }
            case rule:
                //逻辑引擎获取级联数据
                String optionHttp = cascaderItem.getOptionHttp();
                if (ObjectNull.isNotNull(optionHttp)) {
                    if (isMulti && JSON.isValidArray(data.toString())) {
                        return JSON.parseArray(data.toString()).stream().map(e -> getRuleValue(cascaderItem, optionHttp, lineData, e, showPath).toString()).collect(Collectors.joining(","));
                    } else {
                        return getRuleValue(cascaderItem, optionHttp, lineData, data, showPath);
                    }
                }
                break;
            default:
                return data;
        }
        return data;
    }

    public Object getRuleValue(CascaderItemHtml cascaderItem, String optionHttp, Map<String, Object> lineData, Object data, boolean showPath) {
        RunLogService logService = SpringContextUtil.getApplicationContext().getBean(RunLogService.class);
        RuleStartUtils ruleStartUtils = SpringContextUtil.getApplicationContext().getBean(RuleStartUtils.class);
        RuleDesignService ruleDesignService = SpringContextUtil.getApplicationContext().getBean(RuleDesignService.class);
        //逻辑如果需要传递参数，直接查询数据库修改Data即可
        RuleDesignPo po = ruleDesignService.getEnableDesign(optionHttp);
        if (ObjectNull.isNotNull(po)) {
            RunLogPo logPo = logService.create(po.getJvsAppId(), po.getSecret(), RunType.REAL, new HashMap<>(8), po.getReqType(), po.getReqType(), false);
            RuleExecuteDto dto = new RuleExecuteDto().setReqVariableMap(lineData).setVariableMap(lineData);
            RuleExecDto ruleExecDto = new RuleExecDto()
                    .setExecuteDto(dto)
                    .setType(RunType.REAL)
                    .setSecret(po.getSecret())
                    .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
            ruleStartUtils.start(po, logPo, ruleExecDto);
            if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                if (value instanceof List) {
                    List<Map> list = JSONArray.parseArray(JSONObject.toJSONString(value)).stream().map(e -> ((Map) e)).collect(Collectors.toList());
                    List<FormValueHtml> copys = getFormValueHtml(list, cascaderItem.getProps().getLabel(), cascaderItem.getProps().getValue());
                    if (data instanceof List) {
                        return ((List<?>) data).stream().map(e -> getLabel(e.toString(), copys, showPath)).collect(Collectors.joining(","));
                    } else {
                        return getLabel(data.toString(), copys, showPath);
                    }
                }
            }
        }
        return "字典未匹配" + JSONObject.toJSONString(data);
    }

    public List getRuleValue(String optionHttp) {
        RunLogService logService = SpringContextUtil.getApplicationContext().getBean(RunLogService.class);
        RuleStartUtils ruleStartUtils = SpringContextUtil.getApplicationContext().getBean(RuleStartUtils.class);
        RuleDesignService ruleDesignService = SpringContextUtil.getApplicationContext().getBean(RuleDesignService.class);
        //逻辑如果需要传递参数，直接查询数据库修改Data即可
        RuleDesignPo po = ruleDesignService.getEnableDesign(optionHttp);
        if (ObjectNull.isNotNull(po)) {
            RunLogPo logPo = logService.create(po.getJvsAppId(), po.getSecret(), RunType.REAL, new HashMap<>(8), po.getReqType(), po.getReqType(), false);
            RuleExecuteDto dto = new RuleExecuteDto().setReqVariableMap(new HashMap<>()).setVariableMap(new HashMap<>());
            RuleExecDto ruleExecDto = new RuleExecDto().setExecuteDto(dto).setType(RunType.REAL).setSecret(po.getSecret()).setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
            ruleStartUtils.start(po, logPo, ruleExecDto);
            if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                if (value instanceof List) {
                    return (List) value;
                }
            }
        }
        throw new BusinessException("配置异常");
    }

    /**
     * 递归根据属性组装属性对象值
     *
     * @param list  树形数据
     * @param label 组件属性指定的 label  key
     * @param value 组件属性指定的 value  key
     * @return
     */
    private List<FormValueHtml> getFormValueHtml(List<Map> list, String label, String value) {
        List<FormValueHtml> html = new ArrayList<>();
        for (Map jsonObject : list) {
            List children = (List) jsonObject.get(AppConstant.children);
            FormValueHtml formValueHtml = new FormValueHtml().setLabel(jsonObject.get(label).toString()).setValue(jsonObject.get(value).toString());
            if (ObjectNull.isNotNull(children)) {
                //装下一层
                formValueHtml.setChildren(getFormValueHtml(children, label, value));
            }
            html.add(formValueHtml);
        }
        return html;
    }


    @Override
    public String getConversionKey(CascaderItemHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {

        //这里需要判断是否是多选，如果是多选，这里的 obj 需要调整为数组
        if (dto.getMultiple() && (o.toString().contains(",") || o.toString().contains("，"))) {
            String v = o.toString().replace("，", ",");
            Arrays.stream(v.split(",")).forEach(e -> getConversionKey(dto, e.trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
            return null;
        } else {
            Object obj = getConversionKey(dto, o, lineData);
            //如果转换成功，则将 id放进对象中
            Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
            if (ObjectNull.isNotNull(obj)) {
                orDefault.put(o.toString(), obj.toString());
                cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
                return obj.toString();
            } else {
                //不成功转换数据
                String idStr = IdWorker.getIdStr();
                boolean isCascader = orDefault.containsKey(o.toString());
                if (isCascader) {
                    idStr = orDefault.get(o.toString());
                } else {
                    orDefault.put(o.toString(), idStr);
                }
                cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
                //这里需要判断进行递归判断哪一个数据是否存在
                if (!isCascader) {
                    int i = o.toString().lastIndexOf("/");
                    if (i > 0) {
                        String substring = o.toString().substring(0, i);
                        String label = o.toString().substring(i + 1);
                        String conversionKey = getConversionKey(dto, substring, lineData, cascaderFieldPathIdsMap, generateCascaderList);
                        List<Map<String, Object>> dataList = generateCascaderList.getOrDefault(dto.getFormId(), new ArrayList<>());
                        dataList.add(Dict.create().set("id", idStr).set("dataId", idStr).set(dto.getProps().getLabel(), label).set(dto.getProps().getSecTitle(), conversionKey));
                        generateCascaderList.put(dto.getFormId(), dataList);
                    } else {
                        String label = o.toString().substring(i + 1);
                        List<Map<String, Object>> dataList = generateCascaderList.getOrDefault(dto.getFormId(), new ArrayList<>());
                        dataList.add(Dict.create().set("id", idStr).set("dataId", idStr).set(dto.getProps().getLabel(), label));
                        generateCascaderList.put(dto.getFormId(), dataList);

                    }
                }
                return idStr;
            }
        }
    }

    @Override
    public Object getConversionKey(CascaderItemHtml cascaderItem, Object data, Map<String, Object> oldData) {
        //递归循环查询，是否存在这样的数据
        List<String> collect = Arrays.stream(data.toString().split("/")).map(String::trim).filter(ObjectNull::isNotNull).collect(Collectors.toList());
        switch (cascaderItem.getDatatype()) {
            case option:

            case dataModel:
                //此处直接跳过数据模型
                List<String> fieldList = new ArrayList<>();
                //显示字段
                fieldList.add(cascaderItem.getProps().getLabel());
                //传递字段
                fieldList.add(cascaderItem.getProps().getValue());
                //父级字段
                fieldList.add(cascaderItem.getProps().getSecTitle());
                //遍历数据,找出名称与这个字段相同的数据
                Object next = getNextCascaderId(collect, 0, fieldList, cascaderItem);
                return next;
            //根据模型获取字段
            case system:
                Map<String, Object> byUniqueName = jvsTreeService.getByUniqueName(cascaderItem.getDictName());
                return getNextSystem(collect, 0, (List<Tree>) byUniqueName.get(AppConstant.children));
            case rule:
                //逻辑引擎获取级联数据
                String optionHttp = cascaderItem.getOptionHttp();
                if (ObjectNull.isNotNull(optionHttp)) {
                    RunLogService logService = SpringContextUtil.getApplicationContext().getBean(RunLogService.class);
                    RuleStartUtils ruleStartUtils = SpringContextUtil.getApplicationContext().getBean(RuleStartUtils.class);
                    RuleDesignService ruleDesignService = SpringContextUtil.getApplicationContext().getBean(RuleDesignService.class);
                    //逻辑如果需要传递参数，直接查询数据库修改Data即可
                    RuleDesignPo po = ruleDesignService.getEnableDesign(optionHttp);
                    if (ObjectNull.isNotNull(po)) {
                        RunLogPo logPo = logService.create(po.getJvsAppId(), po.getSecret(), RunType.REAL, new HashMap<>(8), po.getReqType(), po.getReqType(), false);
                        RuleExecuteDto dto = new RuleExecuteDto().setReqVariableMap(oldData).setVariableMap(oldData);
                        RuleExecDto ruleExecDto = new RuleExecDto()
                                .setExecuteDto(dto)
                                .setType(RunType.REAL)
                                .setSecret(po.getSecret())
                                .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
                        ruleStartUtils.start(po, logPo, ruleExecDto);
                        if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                            Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                            if (value instanceof List) {
                                List<FormValueHtml> copys = ((List<JSONObject>) value).stream()
                                        .map(e -> new FormValueHtml().setLabel(e.getString(cascaderItem.getProps().getLabel())).setValue(cascaderItem.getProps().getValue()))
                                        .collect(Collectors.toList());
                                return getValue(data.toString(), copys);
                            }
                        } else {
                            return "字典未匹配" + JSONObject.toJSONString(data);
                        }
                    }
                }
                break;
            default:
                return null;
        }
        return null;
    }

    /**
     * 获取这个值的所有下级id
     *
     * @param cascaderItem 设计
     * @param value        数据值
     */
    public Set<String> childrenId(CascaderItemHtml cascaderItem, String value) {
        String dictName = cascaderItem.getDictName();
        if (ObjectNull.isNull(cascaderItem.getDatatype())) {
            return new HashSet<>();
        }
        switch (cascaderItem.getDatatype()) {
            case dataModel: {
                DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);
                //此处直接跳过数据模型
                List<Map<String, Object>> dictList = dynamicDataService.queryList(cascaderItem.getFormId(), cascaderItem.getProps().getLabel(), cascaderItem.getProps().getSecTitle());
                List<Tree<Object>> tree = TreeUtils.tree(dictList, value, cascaderItem.getProps().getSecTitle(), "id", cascaderItem.getProps().getLabel(), "id");
                if (ObjectNull.isNotNull(tree)) {
                    return tree.stream().flatMap(a -> TreeUtils.tree2List(a, Tree::getChildren).stream()).map(e -> e.getId().toString()).collect(Collectors.toSet());
                }
                break;
            }
            //根据模型获取字段
            case system: {
                List<TreeDictDto> dictList = this.getTreeDict(dictName);
                List<TreePo> tree = dictList.stream().filter(e -> ObjectNull.isNotNull(e.getParentId())).map(e -> new TreePo().setId(e.getId()).setSort(1).setName(e.getName()).setParentId(e.getParentId())).collect(Collectors.toList());
                List<Tree<Object>> tree1 = TreeUtils.tree(tree, value);
                if (ObjectNull.isNotNull(tree1)) {
                    return tree1.stream().flatMap(a -> TreeUtils.tree2List(a, Tree::getChildren).stream()).map(e -> e.getId().toString()).collect(Collectors.toSet());
                }
                break;
            }
            case rule: {
                //逻辑引擎获取级联数据
                String optionHttp = cascaderItem.getOptionHttp();
                if (ObjectNull.isNotNull(optionHttp)) {
                    List<Map<String, Object>> ruleValue = getRuleValue(optionHttp);
                    List<Tree<Object>> tree = TreeUtils.tree(ruleValue, value, cascaderItem.getProps().getSecTitle(), cascaderItem.getProps().getValue(), cascaderItem.getProps().getLabel(), "id");
                    if (ObjectNull.isNull(tree)) {
                        break;
                    }
                    return tree.stream().flatMap(a -> TreeUtils.tree2List(a, Tree::getChildren).stream()).map(e -> e.getId().toString()).collect(Collectors.toSet());
                }
                break;
            }
        }
        HashSet<String> strings = new HashSet<>();
        strings.add(value);
        return strings;
    }

    public List<String> getByDataModelIds(String formId, FormValueHtml props, Object rootId) {
        DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);
        //此处直接跳过数据模型
        List<Map<String, Object>> dictList = dynamicDataService.queryList(formId, props.getLabel(), props.getSecTitle());

        List<String> list = new ArrayList<>();
        if (rootId instanceof List) {
            ((List<?>) rootId).forEach(b -> {
                TreeUtils.getListPassingBy(dictList, b, e -> props.getSecTitle(), e -> props.getLabel())
                        .stream()
                        .map(e -> e.get("id").toString()).forEach(a -> {
                            list.add(a);
                        });
            });
        } else {
            TreeUtils.getListPassingBy(dictList, rootId, e -> props.getSecTitle(), e -> props.getLabel())
                    .stream()
                    .map(e -> e.get("id").toString()).forEach(e -> {
                        list.add(e);
                    });
        }
        return list;
    }

    public List<String> getByDataRuleIds(String optionHttp, FormValueHtml props, Object rootId) {
        //逻辑引擎获取级联数据
        if (ObjectNull.isNotNull(optionHttp)) {
            List<Map<String, Object>> ruleValue = getRuleValue(optionHttp);
            List<String> list = new ArrayList<>();
            if (rootId instanceof List) {
                ((List<?>) rootId).forEach(a -> {
                    TreeUtils.getListPassingBy(ruleValue, a, e -> props.getSecTitle(), e -> props.getLabel())
                            .stream()
                            .map(e -> e.get("id").toString()).forEach(c -> list.add(c));
                });
            } else {
                TreeUtils.getListPassingBy(ruleValue, rootId, e -> props.getSecTitle(), e -> props.getLabel())
                        .stream()
                        .map(e -> e.get("id").toString()).forEach(c -> list.add(c));
            }
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    private Object getNextSystem(List<String> collect, int i, List<Tree> list) {
        String s = collect.get(i);
        if (ObjectNull.isNotNull(list)) {
            Optional<Tree> first = list.stream().filter(e -> e.getName().equals(s)).findFirst();
            if (first.isPresent()) {
                if (i + 1 == collect.size()) {
                    return first.get().getId();
                }
                return getNextSystem(collect, ++i, first.get().getChildren());
            }
        }
        return null;
    }

    private Object getNextCascaderId(List<String> collect, int i, List<String> fieldList, CascaderItemHtml cascaderItem) {
        DynamicDataService dynamicDataService = SpringContextUtil.getBean(DynamicDataService.class);
        QueryListDto dto = new QueryListDto();
        dto.setFieldList(fieldList);
        dto.setConditions(Arrays.asList(new QueryConditionDto().setEnabledQueryTypes(DataQueryType.eq).setValue(collect.get(i)).setFieldKey(cascaderItem.getProps().getLabel())));
        List<Map<String, Object>> maps = dynamicDataService.postQueryList(cascaderItem.getFormId(), dto);
        if (ObjectNull.isNotNull(maps)) {
            if (i + 1 == collect.size()) {
                return maps.get(0).get("id");
            } else {
                return getNextCascaderId(collect, ++i, fieldList, cascaderItem);
            }
        }
        return null;
    }

    /**
     * 获取树形字典项的集合
     *
     * @param dictUniqueName 字典标识
     * @return 字典项集合
     */
    public List<TreeDictDto> getTreeDict(String dictUniqueName) {
        List<TreeDictDto> result = new ArrayList<>();
        Map<String, String> idMap = new HashMap<>(64);
        Deque<Map<String, Object>> keyQueue = new ArrayDeque<>(64);
        // 查询树形字典数据
        Map<String, Object> treeDict = jvsTreeService.getByUniqueName(dictUniqueName);
        if (ObjectNull.isNotNull(treeDict)) {
            keyQueue.add(treeDict);
        }
        while (ObjectNull.isNotNull(keyQueue)) {
            Map<String, Object> poll = keyQueue.poll();
            JvsTree extend = (JvsTree) poll.get(Get.name(TreePo::getExtend));
            String id = extend.getUniqueName();
            String name = extend.getName();
            String parentId = extend.getParentId();
            String uniqueName = extend.getUniqueName();
            TreeDictDto dict = new TreeDictDto().setId(id).setName(name).setParentId(parentId);
            result.add(dict);
            // 记录节点id与uniqueName的对应关系
            idMap.put(id, uniqueName);
            // 添加子节点
            List<Map<String, Object>> children = (List<Map<String, Object>>) poll.get(AppConstant.children);
            if (ObjectUtils.isNotEmpty(children)) {
                keyQueue.addAll(children);
            }
        }
        // 因为前端的传递值使用的是uniqueName, 这里将id都换成uniqueName
        for (TreeDictDto dict : result) {
            String newId = idMap.get(dict.getId());
            String newParentId = idMap.get(dict.getParentId());
            dict.setId(newId);
            dict.setParentId(newParentId);
        }
        return result;
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"cascader\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"multiple\": false,\n" +
                "    \"showalllevels\": true,\n" +
                "    \"collapsetags\": false,\n" +
                "    \"emitPath\": false,\n" +
                "    \"emitKey\": \"uniqueName\",\n" +
                "    \"dictName\": \"\",\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"multiple\",\n" +
                "        \"prop\",\n" +
                "        \"datatype\",\n" +
                "        \"url\",\n" +
                "        \"sqlType\",\n" +
                "        \"showalllevels\",\n" +
                "        \"collapsetags\",\n" +
                "        \"emitPath\",\n" +
                "        \"cascaderOption\",\n" +
                "        \"disabled\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请选择" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.cascader.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }

}
