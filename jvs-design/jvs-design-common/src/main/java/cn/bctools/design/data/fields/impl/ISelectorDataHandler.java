package cn.bctools.design.data.fields.impl;

import cn.bctools.auth.api.api.DictApi;
import cn.bctools.auth.api.dto.SysDictItemDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.impl.advance.CascaderFieldHandler.getLabel;

/**
 * 通用回显处理: 允许多选的组件
 * <p>
 * 没有做上下级路径显示的处理
 *
 * @Author: GuoZi
 */
public interface ISelectorDataHandler {

    Logger log = LoggerFactory.getLogger(ISelectorDataHandler.class);

    /**
     * 分割符
     */
    String REGEX = ",";

    default String conversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        FormDataTypeEnum datatype = dto.getDatatype();
        if (datatype.equals(FormDataTypeEnum.option)) {
            if (dto.getMultiple() && o.toString().contains(",")) {
                Arrays.stream(o.toString().split(",")).forEach(e -> conversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
                return null;
            } else {
                Map<String, String> dicMap = dto.getDicData().stream().collect(Collectors.toMap(FormValueHtml::getLabel, FormValueHtml::getValue));
                //判断如果里面不存在设计数据，是否继续保存
                if (dicMap.containsKey(o)) {
                    cascaderFieldPathIdsMap.put(dto.getProp(), dicMap);
                } else {
                    //不存在，则直接报异常不支持导入
                    throw new BusinessException(o.toString() + "未配置在字典中不支持导入");
                }
            }
            //表示没有关联模型直接取原始数据值
        } else if (datatype.equals(FormDataTypeEnum.dataModel)) {
            //获取 关联的模型
            String fromId = dto.getFormId();
            DynamicDataService bean = SpringContextUtil.getBean(DynamicDataService.class);
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("id");
            arrayList.add(dto.getProps().getLabel());
            List<Map<String, Object>> list = bean.queryList(fromId, arrayList);
            if (ObjectNull.isNull(list)) {
                return null;
            }
            if (dto.getMultiple() && o.toString().contains(",")) {
                Arrays.stream(o.toString().split(",")).forEach(e -> conversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
                return null;
            } else {
                Map<String, String> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.get(dto.getProps().getLabel())), s -> String.valueOf(s.get("id")), (s1, s2) -> s1));
                if (map.containsKey(o.toString())) {
                    //存在，直接添加
                    //查询出所有的数据
                    cascaderFieldPathIdsMap.put(dto.getProp(), map);
                } else {
                    Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
                    String idStr = IdWorker.getIdStr();
                    boolean isCascader = orDefault.containsKey(o.toString());
                    if (isCascader) {
                        idStr = orDefault.get(o.toString());
                    } else {
                        //不存在，则需要添加到新增里面
                        List<Map<String, Object>> dataList = generateCascaderList.getOrDefault(dto.getFormId(), new ArrayList<>());
                        dataList.add(Dict.create().set("id", idStr).set("dataId", idStr).set(dto.getProps().getLabel(), o));
                        generateCascaderList.put(dto.getFormId(), dataList);
                        orDefault.put(o.toString(), idStr);
                        cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
                    }

                }
            }
        } else if (datatype.equals(FormDataTypeEnum.system)) {
            if (dto.getMultiple() && o.toString().contains(",")) {
                Arrays.stream(o.toString().split(",")).forEach(e -> conversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
                return null;
            } else {
                //获取字典
                DictApi bean = SpringContextUtil.getBean(DictApi.class);
                String dictKey = dto.getSystemDict();
                List<SysDictItemDto> dictItems = bean.listItems(dictKey).getData();
                Map<String, String> map = dictItems.stream().collect(Collectors.toMap(SysDictItemDto::getLabel, SysDictItemDto::getValue, (s1, s2) -> s1));
                cascaderFieldPathIdsMap.put(dto.getProp(), map);
            }
        } else if (datatype.equals(FormDataTypeEnum.rule)) {
            if (dto.getMultiple() && o.toString().contains(",")) {
                Arrays.stream(o.toString().split(",")).forEach(e -> conversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
                return null;
            } else {
                String optionHttp = dto.getOptionHttp();
                if (ObjectNull.isNotNull(optionHttp)) {
                    RunLogService logService = SpringContextUtil.getApplicationContext().getBean(RunLogService.class);
                    RuleStartUtils ruleStartUtils = SpringContextUtil.getApplicationContext().getBean(RuleStartUtils.class);
                    RuleDesignService ruleDesignService = SpringContextUtil.getApplicationContext().getBean(RuleDesignService.class);
                    //逻辑如果需要传递参数，直接查询数据库修改Data即可
                    RuleDesignPo po = ruleDesignService.getEnableDesign(optionHttp);
                    if (ObjectNull.isNotNull(po)) {
                        RunLogPo logPo = logService.create(po.getJvsAppId(), po.getSecret(), RunType.REAL, new HashMap<>(8), po.getReqType(), po.getReqType(), false);
                        HashMap<String, Object> reqVariableMap = new HashMap<>(lineData);
                        //表示是导入
                        reqVariableMap.put("import", true);
                        RuleExecuteDto executeDto = new RuleExecuteDto().setReqVariableMap(reqVariableMap).setVariableMap(lineData);
                        RuleExecDto ruleExecDto = new RuleExecDto()
                                .setExecuteDto(executeDto)
                                .setType(RunType.REAL)
                                .setSecret(po.getSecret())
                                .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
                        ruleStartUtils.startCache(po, logPo, ruleExecDto);
                        if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getErrorMessage())) {
                            return "字典未匹配," + po.getName() + " 逻辑出错" + JSONObject.toJSONString(ruleExecDto);
                        }
                        if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                            Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                            if (value instanceof List) {
                                Map<String, String> es = new LinkedList<>(((List<JSONObject>) value))
                                        .stream().collect(Collectors.toMap(e -> e.getString(dto.getProps().getLabel()), e -> e.getString(dto.getProps().getValue())));
                                cascaderFieldPathIdsMap.put(dto.getProp(), es);
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            }
            //判断是否在数据库里面，
        } else if (datatype.equals(FormDataTypeEnum.flowable)) {
            throw new BusinessException(dto.getLabel() + "字段不支持导入");
        }
        //这几种下拉选项可以不返回，存在递归，只有树形需要返回这个数据
        return null;
    }

    /**
     * Conversion key object.
     *
     * @param selectItem the select item
     * @param data       the data
     * @param lineData   the line data
     * @return the object
     */
    default Object conversionKey(MultipleHtml selectItem, Object data, Map<String, Object> lineData) {
        if (ObjectNull.isNull(selectItem)) {
            return data;
        }
        FormDataTypeEnum dataType = selectItem.getDatatype();
        if (ObjectNull.isNull(dataType)) {
            return data;
        }
        //是否是多选项
        boolean isMulti = Boolean.TRUE.equals(selectItem.getMultiple());
        DataFieldHandler dataFieldHandler = SpringContextUtil.getApplicationContext().getBean(DataFieldHandler.class);
        boolean aBoolean = Optional.ofNullable(selectItem.getShowalllevels()).orElseGet(() -> false);
        // 配置数据
        if (FormDataTypeEnum.option.equals(dataType)) {
            List<FormValueHtml> dicData = selectItem.getDicData();
            if (ObjectUtils.isEmpty(dicData)) {
                return data;
            }
            Map<String, Object> map = dicData.stream().collect(Collectors.toMap(FormValueHtml::getLabel, FormValueHtml::getValue, (s1, s2) -> s1));
            if (isMulti) {
                return Arrays.stream(data.toString().split(REGEX))
                        .map(String::trim)
                        .map(map::get).collect(Collectors.toList());
            } else {
                return map.get(data);
            }
        }
        // 接口数据
        if (FormDataTypeEnum.dataModel.equals(dataType)) {
            String sourceFieldId = selectItem.getProps().getSourceFieldId();
            //获取 关联的模型
            String fromId = selectItem.getFormId();

            DynamicDataService bean = SpringContextUtil.getBean(DynamicDataService.class);
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("id");
            arrayList.add(selectItem.getProps().getLabel());
            List<Map<String, Object>> list = bean.queryList(fromId, arrayList);
            if (ObjectNull.isNull(list)) {
                return data;
            }
            if (StringUtils.isNotBlank(sourceFieldId)) {
                bean.replaceSourceFieldData(sourceFieldId, selectItem.getProps().getLabel(), list);
            }
            if (isMulti) {
                data = Arrays.stream(data.toString().split(REGEX)).map(String::trim).collect(Collectors.toList());
            }
            Map<String, Object> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.get(selectItem.getProps().getLabel())), s -> String.valueOf(s.get("id")), (s1, s2) -> s1));
            return dataFieldHandler.joinFormItemsValue(map, data, isMulti, aBoolean);
        }
        // 系统字典
        if (FormDataTypeEnum.system.equals(dataType)) {
            //获取字典
            DictApi bean = SpringContextUtil.getBean(DictApi.class);
            String dictKey = selectItem.getSystemDict();
            List<SysDictItemDto> dictItems = bean.listItems(dictKey).getData();
            Map<String, Object> map = dictItems.stream().collect(Collectors.toMap(SysDictItemDto::getLabel, SysDictItemDto::getValue, (s1, s2) -> s1));
            return dataFieldHandler.joinFormItemsValue(map, data, isMulti, aBoolean);
        }
        //逻辑引擎获取字典数据用于转换数据
        if (FormDataTypeEnum.rule.equals(dataType)) {
            String optionHttp = selectItem.getOptionHttp();
            if (ObjectNull.isNotNull(optionHttp)) {
                if (isMulti) {
                    String[] split = data.toString().split(REGEX);
                    return Arrays.stream(split).map(String::trim).map(e -> getRuleValue(optionHttp, lineData, selectItem, e)).collect(Collectors.toList());
                } else {
                    return getRuleValue(optionHttp, lineData, selectItem, data);
                }
            }
        }
        return data;
    }

    /**
     * Gets rule value.
     *
     * @param optionHttp the option http
     * @param lineData   the line data
     * @param selectItem the select item
     * @param data       the data
     * @return the rule value
     */
    default Object getRuleValue(String optionHttp, Map<String, Object> lineData, MultipleHtml selectItem, Object data) {
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
            ruleStartUtils.startCache(po, logPo, ruleExecDto);
            if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getErrorMessage())) {
                return "字典未匹配," + po.getName() + " 逻辑出错" + JSONObject.toJSONString(data);
            }
            if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                if (value instanceof List) {
                    List<JSONObject> es = new LinkedList<>(((List<JSONObject>) value));
                    for (int i = 0; i < es.size(); i++) {
                        Object o = es.get(i).get(selectItem.getProps().getLabel());
                        if (data.equals(o)) {
                            Object o1 = es.get(i).get(selectItem.getProps().getValue());
                            return o1;
                        }
                    }
                }
            }
        }
        return "字典未匹配" + JSONObject.toJSONString(data);
    }

    /**
     * 获取回显数据
     * <p>
     * 各选项的数据来源:
     * 1. 配置数据
     * 2. 接口数据
     * 3. 系统字典
     *
     * @param selectItem 多选组件
     * @param data       数据值
     * @param lineData   the line data
     * @return 显示值 object
     */
    default Object echoValue(MultipleHtml selectItem, Object data, Map<String, Object> lineData) {
        if (ObjectNull.isNull(selectItem)) {
            return data;
        }
        FormDataTypeEnum dataType = selectItem.getDatatype();
        if (ObjectNull.isNull(dataType)) {
            return data;
        }
        boolean isMulti = Boolean.TRUE.equals(selectItem.getMultiple());
        DataFieldHandler dataFieldHandler = SpringContextUtil.getApplicationContext().getBean(DataFieldHandler.class);
        boolean aBoolean = Optional.ofNullable(selectItem.getShowalllevels()).orElseGet(() -> false);
        // 配置数据
        if (FormDataTypeEnum.option.equals(dataType)) {
            List<FormValueHtml> dicData = selectItem.getDicData();
            if (ObjectUtils.isEmpty(dicData)) {
                return data;
            }
            Map<String, Object> map = dicData.stream().collect(Collectors.toMap(FormValueHtml::getValue, FormValueHtml::getLabel, (s1, s2) -> s1));
            return dataFieldHandler.joinFormItems(map, data, isMulti, aBoolean);
        }
        // 接口数据
        if (FormDataTypeEnum.dataModel.equals(dataType)) {
            String sourceFieldId = selectItem.getProps().getSourceFieldId();
            //获取 关联的模型
            String fromId = selectItem.getFormId();

            DynamicDataService bean = SpringContextUtil.getBean(DynamicDataService.class);
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("id");
            arrayList.add(selectItem.getProps().getLabel());
            
            // 优先尝试从预加载缓存中获取数据，避免N+1查询
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Map<String, Object>>> preloadedCache = 
                (Map<String, Map<String, Map<String, Object>>>) SystemThreadLocal.get("PRELOADED_DATA_CACHE");
            
            List<Map<String, Object>> list = null;
            
            if (ObjectNull.isNotNull(preloadedCache)) {
                // 从缓存中获取数据
                Map<String, Map<String, Object>> fieldCache = preloadedCache.get(selectItem.getProp());
                if (ObjectNull.isNotNull(fieldCache)) {
                    Map<String, Object> modelCache = fieldCache.get(fromId);
                    if (ObjectNull.isNotNull(modelCache)) {
                        // 从缓存中查找数据
                        list = new ArrayList<>();
                        if (data instanceof Collection) {
                            for (Object id : (Collection<?>) data) {
                                Object cachedDataObj = modelCache.get(String.valueOf(id));
                                if (ObjectNull.isNotNull(cachedDataObj) && cachedDataObj instanceof Map) {
                                    list.add((Map<String, Object>) cachedDataObj);
                                }
                            }
                        } else {
                            Object cachedDataObj = modelCache.get(String.valueOf(data));
                            if (ObjectNull.isNotNull(cachedDataObj) && cachedDataObj instanceof Map) {
                                list.add((Map<String, Object>) cachedDataObj);
                            }
                        }
                        log.debug("[回显优化] 字段[{}]使用预加载缓存，命中{}条数据", selectItem.getProp(), list.size());
                    }
                }
            }
            
            // 如果缓存未命中，则进行单条查询（兼容老逻辑）
            if (ObjectNull.isNull(list)) {
                //需要跳过数据权限，避免
                DynamicDataUtils.freePermit();
                QueryConditionDto queryConditionDto = new QueryConditionDto();
                queryConditionDto.setValue(data);
                queryConditionDto.setEnabledQueryTypes(DataQueryType.eq);
                queryConditionDto.setFieldKey("id");
                List<Criteria> authCriteria = DynamicDataUtils.getAuthCriteria();
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, null);
                list = bean.queryList(fromId, arrayList, queryConditionDto);
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, authCriteria);
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_FREE, null);
            }
            
            if (ObjectNull.isNull(list)) {
                if (data instanceof Collection) {
                    return ((Collection<?>) data).stream().map(e -> e.toString()).collect(Collectors.joining(","));
                }
                return data;
            }
            if (StringUtils.isNotBlank(sourceFieldId)) {
                bean.replaceSourceFieldData(sourceFieldId, selectItem.getProps().getLabel(), list);
            }
            Map<String, Object> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.get("id")), s -> String.valueOf(s.getOrDefault(selectItem.getProps().getLabel(), "")), (s1, s2) -> s1));
            return dataFieldHandler.joinFormItems(map, data, isMulti, aBoolean);
        }
        // 系统字典
        if (FormDataTypeEnum.system.equals(dataType)) {
            //获取字典
            DictApi bean = SpringContextUtil.getBean(DictApi.class);
            String dictKey = selectItem.getSystemDict();
            List<SysDictItemDto> dictItems = bean.listItems(dictKey).getData();
            Map<String, Object> map = dictItems.stream().collect(Collectors.toMap(SysDictItemDto::getValue, SysDictItemDto::getLabel, (s1, s2) -> s1));

            return dataFieldHandler.joinFormItems(map, data, isMulti, aBoolean);
        }
        //逻辑接口
        if (FormDataTypeEnum.rule.equals(dataType)) {
            String optionHttp = selectItem.getOptionHttp();
            if (ObjectNull.isNotNull(optionHttp)) {
                String label = selectItem.getProps().getLabel();
                String value1 = selectItem.getProps().getValue();
                if (label.equals(value1)) {
                    //如果相同，则直接返回
                    return data;
                }
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
                    RuleExecDto rule = RuleSystemThreadLocal.getRule();
                    ruleStartUtils.startCache(po, logPo, ruleExecDto);
                    if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getErrorMessage())) {
                        return "字典未匹配," + po.getName() + " 逻辑出错" + JSONObject.toJSONString(data);
                    }
                    RuleSystemThreadLocal.set(rule);
                    if (ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getEndResult())) {
                        Object value = ruleExecDto.getExecuteDto().getEndResult().getValue();
                        if (value instanceof List) {
                            List<FormValueHtml> copys = ((List<Map>) value).stream()
                                    .filter(e -> ObjectNull.isNotNullOne(e.get(selectItem.getProps().getLabel()), e.get(selectItem.getProps().getValue())))
                                    .map(e -> new FormValueHtml().setLabel(e.get(selectItem.getProps().getLabel()).toString()).setValue(e.get(selectItem.getProps().getValue()).toString()))
                                    .collect(Collectors.toList());
                            if (data instanceof List) {
                                return ((ArrayList) data).stream().map(e -> getLabel(e.toString(), copys, aBoolean)).collect(Collectors.joining(","));
                            }
                            return getLabel(data.toString(), copys, aBoolean);
                        }
                    } else {
                        return "字典未匹配" + JSONObject.toJSONString(data);
                    }
                }
            }
        }
        return data;
    }


}
