package cn.bctools.design.data.service.impl;

import cn.bctools.ai.api.JvsAiDatasetApi;
import cn.bctools.ai.dto.AiDocumentDto;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.common.utils.sensitive.SensitiveInfoUtils;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.common.OrderFormat;
import cn.bctools.design.config.DesignConfig;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.design.crud.mapper.FormMapper;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.dto.ExportFieldDto;
import cn.bctools.design.data.entity.*;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.*;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.SelectItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabGenerateItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import cn.bctools.design.data.fields.dto.page.ModelDisplayHtml;
import cn.bctools.design.data.fields.dto.page.ModelDisplayLinkageFieldHtml;
import cn.bctools.design.data.fields.enums.*;
import cn.bctools.design.data.fields.impl.container.StepFieldHandler;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.*;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.data.util.QueryConditionUtils;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppLogTypeEnum;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.project.mapper.JvsAppMapper;
import cn.bctools.design.project.service.JvsAppLogService;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.OrderUtils;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.handler.ExpressionAfterHandler;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Decimal128;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.bctools.design.constant.DynamicDataConstant.DATA_EMPTY;
import static cn.bctools.design.util.DynamicDataUtils.*;
import static cn.bctools.function.entity.dto.TableType.add;
import static cn.bctools.function.entity.dto.TableType.line;

/**
 * 动态数据
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
public class DynamicDataServiceImpl implements DynamicDataService, ExpressionAfterHandler {
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
    /**
     * The Flow task service.
     */
    FlowTaskService flowTaskService;

    /**
     * The Data change.
     */
    static final String DATA_CHANGE = "dataChange";
    /**
     * The Retrival.
     */
    static final String RETRIVAL = "retrival";
    /**
     * The Query by.
     */
    static final String QUERY_BY = "queryBy";

    private static final String DATA_LOG_VERSION_KEY = "dataLogVersion";
    static final Cache<String, Object> userCache = CacheUtil.newTimedCache(1000 * 60 * 60);


    /**
     * 不需要回显处理的字段
     */
    private static final List<String> EXCLUDE_ECHO_FIELDS = Stream.of(FlowDataFieldEnum.TASK_STATE.getFieldKey(), FlowDataFieldEnum.TASK_PROGRESS.getFieldKey()).collect(Collectors.toList());

    @Override
    public DynamicDataPo getById(String modelId, String dataId) {
        try {
            return this.parseBean(this.getMap(modelId, dataId));
        } catch (Exception e) {
            log.info("模型数据查询异常: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Map> getByIds(String modelId, List<String> ids) {
        Criteria criteria = DynamicDataUtils.initCriteria(null).and(Get.name(DynamicDataPo::getId)).in(ids);
        Query query = this.getPermitQuery(criteria);
        // 排除MongoDB的id字段
        query.fields().exclude(MONGO_ID);
        return dataModelHandler.find(query, Map.class, modelId);
    }

    @Override
    public List<Map> getByIds(String modelId, List<String> ids, Set<String> field) {
        Criteria criteria = DynamicDataUtils.initCriteria(null).and(Get.name(DynamicDataPo::getId)).in(ids);
        Query query = this.getPermitQuery(criteria);
        // 排除MongoDB的id字段
        field.add("dataId");
        field.add("id");
        query.fields().exclude(MONGO_ID).include(field.toArray(new String[0]));
        return dataModelHandler.find(query, Map.class, modelId);
    }

    /**
     * 避免网络请求嵌套，所有事件和数据存储分为两个处理
     *
     * @return
     */
    @Override
    public RuleExecuteDto save(String appId, String modelId, Map<String, Object> data) {
        RuleExecuteDto ruleExecuteDto = saveData(appId, modelId, data);
        data.remove(DATA_LOG_VERSION_KEY);
        return ruleExecuteDto;
    }

    @Override
    public void checkDataFieldType(String appId, String modelId, Map<String, Object> map) throws BusinessException {
        checkDataFieldType(appId, modelId, map, null);
    }

    @Override
    public void checkDataFieldType(String appId, String modelId, Map<String, Object> map, Boolean emptyEnable) throws BusinessException {
        String designId = DynamicDataUtils.getDesignId();
        JvsApp app = CurrentAppUtils.getApp();
        if (ObjectNull.isNull(app)) {
            CurrentAppUtils.setApp(appMapper.selectById(appId));
        }
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, null, true, true);
        //查询出有哪些有字段设计的
        List<FieldBasicsHtml> designField = fields.stream()
                .filter(e -> ObjectNull.isNotNull(e.getDesignId()))
                .filter(e -> DesignType.form.equals(e.getDesignType()) || e.getDesignId().equals(getDesignId()))
                .collect(Collectors.toList());
        //设计 id如果存在，则直接只使用这个设计id相同的字段，如果没有，则使用模型的字段,避免不同设计中可能存在不同的设计结果
        Map<String, FieldBasicsHtml> fieldBasicsHtmlMap;
        if (ObjectNull.isNull(designField)) {
            fieldBasicsHtmlMap = fields.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
        } else {
            fieldBasicsHtmlMap = designField.stream().collect(Collectors.toMap(FieldPublicHtml::getFieldKey, e -> e, (e1, e2) -> e1));
        }
        StringBuffer error = new StringBuffer();
        for (String key : fieldBasicsHtmlMap.keySet()) {
            //哪些数据类型需要做类型校验的才处理，其它类型不处理
            FieldBasicsHtml fieldBasicsHtml = fieldBasicsHtmlMap.get(key);
            IDataFieldHandler handler = iDataFieldHandler.get(fieldBasicsHtml.getType().getDesc());
            if (ObjectNull.isNotNull(handler, fieldBasicsHtml.getDesignJson())) {
                FieldBasicsHtml html = handler.toHtml(fieldBasicsHtml.getDesignJson());
                //判断是否显示隐藏
                if (checkDataDisplayExpress(html, map, fieldBasicsHtmlMap)) {
                    continue;
                }
                Object o = map.get(key);
                Boolean htmlEmptyEnable = true;
                if (emptyEnable == null) {
                    if (ObjectNull.isNull(html.getRules())) {
                        htmlEmptyEnable = false;
                    } else {
                        htmlEmptyEnable = html.getRules().get(0).getRequired();
                    }
                } else {
                    htmlEmptyEnable = emptyEnable;
                }
                //占位符直接跳过
                if (html.getEmptyEnable() && DATA_EMPTY.equals(o)) {
                    continue;
                }
                if (ObjectNull.isNotNull(o, html)) {
                    try {
                        o = handler.checkDataFieldType(html, o);
                        if (fieldBasicsHtml.getType().equals(DataFieldType.inputNumber)) {
                            map.put(key, o);
                        }
                        if (fieldBasicsHtml.getType().equals(DataFieldType.input) && NumberUtil.isNumber(o.toString())) {
                            //对类型进行强转，避免是数字类型
                            map.put(key, o.toString());
                        }
                        if (fieldBasicsHtml.getType().equals(DataFieldType.datePicker)) {
                            //对类型进行强转
                            map.put(key, o);
                        }
                    } catch (Exception e) {
                        log.error(o + " " + html.getLabel() + "组件数据格式不正确:" + o + e.getMessage() + "<br/>");
                        error.append(html.getLabel()).append("组件数据格式不正确:").append(o).append(e.getMessage()).append("<br/>");
                    }
                } else if (htmlEmptyEnable && map.containsKey(key)) {
                    //暂时不处理类型为空的情况
                    error.append(html.getLabel()).append("不能为空<br/>");
                }
            }
        }
        if (ObjectNull.isNotNull(error.toString())) {
            throw new BusinessException(error.toString());
        }
    }

    /**
     * 判断前端显示隐藏，是否要进行数据校验
     *
     * @param html
     * @param map
     * @param basicsHtmlMap
     * @return
     */
    private boolean checkDataDisplayExpress(FieldBasicsHtml html, Map<String, Object> map, Map<String, FieldBasicsHtml> basicsHtmlMap) {
        if (ObjectNull.isNotNull(html.getDisplayExpress())) {
            if (html.getShowOperator().equals("&")) {
                //需要全部满足
                return html.getDisplayExpress().stream().allMatch(e -> {
                    String[] split = e.getValue().split(",");
                    if (Arrays.stream(split).anyMatch(a -> a.equals(map.get(e.getProp())))) {
                        return false;
                    }
                    return true;
                });
            } else {
                //只满足一个
                return html.getDisplayExpress().stream().anyMatch(e -> {
                    String[] split = e.getValue().split(",");
                    if (Arrays.stream(split).anyMatch(a -> a.equals(map.get(e.getProp())))) {
                        return false;
                    }
                    return true;
                });
            }
        }
        return false;
    }

    /**
     * 数据
     * 保存
     *
     * @param modelId 模型id
     * @param data    数据
     * @param appId   应用id
     * @return
     */
    private RuleExecuteDto saveData(String appId, String modelId, Map<String, Object> data) {
        DataModelUtil.setCurrentSaveData();
        // true-数据已存在，false-数据不存在
        boolean exists = ObjectNull.isNotNull(data.get("dataId"));
        // 因为触发新增事件需要数据id, 所以这里需要手动生成一下
        String dataId = exists ? String.valueOf(data.get("dataId")) : IdGenerator.getIdStr();
        data.put("dataId", dataId);
        //逻辑的前置操作将数据为空的value和值都清空搞
        data.keySet().removeIf(e -> ObjectNull.isNull(data.get(e)));
        RuleExecuteDto callback = Optional.ofNullable(dataEventService.callback(modelId, dataId, DataEventType.DATA_NEW, data, true)).orElseGet(RuleExecuteDto::new);
        //如果返回信息被逻辑引擎定义,则直接返回
        if (ObjectNull.isNotNull(callback.getSyncMessageTips())) {
            return callback;
        }

        DataEventType dataEventType = DataEventType.DATA_NEW;
        Map<String, Object> oldData = null;
        // 数据存在则修改，否则新增
        if (exists) {
            dataEventType = DataEventType.DATA_UPDATE;
            DynamicDataPo dataPo = this.get(modelId, dataId);
            oldData = dataPo.getJsonData();
            checkDataFieldType(appId, modelId, data);
            updateTransactional(appId, modelId, dataId, data, dataPo);
        } else {
            checkDataFieldType(appId, modelId, data);
            saveTransactional(appId, modelId, data, dataId);
        }
        data.remove(MONGO_ID);

        callback = Optional.ofNullable(dataEventService.callback(modelId, dataId, DataEventType.DATA_NEW, data, false)).orElseGet(RuleExecuteDto::new);

        // 保存数据变更日志
        String version = saveDataLog(dataEventType, appId, modelId, dataId, data, oldData);
        data.put(DATA_LOG_VERSION_KEY, version);

        if (ObjectNull.isNotNull(callback.getSyncMessageTips())) {
            return callback;
        }
        return callback;
    }

    /**
     * 保存数据版本
     *
     * @param dataEventType 版本类型
     * @param appId         应用id
     * @param modelId       数据模型id
     * @param dataId        数据id
     * @param data          待保存的数据
     * @param oldData       已保存的数据
     * @return 数据版本号
     */
    private String saveDataLog(DataEventType dataEventType, String appId, String modelId, String dataId, Map<String, Object> data, Map<String, Object> oldData) {
        if (DataEventType.DATA_NEW.equals(dataEventType)) {

            MapDifference<String, Object> difference = Maps.difference(new HashMap<>(), data);
            DataChangePo o = saveDataChange(appId, modelId, oldData, difference, data).get(0);
            ArrayList<Object> value = new ArrayList<>();
            value.add(new Dict().set("timestamp", DateUtil.now()).set("content", UserCurrentUtils.getRealName() + "添加了数据"));
            if (designConfig.getAi()) {
                try {
                    ArrayList<AiDocumentDto> document = new ArrayList<>();
                    document.add(new AiDocumentDto().setId(dataId).setName(dataId).setDatasetName(modelId).setDatasetId(modelId).setContent(o.getContent()));
                    jvsAiDatasetApi.documents(document);
                } catch (Exception e) {

                }
            }
            return dataLogService.saveLog(modelId, dataId, data, value, DataEventType.DATA_NEW);
        }
        if (DataEventType.DATA_UPDATE.equals(dataEventType)) {
            MapDifference<String, Object> difference = Maps.difference(oldData, data);
            List<DataChangePo> dataChange = null;
            if (!difference.areEqual()) {
                dataChange = saveDataChange(appId, modelId, oldData, difference, data);
            }
            if (designConfig.getAi()) {
                try {
                    ArrayList<AiDocumentDto> document = new ArrayList<>();
                    document.add(new AiDocumentDto().setId(dataId).setName(dataId).setDatasetName(modelId).setDatasetId(modelId).setContent(dataChange.get(0).getContent()));
                    jvsAiDatasetApi.documents(document);
                } catch (Exception e) {

                }
            }
            return dataLogService.saveLog(modelId, dataId, data, Arrays.asList(dataChange.toArray()), DataEventType.DATA_UPDATE);
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveReturnVersion(String appId, String modelId, Map<String, Object> data) {
        saveData(appId, modelId, data);
        String dataVersion = (String) data.get(DATA_LOG_VERSION_KEY);
        data.remove(DATA_LOG_VERSION_KEY);
        return dataVersion;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveTransactional(String appId, String dataModelId, Map<String, Object> data) {
        String dataId = IdGenerator.getIdStr();
        data.put("dataId", dataId);
        saveTransactional(appId, dataModelId, data, dataId);
        data.remove(MONGO_ID);
        return dataId;
    }

    @Override
    public String saveTransactionalRule(String appId, String modelId, Map<String, Object> data) {
        String dataId = IdGenerator.getIdStr();
        data.put("dataId", dataId);
        //生成自动顺序键
        this.handleIncreasedIdFields(appId, modelId, Collections.singletonList(data));
        //保存数据
        this.onlySave(modelId, dataId, data);
        data.remove(MONGO_ID);
        return dataId;
    }

    private void saveTransactional(String appId, String modelId, Map<String, Object> data, String dataId) {
        DynamicDataUtils.clearEcho(data);
        this.handleIncreasedIdFields(appId, modelId, Collections.singletonList(data));
        //记录日志
        jvsAppLogService.savelog(modelId, AppLogTypeEnum.add, null, data);

        //检查同模型下是否存在关联字段是否也存在这几个key中
        Map<String, DataFieldPo> list = dataFieldService.list(Wrappers.query(new DataFieldPo().setModelId(modelId)).lambda().orderByDesc(DataFieldPo::getCreateTime).in(DataFieldPo::getFieldType, DataFieldType.CONTAINER)).stream().filter(a -> {
            if (ObjectNull.isNull(a.getDesignJson())) {
                return false;
            }
            if (DataFieldType.tab.equals(a.getFieldType())) {
                return true;
            }
            return data.keySet().contains(a.getFieldKey());
        }).distinct().collect(Collectors.toMap(DataFieldPo::getFieldKey, Function.identity(), (t1, t2) -> t1));
        //保存或更新下级表格里的数据
        for (DataFieldPo dataFieldPo : list.values()) {
            FieldPublicHtml publicHtml = iDataFieldHandler.get(dataFieldPo.getFieldType().getDesc()).toHtml(dataFieldPo.getDesignJson());
            saveOrUpdateSubsetTable(appId, publicHtml, data, modelId, "");
        }
        this.onlySave(modelId, dataId, data);
    }

    /**
     * 修改或新增下级控件为表格的数据，获取数据修改数据库或新增数据库
     *
     * @param e          当前这个组件
     * @param data       数据集
     * @param parentPath
     */
    private void saveOrUpdateSubsetTable(String appId, FieldPublicHtml e, Map<String, Object> data, String modelId, String... parentPath) {
        List<String> strings = new ArrayList<String>(Arrays.asList(parentPath));
        strings.add(e.getProp());
        switch (e.getType()) {
            case tableForm:
                //递归获取下级的控件，判断是否是表格控件，如果是表格控件，并模型不为空，则把控件里面的数据，把控件数据获取出来进行保存或更新
                TableFormItemHtml tableFormItemHtml = tableFormFieldHandler.toHtml(e.getDesignJson());
                String dataKey = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining());
                String tablePathKey = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining(StrUtil.DOT));
                //如果是表格组件,直接更新数据，根据路径获取数据
                Object o = JvsJsonPath.read((data), tablePathKey);
                //如果读取数据为空,需要将关联数据清空,当关联了模型的表格只最后一行数据也被删除后,数据库中的数据也需要被设置为空数组,否则下次加载会出现BUG
                if (ObjectNull.isNull(o)) {
                    JSONPath.set(data, tablePathKey, DynamicDataConstant.getEmpty(tableFormItemHtml.getType().getAClass()));
                }
                //判断有没有删除的字段
                Object del = JvsJsonPath.read(data, tablePathKey + "_del");
                //关联模型不为空的情况
                if (ObjectNull.isNotNull(tableFormItemHtml.getDataModelId())) {
                    if (o instanceof List) {
                        //类型转换为listMap
                        List<Map<String, Object>> box = ((List) o);
                        //如果已删除没有了,但还有未删除的
                        if (ObjectNull.isNotNull(box)) {
                            dataFieldService.getIncreasedIdFields(appId, modelId).stream().forEach(s -> {
                                box.forEach(s1 -> s1.put(s.getFieldKey(), data.get(s.getFieldKey())));
                            });
                            //获取主模型的流水号 获取当前模型的流水号进行填充
                            this.handleIncreasedIdFields(appId, tableFormItemHtml.getDataModelId(), box);
                        }
                        saveOrUpdateTable(appId, tableFormItemHtml, box, del);
                        //将路径里面的数据给删除掉
                        //data.remove(tablePathKey);
                        data.remove(tablePathKey + "_del");
                        data.remove(tablePathKey + "_line");
                    }

                }
                break;
            case tab:
                TabItemHtml tabItemHtml = tabFieldHandler.toHtml(e.getDesignJson());
                //判断是否添加了数据脱离，数据添加了，操作数据就将其数据格式进行分开处理
                if (tabItemHtml.getDetachData()) {
                    //只处理下级的表格，其它的字段不需要处理
                    for (FormValueHtml dicDatum : tabItemHtml.getDicData()) {
                        //过滤出表格的操作
                        List<FieldBasicsHtml> fieldBasicsHtmls = tabItemHtml.getColumn().get(dicDatum.getName());
                        //某一个选项里面没有配置
                        if (ObjectNull.isNull(fieldBasicsHtmls)) {
                            continue;
                        }
                        List<FieldBasicsHtml> tableFields = fieldBasicsHtmls.stream().filter(s -> s.getType().equals(DataFieldType.tableForm)).collect(Collectors.toList());
                        for (FieldBasicsHtml tableField : tableFields) {
                            //如果属性不为空，获取的数据方式不一样
                            if (ObjectNull.isNotNull(dicDatum.getProp())) {
                                //获取表格的数据
                                LinkedHashMap tableData = (LinkedHashMap) data.get(dicDatum.getProp());
                                saveOrUpdateSubsetTable(appId, tableField, tableData, modelId);
                            } else {
                                //获取表格的数据
                                saveOrUpdateSubsetTable(appId, tableField, data, modelId);
                            }
                        }

                    }
                } else {
                    if (tabItemHtml.getNext()) {
                        //判断是否添加了脱离数据
                        //处理的数据结构就不一致,除表格外，其它都不处理
                        Map<String, List<FieldBasicsHtml>> column = tabItemHtml.getColumn();
                        for (String tabKey : column.keySet()) {
                            List<FieldBasicsHtml> fieldBasicsHtmls = column.get(tabKey);
                            if (ObjectNull.isNotNull(fieldBasicsHtmls)) {
                                List<FieldBasicsHtml> fieldPublicHtmls = fieldBasicsHtmls.stream().filter(s -> DataFieldType.CONTAINER.contains(s.getType())).collect(Collectors.toList());
                                if (ObjectNull.isNotNull(fieldPublicHtmls)) {
                                    for (FieldBasicsHtml fieldPublicHtml : fieldPublicHtmls) {
                                        List<String> collect = new ArrayList<>();
                                        if (tabItemHtml.getDetachData()) {
                                            //如果开启了数据脱离，path的路径需要调整
                                        } else {
                                            List<String> tableKeys = new ArrayList<String>(strings);
                                            tableKeys.add(tabKey);
                                            collect = tableKeys.stream().filter(ObjectNull::isNotNull).collect(Collectors.toList());
                                        }
                                        //将数据再传递至下级控件继续递归
                                        saveOrUpdateSubsetTable(appId, fieldPublicHtml, data, modelId, collect.toArray(new String[0]));
                                    }
                                }
                            }
                        }
                    }
                }
                //寻找下级
                break;
            case step:
                //寻找下级
                break;
            default:
        }
    }

    /**
     * 直接保存或更新表格组件
     *
     * @param dataFieldPo 当前控件字段
     * @param dels        删除的字段
     * @param box         当前控件的前端传递的数据
     * @return 返回保存后的所有Id的值
     */
    private List<String> saveOrUpdateTable(String appId, TableFormItemHtml dataFieldPo, List<Map<String, Object>> box, Object dels) {
        //一对多表格
        if (ObjectNull.isNotNull(dels)) {
            List<JSONObject> linkedHashMapList = JSONArray.parseArray(JSON.toJSONString(dels), JSONObject.class);
            linkedHashMapList.forEach(e -> {
                String id = Optional.ofNullable(e.get(Get.name(DataFieldPo::getId))).map(String::valueOf).orElse(null);
                //删除字段
                if (ObjectNull.isNotNull(id)) {
                    try {
                        this.onlyRemove(dataFieldPo.getDataModelId(), id);
                    } catch (BusinessException businessException) {
                        //屏蔽删除错误，可能数据已经被删除了
                    }
                }
            });

        }
        // 保存数据
        List<String> dataIds = new ArrayList<>();
        box.stream().forEach(s -> {
            //根据条件查询出历史数据，删除之前的数据
            String id = Optional.ofNullable(s.get(Get.name(DataFieldPo::getId))).map(String::valueOf).orElse(null);
            // 新增
            if (StringUtils.isEmpty(id)) {
                id = IdGenerator.getIdStr();
                s.put("dataId", id);
                //将ID和dataId保持一致
                s.put("id", id);
                //流水号添加
                handleIncreasedIdFields(appId, dataFieldPo.getDataModelId(), Collections.singletonList(s));
                onlySave(dataFieldPo.getDataModelId(), id, s);
            } else {
                try {
                    // 修改
                    DynamicDataUtils.freePermit();
                    DynamicDataPo childDataPo = this.get(dataFieldPo.getDataModelId(), id);
                    s.remove("_id");
                    onlyUpdate(dataFieldPo.getDataModelId(), id, childDataPo.getJsonData(), s);
                } catch (Exception e) {
                    // 逻辑引擎查询回显的数据存在表单列表中时导致提交找不到数据。找不到数据时，需要挪新增操作
                    {
                        id = IdGenerator.getIdStr();
                        s.put("dataId", id);
                        //将ID和dataId保持一致
                        s.put("id", id);
                        //流水号添加
                        handleIncreasedIdFields(appId, dataFieldPo.getDataModelId(), Collections.singletonList(s));
                        onlySave(dataFieldPo.getDataModelId(), id, s);
                    }
                }
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_FREE, false);
            }
            dataIds.add(id);
        });
        //删除历史的关联性

        return dataIds;

    }

    @Override
    public boolean saveBatch(List<DynamicDataPo> dataList, String dataModelId) {
        if (ObjectUtils.isEmpty(dataList)) {
            return false;
        }
        List<Map<String, Object>> mapList = dataList.stream().map(this::paresMap).collect(Collectors.toList());
        return this.onlySave(dataModelId, mapList);
    }

    @Override
    public RuleExecuteDto saveBatch(String appId, String modelId, List<Map<String, Object>> dataList) {
        if (StringUtils.isBlank(modelId) || ObjectUtils.isEmpty(dataList)) {
            return null;
        }
        this.handleIncreasedIdFields(appId, modelId, dataList);
        RuleExecuteDto callback = null;
        for (Map<String, Object> data : dataList) {
            String dataId = IdGenerator.getIdStr();
            if (!data.containsKey("dataId")) {
                data.put("dataId", dataId);
            }
            callback = dataEventService.callback(modelId, dataId, DataEventType.DATA_IMPORT, data, true);
            if (ObjectNull.isNotNull(callback) && ObjectNull.isNotNull(callback.getStats()) && !callback.getStats()) {
                return callback;
            }
        }
        this.onlySave(modelId, dataList);
        // 导入后置处理
        for (Map<String, Object> data : dataList) {
            String dataId = String.valueOf(data.get("dataId"));
            data.remove(MONGO_ID);
            callback = Optional.ofNullable(dataEventService.callback(modelId, dataId, DataEventType.DATA_IMPORT, data, false)).orElseGet(RuleExecuteDto::new);
            if (ObjectNull.isNotNull(callback.getStats()) && !callback.getStats()) {
                return callback;
            }
        }
        return callback;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RuleExecuteDto update(String appId, String modelId, String dataId, Map<String, Object> data) {
        RuleExecuteDto ruleExecuteDto = updateData(appId, modelId, dataId, data);
        data.remove(DATA_LOG_VERSION_KEY);
        return ruleExecuteDto;
    }

    /**
     * 批量修改数据
     */
    @Override
    public long updateMulti(String modelId, Map<String, Object> filterData, Map<String, Object> setData) {
        List<QueryConditionDto> collect = filterData.keySet().stream().map(e -> new QueryConditionDto().setValue(filterData.get(e)).setFieldKey(e).setEnabledQueryTypes(DataQueryType.eq)).collect(Collectors.toList());
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(collect);
        // 构建更新条件
        Query query = this.getPermitQuery(criteria);
        // 设置更新操作
        Update update = new Update();
        //修改的值
        for (Map.Entry<String, Object> entry : setData.entrySet()) {
            if (ObjectNull.isNotNull(entry.getValue())) {
                update.set(entry.getKey(), entry.getValue());
            }
        }
        return dataModelHandler.updateMulti(query, update, modelId).getMatchedCount();
    }


    /**
     * 批量修改数据
     */
    @Override
    public long updateMulti(String modelId, List<QueryConditionDto> queryConditionDtos, Map<String, Object> setData) {
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditionDtos);
        // 构建更新条件
        Query query = this.getPermitQuery(criteria);
        // 设置更新操作
        Update update = new Update();
        //修改的值
        if (ObjectNull.isNull(setData.keySet())) {
            return 0;
        }
        for (Map.Entry<String, Object> entry : setData.entrySet()) {
            if (entry.getValue() instanceof BigDecimal) {
                //todo  这里可能有子项
                update.set(entry.getKey(), new Decimal128((BigDecimal) entry.getValue()));
            } else {
                update.set(entry.getKey(), entry.getValue());
            }
        }
        return dataModelHandler.updateMulti(query, update, modelId).getMatchedCount();
    }

    @Override
    public void updateBatchById(String modelId, List<Map<String, Object>> setData) {
        if (ObjectNull.isNull(setData)) {
            return;
        }
        List<Pair<Query, Update>> updateList = new ArrayList<>(setData.size());
        setData.forEach(data -> {
            String dataId = String.valueOf(data.get("dataId"));
            this.updateFill(modelId, dataId, data);
            Criteria criteria = DynamicDataUtils.initCriteria(null).and(Get.name(DynamicDataPo::getId)).is(dataId);
            Query query = new Query(criteria);
            // 设置更新操作
            Update update = new Update();
            data.forEach((key, value) -> {
                if (value != null) {
                    update.set(key, value);
                }
                if (ObjectNull.isNotNull(value) || "".equals(value)) {
                    if (value.equals(DATA_EMPTY)) {
                        value = null;
                        update.set(key, null);
                    } else {
                        update.set(key, value);
                    }
                }
            });
            Pair<Query, Update> updatePair = Pair.of(query, update);
            updateList.add(updatePair);
        });
        dataModelHandler.updateBatch(updateList, modelId);
    }

    /**
     * 批量删除数据
     */
    @Override
    public void removeMulti(String dataModelId, Map<String, Object> filterData) {
        List<QueryConditionDto> collect = filterData.keySet().stream().map(e -> new QueryConditionDto().setValue(filterData.get(e)).setFieldKey(e).setEnabledQueryTypes(DataQueryType.eq)).collect(Collectors.toList());
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(collect);
        // 构建更新条件
        Query query = this.getPermitQuery(criteria);
        dataModelHandler.remove(query, dataModelId);
    }


    /**
     * 批量删除数据
     *
     * @return
     */
    @Override
    public List<Object> removeMulti(String dataModelId, List<QueryConditionDto> queryConditionDtoList) {
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditionDtoList);
        // 构建更新条件
        Query query = this.getPermitQuery(criteria);
        List<Object> objects = dataModelHandler.findAllAndRemove(query, dataModelId);
        if (ObjectNull.isNotNull(objects)) {
            dataModelHandler.insertBatch(objects, DataModelUtil.buildRemoveCollectionName(dataModelId));
            // 删除流程数据
            removeTask(Convert.toList(Map.class, objects));
        }
        return objects;
    }


    /**
     * 修改数据(会发送回调)
     * <p>
     * 1. 数据操作: 对原数据的Map做putAll操作, 而非数据覆盖
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据
     * @param appId   应用id
     * @return 数据版本号
     */
    private RuleExecuteDto updateData(String appId, String modelId, String dataId, Map<String, Object> data) {
        DataModelUtil.setCurrentSaveData();
        DynamicDataPo dataPo = this.get(modelId, dataId);
        //清除空值
        Set<String> nullMap = new HashSet<>();
        data.keySet().removeIf(e -> {
            boolean equals = DATA_EMPTY.equals(data.get(e));
            if (equals) {
                nullMap.add(e);
            }
            return equals;
        });
        RuleExecuteDto callback = Optional.ofNullable(dataEventService.callback(modelId, dataId, DataEventType.DATA_UPDATE, data, true)).orElseGet(RuleExecuteDto::new);
        if (ObjectNull.isNotNull(callback.getStats()) && !callback.getStats()) {
            return callback;
        }
        nullMap.forEach(e -> data.put(e, DATA_EMPTY));
        //校验字段类型
        checkDataFieldType(appId, modelId, data);
        //设置空值
        updateTransactional(appId, modelId, dataId, data, dataPo);
        data.remove(MONGO_ID);
        data.keySet().removeIf(e -> DATA_EMPTY.equals(data.get(e)));
        //清除空值
        callback = dataEventService.callback(modelId, dataId, DataEventType.DATA_UPDATE, data, false);

        String version = saveDataLog(DataEventType.DATA_UPDATE, appId, modelId, dataId, data, Optional.ofNullable(dataPo).map(DynamicDataPo::getJsonData).orElseGet(Collections::emptyMap));
        data.put(DATA_LOG_VERSION_KEY, version);
        return callback;
    }


    private void updateTransactional(String appId, String modelId, String dataId, Map<String, Object> data, DynamicDataPo dataPo) {
        DynamicDataUtils.clearEcho(data);

        //检查同模型下是否存在关联字段是否也存在这几个key中
        Map<String, DataFieldPo> list = dataFieldService.list(Wrappers.query(new DataFieldPo().setJvsAppId(appId).setModelId(modelId)).lambda().in(DataFieldPo::getFieldType, DataFieldType.CONTAINER)).stream().filter(a -> {
            if (ObjectNull.isNull(a.getDesignJson())) {
                return false;
            }
            if (DataFieldType.tab.equals(a.getFieldType())) {
                return true;
            }
            return data.keySet().contains(a.getFieldKey());
        }).collect(Collectors.toMap(DataFieldPo::getFieldKey, Function.identity(), (t1, t2) -> t1));

        //保存或更新下级表格里的数据
        for (DataFieldPo dataFieldPo : list.values()) {
            FieldPublicHtml publicHtml = iDataFieldHandler.get(dataFieldPo.getFieldType().getDesc()).toHtml(dataFieldPo.getDesignJson());
            saveOrUpdateSubsetTable(appId, publicHtml, data, modelId, "");
        }
        //可能模型没有找到
        if (ObjectNull.isNull(dataPo)) {
            return;
        }
        Map<String, Object> jsonData = dataPo.getJsonData();
        jvsAppLogService.savelog(modelId, AppLogTypeEnum.update, jsonData, data);
        this.onlyUpdate(modelId, dataId, jsonData, data);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateReturnVersion(String appId, String modelId, String dataId, Map<String, Object> setData) {
        updateData(appId, modelId, dataId, setData);
        String dataVersion = (String) setData.get(DATA_LOG_VERSION_KEY);
        setData.remove(DATA_LOG_VERSION_KEY);
        return dataVersion;
    }

    /**
     * 记录修改日志
     *
     * @param appId      应用ID
     * @param modelId    模型ID
     * @param jsonData   原始数据
     * @param difference 修改数据
     * @param data       修改后的数据
     */
    @Override
    public List<DataChangePo> saveDataChange(String appId, String modelId, Map<String, Object> jsonData, MapDifference<String, Object> difference, Map<String, Object> data) {
        Map<String, FieldBasicsHtml> map = dataFieldService.getFields(appId, modelId, true, false).stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
        //记录内容变化到一个字段中去
        List<DataChangePo> dataChange = new ArrayList<DataChangePo>();
        DataChangePo dataChangePo = new DataChangePo();
        UserDto userDto = UserCurrentUtils.getNullableUser();
        if (ObjectNull.isNotNull(userDto)) {
            dataChangePo.setUserName(userDto.getRealName()).setHeadImg(userDto.getHeadImg());
        }
        dataChangePo.setTimestamp(DateUtil.now());

        List<String> changes = new ArrayList<>();

        //新增的字段
        difference.entriesOnlyOnRight().keySet().stream().filter(map::containsKey).filter(e -> ObjectNull.isNotNull(difference.entriesOnlyOnRight().get(e))).forEach(e -> {
            switch (map.get(e).getType()) {
                case htmlEditor:
                case signature:
                case imageUpload:
                case file:
                case fileUpload:
                case flowNode:
                case image:
                case tableForm:
                case tab:
                case flowTable:
                case jsonEditor:
                    String s = " 创建了 " + map.get(e).getFieldName();
                    changes.add(s);
                    break;
                default:
                    Object echo = dataFieldHandler.echo(map.get(e), difference.entriesOnlyOnRight().get(e), data);
                    if (!(map.get(e).getFieldName().equals("创建时间") || map.get(e).getFieldName().equals("修改时间"))) {
                        String b = " 创建了 " + map.get(e).getFieldName() + "为 " + echo;
                        changes.add(b);
                    }
            }

        });

        //需要将修改记录,单独存放到一个数据表中,用于数据留痕
        //todo 后续添加上是否记录此模型的变化日志，优化数据处理
        difference.entriesDiffering().keySet().stream().filter(e -> {
                    return !DATA_CHANGE.equalsIgnoreCase(e);
                })
                //判断字段
                .filter(map::containsKey).map(e -> {
                    if (ObjectNull.isNull(difference.entriesDiffering().get(e))) {
                        return null;
                    }
                    if (ObjectNull.isNull(difference.entriesDiffering().get(e).leftValue(), difference.entriesDiffering().get(e).rightValue())) {
                        return null;
                    }
                    if (difference.entriesDiffering().get(e).leftValue().equals(difference.entriesDiffering().get(e).rightValue())) {
                        return null;
                    } else {
                        switch (map.get(e).getType()) {
                            case htmlEditor:
                            case signature:
                            case imageUpload:
                            case file:
                            case fileUpload:
                            case flowNode:
                            case image:
                            case tableForm:
                            case tab:
                            case flowTable:
                            case jsonEditor:
                                String s = " 创建了 " + map.get(e).getFieldName();
                                return s;
                            default:
                                Object right = dataFieldHandler.echo(map.get(e), difference.entriesDiffering().get(e).rightValue(), data);
                                Object left = dataFieldHandler.echo(map.get(e), difference.entriesDiffering().get(e).leftValue(), data);
                                return "更新了 \"" + map.get(e).getFieldName() + "\"   " + left + " 为  " + right;
                        }
                    }
                }).filter(ObjectNull::isNotNull)
                //将所有的变化添加起来
                .forEach(changes::add);
        dataChangePo.setContent(String.join("<br />", changes));
        dataChange.add(dataChangePo);
        return dataChange;
    }

    @Override
    public RuleExecuteDto remove(String modelId, String dataId) {
        DynamicDataPo dataPo = this.get(modelId, dataId);
        Map<String, Object> oldData = dataPo.getJsonData();
        RuleExecuteDto callback = dataEventService.callback(dataPo.getModelId(), dataId, DataEventType.DATA_DELETE, oldData, true);
        if (ObjectNull.isNotNull(callback) && !callback.getStats()) {
            return callback;
        }
        Map<String, Object> body = this.onlyRemove(modelId, dataId);
        // 删除关联表单数据,默认不修改其它数据
        ArrayList<Object> value = new ArrayList<>();
        value.add(new Dict().set("timestamp", DateUtil.now()).set("content", UserCurrentUtils.getRealName() + "删除了数据"));
        dataLogService.saveLog(modelId, dataId, body, value, DataEventType.DATA_DELETE);
        oldData.remove(MONGO_ID);
        callback = dataEventService.callback(dataPo.getModelId(), dataId, DataEventType.DATA_DELETE, oldData, false);
        return callback;
    }

    @Override
    public Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts, List<String> fieldKeyList, boolean addButtonInfo, boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls) {
        return queryPage(appId, page, modelId, combiningFieldFormulaContentMap, conditions, sorts, fieldKeyList, addButtonInfo, echo, andOr, fieldBasicsHtmls, new HashSet<>());
    }

    @Override
    public Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts, List<String> fieldKeyList, boolean addButtonInfo, boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls, Set<String> stringSet) {
        return queryPage(appId, page, modelId, combiningFieldFormulaContentMap, conditions, sorts, fieldKeyList, addButtonInfo, echo, andOr, fieldBasicsHtmls, new HashSet<>(), true);
    }

    @Override
    public Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts, List<String> fieldKeyList, boolean addButtonInfo, boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls, Set<String> stringSet, Boolean encryptionData) {
        if (ObjectNull.isNotNull(fieldBasicsHtmls)) {
            //这里会发布应用到模板中心。字段可能为空
            Set<String> baseField = fieldBasicsHtmls.stream().map(FieldPublicHtml::getFieldKey).collect(Collectors.toSet());
            //添加动态数据组件
            List<DataTableFieldDesignHtml> pageAutoTableFields = dataFieldDynamicService.getPageAutoTableFields(appId, modelId, getDesignId());
            if (ObjectNull.isNotNull(pageAutoTableFields)) {
                List<String> dynamicFiled = pageAutoTableFields.stream().peek(e -> {
                    FieldBasicsHtml copy = BeanCopyUtil.copy(e.getDesignJson(), FieldBasicsHtml.class);
                    copy.setFieldKey(copy.getProp());
                    copy.setDesignJson(e.getDesignJson());
                    if (!baseField.contains(copy.getFieldKey())) {
                        //需要判断是否存在
                        fieldBasicsHtmls.add(copy);
                    }
                }).map(DataTableFieldDesignHtml::getAliasColumnName).filter(ObjectNull::isNotNull).collect(Collectors.toList());
                fieldKeyList.addAll(dynamicFiled);
            }
        }
        if (ObjectNull.isNotNull(combiningFieldFormulaContentMap)) {
            //添加公式中使用到的字段
            combiningFieldFormulaContentMap.values().forEach(e -> fieldKeyList.addAll(e.getRelatedIds()));
        }
        long size = page.getSize();
        long current = page.getCurrent();
        Page<Map<String, Object>> mapPage = new Page<>(current, size);
        List<Criteria> criteriaList = DynamicDataUtils.getAuthCriteria();
        Criteria authCriteria = DynamicDataUtils.trueCriteria();
        List<Criteria> list = new ArrayList<>();
        //将查询条件和列表过滤条件进行分离
        if (ObjectNull.isNotNull(conditions)) {
            //列表过滤条件
            List<QueryConditionDto> collect = conditions.stream().flatMap(Collection::stream).filter(QueryConditionDto::getCrud).collect(Collectors.toList());
            List<QueryConditionDto> queryConditionDtoList = conditions.stream().flatMap(Collection::stream).filter(s -> !s.getCrud()).collect(Collectors.toList());
            queryConditionDtoList.removeIf(collect::contains);
            //过滤出列表过滤的数据
            if (ObjectNull.isNotNull(collect) && ObjectNull.isNull(queryConditionDtoList)) {
                //将列表过滤和数据权限添加上组合查询
                List<Criteria> crud = buildDynamicCriteriaList(collect);
                if (ObjectNull.isNotNull(criteriaList, crud)) {
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud).orOperator(criteriaList);
                } else if (ObjectNull.isNotNull(criteriaList)) {
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(criteriaList);
                } else if (ObjectNull.isNotNull(crud)) {
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud);
                }
            } else if (ObjectNull.isNotNull(criteriaList) && ObjectNull.isNull(queryConditionDtoList)) {
                if (criteriaList.size() == 1) {
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(criteriaList);
                } else {
                    authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteriaList);
                }
            }
            if (ObjectNull.isNotNull(collect, queryConditionDtoList)) {
                List<QueryConditionDto> conditionDtos = queryConditionDtoList.stream().filter(e -> !DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                List<QueryConditionDto> conditionDtoList = queryConditionDtoList.stream().filter(e -> DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                //将列表过滤和数据权限添加上组合查询
                List<Criteria> crud = buildDynamicCriteriaList(collect);
                if (ObjectNull.isNotNull(conditionDtos)) {
                    List<Criteria> criteria = buildDynamicCriteriaList(conditionDtos);
                    if (ObjectNull.isNotNull(criteria)) {
                        crud.addAll(criteria);
                    }
                    if (andOr) {
                        if (ObjectNull.isNotNull(criteriaList, crud)) {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud).orOperator(criteriaList);
                        } else if (ObjectNull.isNotNull(criteriaList)) {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(criteriaList);
                        } else if (ObjectNull.isNotNull(crud)) {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud);
                        }
                    } else {
                        if (ObjectNull.isNotNull(conditionDtoList)) {
                            List<Criteria> buildDynamicCriteriaList = buildDynamicCriteriaList(conditionDtoList);
                            if (ObjectNull.isNotNull(criteria, crud) && ObjectNull.isNull(buildDynamicCriteriaList)) {
                                criteria.forEach(e -> e.andOperator(crud));
                                authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteria);
                            } else if (ObjectNull.isNotNull(criteriaList) && ObjectNull.isNull(crud)) {
                                authCriteria = DynamicDataUtils.trueCriteria().andOperator(buildDynamicCriteriaList);
                            } else if (ObjectNull.isNotNull(crud) && ObjectNull.isNull(buildDynamicCriteriaList)) {
                                authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud);
                            } else if (ObjectNull.isNotNull(crud) && ObjectNull.isNotNull(buildDynamicCriteriaList)) {
                                criteria.addAll(crud);
                                buildDynamicCriteriaList.forEach(e -> e.andOperator(criteria));
                                authCriteria = DynamicDataUtils.trueCriteria().orOperator(buildDynamicCriteriaList);
                            }
                        }
                    }
                } else if (ObjectNull.isNotNull(conditionDtoList)) {
                    List<Criteria> buildDynamicCriteriaList = buildDynamicCriteriaList(conditionDtoList);
                    if (ObjectNull.isNotNull(buildDynamicCriteriaList, crud)) {
                        buildDynamicCriteriaList.forEach(e -> e.andOperator(crud));
                        authCriteria = DynamicDataUtils.trueCriteria().orOperator(buildDynamicCriteriaList);
                    } else if (ObjectNull.isNotNull(criteriaList) && ObjectNull.isNull(crud)) {
                        authCriteria = DynamicDataUtils.trueCriteria().andOperator(buildDynamicCriteriaList);
                    } else if (ObjectNull.isNotNull(crud) && ObjectNull.isNull(buildDynamicCriteriaList)) {
                        authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud);
                    }
                }
            } else if (ObjectNull.isNull(collect) && ObjectNull.isNull(criteriaList) && ObjectNull.isNotNull(queryConditionDtoList)) {
                List<QueryConditionDto> conditionDtos = queryConditionDtoList.stream().filter(e -> !DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                List<QueryConditionDto> conditionDtoList = queryConditionDtoList.stream().filter(e -> DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                List<Criteria> buildDynamicCriteriaList = buildDynamicCriteriaList(conditionDtoList);
                List<Criteria> dynamicCriteriaList = buildDynamicCriteriaList(conditionDtos);
                if (ObjectNull.isNotNull(buildDynamicCriteriaList, dynamicCriteriaList)) {
                    buildDynamicCriteriaList.forEach(e -> e.andOperator(dynamicCriteriaList));
                    if (andOr) {
                        authCriteria = DynamicDataUtils.trueCriteria().andOperator(buildDynamicCriteriaList);
                    } else {
                        authCriteria = DynamicDataUtils.trueCriteria().orOperator(buildDynamicCriteriaList);
                    }
                } else if (ObjectNull.isNotNull(buildDynamicCriteriaList) && ObjectNull.isNull(dynamicCriteriaList)) {
                    if (andOr) {
                        authCriteria = DynamicDataUtils.trueCriteria().andOperator(buildDynamicCriteriaList);
                    } else {
                        authCriteria = DynamicDataUtils.trueCriteria().orOperator(buildDynamicCriteriaList);
                    }
                } else if (ObjectNull.isNull(buildDynamicCriteriaList) && ObjectNull.isNotNull(dynamicCriteriaList)) {
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(dynamicCriteriaList);
                }
            } else if (ObjectNull.isNull(collect) && ObjectNull.isNull(queryConditionDtoList)) {

            } else if (ObjectNull.isNotNull(criteriaList, queryConditionDtoList)) {
                List<QueryConditionDto> conditionDtos = queryConditionDtoList.stream().filter(e -> !DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                List<QueryConditionDto> conditionDtoList = queryConditionDtoList.stream().filter(e -> DataQueryType.like.equals(e.getEnabledQueryTypes())).collect(Collectors.toList());
                if (ObjectNull.isNotNull(conditionDtos) && ObjectNull.isNull(conditionDtoList)) {
                    List<Criteria> criteria = buildDynamicCriteriaList(conditionDtos);
                    criteriaList.forEach(e -> e.andOperator(criteria));
                    authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteriaList);
                } else if (ObjectNull.isNull(conditionDtos) && ObjectNull.isNotNull(conditionDtoList)) {
                    Set<String> field = criteriaList.stream().map(Criteria::getKey).filter(ObjectNull::isNotNull).collect(Collectors.toSet());
                    //这里需要将数据权限中的流程权限给删除掉，重新设置
                    criteriaList.removeIf(e -> ObjectNull.isNull(e.getKey()));
                    conditionDtoList.removeIf(e -> field.contains(e.getFieldKey()));
                    List<Criteria> criteria = buildDynamicCriteriaList(conditionDtoList);
                    criteria.forEach(e -> e.orOperator(criteriaList));
                    authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteria);
                } else if (ObjectNull.isNotNull(conditionDtos) && ObjectNull.isNotNull(conditionDtoList)) {
                    Set<String> field = criteriaList.stream().map(Criteria::getKey).filter(ObjectNull::isNotNull).collect(Collectors.toSet());
                    conditionDtos.removeIf(e -> field.contains(e.getFieldKey()));
                    List<Criteria> criteria = buildDynamicCriteriaList(conditionDtos);
                    conditionDtoList.removeIf(e -> field.contains(e.getFieldKey()));
                    List<Criteria> dynamicCriteriaList = buildDynamicCriteriaList(conditionDtoList);
                    Criteria dynamic1 = new Criteria().orOperator(dynamicCriteriaList);
                    Criteria dynamic2 = new Criteria().orOperator(criteria);
                    Criteria dynamic3 = new Criteria().orOperator(criteriaList);
                    authCriteria = DynamicDataUtils.trueCriteria().andOperator(dynamic1, dynamic2, dynamic3);
                }
            }
        } else if (ObjectNull.isNotNull(criteriaList)) {
            authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteriaList);
        }


        Boolean isFree = SystemThreadLocal.get(DynamicDataUtils.KEY_AUTH_FREE);
        if (ObjectNull.isNotNull(isFree) && isFree) {
            List<Criteria> criteria = buildDynamicCriteriaList(conditions.get(0));
            authCriteria = DynamicDataUtils.trueCriteria().andOperator(criteria);
        }
        Query query;
        if (ObjectNull.isNotNull(list)) {
            query = DynamicDataUtils.andOr(list, authCriteria, andOr);
        } else {
            query = new Query(authCriteria);
        }
        //限制查询字段
        query.fields().exclude(MONGO_ID);
        if (ObjectUtils.isNotEmpty(fieldKeyList)) {
            Set<String> fieldKeys = new HashSet<>(fieldKeyList);
            // 默认加上数据id
            fieldKeys.add(Get.name(DynamicDataPo::getId));
            String[] fields = new String[fieldKeys.size()];
            query.fields().include(fieldKeys.toArray(fields));
        }
        //如果没有跳过
        // 查询总页数
        long total = dataModelHandler.count(query, modelId);

        long skip = size * (current - 1);
        mapPage.setTotal(total);
        if (total == 0 || skip >= total || size < 1 || current < 1) {
            mapPage.setRecords(Collections.emptyList());
            return mapPage;
        }
        if (ObjectNull.isNotNull(sorts)) {
            List<Sort.Order> collect = sorts.stream().filter(e -> ObjectNull.isNotNull(e.getDirection(), e.getFieldKey())).map(e -> new Sort.Order(e.getDirection(), e.getFieldKey())).collect(Collectors.toList());
            if (ObjectNull.isNotNull(collect)) {
                Sort sort = Sort.by(collect);
                query.with(sort);
            }
        } else {
            // 默认按创建时间倒序排序  需要在库里面创建对应的索引,进行数据加速
            Sort sort = Sort.by(Sort.Direction.DESC, Get.name(DynamicDataPo::getCreateTime))
                    //导入进来的数据时间一致.需要再通过ID排序
                    .and(Sort.by(Sort.Direction.ASC, "dataId"));
            query.with(sort);
        }
        // 查询数据
        query.skip(skip).limit((int) size);
        List<Map> mapList = dataModelHandler.find(query, Map.class, modelId);
        List<Map<String, Object>> dataList = mapList.stream().map(e -> (Map<String, Object>) e).collect(Collectors.toList());
        // 获取设计字段
        DataModelPo byId = dataModelService.getById(modelId);
        if (echo) {
            RequestAttributes context = null;
            Authentication authenticationAuthentication = null;
            try {
                context = RequestContextHolder.currentRequestAttributes();
                SecurityContext authentication = SecurityContextHolder.getContext();
                authenticationAuthentication = authentication.getAuthentication();
            } catch (IllegalStateException e) {
            }
            String tenantId = TenantContextHolder.getTenantId();
            Map<String, Object> systemThreadLocalMap = SystemThreadLocal.get();
            Map<String, Object> map = new HashMap<>();
            systemThreadLocalMap.entrySet()
                    .forEach(e -> {
                        if (ObjectNull.isNotNull(e.getValue())) {
                            try {
                                map.put(e.getKey(), BeanCopyUtil.deepCopy(e.getValue()));
                            } catch (Exception ex) {

                            }
                        }
                    });
            Authentication finalAuthenticationAuthentication = authenticationAuthentication;
            RequestAttributes finalContext = context;
            Map<String, FieldBasicsHtml> fieldMap = fieldBasicsHtmls.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));

            dataList = dataList.parallelStream().map(e -> {
                SystemThreadLocal.setAll(map);
                TenantContextHolder.setTenantId(tenantId);
                if (ObjectNull.isNotNull(finalContext)) {
                    //设置上下文user对象
                    RequestContextHolder.setRequestAttributes(finalContext);
                    Object principal = finalAuthenticationAuthentication.getPrincipal();
                    if (!(principal instanceof String)) {
                        SystemThreadLocal.set("user", principal);
                    }
                    SecurityContextHolder.getContext().setAuthentication(finalAuthenticationAuthentication);
                }
                return echo(e, fieldMap, false);
            }).collect(Collectors.toList());
            //清理逻辑 可能会存在缓存
            RuleStartUtils.threadLocalCache.remove();
        }
        if (addButtonInfo) {
            designHandler.handleButtonInfo(dataList, EnvConstant.PAGE_BUTTON_DISPLAY);
        }
        //数据脱敏操作
        if (encryptionData) {
            dataList.forEach(e -> encryptionData(e, byId, true));
        }
        if (ObjectNull.isNotNull(combiningFieldFormulaContentMap)) {
            dataList.forEach(e -> combiningFieldFormulaContent(e, combiningFieldFormulaContentMap));
        }
        // 封装返回值
        mapPage.setRecords(dataList);
        return mapPage;
    }

    /**
     * 组合字段
     *
     * @param line                            某一行的数据
     * @param combiningFieldFormulaContentMap 可能需要进行转换的数据
     */
    private void combiningFieldFormulaContent(Map<String, Object> line, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap) {
        for (String key : combiningFieldFormulaContentMap.keySet()) {
            Object o = designHandler.runFormula(combiningFieldFormulaContentMap.get(key), line, EnvConstant.PAGE_BUTTON_DISPLAY);
            line.put(key + DynamicDataUtils.SUFFIX_ECHO, o);
            line.put(key, o);
        }
    }

    @Override
    public List<DynamicDataPo> queryList(List<String> modelIds) {
        if (ObjectUtils.isEmpty(modelIds)) {
            return Collections.emptyList();
        }
        Query query = new Query();
        query.fields().exclude(MONGO_ID);
        List<DynamicDataPo> result = new ArrayList<>();
        for (String modelId : modelIds) {
            List<Map> dataList = dataModelHandler.find(query, Map.class, modelId);
            result.addAll(dataList.stream().map(e -> parseBean((Map<String, Object>) e)).collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Sort sorts) {
        return queryList(modelId, criteria, fieldKeyList, sorts, null);
    }

    @Override
    public List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Sort sorts, Integer limit) {
        if (StringUtils.isBlank(modelId) || ObjectUtils.isEmpty(fieldKeyList)) {
            return Collections.emptyList();
        }
        // 对于字典查询，绕过权限检查
        Query query;
        if (criteria != null) {
            // 检查是否是字典回显查询
            boolean isDictionaryQuery = false;
            try {
                // 添加多重保护，防止各种空指针情况
                if (fieldKeyList != null && fieldKeyList.size() == 2) {
                    String criteriaStr = String.valueOf(criteria);  // 使用String.valueOf而不是toString()
                    if (criteriaStr != null && !"null".equals(criteriaStr) && criteriaStr.length() > 0) {
                        isDictionaryQuery = criteriaStr.contains("id") &&
                                           (criteriaStr.contains("eq") || criteriaStr.contains("=")) &&
                                           fieldKeyList.contains("id") &&
                                           (fieldKeyList.contains("label") || fieldKeyList.contains("name"));
                    }
                }
            } catch (Exception e) {
                // 如果任何检查抛出异常，记录警告并使用默认行为
                log.warn("检查criteria时出现异常，将使用默认权限检查 - modelId={}, error={}", modelId, e.getMessage());
                log.debug("criteria异常详情", e);
                // 异常时默认使用权限检查，确保安全性
                isDictionaryQuery = false;
            }

            if (isDictionaryQuery) {
                // 字典查询，直接创建查询，绕过权限
                log.info("字典查询绕过权限检查 - modelId={}, criteria={}, fields={}", modelId, criteria, fieldKeyList);
                query = new Query(criteria);
            } else {
                // 普通查询，应用权限
                query = this.getPermitQuery(criteria);
            }
        } else {
            // criteria为null，可能是获取所有数据，也绕过权限（字典获取场景）
            log.info("无查询条件的列表查询，绕过权限检查 - modelId={}, fields={}", modelId, fieldKeyList);
            query = new Query();
        }

        query.with(sorts);
        if (ObjectNull.isNotNull(limit)) {
            query.limit(limit);
        }
        String field = Get.name(DynamicDataPo::getId);
        if (!fieldKeyList.contains(field)) {
            // 指定查询字段
            fieldKeyList.add(Get.name(DynamicDataPo::getId));
        }
        fieldKeyList = fieldKeyList.stream().distinct().collect(Collectors.toList());
        // 指定查询字段
        String[] fields = new String[fieldKeyList.size()];
        query.fields().exclude(MONGO_ID).include(fieldKeyList.toArray(fields));
        List<Map> dataList = dataModelHandler.find(query, Map.class, modelId);
        if (ObjectNull.isNull(dataList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> collect = dataList.stream().map(e -> (Map<String, Object>) e).collect(Collectors.toList());
        //如果是快速检索 需要去重数据
        try {
            if (RETRIVAL.equals(WebUtils.getRequest().getHeader(QUERY_BY))) {
                //只返回10条数据
//                collect = collect.subList(0, 10);
            }
        } catch (Exception e) {
            // 定时任务无法获取请求头.
        }
        return collect;
    }

    @Override
    public List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Integer limit) {
        // 默认按创建时间倒序排序
        Sort sort = Sort.by(Get.name(DynamicDataPo::getCreateTime)).descending();
        return queryList(modelId, criteria, fieldKeyList, sort, limit);

    }

    @Override
    public List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList) {
        // 默认按创建时间倒序排序
        Sort sort = Sort.by(Get.name(DynamicDataPo::getCreateTime)).descending();
        return queryList(modelId, criteria, fieldKeyList, sort, null);

    }


    @Override
    public List<Map<String, Object>> queryList(String modelId, String... fieldKey) {
        return queryList(modelId, Arrays.stream(fieldKey).collect(Collectors.toList()));
    }

    @Override
    public List<Map<String, Object>> queryList(String modelId, List<String> fieldKey) {
        if (StringUtils.isBlank(modelId) || ObjectNull.isNull(fieldKey)) {
            // 不传字段时, 数据量可能较大, 直接返回空集合
            return Collections.emptyList();
        }
        Criteria criteria = DynamicDataUtils.initCriteria(null);
        Query query = this.getPermitQuery(criteria);
        // 默认按创建时间倒序排序
        Sort sort = Sort.by(Get.name(DynamicDataPo::getCreateTime)).descending();
        query.with(sort);
        // 指定查询字段
        fieldKey.add(Get.name(DynamicDataPo::getId));
        query.fields().exclude(MONGO_ID).include(fieldKey.toArray(new String[0]));
        List<Map> dataList = dataModelHandler.find(query, Map.class, modelId);
        if (ObjectNull.isNull(dataList)) {
            //如果查询为空,返回为空数组
            return new ArrayList<>();
        }
        return dataList.stream().map(e -> (Map<String, Object>) e).collect(Collectors.toList());
    }

    @Override
    public List<Map> queryList(String appId, String modelId, Criteria criteria, Sort sort, List<String> excludeFieldKeyList, List<String> fieldKeyList) {
        if (StringUtils.isBlank(modelId)) {
            return Collections.emptyList();
        }
        Query query = this.getPermitQuery(criteria);

        // 默认按创建时间倒序排序

        if (ObjectNull.isNull(sort)) {
            // 默认按创建时间倒序排序
            sort = Sort.by(Sort.Direction.DESC, Get.name(DynamicDataPo::getCreateTime)).descending();
            query.with(sort);
        } else {
            query.with(sort);
        }
        if (CollectionUtils.isNotEmpty(excludeFieldKeyList)) {
            String[] excludeFields = new String[excludeFieldKeyList.size()];
            query.fields().exclude(excludeFieldKeyList.toArray(excludeFields));
        }
        if (CollectionUtils.isNotEmpty(fieldKeyList)) {
            // 指定查询字段
            String[] fields = new String[fieldKeyList.size()];
            query.fields().include(fieldKeyList.toArray(fields));
        }
        List<Map> dataList = dataModelHandler.find(query, Map.class, modelId);
        return dataList;
    }

    @Override
    public Map<String, Object> querySingle(String appId, String modelId, String dataId) {
        return this.querySingle(appId, modelId, dataId, false);
    }

    @Override
    public Map<String, Object> querySingle(String appId, String modelId, String dataId, boolean addButtonInfo) {
        if (StringUtils.isBlank(modelId) || StringUtils.isBlank(dataId)) {
            throw new BusinessException("数据查询异常, 查询条件为空");
        }
        Map<String, Object> dataMap = this.getMap(modelId, dataId);
        if (addButtonInfo) {
            designHandler.handleButtonInfo(dataMap, EnvConstant.FORM_BUTTON_DISPLAY);
        }

        return dataMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object queryField(String appId, String modelId, String dataId, String fieldId) {
        Map<String, Object> map = this.querySingle(appId, modelId, dataId);
        if (!map.containsKey(fieldId)) {
            //没有数据直接返回为空
            return null;
//            throw new BusinessException("数据不存在");
        }
        return map.get(fieldId);
    }

    @Override
    public Map<String, Object> paresMap(DynamicDataPo data) {
        Map<String, Object> map = BeanToMapUtils.beanToMap(data);
        map.put(Get.name(DynamicDataPo::getCreateTime), LocalDateTimeUtil.formatNormal((LocalDateTime) map.get(Get.name(DynamicDataPo::getCreateTime))));
        map.put(Get.name(DynamicDataPo::getUpdateTime), LocalDateTimeUtil.formatNormal((LocalDateTime) map.get(Get.name(DynamicDataPo::getUpdateTime))));
        Map<String, Object> dynamicMap = data.getJsonData();
        map.remove(Get.name(DynamicDataPo::getJsonData));
        dynamicMap.putAll(map);
        return dynamicMap;
    }

    @Override
    public Map<String, Object> paresMap(DynamicDataPo data, Collection<String> fieldKeys) {
        Map<String, Object> map = BeanToMapUtils.beanToMap(data);
        Map<String, Object> dynamicMap = this.clear(data.getJsonData(), fieldKeys);
        map.remove(Get.name(DynamicDataPo::getJsonData));
        dynamicMap.putAll(map);
        return dynamicMap;
    }

    @Override
    public DynamicDataPo parseBean(Map<String, Object> data) {
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        DynamicDataPo dataPo = BeanCopyUtil.copy(DynamicDataPo.class, data);
        Map<String, Object> beanMap = BeanToMapUtils.beanToMap(dataPo);
        for (String key : beanMap.keySet()) {
            data.remove(key);
        }
        dataPo.setJsonData(data);
        return dataPo;
    }

    /**
     * 查询数据对象
     *
     * @param dataId 数据id
     * @return 数据对象
     */
    private DynamicDataPo get(String modelId, String dataId) {
        return this.parseBean(this.getMap(modelId, dataId));
    }

    /**
     * 查询数据
     *
     * @param dataId 数据id
     * @return 数据
     */
    private Map<String, Object> getMap(String modelId, String dataId) {
        if (StringUtils.isBlank(dataId)) {
            throw new BusinessException("数据id为空");
        }
        Criteria criteria = DynamicDataUtils.initCriteria(null).and(Get.name(DynamicDataPo::getId)).is(dataId);
        Query query = this.getPermitQuery(criteria);
        // 排除MongoDB的id字段
        query.fields().exclude(MONGO_ID);
        Map<String, Object> dataMap = dataModelHandler.findOne(query, Map.class, modelId);
        if (Objects.isNull(dataMap)) {
            log.info("数据不存在, 模型id: {}, 数据id: {}", modelId, dataId);
            //不返回空数据,避免报错,直接返回空对象
            return new HashMap<>(1);
        }
        return dataMap;
    }

    @Override
    public String onlySave(String modelId, Map<String, Object> data) {
        String dataId = IdGenerator.getIdStr();
        return onlySave(modelId, dataId, data);
    }

    /**
     * 保存数据
     *
     * @param modelId 数据模型id
     * @param data    数据内容
     * @return 新增后的数据id
     */
    private String onlySave(String modelId, String dataId, Map<String, Object> data) {
        if (ObjectUtils.isEmpty(data)) {
            data = new HashMap<>(1);
        }
        // 保存至MongoDB
        dataModelHandler.insert(this.insertFill(modelId, dataId, data), modelId);
        return dataId;
    }

    /**
     * 保存数据
     *
     * @param modelId  数据模型id
     * @param dataList 数据内容
     * @return 新增后的数据id
     */
    private boolean onlySave(String modelId, List<Map<String, Object>> dataList) {
        if (ObjectUtils.isEmpty(dataList)) {
            return false;
        }
        // 保存至MongoDB
        List<Map<String, Object>> mapList = dataList.stream().map(data -> this.insertFill(modelId, String.valueOf(data.getOrDefault("dataId", "")), data)).collect(Collectors.toList());
        int batch = 2000;
        int size = mapList.size();
        for (int i = 0; i < size; i += batch) {
            List<Map<String, Object>> subList = mapList.subList(i, Math.min(i + batch, size));
            dataModelHandler.insertBatch(subList, modelId);
        }
        return true;
    }

    /**
     * 删除数据
     *
     * @param dataId 数据id
     * @return 删除前的数据内容
     */
    @Override
    public Map<String, Object> onlyRemove(String modelId, String dataId) throws BusinessException {
        //逻辑删除
        Criteria criteria = Criteria.where(Get.name(DynamicDataPo::getId)).is(dataId);
        Query query = this.getPermitQuery(criteria);
        Map andRemove = dataModelHandler.findAndRemove(query, Map.class, modelId);
        if (ObjectNull.isNotNull(andRemove)) {
            jvsAppLogService.savelog(modelId, AppLogTypeEnum.remove, andRemove, null);

            dataModelHandler.insert(andRemove, DataModelUtil.buildRemoveCollectionName(modelId));
            // 删除数据的流程任务
            removeTask(Collections.singletonList(andRemove));
        }
        return andRemove;
    }

    @Override
    public void onlyUpdate(String modelId, String dataId, Map<String, Object> data) {
        onlyUpdate(modelId, dataId, null, data);
    }

    /**
     * 修改数据
     *
     * @param dataId  数据id
     * @param oldData 原数据内容
     * @param newData 数据内容(key相同时会覆盖)
     * @return 修改后的数据内容
     */
    private Map<String, Object> onlyUpdate(String modelId, String dataId, Map<String, Object> oldData, Map<String, Object> newData) {
        if (ObjectUtils.isEmpty(newData)) {
            newData = Collections.emptyMap();
        }
        if (ObjectUtils.isEmpty(oldData)) {
            oldData = newData;
        }
        // 更新至MongoDB
        newData = this.updateFill(modelId, dataId, newData);
        // 构建更新条件
        Criteria criteria = DynamicDataUtils.initCriteria(null).and(Get.name(DynamicDataPo::getId)).is(dataId);
        Query query = this.getPermitQuery(criteria);
        // 设置更新操作
        Update update = new Update();
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof BigDecimal) {
                    //todo  这里可能有子项
                    update.set(entry.getKey(), new Decimal128((BigDecimal) entry.getValue()));
                } else {
                    update.set(entry.getKey(), entry.getValue());
                }
            }
            if (ObjectNull.isNotNull(entry.getValue()) || "".equals(entry.getValue())) {
                if (entry.getValue().equals(DATA_EMPTY)) {
                    entry.setValue(null);
                    update.set(entry.getKey(), null);
                } else {
                    if (entry.getValue() instanceof BigDecimal) {
                        update.set(entry.getKey(), new Decimal128((BigDecimal) entry.getValue()));
                    } else {
                        update.set(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        dataModelHandler.updateFirst(query, update, modelId);
        return newData;
    }


    /**
     * 填充新增时的数据字段
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据内容
     * @return 填充后的数据内容
     */
    private Map<String, Object> insertFill(String modelId, String dataId, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof BigDecimal) {
                entry.setValue(new Decimal128((BigDecimal) entry.getValue()));
            } else {
                entry.setValue(entry.getValue());
            }
        }
        LocalDateTime now = LocalDateTime.now();
        UserDto user = UserCurrentUtils.getNullableUser();
        if (StringUtils.isBlank(dataId)) {
            // id默认为雪花算法
            dataId = IdWorker.getIdStr();
        }
        data = this.updateFill(modelId, dataId, data);
        // 逻辑删除字段
        Object delFlag = Optional.ofNullable(data.get(Get.name(DynamicDataPo::getDelFlag))).orElse(Boolean.FALSE);
        data.put(Get.name(DynamicDataPo::getDelFlag), delFlag);
        // BasePo
        if (ObjectNull.isNotNull(user)) {
            data.put(Get.name(DynamicDataPo::getJobId), user.getJobId());
            data.put(Get.name(DynamicDataPo::getDeptId), user.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toSet()));
            data.put(Get.name(DynamicDataPo::getCreateById), user.getId());
            data.put(Get.name(DynamicDataPo::getCreateBy), user.getRealName());
        }
        String name = Get.name(DynamicDataPo::getCreateTime);
        // BasalPo
        if (ObjectNull.isNull(data.containsKey(name))) {
            data.put(name, LocalDateTime.now());
        } else {
            try {
                DateTime dateTime = DateUtil.parseDateTime(data.get(name).toString());
            } catch (Exception e) {
                data.put(name, LocalDateTime.now());
            }
        }
        return data;
    }

    /**
     * 填充更新时的数据字段
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据内容
     * @return 填充后的数据内容
     */
    private Map<String, Object> updateFill(String modelId, String dataId, Map<String, Object> data) {
        UserDto user = UserCurrentUtils.getNullableUser();
        if (Objects.isNull(data)) {
            data = new HashMap<>(16);
        }
        String id = data.getOrDefault("dataId", dataId).toString();
        data.put(Get.name(DynamicDataPo::getId), id);
        if (StringUtils.isNotBlank(modelId)) {
            data.put(Get.name(DynamicDataPo::getModelId), modelId);
        }
        // BasalPo
        data.put(Get.name(DynamicDataPo::getUpdateTime), LocalDateTime.now());
        if (ObjectNull.isNotNull(user)) {
            data.put(Get.name(DynamicDataPo::getUpdateBy), user.getRealName());
            data.put("updateById", user.getId());
        }
        //如果不删除的情况可能会导致时间格式出错
        String name = Get.name(DynamicDataPo::getCreateTime);
        if (data.containsKey(name)) {
            data.remove(name);
        }
        return data;
    }

    /**
     * 筛选指定key的数据
     *
     * @param originalData 原始数据(该集合的元素可能会被删除)
     * @param keys         需要的key集合
     * @return 筛选后的数据
     */
    private Map<String, Object> clear(Map<String, Object> originalData, Collection<String> keys) {
        if (ObjectUtils.isEmpty(originalData) || ObjectUtils.isEmpty(keys)) {
            return new HashMap<>(1);
        }
        int targetSize = keys.size();
        int size = originalData.size();
        if (targetSize > size) {
            originalData.keySet().removeIf(e -> !keys.contains(e));
            return originalData;
        }
        Map<String, Object> result = new HashMap<>(targetSize);
        for (String key : keys) {
            result.put(key, originalData.get(key));
        }
        return result;
    }

    @Override
    public Map<String, Object> paresMapWithEcho(String appId, Map<String, Object> data, String modelId, String designId, boolean override) {
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, designId, true, false);
        // 数据类型回显处理
        return echo(data, fields, override);
    }

    @Override
    public Map<String, Object> paresMapWithEcho(String appId, DynamicDataPo data, String modelId) {
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, true, false);
        List<FieldBasicsHtml> relationFields = fields.stream().filter(f -> DataFieldType.tableForm.equals(f.getType())).collect(Collectors.toList());
        // 不是自己看字段中是否存在脱敏处理
        // 数据类型回显处理
        fields.removeAll(relationFields);
        Map<String, Object> echo = this.echo(this.paresMap(data), relationFields, true);
        DataModelPo byId = dataModelService.getById(modelId);
        this.paresMap(data);
        //处理表单中的脱敏
        encryptionData(echo, byId);
        // 关联数据类型回显处理
        return echo;
    }

    /**
     * 数据表单脱敏处理
     *
     * @param data
     * @param byId
     */
    @Override
    public void encryptionData(Map<String, Object> data, DataModelPo byId) {
        if (ObjectNull.isNotNull(byId)) {
            encryptionData(data, byId, false);
        }
    }

    public void encryptionData(Map<String, Object> data, DataModelPo byId, Boolean page) {
        if (ObjectNull.isNotNull(byId, byId.getSetting())) {
            if (ObjectNull.isNull(byId.getSetting().getEncryptionFields())) {
                return;
            }
            List<PersonnelDto> userList = byId.getSetting().getUserList();
            if (!RoleUtils.checkPersonnels(userList)) {
                //放行。不处理
                return;
            }
            if (ObjectNull.isNotNull(userList)) {
                if (!byId.getSetting().getEncryption()) {
                    return;
                }
                List<EncryptionFieldsPo> encryptionFields = byId.getSetting().getEncryptionFields();
                encryptionFields.forEach(e -> {
                    try {
                        if (data.containsKey(e.getFieldKey())) {
                            Object orDefault = data.getOrDefault(e.getFieldKey(), "");
                            if (!page && orDefault instanceof Number) {
                                data.remove(e.getFieldKey());
                            } else {
                                String o = String.valueOf(orDefault);
                                String desensitized = SensitiveInfoUtils.getSensitiveKey().get(e.getEncryptionExpress()).apply(o);
                                //支持下拉转换后的脱敏
                                if (data.containsKey(e.getFieldKey() + "_1")) {
                                    data.put(e.getFieldKey() + "_1", desensitized);
                                } else {
                                    data.put(e.getFieldKey(), desensitized);
                                }
                            }
                        }
                    } catch (Exception exception) {
                        log.error("脱敏失败", exception);
                    }
                });
            }
        }
    }

    @Override
    public List<String> encryptionData(String byId) {
        DataModelPo byId1 = dataModelService.getById(byId);
        return encryptionData(byId1);
    }

    @Override
    public List<String> encryptionData(DataModelPo byId) {
        List<String> list = new ArrayList<>();
        if (ObjectNull.isNotNull(byId, byId.getSetting())) {
            if (ObjectNull.isNull(byId.getSetting().getEncryptionFields())) {
                return list;
            }
            List<PersonnelDto> userList = byId.getSetting().getUserList();
            if (!RoleUtils.checkPersonnels(userList)) {
                //放行。不处理
                return list;
            }
            if (ObjectNull.isNotNull(userList)) {
                List<EncryptionFieldsPo> encryptionFields = byId.getSetting().getEncryptionFields();
                List<String> field = encryptionFields.stream().map(EncryptionFieldsPo::getFieldKey).collect(Collectors.toList());
                return field;
            }
        }
        return list;
    }

    /**
     * 数据对象转Map结构数据
     * <p>
     * 并处理回显内容
     *
     * @param data     数据对象(Map)
     * @param fieldMap 字段信息集合
     * @param override 回显字段是否覆盖原字段
     * @return Map数据
     */
    @Override
    public Map<String, Object> echo(Map<String, Object> data, Map<String, FieldBasicsHtml> fieldMap, boolean override) {
        return echo(data, fieldMap, override, ExportFieldDto::getObject);
    }

    @Override
    public Map<String, Object> echo(Map<String, Object> data, Map<String, FieldBasicsHtml> fieldMap, boolean override, Function<ExportFieldDto, Object> function) {
        //数据库的数据，用于多层下级数据
        Map<String, Object> olddata = new HashMap<>(data);
        //因为这个循环是改变内部对象，所以需要创建一个新的
        fieldMap.values().stream().collect(Collectors.toList()).stream().filter(e -> e.getType().equals(DataFieldType.tab))
                //判断是否开启了数据脱离， 将过滤出有多个数据脱离的选项卡
                .filter(e -> {
                    Object eval = JvsJsonPath.read(e.getDesignJson(), Get.name(TabItemHtml::getDetachData));
                    if (ObjectNull.isNotNull(eval)) {
                        return (boolean) eval;
                    } else {
                        //兼容老的选项卡数据值
                        return true;
                    }
                }).forEach(e -> {
                    //将数据脱离的 key ， 组装为对象属性值
                    TabItemHtml html = (TabItemHtml) iDataFieldHandler.get(DataFieldType.tab.getDesc()).toHtml(e);
                    if (ObjectNull.isNotNull(html)) {
                        if (ObjectNull.isNotNull(html.getDicData())) {
                            //判断是否配置了选项的 key名
                            for (FormValueHtml dicDatum : html.getDicData()) {
                                //获取 key名
                                String prop = dicDatum.getProp();
                                List<FieldBasicsHtml> fieldBasicsHtmls = html.getColumn().get(dicDatum.getName());
                                if (ObjectNull.isNull(fieldBasicsHtmls)) {
                                    continue;
                                }
                                if (ObjectNull.isNull(prop)) {
                                    //将这一级的所有字段类型匹配放入外层
                                    fieldBasicsHtmls.forEach(s -> fieldMap.put(s.getProp(), s));
                                } else {
                                    //这里需要创建一个生成组件，用于解析。
                                    TabGenerateItemHtml tabGenerateItemHtml = new TabGenerateItemHtml(prop).setColumn(fieldBasicsHtmls);
                                    //将组件解析器处理到map中在下级回显进行处理
                                    fieldMap.put(prop, tabGenerateItemHtml);
                                }
                            }
                        }
                    }
                });

        // 处理回显数据
        data.entrySet().stream().collect(Collectors.toList()).stream()
                //有key的存在
                .filter(e -> fieldMap.containsKey(e.getKey()))
                .forEach(entry -> {
                    try {
                        if (ObjectNull.isNull(entry.getValue())) {
                            //可能是空数组 ，或空对象。当选择了用户部门，后又取消。
//                            entry.setValue(null);
                        } else {
                            String fieldKey = entry.getKey();
                            Object value = entry.getValue();
                            FieldBasicsHtml fieldDto = fieldMap.get(fieldKey);

                            IDataFieldHandler fieldHandler = iDataFieldHandler.get(fieldDto.getType().getDesc());
                            if (ObjectNull.isNotNull(fieldHandler)) {
                                if (ObjectNull.isNull(fieldDto.getDesignJson())) {
                                    Map generate = fieldHandler.generate(fieldDto.getLabel(), fieldDto.getFieldKey(), new ArrayList<>());
                                    fieldDto.setDesignJson(generate);
                                }
                                FieldBasicsHtml html = fieldHandler.toHtml(fieldDto);
                                Object echoValue = fieldHandler.getEcho(html, value, override, olddata);
                                if (ObjectNull.isNotNull(echoValue)) {
                                    //如果是导出进行特殊处理
                                    echoValue = function.apply(new ExportFieldDto().setField(fieldKey).setType(html.getType()).setObject(echoValue));
                                }
                                String path = DynamicDataUtils.getEchoFieldKey(html.getProp());
                                if ((html.getType().equals(DataFieldType.input) || html.getType().equals(DataFieldType.tabGenerate)) && echoValue instanceof HashMap) {
                                    fieldHandler.setDataOverride(data, fieldKey, html, path, override, "不支持显示");
                                } else {
                                    fieldHandler.setDataOverride(data, fieldKey, html, path, override, echoValue);
                                }
                            }
                        }
                    } catch (BusinessException be) {
                        log.error("格式转换异常{}", JSONObject.toJSONString(entry), be);
                        throw new RuntimeException("格式转换异常" + be.getMessage());
                    } catch (Exception e) {
                        log.error("格式转换异常{}", JSONObject.toJSONString(entry), e);
                        throw new RuntimeException("格式转换异常:" + entry.getKey() + " : " + entry.getValue());
                    }

                });
        return data;
    }

    @Override
    public List echo(List<Map> list, Collection<FieldBasicsHtml> fields, boolean override) {
        for (Map data : list) {
            echo(data, fields, override);
        }
        return list;
    }

    @Override
    public Map<String, Object> echo(Map<String, Object> data, Collection<FieldBasicsHtml> fields, boolean override) {
        Map<String, FieldBasicsHtml> fieldMap = fields.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));
        return echo(data, fieldMap, override);
    }

    /**
     * 新增数据前处理自增序号
     *
     * @param modelId  数据模型id
     * @param dataList 数据集合
     */
    private void handleIncreasedIdFields(String appId, String modelId, List<Map<String, Object>> dataList) {
        if (ObjectUtils.isEmpty(dataList)) {
            return;
        }

        List<FieldBasicsHtml> increasedIdFields = dataFieldService.list(Wrappers.<DataFieldPo>lambdaQuery().eq(DataFieldPo::getModelId, modelId).eq(DataFieldPo::getJvsAppId, appId).eq(DataFieldPo::getFieldType, DataFieldType.serialNumber).ne(DataFieldPo::getDesignType, DesignType.data)).stream().map(e -> BeanCopyUtil.copy(e, FieldBasicsHtml.class)).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(increasedIdFields)) {
            return;
        }
        // 根据字段设计获取序号前缀
        Map<String, String> prefixMap = this.parseOrderPrefix(increasedIdFields);
        int size = dataList.size();
        DataIdPo nextId = dataIdService.nextId(DesignType.data, modelId, size);
        for (Map<String, Object> data : dataList) {
            for (FieldBasicsHtml field : increasedIdFields) {
                String fieldKey = field.getFieldKey();
                String prefix = prefixMap.get(fieldKey);
                // 根据字段设计获取序号
                String orderNumber = this.getOrderNumber(field, prefix, nextId);
                data.put(fieldKey, orderNumber);
            }
            nextId.next();
        }
    }

    /**
     * 根据字段设计获取序号前缀
     *
     * @param fields 字段数据集合
     * @return 序号前缀映射 <字段key, 序号前缀>
     */
    private Map<String, String> parseOrderPrefix(List<FieldBasicsHtml> fields) {
        DateTime now = new DateTime();
        Map<String, String> result = new HashMap<>(1);
        for (FieldBasicsHtml field : fields) {
            OrderFormat orderFormat = BeanCopyUtil.copy(OrderFormat.class, field.getDesignJson());
            // 获取流水号前缀
            String prefix = OrderUtils.parseOrderPrefix(orderFormat, now);
            result.put(field.getFieldKey(), prefix);
        }
        return result;
    }

    /**
     * 根据字段设计获取序号重置规则
     *
     * @param field  字段数据
     * @param nextId 序号数据
     * @return 流水号
     */
    private String getOrderNumber(FieldBasicsHtml field, String prefix, DataIdPo nextId) {
        Map<String, Object> designJson = field.getDesignJson();
        OrderFormat orderFormat = BeanCopyUtil.copy(OrderFormat.class, designJson);
        return OrderUtils.getOrderNumber(prefix, orderFormat, nextId);
    }

    /**
     * 处理数据查询权限
     *
     * @param criteria 查询条件构造类
     */
    private Query getPermitQuery(Criteria criteria) {
        List<Criteria> authCondition = DynamicDataUtils.getAuthCriteria();
        if (Objects.nonNull(criteria) && ObjectNull.isNotNull(authCondition)) {
            return new Query(DynamicDataUtils.trueCriteria().andOperator(criteria).orOperator(authCondition));
        }
        if (ObjectNull.isNull(criteria)) {
            return new Query(trueCriteria());
        }
        return new Query(trueCriteria().andOperator(criteria));
    }

    @Override
    public void checkDataModel(Map<String, Object> data, String modelId) {
        DataModelPo byId = dataModelService.getById(modelId);
        if (ObjectNull.isNull(byId)) {
            throw new BusinessException("数据错误");
        } else {
            DataSettingBo setting = byId.getSetting();
            //如果没有设置
            if (ObjectNull.isNull(setting)) {
                return;
            } else {
                if (setting.isOnce()) {
                    String userId = Optional.ofNullable(UserCurrentUtils.getUserId()).orElseThrow(() -> new BusinessException("用户未登录"));
                    long total = getTotalKey(modelId, "createById", userId);
                    if (total > 0) {
                        throw new BusinessException("数据唯一性校验不通过,存在重复提交,请检查");
                    }
                }

                String formulaContent = setting.getFormulaContent();
                if (ObjectNull.isNotNull(formulaContent)) {
                    if (!formulaContent.trim().isEmpty()) {
                        String expression = setting.getFormulaContent();
                        List<ExpressionParam> expressionParams = ExpressionUtils.parsePostfixExpression(expression);
                        if (expressionParams.isEmpty()) {
                            return;
                        }
                        //处理特殊符
                        Object result = SpringContextUtil.getBean(ExpressionHandler.class).calculate(expression, data, EnvConstant.THIS_DATA_ITEM_VALUE);
                        if (ObjectNull.isNotNull(result)) {
                            //查询库里面是否存在这个值
                            //查询数据库保留字段
                            long total = getTotalKey(modelId, "DataReservedKey", result.toString());
                            if (total > 0) {
                                throw new BusinessException("数据唯一性校验不通过,存在重复提交,请检查");
                            }
                            //如果没有重复，将此数据id存放进数据里面
                            data.put("DataReservedKey", result.toString());
                        }
                    }
                }
            }
        }
    }


    @Override
    public void refreshData(String modelId, DataModelPo dataModelPo) {
        DataModelPo byId = dataModelService.getById(modelId);
        if (ObjectNull.isNull(byId)) {
            throw new BusinessException("数据错误");
        } else {
            DataSettingBo setting = byId.getSetting();
            //如果没有设置
            if (ObjectNull.isNull(setting)) {
                return;
            } else {
                if (setting.isOnce()) {
                    String userId = Optional.ofNullable(UserCurrentUtils.getUserId()).orElseThrow(() -> new BusinessException("用户未登录"));
                    long total = getTotalKey(modelId, "createById", userId);
                    if (total > 0) {
                        dataModelHandler.remove(new Query(new Criteria()), modelId);
                    }
                }
            }
        }

    }

    private long getTotalKey(String modelId, String key, String value) {
        // 拼接查询条件
        List<QueryConditionDto> conditions = new ArrayList<>();
        //创建人等于自己
        conditions.add(new QueryConditionDto().setFieldKey(key).setEnabledQueryTypes(DataQueryType.eq).setValue(value));
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(conditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        Query query = this.getPermitQuery(criteria);
        // 限制查询字段
        query.fields().exclude(MONGO_ID);
        Set<String> fieldKeys = new HashSet<>();
        fieldKeys.add(key);
        // 默认加上数据id
        fieldKeys.add(Get.name(DynamicDataPo::getId));
        String[] fields = new String[fieldKeys.size()];
        query.fields().include(fieldKeys.toArray(fields));
        // 查询总页数
        long total = dataModelHandler.count(query, modelId);
        return total;
    }

    @Override
    public void deleteFields(List<String> fields, String dataModelId) {
        Update update = new Update();
        //将这个字段值设置为空
        fields.forEach(e -> update.set(e, null));
        dataModelHandler.updateMulti(new Query(new Criteria()), update, dataModelId);
    }

    @Override
    public void replaceSourceFieldData(String sourceFieldId, String replaceFieldKey, List<Map<String, Object>> data) {
        if (StringUtils.isBlank(sourceFieldId) || StringUtils.isBlank(replaceFieldKey) || CollectionUtils.isEmpty(data)) {
            return;
        }
        // 获取显示来源字段数据
        DataFieldPo dataFieldPo = dataFieldService.getById(sourceFieldId);
        if (ObjectNull.isNull(dataFieldPo)) {
            return;
        }
        // 目前只处理下拉框
        if (Boolean.FALSE.equals(DataFieldType.select.equals(dataFieldPo.getFieldType()))) {
            return;
        }
        SelectItemHtml selectItemHtml = BeanCopyUtil.copy(dataFieldPo.getDesignJson(), SelectItemHtml.class);
        String sourceModelId = selectItemHtml.getFormId();
        String sourceFieldKey = selectItemHtml.getProps().getLabel();
        String idFieldKey = Get.name(DynamicDataPo::getId);
        List<Object> dataIds = data.stream().map(d -> d.get(replaceFieldKey)).collect(Collectors.toList());
        List<QueryConditionDto> conditionDtos = Arrays.asList(new QueryConditionDto().setFieldKey(idFieldKey).setValue(dataIds).setEnabledQueryTypes(DataQueryType.in));
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(conditionDtos);
        criteria = DynamicDataUtils.initCriteria(criteria);
        List<Map<String, Object>> sourceData = queryList(sourceModelId, criteria, new ArrayList<String>() {{
            add(sourceFieldKey);
        }});
        if (CollectionUtils.isEmpty(sourceData)) {
            return;
        }
        // 替换数据
        data.forEach(d -> {
            Object fieldKeyValue = d.get(replaceFieldKey);
            Object replaceData = sourceData.stream().filter(sdata -> sdata.get(idFieldKey).equals(fieldKeyValue)).map(s -> s.get(sourceFieldKey)).findFirst().orElse(null);
            if (ObjectNull.isNotNull(replaceData)) {
                d.put(replaceFieldKey, replaceData);
            }
        });
    }

    @Override
    public void expandOtherData(Map<String, Object> data) {
        String createUserId = Optional.ofNullable(data.get(Get.name(DynamicDataPo::getCreateById))).orElse("").toString();
        if (StringUtils.isBlank(createUserId)) {
            return;
        }
        Object o = userCache.get(createUserId, true, () -> {
            //这里添加一下缓存避免每一次都去请求
            R<UserDto> userDtoRes = authUserServiceApi.getBasicInfoById(createUserId);
            if (ObjectNull.isNull(userDtoRes)) {
                return null;
            }
            UserDto userDto = userDtoRes.getData();
            return userDto.getHeadImg();
        });
        if (ObjectNull.isNotNull(o)) {
            // 返回创建人头像
            data.put(Get.name(UserDto::getHeadImg), o);
        }
    }

    @Override
    public List<Map<String, Object>> postQueryList(String appId, String dataModelId, QueryListDto queryPageDto) {
        return postQueryList(appId, dataModelId, queryPageDto, true);
    }

    @Override
    public List<Map<String, Object>> postQueryList(String appId, String dataModelId, QueryListDto queryPageDto, Boolean override) {
        DynamicDataUtils.dataModelScope(dataModelId);
        List<List<QueryConditionDto>> conditions = CollectionUtils.isNotEmpty(queryPageDto.getGroupConditions()) ? queryPageDto.getGroupConditions() : Collections.singletonList(queryPageDto.getConditions());
        Criteria criteria = DynamicDataUtils.buildDynamicGroupCriteria(conditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        List<Map<String, Object>> data = queryList(dataModelId, criteria, queryPageDto.getFieldList());
        // 替换显示字段的值
        replaceSourceFieldData(appId, dataModelId, queryPageDto.getFieldList(), data, override);
        return data;
    }

    /**
     * 根据模型查询所有数据
     *
     * @param dataModelId 模型id
     * @param fieldList   字段名  末认会加Id
     * @param list        查询条件
     * @return
     */
    @Override
    public List<Map<String, Object>> queryList(String dataModelId, List<String> fieldList, QueryConditionDto... list) {
        List<QueryConditionDto> queryConditions = Arrays.stream(list).collect(Collectors.toList());
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
        criteria = DynamicDataUtils.initCriteria(criteria);
        return queryList(dataModelId, criteria, fieldList);
    }

    @Override
    public List<Map<String, Object>> postQueryList(String dataModelId, QueryListDto queryPageDto) {

        DynamicDataUtils.dataModelScope(dataModelId);
        List<List<QueryConditionDto>> conditions = CollectionUtils.isNotEmpty(queryPageDto.getGroupConditions()) ? queryPageDto.getGroupConditions() : Collections.singletonList(queryPageDto.getConditions());
        Criteria criteria = DynamicDataUtils.buildDynamicGroupCriteria(conditions);
        if (ObjectNull.isNull(queryPageDto.getFieldList())) {
            List<String> fieldKeys = dataFieldService.getFieldKeys(null, dataModelId);
            queryPageDto.setFieldList(fieldKeys);
        }
        List<Map<String, Object>> data = queryList(dataModelId, criteria, queryPageDto.getFieldList());
        // 替换显示字段的值
        replaceSourceFieldData(queryPageDto.getSourceFieldId(), queryPageDto.getFieldList(), data);
        return data;
    }

    @Override
    public void replaceSourceFieldDataMap(String appId, String dataModelId, Map<String, Object> data) {
        List<FieldBasicsHtml> collect = dataFieldService.getAllField(appId, dataModelId).stream().collect(Collectors.toList());
        echo(data, collect, true);
        // 扩展返回数据
        //判断Data中哪些是key带有_1的，通过_1进行分离并替换数据
        List<String> set = new ArrayList<>();
        data.keySet().forEach(e -> {
            int i = e.indexOf(DynamicDataUtils.SUFFIX_ECHO);
            if (i > 0) {
                data.put(e.substring(0, i), data.get(e));
                set.add(e);
            }
        });
        set.forEach(data::remove);
    }

    @Override
    public void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, List<Map<String, Object>> data) {
        replaceSourceFieldData(appId, dataModelId, fieldList, data, true);
    }

    @Override
    public void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, List<Map<String, Object>> data, Boolean override) {
        if (ObjectNull.isNull(fieldList) || ObjectNull.isNull(data)) {
            return;
        }
        List<FieldBasicsHtml> collect = dataFieldService.getAllField(appId, dataModelId).stream().filter(e -> fieldList.contains(e.getFieldKey())).collect(Collectors.toList());
        for (Map<String, Object> datum : data) {
            echo(datum, collect, override);
        }
    }

    @Override
    public void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, Map<String, Object> data) {
        List<FieldBasicsHtml> collect = dataFieldService.getAllField(appId, dataModelId).stream().filter(e -> fieldList.contains(e.getFieldKey())).collect(Collectors.toList());
        echo(data, collect, true);
    }

    @Override
    public void enableDataItem(List<Map<String, Object>> dataList, List<List<QueryConditionDto>> disableConditions) {
        if (ObjectNull.isNull(dataList) || ObjectNull.isNull(disableConditions)) {
            return;
        }
        // 条件组之间关系为‘或’，组内条件关系为‘且’
        // 所有组条件都不满足，则标记为禁用
        dataList.forEach(data -> {
            // true-满足条件，false-不满足条件
            boolean pass = false;
            for (List<QueryConditionDto> disableConditionGroup : disableConditions) {
                // 组内条件是否全部满足：true-满足所有条件，false-不满足所有条件
                // 只要有一个条件不满足，就不需要再校验当前组的后续条件了
                boolean groupResult = true;
                for (QueryConditionDto queryCondition : disableConditionGroup) {
                    if (Boolean.FALSE.equals(groupResult)) {
                        break;
                    }
                    Object dataValue = data.get(queryCondition.getFieldKey());
                    Object conditionValue = queryCondition.getValue();
                    DataQueryType queryType = queryCondition.getEnabledQueryTypes();
                    // 条件判断
                    if (DataQueryType.isNull.equals(queryType)) {
                        groupResult = ObjectNull.isNull(dataValue);
                        continue;
                    }
                    // 条件值对比
                    if (ObjectNull.isNull(conditionValue) || ObjectNull.isNull(dataValue)) {
                        groupResult = Boolean.FALSE;
                        continue;
                    }
                    switch (queryCondition.getEnabledQueryTypes()) {
                        case eq:
                            groupResult = conditionValue.equals(dataValue);
                            break;
                        case like:
                            groupResult = String.valueOf(dataValue).contains(conditionValue.toString());
                            break;
                        case in:
                            List<Object> conditionValues = new ArrayList<>();
                            if (conditionValue instanceof List) {
                                conditionValues.addAll((List) conditionValue);
                            } else {
                                if (JSONUtil.isTypeJSONObject(conditionValue.toString())) {
                                    if (JSONUtil.isTypeJSONArray(conditionValue.toString())) {
                                        conditionValues = JSONArray.parseArray(JSONObject.toJSONString(conditionValue));
                                    }
                                } else {
                                    conditionValues.add(conditionValues);
                                }
                            }
                            groupResult = conditionValues.stream().anyMatch(condValue -> condValue.equals(dataValue));
                            break;
                        default:
                            log.info("不支持的查询类型");
                            break;
                    }
                }
                // 只要有一个满足条件，则结束判断
                pass = groupResult;
                if (pass) {
                    break;
                }
            }

            // 添加一个固定的字段，标识数据项是否禁用
            data.put("jvsDisableItem", Boolean.FALSE.equals(pass));
        });

    }

    /**
     * 替换显示字段的值
     *
     * @param sourceFieldId 用以替换的显示字段id
     * @param fieldList     查询结果的字段
     * @param data          数据
     */
    private void replaceSourceFieldData(String sourceFieldId, List<String> fieldList, List<Map<String, Object>> data) {
        // 约定fieldList的第一个值为需要替换的字段key
        if (ObjectNull.isNull(fieldList)) {
            return;
        }
        String fieldKey = fieldList.get(0);
        replaceSourceFieldData(sourceFieldId, fieldKey, data);
    }

    /**
     * 删除流程数据
     *
     * @param datas 数据集合
     */
    private void removeTask(List<Map> datas) {
        List<String> dataIds = datas.stream().filter(data -> ObjectNull.isNotNull(data.get("dataId"))).map(data -> String.valueOf(data.get("dataId"))).collect(Collectors.toList());
        if (ObjectNull.isNull(dataIds)) {
            return;
        }
        flowTaskService.removeTaskAllByDataId(dataIds);
    }

    /**
     * 聚合mongo指定设计地址
     *
     * @param queryConditions 组件的查询条件
     * @param collectionName  集合对象
     * @param type            类型，分组，求和，平均
     * @param groupBy         分组数据信息
     * @param aggregateField  聚合字段
     * @return list
     */
    public List<Map> aggregate(List<QueryConditionDto> queryConditions, String collectionName, AggregateEnumType type, String groupBy, String aggregateField) {
        List conditions = Collections.singletonList(queryConditions);
        List<Criteria> list = DynamicDataUtils.buildDynamicGroupCriteriaList(conditions);
        List<Criteria> authCriteria = DynamicDataUtils.getAuthCriteria();
        authCriteria.addAll(list);
        Criteria criteria = DynamicDataUtils.trueCriteria().andOperator(authCriteria);
        return aggregate(criteria, collectionName, type, aggregateField, Fields.from(Fields.field(groupBy)));
    }

    @Override
    public List aggregate(Criteria criteria, String collectionName, AggregateEnumType type, Object aggregateField, Fields groupBy) {
        //根据数据权限和查询条件过滤数据
        GroupOperation groupOperation = ObjectNull.isNotNull(groupBy) ? Aggregation.group(groupBy) : Aggregation.group();
        Aggregation aggregation;

        //根据分组进行处理，是按计数，还是平均，还是求和
        switch (type) {
            case max:
                if (aggregateField instanceof String) {
                    groupOperation.max(((String) aggregateField).trim()).as("value");
                } else if (aggregateField instanceof List) {
                    for (Object e : ((List<?>) aggregateField)) {
                        groupOperation = groupOperation.max(e.toString()).as("value");
                    }
                }
                break;
            case min:
                if (aggregateField instanceof String) {
                    groupOperation.min(((String) aggregateField).trim()).as("value");
                } else if (aggregateField instanceof List) {
                    for (Object e : ((List<?>) aggregateField)) {
                        groupOperation = groupOperation.min(e.toString()).as(e.toString());
                    }
                }
                break;
            case count:
                groupOperation = groupOperation.count().as("value");
                break;
            case ave:
                if (aggregateField instanceof String) {
                    groupOperation.avg(((String) aggregateField).trim()).as("value");
                } else if (aggregateField instanceof List) {
                    for (Object e : ((List<?>) aggregateField)) {
                        groupOperation = groupOperation.avg(e.toString()).as(e.toString());
                    }
                }
                break;
            case sum:
                if (aggregateField instanceof String) {
                    groupOperation.sum(((String) aggregateField).trim()).as("value");
                } else if (aggregateField instanceof List) {
                    for (Object e : ((List<?>) aggregateField)) {
                        groupOperation = groupOperation.sum(e.toString()).as(e.toString());
                    }
                }
            default:

        }
        aggregation = Aggregation.newAggregation(Aggregation.match(criteria), groupOperation);
        List<Map> mappedResults = dataModelHandler.aggregate(aggregation, collectionName);
        mappedResults.forEach(e -> {
            if (ObjectNull.isNotNull(groupBy)) {
                if (groupBy.asList().size() == 1) {
                    e.put("name", e.get("_id"));
                } else {
                    e.putAll((Map) e.get("_id"));
                }
                e.remove(("_id"));
                if (aggregateField instanceof List) {
                    Set<Map.Entry> set = e.entrySet();
                    set.forEach((a) -> {
                        if (ObjectNull.isNull(a.getValue())) {
                            a.setValue(0);
                        }
                    });
                }
            } else {
                e.remove(("_id"));
            }
        });
        return mappedResults;
    }

    @Override
    public List<Map> aggregate(Criteria criteria, String collectionName, AggregateEnumType type, String aggregateField, Fields groupBy) {

        //拼装模型的数据权限
        List<AggregationOperation> operations = new ArrayList<>();
        //根据数据权限和查询条件过滤数据
        operations.add(Aggregation.match(criteria));
        GroupOperation groupOperation = ObjectNull.isNotNull(groupBy) ? Aggregation.group(groupBy) : Aggregation.group();
        //根据分组进行处理，是按计数，还是平均，还是求和
        switch (type) {
            case max:
                operations.add(groupOperation.max(aggregateField).as("value"));
                break;
            case min:
                operations.add(groupOperation.min(aggregateField).as("value"));
                break;
            case count:
                operations.add(groupOperation.count().as("value"));
                break;
            case ave:
                operations.add(groupOperation.avg(aggregateField).as("value"));
                break;
            case sum:
                operations.add(groupOperation.sum(aggregateField).as("value"));
            default:

        }
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<Map> mappedResults = dataModelHandler.aggregate(aggregation, collectionName);
        mappedResults.forEach(e -> {
            if (ObjectNull.isNotNull(groupBy)) {
                if (groupBy.asList().size() == 1) {
                    e.put("name", e.get("_id"));
                } else {
                    e.putAll((Map) e.get("_id"));
                }
                e.remove(("_id"));
            } else {
                e.remove(("_id"));
            }
        });
        return mappedResults;
    }


    /**
     * 所有表单第一次初始化的时候，存在大量的下拉或多选，需要进行转换显示，用于优化数据展示速度
     *
     * @param designId 设计 id
     * @param body     表单的数据
     * @return
     */

    @Override
    public Map<String, Object> handler(String designId, Boolean init, ExecDto body) {
        //获取表单的设计
        if (ObjectNull.isNull(designId)) {
            return body.getParams();
        }
        FormPo formPo = formMapper.selectById(designId);
        if (ObjectNull.isNull(formPo)) {
            return body.getParams();
        }
        DynamicDataUtils.dataModelScope(formPo.getDataModelId());
        // 扩展返回数据
        //获取所有的字段
        Map<String, FieldBasicsHtml> fieldsMap = dataFieldService.getFields(formPo.getJvsAppId(), formPo.getDataModelId(), designId, true, true).stream().filter(e -> ObjectNull.isNotNull(e.getType())).collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
        Map<String, Object> params = body.getParams();
        return getStringObjectMap(init, body, params, fieldsMap, formPo);
    }

    @Override
    public Map<String, Object> getStringObjectMap(Boolean init, ExecDto body, Map<String, Object> params, Map<String, FieldBasicsHtml> fieldsMap, FormPo formPo) {
        //将转换后的数据进行返回给公式
        //表示用户第一次打开表单渲染
        params = echo(params, fieldsMap, false);
        if (init) {
            return params;
        } else {
            //判断是否是表格操作。如果不是表格操作，直接返回
            if (ObjectNull.isNull(body.getIndex())) {
                return params;
            }
            FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
            if (ObjectNull.isNotNull(formDesignHtml.getTablePath())) {
                //返回的数据需要对其进行优化数据处理,避免返回的数据量大导致前端渲染卡顿
                //需要先做转换，再做删除，此处不能直接拿转换的数据进行处理
                //获取公式触发表格操作的这一行数据

                TableType tableType = SystemThreadLocal.get("tableType");
                if (ObjectNull.isNull(tableType)) {
                    return params;
                }
                if (ObjectNull.isNull(body.getParentKey())) {
                    //如果上级为空，则使用触发的值
                    ArrayList<String> parentKey = new ArrayList<String>();
                    parentKey.add(body.getModifiedField());
                    body.setParentKey(parentKey);
                }
                String collect = null;
                try {
                    formDesignHtml.getFormdata().get(0).getForms().stream().filter(e -> e.get("prop").toString().equals(body.getParentKey().get(0))).filter(e -> e.get("detachData").equals(true)).findFirst().get();
                    collect = body.getParentKey().get(body.getParentKey().size() - 1);
                } catch (Exception e) {
                    collect = String.join(".", body.getParentKey());
                }
                Object tableLineList = null;
                //如果是删除只删除，直接返回
                if (add.equals(tableType) || line.equals(tableType)) {
                    String path = collect + "[" + body.getIndex() + "]";
                    tableLineList = JvsJsonPath.read(params, path);
                }
                Object read = JvsJsonPath.read(params, collect);
                //删除和，行级操作。都直接操作。
                for (String tablePath : formDesignHtml.getTablePath()) {
                    //删除表格的数据
                    if (tablePath.equals(collect)) {
                        JSONPath.remove(params, tablePath);
                    }
                    //处理选择卡里面的数据
                    if (!tablePath.contains(".")) {
                        JSONPath.remove(params, tablePath);
                    }
                }
                if (ObjectNull.isNotNull(tableLineList)) {
                    //如果是表格内部操作，只返回某一行的数据，其它行级数据，不返回。 其它的表格数据不返回。
                    //如果是表格添加操作，只返回这个表格新增的那一条数据 。 其它的表格数据不返回。
                    //获取设置的路径
                    JSONPath.set(params, collect + "[0]", tableLineList);
                    if (ObjectNull.isNotNull(read) && read instanceof List) {
                        if (((List) read).size() == 1) {
                            //如果只有一行数据时，则需要进行多返回一个字段，用于前端展示效果
                            JSONPath.set(params, collect + "_line[0]", tableLineList);
                        } else if (collect.contains(".")) {
                            //处理选择卡里面的数据
                            JSONPath.set(params, collect + "_line[0]", tableLineList);
                        }
                        JSONPath.set(params, collect + "_line[0]", tableLineList);
                    }
                }
            }
            //直接返回数据
            return params;
        }
    }

    @Override
    public void removeAll(String modelId) {
        dataModelHandler.removeAll(modelId);
    }

    @Override
    public void echoModelDisplay(String appId, List<Map<String, Object>> dataList, Map<String, ModelDisplayHtml> fieldModelDisplayMap) {
        log.info("DynamicDataServiceImpl echoModelDisplay 开始 - appId={}, dataList={}, fieldModelDisplayMap={}",
                appId,
                dataList == null ? "null" : "size=" + dataList.size(),
                fieldModelDisplayMap == null ? "null" : "size=" + fieldModelDisplayMap.size());

        if (ObjectNull.isNull(dataList, fieldModelDisplayMap)) {
            log.warn("echoModelDisplay 参数为空，直接返回 - dataList={}, fieldModelDisplayMap={}",
                    dataList, fieldModelDisplayMap);
            return;
        }
        for (Map<String, Object> data : dataList) {
            // 获取所有显示配置关联模型数据
            for (Map.Entry<String, ModelDisplayHtml> entry : fieldModelDisplayMap.entrySet()) {
                // 查询关联模型配置的数据
                ModelDisplayHtml modelDisplay = entry.getValue();
                if (ObjectNull.isNull(modelDisplay)) {
                    continue;
                }
                if (ObjectNull.isNull(modelDisplay.getDataLinkageModelId(), modelDisplay.getDataLinkageList(), modelDisplay.getLinkageFieldKeys(), modelDisplay.getShowModelType())) {
                    continue;
                }
                // 一对多，不返回数据。前端手动触发数据查询
                if (DisplayShowModelTypeEnum.oneToMany.equals(modelDisplay.getShowModelType())) {
                    continue;
                }
                // 一对一处理数据回显
                List<String> linkageQueryFieldKeys = modelDisplay.getLinkageFieldKeys().stream().map(ModelDisplayLinkageFieldHtml::getProp).collect(Collectors.toList());

                log.debug("处理关联模型回显: modelId={}, linkageFieldKeys={}", modelDisplay.getDataLinkageModelId(), linkageQueryFieldKeys);

                // 替换条件值，并查询关联模型数据
                List<QueryConditionDto> dataLinkageList = BeanCopyUtil.copys(modelDisplay.getDataLinkageList(), QueryConditionDto.class);
                QueryConditionUtils.replaceConditionValue(dataLinkageList, data);

                log.debug("查询关联模型数据条件: {}", dataLinkageList);

                // 修复回显
                List<Criteria> authCriteria = DynamicDataUtils.getAuthCriteria();
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, null);
                log.debug("临时清除数据权限以查询关联模型数据");

                List<Map<String, Object>> linkageDataList;
                try {
                    linkageDataList = queryList(modelDisplay.getDataLinkageModelId(), linkageQueryFieldKeys, dataLinkageList.toArray(new QueryConditionDto[0]));
                } finally {
                    // 确保数据权限总是被恢复
                    SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, authCriteria);
                    log.debug("恢复数据权限设置");
                }
                if (ObjectNull.isNull(linkageDataList)) {
                    log.warn("关联模型查询结果为空: modelId={}, conditions={}", modelDisplay.getDataLinkageModelId(), dataLinkageList);
                    continue;
                }

                log.debug("关联模型查询结果: 记录数={}, 第一条数据={}", linkageDataList.size(), linkageDataList.get(0));

                // 回显处理
                List<FieldBasicsHtml> allLinkageDataFieldList = dataFieldService.getFields(appId, modelDisplay.getDataLinkageModelId(), true, true).stream().filter(fieldBasicsHtml -> linkageQueryFieldKeys.contains(fieldBasicsHtml.getFieldKey())).collect(Collectors.toList());

                log.debug("关联模型字段列表: 字段数={}, 字段keys={}", allLinkageDataFieldList.size(), allLinkageDataFieldList.stream().map(FieldBasicsHtml::getFieldKey).collect(Collectors.toList()));

                // 只获取一条数据回显
                Map<String, Object> linkageData = linkageDataList.get(0);
                log.info("开始调用echoModelDisplay进行关联字段回显处理: 数据模型ID={}, 关联字段数量={}", modelDisplay.getDataLinkageModelId(), modelDisplay.getLinkageFieldKeys().size());
                echoModelDisplay(data, linkageData, allLinkageDataFieldList, modelDisplay.getLinkageFieldKeys());
            }
        }
    }

    @Override
    public void echoModelDisplay(Map<String, Object> data, Map<String, Object> linkageData, Collection<FieldBasicsHtml> fieldBasicsHtmls, List<ModelDisplayLinkageFieldHtml> linkageFieldKeys) {
        log.info("开始处理模型显示回显: linkageFieldKeys数量={}, fieldBasicsHtmls数量={}", linkageFieldKeys.size(), fieldBasicsHtmls.size());

        // 记录回显前的linkageData状态
        log.debug("回显前linkageData: {}", linkageData);

        echo(linkageData, fieldBasicsHtmls, false);

        // 记录回显后的linkageData状态
        log.debug("回显后linkageData: {}", linkageData);

        log.info("echoModelDisplay2 ,data:{} ,linkageData:{},linkageFieldKeys{}",data,linkageData,linkageFieldKeys);
        linkageFieldKeys.forEach(linkageFieldKey -> {
            String prop = linkageFieldKey.getProp();
            String aliasProp = linkageFieldKey.getAliasProp();
            Object originalValue = linkageData.get(prop);
            Object echoValue = linkageData.get(prop + DynamicDataUtils.SUFFIX_ECHO);

            log.debug("处理关联字段: prop={}, aliasProp={}, originalValue={}, echoValue={}",
                     prop, aliasProp, originalValue, echoValue);

            data.put(aliasProp, originalValue);
            data.put(aliasProp + DynamicDataUtils.SUFFIX_ECHO, echoValue);

            // 如果echoValue为空，记录警告
            if (ObjectNull.isNull(echoValue)) {
                log.warn("关联字段回显值为空: prop={}, originalValue={}, 请检查字典配置或数据模型");
            }
        });
    }

    @Override
    public void clearSensitiveBody(String modelId, Map<String, Object> data, Map<String, Object> oldData) {
        //查看哪些字段是脱敏的，是否脱敏
        DataModelPo model = dataModelService.getModel(modelId);
        if (ObjectNull.isNotNull(model.getSetting())) {
            if (ObjectNull.isNotNull(model.getSetting().getEncryption())) {
                if (model.getSetting().getEncryption()) {
                    //判断加密字段，
                    Map<String, Object> objectMap = new HashMap<>(model.getSetting().getEncryptionFields().size());
                    model.getSetting().getEncryptionFields().forEach(e -> {
                        try {
                            String o = String.valueOf(oldData.getOrDefault(e.getFieldKey(), ""));
                            String desensitized = SensitiveInfoUtils.getSensitiveKey().get(e.getEncryptionExpress()).apply(o);
                            objectMap.put(e.getFieldKey(), desensitized);
                        } catch (Exception ignored) {
                        }
                    });
                    //判断脱敏数据 和原始字段是否一致，如果和加密的结果一致则删除历史的 key
                    objectMap.keySet().forEach(e -> {
                        if (objectMap.get(e).equals(data.get(e))) {
                            data.remove(e);
                        }
                    });
                }
            }
        }

    }
}
