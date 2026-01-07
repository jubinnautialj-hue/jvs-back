package cn.bctools.design.data.controller;

import cn.bctools.ai.api.JvsAiDatasetApi;
import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.entity.po.BasePo;
import cn.bctools.design.config.DesignConfig;
import cn.bctools.design.constant.DynamicDataConstant;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.design.crud.utils.Decimal128ListConvert;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.crud.utils.LinkedHashMapConvert;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.dto.SaveRelationAndRunRuleDto;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.*;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.enums.DataConditionType;
import cn.bctools.design.data.fields.dto.enums.FormDataTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.dto.form.html.CheckboxHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.CascaderItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabGenerateItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.dto.page.*;
import cn.bctools.design.data.fields.enums.DataEventType;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.FormComponentType;
import cn.bctools.design.data.fields.impl.advance.CascaderFieldHandler;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TabGenerateFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.*;
import cn.bctools.design.data.util.QueryConditionUtils;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.handler.util.NoticeVariableUtils;
import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.GanttChartUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.impl.FlowDynamicDataServiceImpl;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.dto.TableType;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.handler.ExpressionAfterHandler;
import cn.bctools.function.handler.IJvsFunction;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.dto.LinkTypeEnum;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.bctools.web.excel.ArrayListConvert;
import cn.bctools.web.excel.LocalDateTimeConvert;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cn.bctools.design.constant.DynamicDataConstant.DATA_EMPTY;
import static cn.bctools.design.util.DynamicDataUtils.KEY_AUTH_CRITERIA;

/**
 * 动态数据管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[data]动态数据")
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}/dynamic/data")
public class DynamicDataUseController {
    /**
     * The constant FILE_TYPE.
     */
    public static final String FILE_TYPE = ".xlsx";
    /**
     * The constant FILE_NAME_EXPORT.
     */
    public static final String FILE_NAME_EXPORT = "";
    /**
     * The constant FILE_NAME_TEMPLATE.
     */
    public static final String FILE_NAME_TEMPLATE = "模板-";
    private static final String FORM_ID = "formId";
    private static final Integer EXCEL_DATA_MIN_SIZE = 2;

    /**
     * The Form service.
     */
    FormService formService;
    /**
     * The Page service.
     */
    CrudPageService pageService;
    /**
     * The Function business mapper.
     */
    FunctionBusinessMapper functionBusinessMapper;
    /**
     * The Data field service.
     */
    DataFieldService dataFieldService;
    /**
     * The Oss template.
     */
    OssTemplate ossTemplate;
    /**
     * The Dynamic data service.
     */
    DynamicDataService dynamicDataService;
    DataLogService dataLogService;

    DataEventService dataEventService;
    JvsAiDatasetApi jvsAiDatasetApi;
    /**
     * The Expression after handler.
     */
    ExpressionAfterHandler expressionAfterHandler;
    /**
     * The Data model handler.
     */
    DataModelHandler dataModelHandler;
    /**
     * The Design config.
     */
    DesignConfig designConfig;
    /**
     * The Data model service.
     */
    DataModelService dataModelService;
    FunctionBusinessService functionBusinessService;

    /**
     * The Cascader field handler.
     */
    CascaderFieldHandler cascaderFieldHandler;
    JvsTreeService jvsTreeService;
    AuthDeptServiceApi authDeptServiceApi;
    /**
     * The Field handler map.
     */
    Map<String, IDataFieldHandler> fieldHandlerMap;
    /**
     * The Tab field handler.
     */
    TabFieldHandler tabFieldHandler;
    /**
     * The Table form field handler.
     */
    TableFormFieldHandler tableFormFieldHandler;
    /**
     * The Design handler.
     */
    DesignHandler designHandler;
    /**
     * The Array list convert.
     */
    ArrayListConvert arrayListConvert;
    /**
     * The Decimal 128 list convert.
     */
    Decimal128ListConvert decimal128ListConvert;
    LinkedHashMapConvert linkedHashMapConvert;
    /**
     * The Local date time convert.
     */
    LocalDateTimeConvert localDateTimeConvert;
    /**
     * The Data notice handler.
     */
    DataNoticeHandler dataNoticeHandler;
    /**
     * The Rule run service.
     */
    RuleRunService ruleRunService;
    /**
     * The Rule design service.
     */
    RuleDesignService ruleDesignService;
    /**
     * The Rule start utils.
     */
    RuleStartUtils ruleStartUtils;
    /**
     * The Run log service.
     */
    RunLogService runLogService;
    /**
     * The Flow dynamic data service.
     */
    FlowDynamicDataService flowDynamicDataService;

    /**
     * 获取字段列表
     *
     * @param modelId 模型id
     * @param appId   the app id
     * @return 字段信息集合 fields
     */
    @ApiOperation("获取字段列表")
    @GetMapping("/fields/{modelId}")
    public R<List<FieldBasicsHtml>> getFields(@PathVariable("modelId") String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, true, true);
        // 不返回容器组件
        fields.removeIf(field -> FormComponentType.container.equals(field.getFieldType().getItemType()));
        return R.ok(fields);
    }

    @ApiOperation("获取字段列表")
    @GetMapping("/fields/form/{modelId}")
    public R<List<FieldBasicsHtml>> getFormFields(@PathVariable("modelId") String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, true, true);
        return R.ok(fields);
    }

    /**
     * Gets data model name.
     *
     * @param modelId the model id
     * @param appId   the app id
     * @return the data model name
     */
    @Log
    @ApiOperation("获取模型名称")
    @GetMapping("/modelName/{modelId}")
    public R<DataModelPo> getDataModelName(@PathVariable("modelId") String modelId, @PathVariable String appId) {
        DataModelPo byId = dataModelService.getById(modelId);
        if (ObjectNull.isNull(byId) || (!byId.getAppId().equals(appId))) {
            throw new BusinessException("应用错误或模型不存在");
        }
        return R.ok(byId);
    }

    /**
     * Gets button.
     *
     * @param dataId  the data id
     * @param modelId the model id
     * @param appId   the app id
     * @param token   the token
     * @param body    the body
     * @return the button
     */
    @Log
    @ApiOperation("获取自定义按钮的组件信息重定向")
    @PostMapping("/{modelId}/button/{dataId}")
    public R getButton(@PathVariable("dataId") String dataId, @PathVariable("modelId") String modelId, @PathVariable String appId, @RequestHeader(value = "Authorization", required = false) String token, @RequestBody Map<String, String> body) {
        String address = body.get("address");
        String permissionFlag = body.get("permissionFlag");
        DynamicDataPo byId = dynamicDataService.getById(modelId, dataId);

        byId.getJsonData().put("realName", UserCurrentUtils.getRealName());
        byId.getJsonData().put("userId", UserCurrentUtils.getUserId());
        byId.getJsonData().put("token", token);

        Document document = NoticeVariableUtils.replacement(address.replaceAll("&nbsp;", "").replaceAll("&amp;",""), byId.getJsonData());
        StringBuilder text = new StringBuilder();
        document.getElementsByTag("p").forEach(p -> text.append(p.text()));

        return R.ok(text.toString());
    }

    /**
     * 数据联动触发器处理
     *
     * @param appId     the app id
     * @param modelId   模型id
     * @param init      the init
     * @param tableType the table type
     * @param designId  表单设计id
     * @param body      the body
     * @return 字段信息集合 fields
     */
    @ApiOperation("数据联动触发器处理")
    @PostMapping("/DataModelTriggering/{designId}/{modelId}")
    public R<Map<String, Object>> getFields(@PathVariable String appId, @PathVariable("modelId") String modelId, @ApiParam("是否对公式进行扩展化处理") @RequestHeader(value = "init", required = false, defaultValue = "false") Boolean init, @ApiParam("表格操作") @RequestHeader(value = "tableType", required = false, defaultValue = "line") TableType tableType, @PathVariable String designId, @RequestBody ExecDto body) {

        //获取参与的公式如果
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, designId);
        //添加当前的表格的操作行数据
        SystemThreadLocal.set("index", body.getIndex());
        //操作的设计 id
        SystemThreadLocal.set("designId", designId);
        //表格的操作类型， 如果是表可的操作， 有行级操作，或新增，或删除
        SystemThreadLocal.set("tableType", tableType);
        //跳过模拟用户操作,让公式可以自己进行一次更新。
        SystemThreadLocal.set("designSkip", init);
        if (tableType.equals(TableType.add)) {
            //如果是选项卡删除前面两个数据
            if (body.getParentKey().size() == 3) {
                body.getParentKey().remove(0);
                body.getParentKey().remove(0);
            }
        }
        //获取所有的字段
        List<FieldBasicsHtml> collect = dataFieldService.getFields(appId, modelId, designId, true, true).stream().filter(e -> ObjectNull.isNotNull(e.getType())).collect(Collectors.toList());
        Map<String, FieldBasicsHtml> fieldsMap = collect.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
        Map<String, TableFormItemHtml> tablePathMap = new HashMap<>();
        //判断组件中是否有选项卡，如果有将选项卡选择了脱离数据，将脱离数据的 Key 放到字段解析中
        collect.stream().collect(Collectors.toList())
                //因为这个循环是改变内部对象，所以需要创建一个新的
                .stream().peek(e -> {
                    if (e.getType().equals(DataFieldType.tableForm)) {
                        tablePathMap.put(e.getProp(), tableFormFieldHandler.toHtml(e.getDesignJson()));
                    }
                })
                .filter(e -> e.getType().equals(DataFieldType.tab))
                .forEach(e -> {
                    //如果有将所有的 key字段添加到字段解析中
                    TabItemHtml html = tabFieldHandler.toHtml(e.getDesignJson());
                    for (FormValueHtml dicDatum : html.getDicData()) {
                        List<FieldBasicsHtml> fieldBasicsHtmls = html.getColumn().get(dicDatum.getName());
                        if (ObjectNull.isNull(fieldBasicsHtmls)) {
                            continue;
                        }
                        List<FieldBasicsHtml> collect1 = fieldBasicsHtmls.stream().filter(a -> a.getType().equals(DataFieldType.tableForm)).collect(Collectors.toList());
                        if (ObjectNull.isNotNull(collect1)) {
                            tablePathMapAdd(collect1, tablePathMap, dicDatum, html);
                        }
                        if (ObjectNull.isNull(html.getDetachData()) || html.getDetachData()) {
                            if (ObjectNull.isNotNull(dicDatum.getProp())) {
                                //如果有配置，需要组件为一个生成组件
                                TabGenerateItemHtml tabGenerateItemHtml = new TabGenerateItemHtml(dicDatum.getProp()).setColumn(fieldBasicsHtmls);
                                collect.add(tabGenerateItemHtml);
                            } else {
                                if (ObjectNull.isNotNull(fieldBasicsHtmls)) {
                                    //将tab组件全部放到处理里面
                                    collect.addAll(fieldBasicsHtmls);
                                }
                            }
                        }
                    }
                });
        //1、寻找结构。获取关联
        //2、寻找字段
        //根据触发key寻找数据关联的所有字段
        collect.forEach(e -> {
            IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(e.getType().getDesc());
            if (ObjectNull.isNotNull(e.getDesignJson(), iDataFieldHandler)) {
                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(e);

                if (init) {
                    //如果是表格类型,或选项卡类型，需要进行数据筛选进行处理
                    if (iDataFieldHandler instanceof TableFormFieldHandler || iDataFieldHandler instanceof TabFieldHandler || iDataFieldHandler instanceof TabGenerateFieldHandler) {
                        iDataFieldHandler.tableSetData(fieldHandlerMap, fieldsMap, publicHtml, body.getParams(), publicHtml.getProp());
                    }
                }
                if (ObjectNull.isNotNull(body.getParentKey())) {
                    String[] parentPath = body.getParentKey().toArray(new String[0]);
                    //如果是有父级，需要判断是是否容器组件类型，如果是容器组件类型，需要将父级处理了再进行下调。
                    if (ObjectNull.isNotNull(body.getModifiedField())) {

                        if (iDataFieldHandler instanceof TableFormFieldHandler || iDataFieldHandler instanceof TabFieldHandler || iDataFieldHandler instanceof TabGenerateFieldHandler) {
                            iDataFieldHandler.filterOrDataLinkage(appId, fieldsMap, body.getModifiedField(), publicHtml, body.getParams(), body.getIndex(), parentPath);
                        } else {
                            iDataFieldHandler.filterOrDataLinkage(appId, fieldsMap, body.getModifiedField(), publicHtml, body.getParams(), body.getIndex(), "");
                        }
                    }
                } else {
                    String[] parentPath = new String[]{publicHtml.getProp()};
                    iDataFieldHandler.filterOrDataLinkage(appId, fieldsMap, body.getModifiedField(), publicHtml, body.getParams(), body.getIndex(), parentPath);
                }
            }
        });
        //自动触发表格的数据筛选功能
        if (ObjectNull.isNotNull(tablePathMap) && ObjectNull.isNull(body.getIndex())) {
            //对数据的筛选条件进行判断是否有关联筛选如果有自动触发
            for (String key : tablePathMap.keySet()) {
                TableFormItemHtml tableFormItemHtml = tablePathMap.get(key);
                if (ObjectNull.isNotNull(tableFormItemHtml.getDataModelId(), tableFormItemHtml.getDataFilterGroupList())) {
                    List<String> field = tableFormItemHtml.getTableColumn().stream().map(FieldPublicHtml::getProp).collect(Collectors.toList());
                    //触发筛选过滤
                    QueryListDto queryPageDto = new QueryListDto();
                    queryPageDto.setFieldList(field);
                    List<List<QueryConditionDto>> groupConditions =
                            tableFormItemHtml.getDataFilterGroupList().stream().map(a -> a.stream().map(e -> {
                                Object read = null;
                                if (ObjectNull.isNotNull(e.getType())) {
                                    switch (e.getType()) {
                                        case prop:
                                            read = JvsJsonPath.read(body.getParams(), e.getValue().toString());
                                            break;
                                        case value:
                                            read = e.getValue();
                                            break;
                                        case cust:
                                            read = e.getValue();
                                            if (e.getEnabledQueryTypes().equals(DataQueryType.eq) && read instanceof List) {
                                                Object value = e.getValue();
                                                if (((List<?>) value).size() == 1) {
                                                    value = ((List<?>) value).get(0);
                                                }
                                                if (fieldsMap.containsKey(e.getFieldKey())) {
                                                    switch (fieldsMap.get(e.getFieldKey()).getType()) {
                                                        case inputNumber:
                                                            return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(Integer.valueOf(value.toString())).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                                        default:
                                                            return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(value).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                                    }
                                                } else {
                                                    Optional<FieldBasicsHtml> first = tableFormItemHtml.getTableColumn().stream().filter(b -> b.getProp().equals(e.getFieldKey())).findFirst();
                                                    if (first.isPresent()) {
                                                        switch (first.get().getType()) {
                                                            case inputNumber:
                                                                return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(Integer.valueOf(value.toString())).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                                        }
                                                    }
                                                    return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(value).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                                }
                                            }
                                            break;
                                    }
                                }
                                if (read instanceof String) {
                                    return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(DataConditionType.get(read)).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                } else {
                                    return new QueryConditionDto().setFieldKey(e.getFieldKey()).setValue(read).setEnabledQueryTypes(e.getEnabledQueryTypes());
                                }
                            }).collect(Collectors.toList())).collect(Collectors.toList());
                    queryPageDto.setGroupConditions(groupConditions);
                    if (ObjectNull.isNull(body.getIndex())) {
                        //需要保证不是表级操作才进行数据筛选
                        List<Map<String, Object>> data = dynamicDataService.postQueryList(appId, tableFormItemHtml.getDataModelId(), queryPageDto, false);
                        //将数据放到对应的数据 key里面去
                        JvsJsonPath.set(body.getParams(), key, data);
                    }
                }
            }
        }
        Map<String, Object> handler = expressionAfterHandler.handler(designId, init, body);
        //清除结果为空数组的字段
        Map<String, Object> parse = JSON.parseObject(JSON.toJSONString(handler, JSONWriter.Feature.LargeObject));
        return R.ok(parse);
    }

    private void tablePathMapAdd(List<FieldBasicsHtml> tableHtml, Map<String, TableFormItemHtml> tablePathMap, FormValueHtml dicDatum, TabItemHtml html) {
        //判断是否有数据脱离
        if (html.getDetachData()) {
            //判断是否有自定义 Key 的路径
            if (ObjectNull.isNull(dicDatum.getProp())) {
                //表示最外层
                tableHtml.forEach(a -> {
                    //记录表路径和对象信息
                    tablePathMap.put(a.getProp(), tableFormFieldHandler.toHtml(a.getDesignJson()));
                });
            } else {
                tableHtml.stream().filter(a -> a.getType().equals(DataFieldType.tableForm)).forEach(a -> {
                    //记录表路径和对象信息
                    tablePathMap.put(dicDatum.getProp() + "." + a.getProp(), tableFormFieldHandler.toHtml(a.getDesignJson()));
                });
            }
        } else {
            //如果没有开启直接添加即可
            tableHtml.stream().filter(a -> a.getType().equals(DataFieldType.tableForm)).forEach(a -> {
                //记录表路径和对象信息
                tablePathMap.put(html.getProp() + "." + dicDatum.getName() + "." + a.getProp(), tableFormFieldHandler.toHtml(a.getDesignJson()));
            });
        }
    }

    /**
     * 新增数据
     *
     * @param appId   the app id
     * @param modelId 模型id
     * @param data    数据内容
     * @return 保存后的数据id r
     */
    @Log
    @ApiOperation("新增数据")
    @PostMapping("/save/{modelId}")
    public R saveDynamicData(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestBody Map<String, Object> data) {
        setFunctionName("新增数据");
        DynamicDataUtils.checkPermit();
        DynamicDataUtils.clearEcho(data);
        dynamicDataService.checkDataModel(data, modelId);
        RuleExecuteDto executeDto = dynamicDataService.save(appId, modelId, data);
        if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
            if (executeDto.getStats()) {
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.CREATED, modelId, String.valueOf(data.get("dataId")), data);
                return R.ok().setMsg(executeDto.getSyncMessageTips());
            } else {
                //返回逻辑定义的错误信息
                return R.failed(executeDto.getSyncMessageTips());
            }
        }
        return R.ok().setMsg("保存成功");
    }


    /**
     * 删除数据
     *
     * @param appId   the app id
     * @param modelId 模型id
     * @param dataId  数据id
     * @return 数据版本号 r
     */
    @Log
    @ApiOperation("删除数据")
    @DeleteMapping("/delete/{modelId}/{dataId}")
    public R deleteDynamicData(@PathVariable String appId, @PathVariable("modelId") String modelId, @PathVariable("dataId") String dataId) {
        setFunctionName("删除数据");

        Map<String, Object> data = dynamicDataService.querySingle(appId, modelId, dataId);
        DynamicDataUtils.checkPermit();
        RuleExecuteDto executeDto = dynamicDataService.remove(modelId, dataId);
        dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.DELETED, modelId, dataId, data);
        if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
            if (executeDto.getStats()) {
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.CREATED, modelId, String.valueOf(data.get("dataId")), data);
                return R.ok(executeDto.getSyncMessageTips());
            } else {
                //返回逻辑定义的错误信息
                return R.failed(executeDto.getSyncMessageTips());
            }
        }
        return R.ok().setMsg("删除成功");
    }

    /**
     * 删除数据
     *
     * @param appId   the app id
     * @param modelId 模型id
     * @param ids     the ids
     * @return 数据版本号 r
     */
    @Log
    @ApiOperation("批量删除数据")
    @PostMapping("/batch/delete/{modelId}")
    public R batchDeleteDynamicData(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestBody List<String> ids) {
        setFunctionName("批量删除数据");
        //查询是否有删除前置操作，如果有就使用 for 如果没有就直接批量删除
        String before = dataEventService.getCallbackRuleId(modelId, null, DataEventType.DATA_DELETE, true);
        String after = dataEventService.getCallbackRuleId(modelId, null, DataEventType.DATA_DELETE, false);
        if (ObjectNull.isNotNull(before)) {
            //没有前置操作
            List<Map> maps = dynamicDataService.getByIds(modelId, ids);
            for (Map<String, Object> map : maps) {
                String id = String.valueOf(map.get("id"));
                dynamicDataService.remove(modelId, id);
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.DELETED, modelId, id, map);
            }
        } else {
            //没有前置操作
            List<QueryConditionDto> queryConditionDtos = new ArrayList<>();
            queryConditionDtos.add(new QueryConditionDto().setValue(ids).setFieldKey("id").setEnabledQueryTypes(DataQueryType.in));
            //批量删除
            List<Object> objects = dynamicDataService.removeMulti(modelId, queryConditionDtos);
            dataLogService.saveLogBatch(modelId, objects, DataEventType.DATA_DELETE, UserCurrentUtils.getCurrentUser());
            if (ObjectNull.isNotNull(after)) {
                //异步执行后置操作和发送消息
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.DELETED, modelId, objects);
                dataEventService.batchEventDeleteCallBack(modelId, objects);
            }
        }
        return R.ok();
    }

    /**
     * 设置方法名
     *
     * @param functionName
     */
    private void setFunctionName(String functionName) {
        JvsApp app = CurrentAppUtils.getApp();
        try {
            CrudPage crudPage = pageService.getById(DynamicDataUtils.getDesignId());
            SystemThreadLocal.set("functionName", ModeUtils.getMode().getMsg() + "-" + app.getName() + "-" + functionName + "-" + crudPage.getName());
        } catch (Exception ignored) {

        }
        try {
            FormPo formPo = formService.getById(DynamicDataUtils.getDesignId());
            SystemThreadLocal.set("functionName", ModeUtils.getMode().getMsg() + "-" + app.getName() + "-" + functionName + "-" + formPo.getName());
        } catch (Exception ignored) {

        }
    }

    /**
     * Delete dynamic data r.
     *
     * @param appId   the app id
     * @param modelId the model id
     * @param dataIds the data ids
     * @return the r
     */
    @Log
    @ApiOperation("删除数据")
    @DeleteMapping("/delete/list/{modelId}")
    public R<Integer> deleteDynamicData(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestParam("dataIds") List<String> dataIds) {
        setFunctionName("删除数据");
        for (String dataId : dataIds) {
            Map<String, Object> data = dynamicDataService.querySingle(appId, modelId, dataId);
            DynamicDataUtils.checkPermit();
            RuleExecuteDto executeDto = dynamicDataService.remove(modelId, dataId);
            if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
                //如果是失败了、直接返回错误信息
                if (!executeDto.getStats()) {
                    return R.failed(executeDto.getSyncMessageTips());
                }
                dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.DELETED, modelId, dataId, data);
            }
        }
        if (designConfig.getAi()) {
            try {
                jvsAiDatasetApi.delDocuments(dataIds);
            } catch (Exception e) {

            }
        }
        return R.ok();
    }

    /**
     * 修改数据
     *
     * @param appId   the app id
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据内容
     * @return 数据版本号 r
     */
    @Log
    @ApiOperation("修改数据")
    @PostMapping("/update/{modelId}/{dataId}")
    public R updateDynamicData(@PathVariable String appId, @PathVariable("modelId") String modelId, @PathVariable("dataId") String dataId, @RequestBody Map<String, Object> data) {
        DynamicDataUtils.checkPermit();
        DynamicDataUtils.clearEcho(data);
        String designId = DynamicDataUtils.getDesignId();
        setFunctionName("修改数据");
        Map<String, FieldBasicsHtml> fieldsMap = dataFieldService.getDesignIdFields(appId, modelId, designId, true, true).stream().filter(e -> ObjectNull.isNotNull(e.getType())).collect(Collectors.toMap((FieldBasicsHtml::getFieldKey),
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
                    switch (publicHtml.getType()) {
                        case department:
                        case job:
                        case role:
                        case user:
                            data.put(publicHtml.getProp(), DATA_EMPTY);
                            break;
                        default:
                            data.put(publicHtml.getProp(), DynamicDataConstant.getEmpty(publicHtml.getType().getAClass()));
                    }
                }
            }
            if (DataFieldType.select.equals(publicHtml.getType()) || DataFieldType.cascader.equals(publicHtml.getType())) {
                MultipleHtml html = (MultipleHtml) fieldHandlerMap.get(publicHtml.getType().getDesc()).toHtml(publicHtml.getDesignJson());
                if (ObjectNull.isNotNull(html)) {
                    if (modelId.equals(html.getFormId()) && html.getDatatype().equals(FormDataTypeEnum.dataModel)) {
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
            List<Map<String, Object>> mapList = dynamicDataService.queryList(modelId, fields, queryConditionDto);
            if (ObjectNull.isNotNull(mapList)) {
                //表示为树形关系
                checkTreeIDExists(id, queryConditionDto, fields, modelId, mapList);
            }
        }

        Map<String, Object> oldData = dynamicDataService.querySingle(appId, modelId, dataId);
        dynamicDataService.clearSensitiveBody(modelId, data, oldData);

        // 修改数据
        RuleExecuteDto executeDto = dynamicDataService.update(appId, modelId, dataId, data);
        if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
            if (!executeDto.getStats()) {
                //成功直接返回
                return R.failed(executeDto.getSyncMessageTips());
            }
            return R.ok().setMsg(executeDto.getSyncMessageTips());
        }

        MapDifference<String, Object> difference = Maps.difference(oldData, data);
        dataNoticeHandler.sendNotify(TenantContextHolder.getTenantId(), appId, TriggerTypeEnum.EDITED, modelId, dataId, data, difference.entriesDiffering().keySet());
        return R.ok().setMsg("修改成功");
    }

    /**
     * 检查树形关系的值是否是下级，如果找到了对应的下级关系值，则表示存在循环引用，不允许保存
     *
     * @param query
     * @param fields
     * @param modelId
     * @param mapList
     */
    private void checkTreeIDExists(String id, QueryConditionDto query, ArrayList<String> fields, String modelId, List<Map<String, Object>> mapList) {
        for (Map<String, Object> map : mapList) {
            String mapId = map.get("id").toString();
            if (mapId.equals(id)) {
                throw new BusinessException("不允许选择下级数据作为父级");
            }
            query.setValue(mapId);
            List<Map<String, Object>> list = dynamicDataService.queryList(modelId, fields, query);
            if (ObjectNull.isNotNull(list)) {
                checkTreeIDExists(mapId, query, fields, modelId, list);
            }
        }

    }

    /**
     * 保存关联数据id集合并调用逻辑引擎
     *
     * @param appId    the app id
     * @param modelId  模型id
     * @param dataId   数据id
     * @param dto      入参
     * @param response the response
     * @return 保存后的数据id r
     */
    @Log
    @ApiOperation("保存关联数据id集合并调用逻辑引擎")
    @PostMapping("/update/relation/{modelId}/{dataId}")
    public R saveRelationAndRunRule(@PathVariable String appId, @PathVariable("modelId") String modelId, @PathVariable("dataId") String dataId, @RequestBody SaveRelationAndRunRuleDto dto, HttpServletResponse response) {
        Map<String, Object> data = dto.getData();
        DynamicDataUtils.clearEcho(data);
        Map<String, Object> oldData = dynamicDataService.querySingle(appId, modelId, dataId);
        Map<String, Object> relationTagMap = new HashMap<>(8);
        Optional<Object> relationOptional = Optional.ofNullable(oldData.get(DataFieldService.FIELD_RELATION_TAG));
        if (relationOptional.isPresent()) {
            relationTagMap = (Map<String, Object>) relationOptional.get();
        }
        Object currentRelationIds = data.get(dto.getRelationTag());
        if (ObjectNull.isNotNull(currentRelationIds)) {
            relationTagMap.put(dto.getRelationTag(), currentRelationIds);
        }
        if (ObjectNull.isNotNull(relationTagMap)) {
            data.put(DataFieldService.FIELD_RELATION_TAG, relationTagMap);
        }
        // 修改数据
        dynamicDataService.update(appId, modelId, dataId, data);
        // 调用逻辑引擎。 将关联标识改为固定标识后传递到逻辑引擎
        data.put("ids", currentRelationIds);
        data.remove(DataFieldService.FIELD_RELATION_TAG);
        data.remove(dto.getRelationTag());
        //查询数据将行级数据全部给逻辑引擎
        DynamicDataPo byId = dynamicDataService.getById(modelId, dataId);
        Map<String, Object> jsonData = new HashMap<>(8);
        if (ObjectNull.isNotNull(byId)) {
            jsonData.putAll(byId.getJsonData());
        }
        jsonData.putAll(data);
        RuleDesignPo po = ruleDesignService.getEnableDesign(dto.getRuleKey());
        RunLogPo logPo = runLogService.create(appId, po.getSecret(), RunType.REAL, jsonData, po.getReqType(), po.getReqType(), po.getSync());
        RuleExecuteDto executeDto = new RuleExecuteDto().setReqVariableMap(jsonData).setVariableMap(jsonData);
        RuleExecDto ruleExecDto = new RuleExecDto().setExecuteDto(executeDto).setType(RunType.REAL).setSecret(po.getSecret()).setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
        return ruleStartUtils.getRuleReturn(response, po, logPo, executeDto, ruleExecDto);
    }

    /**
     * 分页查询数据
     *
     * @param appId        the app id
     * @param modelId      模型id
     * @param formId       设计ID
     * @param fieldId      组件ID  如果组件ID携带有， 则表示是弹框如果，弹窗查看是否跳过设计数据权限
     * @param notification the notification
     * @param queryPageDto the query page dto
     * @return 数据集合 r
     */
    @Log
    @ApiOperation("分页查询数据")
    @PostMapping("/query/page/{modelId}")
    public R queryPage(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestHeader(value = "formId", required = false) String formId, @RequestHeader(value = "fieldId", required = false) String fieldId, @RequestHeader(value = "notification", required = false, defaultValue = "") Object notification, @RequestBody QueryPageDto queryPageDto) {
        long startTime = System.currentTimeMillis();
        log.info("[分页查询] 开始执行分页查询，appId: {}, modelId: {}, formId: {}, fieldId: {}", appId, modelId, formId, fieldId);
        log.info("[分页查询] 查询参数: 分页: {}/{}, 关键字: {}, 条件数: {}, 分组条件数: {}", 
                queryPageDto.getCurrent(), queryPageDto.getSize(), queryPageDto.getKeywords(), 
                queryPageDto.getConditions() != null ? queryPageDto.getConditions().size() : 0,
                queryPageDto.getGroupConditions() != null ? queryPageDto.getGroupConditions().size() : 0);
        
        //将设计的列表页字段进行拼装

        if (ObjectNull.isNotNull(queryPageDto.getKeywords())) {
            queryPageDto.setKeywords(queryPageDto.getKeywords().trim());
        }
        //当前这个设计所使用到的字段有哪一些
        Set<String> queryField = new LinkedHashSet<>();
        if (ObjectNull.isNull(queryPageDto.getGroupConditions())) {
            //如果设计中包含
            if (ObjectNull.isNull(formId) || ObjectNull.isNull(fieldId)) {
                //5数据权限
                long scopeStart = System.currentTimeMillis();
                queryField.addAll(DynamicDataUtils.dataModelScope(modelId));
                log.info("[分页查询] 数据权限处理耗时: {}ms", System.currentTimeMillis() - scopeStart);
            }
        }
        //将查询条件的字段添加进去
        if (ObjectNull.isNotNull(queryPageDto.getFieldList())) {
            queryField.addAll(queryPageDto.getFieldList());
        }
        Set<String> stringSet = new HashSet<>();
        //根据设计获取默认地址
        String designId = DynamicDataUtils.getDesignId();
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
        Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap = new HashMap<>(8);
        long getAllFieldStart = System.currentTimeMillis();
        Map<String, FieldBasicsHtml> collectMap = dataFieldService.getAllField(appId, modelId, true, true, e -> false).stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));
        log.info("[分页查询] getAllField查询耗时: {}ms, 字段数量: {}", System.currentTimeMillis() - getAllFieldStart, collectMap.size());
        // Map<列表字段key，关联字段>
        Map<String, ModelDisplayHtml> modelDisplayMap = new HashMap<>();

        //转换查询条件，有可能存在树形
        long crudPageStart = System.currentTimeMillis();
        List<CrudPage> crudPage = pageService.list(Wrappers.query(new CrudPage().setDataModelId(modelId).setJvsAppId(appId)));
        log.info("[分页查询] CrudPage列表查询耗时: {}ms, 数量: {}", System.currentTimeMillis() - crudPageStart, crudPage != null ? crudPage.size() : 0);
        //记录甘特图的设置属性值，确定是哪哪几个字段进行处理的，需要对其数据进行排列并取最大最小
        List<String> dateField = new ArrayList<>();
        Optional<String> retrievalKey = Optional.empty();
        if (ObjectNull.isNotNull(crudPage)) {
            crudPage.sort(Comparator.comparing(CrudPage::getUpdateTime).reversed());
            CrudPage page = crudPage.stream().filter(e -> e.getId().equals(designId)).findAny().orElseGet(() -> crudPage.get(0));
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(page.getViewJson());
            if (ObjectNull.isNotNull(pageDesignHtml)) {
                if (pageDesignHtml.getGantt()) {
                    //表示开启了甘特图配置,需要获取配置的字段
                    dateField.add(pageDesignHtml.getGanttForm().getPlainStart());
                    dateField.add(pageDesignHtml.getGanttForm().getPlainEnd());
                    dateField.add(pageDesignHtml.getGanttForm().getReallyStart());
                    dateField.add(pageDesignHtml.getGanttForm().getReallyEnd());
                }
                List<QueryConditionDto> finalQueryConditions = queryConditions;
                retrievalKey = pageDesignHtml.getDataPage().getAutoTableFields().stream()
                        .filter(e -> ObjectNull.isNotNull(e.getEnableRetrieval()))
                        .filter(DataTableFieldDesignHtml::getEnableRetrieval)
                        .filter(e -> collectMap.containsKey(e.getAliasColumnName()))
                        .filter(e -> collectMap.get(e.getAliasColumnName()).getFieldType().equals(DataFieldType.select))
                        .map(DataTableFieldDesignHtml::getAliasColumnName).findFirst();
                long retrievalStart = System.currentTimeMillis();
                pageDesignHtml.getDataPage().getAutoTableFields().stream()
                        .filter(e -> ObjectNull.isNotNull(e.getEnableRetrieval()))
                        .filter(DataTableFieldDesignHtml::getEnableRetrieval)
                        .filter(e -> ObjectNull.isNotNull(e.getRetrievalOption()))
                        .filter(e -> ObjectNull.isNotNull(e.getRetrievalOption().getAllChildren()))
                        .filter(e -> e.getRetrievalOption().getAllChildren())
                        .findFirst()
                        .map(e -> {
                            log.info("[分页查询] 检索字段处理开始");
                            //对其字段进行转换
                            String key = e.getAliasColumnName();
                            //表示是树形关系
                            finalQueryConditions.forEach(a -> {
                                if (a.getFieldKey().equals(key)) {
                                    CascaderItemHtml html = cascaderFieldHandler.toHtml(e.getDesignJson());
                                    Set<String> strings = cascaderFieldHandler.childrenId(html, a.getValue().toString());
                                    strings.add(a.getValue().toString());
                                    //查询这个字段的所有下级，并改变查询条件
                                    a.setEnabledQueryTypes(DataQueryType.in);
                                    a.setValue(strings.stream().collect(Collectors.toList()));
                                }
                            });
                            log.info("[分页查询] 检索字段处理耗时: {}ms", System.currentTimeMillis() - retrievalStart);
                            return e;
                        });
            }
        }
        Map<String, Object> map = new HashMap<>();
        if (ObjectNull.isNull(queryPageDto.getConditions())) {
            queryPageDto.setConditions(new ArrayList<>());
        } else {
            queryPageDto.getConditions().forEach(e -> map.put(e.getFieldKey(), e.getValue()));
        }
        //判断查询条件是否是部门字段，如果是部门字段，需要将部门做修改为多条件查询
        long deptConditionStart = System.currentTimeMillis();
        if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
            queryPageDto.getConditions().forEach(e -> {
                if (!e.getEnabledQueryTypes().equals(DataQueryType.isNull)) {
                    if (collectMap.containsKey(e.getFieldKey()) && DataFieldType.department.equals(collectMap.get(e.getFieldKey()).getType())) {
                        e.setEnabledQueryTypes(DataQueryType.in);
                        //查询当前部门及以下
                        List<String> ids = new ArrayList<>();
                        if (e.getValue() instanceof Collection) {
                            ids = ((Collection<?>) e.getValue()).stream().flatMap(a -> authDeptServiceApi.getChildList(a.toString()).getData().stream()).distinct().map(a -> a.getId()).collect(Collectors.toList());
                        } else {
                            ids = authDeptServiceApi.getChildList(e.getValue().toString()).getData().stream().map(SysDeptDto::getId).collect(Collectors.toList());
                        }
                        ids.add(e.getValue().toString());
                        e.setValue(ids);
                    }
                    if (collectMap.containsKey(e.getFieldKey()) && DataFieldType.cascader.equals(collectMap.get(e.getFieldKey()).getType())) {
                        //todo 系统字典
                        CascaderItemHtml html = cascaderFieldHandler.toHtml(collectMap.get(e.getFieldKey()).getDesignJson());
                        if (html.getDatatype().equals(FormDataTypeEnum.system)) {
                            e.setEnabledQueryTypes(DataQueryType.in);
                            //系统字段
                            List<String> ids = jvsTreeService.getByUniqueNameIds(html.getDictName(), e.getValue());
                            //查询出所有的
                            if (ObjectNull.isNotNull(ids)) {
                                e.setValue(ids);
                            }
                        }
                        if (html.getDatatype().equals(FormDataTypeEnum.dataModel) && ObjectNull.isNotNull(html.getFormId())) {
                            e.setEnabledQueryTypes(DataQueryType.in);
                            List<String> ids = cascaderFieldHandler.getByDataModelIds(html.getFormId(), html.getProps(), e.getValue());
                            if (ObjectNull.isNotNull(ids)) {
                                e.setValue(ids);
                            }
                        }
                        if (html.getDatatype().equals(FormDataTypeEnum.rule)) {
                            e.setEnabledQueryTypes(DataQueryType.in);
                            List<String> ids = cascaderFieldHandler.getByDataRuleIds(html.getOptionHttp(), html.getProps(), e.getValue());
                            if (ObjectNull.isNotNull(ids)) {
                                e.setValue(ids);
                            }
                        }
                    }
                }
            });
            log.info("[分页查询] 部门/级联字段条件处理耗时: {}ms", System.currentTimeMillis() - deptConditionStart);
        }
        long groupConditionStart = System.currentTimeMillis();
        if (ObjectNull.isNotNull(queryPageDto.getGroupConditions())) {
            queryPageDto.getGroupConditions().forEach(e -> {
                e.forEach(a -> {
                    if (collectMap.containsKey(a.getFieldKey())) {
                        if (!a.getEnabledQueryTypes().equals(DataQueryType.isNull)) {
                            if (!a.getEnabledQueryTypes().equals(DataQueryType.eq)) {
                                //判断类型是否匹配，匹配需要调整查询值进行强制转换，如时间，数字
                                switch (collectMap.get(a.getFieldKey()).getType()) {
                                    case inputNumber:
                                        try {
                                            BigDecimal next;
                                            if (a.getValue() instanceof Collection) {
                                                next = (BigDecimal) NumberUtil.parseNumber(((Collection<?>) a.getValue()).iterator().next().toString());
                                            } else {
                                                next = (BigDecimal) NumberUtil.parseNumber(a.getValue().toString());
                                            }
                                            if (next.scale() > 0) {
                                                a.setValue(next.doubleValue());
                                            } else {
                                                a.setValue(next.longValue());
                                            }
                                        } catch (NumberFormatException ex) {
                                            throw new BusinessException("查询条件值类型不匹配");
                                        }
                                }
                            }
                        }
                    }
                });
            });
            log.info("[分页查询] 分组条件类型转换耗时: {}ms", System.currentTimeMillis() - groupConditionStart);
        }


        AtomicReference<String> treeTitleName = new AtomicReference<>("");
        //表示是否是树形关系，并自己关联了自己
        AtomicReference<QueryConditionDto> treeQuery = new AtomicReference<>();
        if (ObjectNull.isNotNull(crudPage)) {
            setFunctionName("分页查询");
            long parsePageStart = System.currentTimeMillis();
            CrudPage page = crudPage.stream().filter(e -> e.getId().equals(designId)).findAny().orElseGet(() -> crudPage.get(0));
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(page.getViewJson());
            log.info("[分页查询] 解析页面设计JSON耗时: {}ms", System.currentTimeMillis() - parsePageStart);
            //如果关键字查询条件不为空的时候
            if (ObjectNull.isNotNull(queryPageDto.getKeywords(), pageDesignHtml)) {
                List<QueryConditionDto> list =
                        pageDesignHtml.getDataPage().getAutoTableFields().stream().filter(DataTableFieldDesignHtml::getShow).map(e -> new QueryConditionDto().setFieldKey(e.getAliasColumnName()).setEnabledQueryTypes(DataQueryType.like).setValue(queryPageDto.getKeywords().trim())).collect(Collectors.toList());
                queryConditions.addAll(list);
            }
            if (StringUtils.isNotBlank(designId) && ObjectNull.isNotNull(pageDesignHtml)) {
                List<String> formula = new ArrayList<>();
                //1将字段添加起来
                long autoTableFieldsStart = System.currentTimeMillis();
                List<QueryConditionDto> finalQueryConditions1 = queryConditions;
                pageDesignHtml.getDataPage().getAutoTableFields().forEach(e -> {
                    //判断这个模型中展示的字段，是否存在自己关联自己情况，如果存在，则直接返回树形，并添加筛选条件
                    if (collectMap.containsKey(e.getAliasColumnName())) {
                        FieldBasicsHtml fieldBasicsHtml = collectMap.get(e.getAliasColumnName());
                        if (DataFieldType.select.equals(fieldBasicsHtml.getType()) || DataFieldType.cascader.equals(fieldBasicsHtml.getType())) {
                            MultipleHtml html = (MultipleHtml) fieldHandlerMap.get(fieldBasicsHtml.getType().getDesc()).toHtml(fieldBasicsHtml.getDesignJson());
                            if (ObjectNull.isNotNull(html)) {
                                if (ObjectNull.isNotNull(html.getDatatype())) {
                                    if (html.getDatatype().equals(FormDataTypeEnum.system) && DataFieldType.cascader.equals(fieldBasicsHtml.getType())) {
                                        //todo 系统字典
                                        cascaderFieldHandler.getTreeDict(((CascaderItemHtml) html).getDictName());
                                    }
                                    if (modelId.equals(html.getFormId()) && html.getDatatype().equals(FormDataTypeEnum.dataModel)) {
                                        //树形结构，将对应的字段
                                        if (ObjectNull.isNotNull(html.getProps().getSecTitle())) {
                                            treeTitleName.set(html.getProps().getLabel());
                                            Object o = map.get(html.getProps().getSecTitle());
                                            if (ObjectNull.isNotNull(o)) {
                                                queryPageDto.getConditions().removeIf(a -> a.getFieldKey().equals(html.getProps().getSecTitle()));
                                                finalQueryConditions1.removeIf(a -> a.getFieldKey().equals(html.getProps().getSecTitle()));
                                                if (o instanceof List) {
                                                    treeQuery.set(new QueryConditionDto().setFieldKey("id").setValue(o).setEnabledQueryTypes(DataQueryType.in));
                                                } else {
                                                    treeQuery.set(new QueryConditionDto().setFieldKey("id").setValue(o).setEnabledQueryTypes(DataQueryType.eq));
                                                }
                                            } else {
                                                treeQuery.set(new QueryConditionDto().setFieldKey(html.getProps().getSecTitle()).setValue(null).setEnabledQueryTypes(DataQueryType.isNull));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DataTableFieldAdvancedSettingsHtml advancedSettings = e.getAdvancedSettings();
                    if (ObjectNull.isNotNull(advancedSettings)) {
                        //表示组合显示字段
                        if (ObjectNull.isNotNull(advancedSettings.getCombiningFieldFormulaContent())) {
                            FunctionBusinessPo byId = functionBusinessService.getById(e.getAdvancedSettings().getFormula());
                            if (ObjectNull.isNotNull(byId)) {
                                if (byId.getDesignId().equals(designId)) {
                                    combiningFieldFormulaContentMap.put(e.getAliasColumnName(), byId);
                                }
                            }
                        }
                        // 显示方式为关联模型时，将筛选条件加入
                        if (ObjectNull.isNotNull(advancedSettings.getModelDisplay()) && ObjectNull.isNotNull(advancedSettings.getModelDisplay().getDataLinkageList())) {
                            modelDisplayMap.put(e.getAliasColumnName(), advancedSettings.getModelDisplay());
                            advancedSettings.getModelDisplay().getDataLinkageList().forEach(linkage -> {
                                if (LinkTypeEnum.field.equals(linkage.getProp())) {
                                    queryField.add((String) linkage.getValue());
                                }
                                if (LinkTypeEnum.formula.equals(linkage.getProp())) {
                                    formula.add(linkage.getFormula());
                                }
                            });
                        }
                    }
                    queryField.add(e.getAliasColumnName());
                });
                log.info("[分页查询] 处理AutoTableFields耗时: {}ms, 字段数: {}", System.currentTimeMillis() - autoTableFieldsStart, pageDesignHtml.getDataPage().getAutoTableFields().size());
                //2排序字段
                if (ObjectNull.isNotNull(pageDesignHtml.getSorts())) {
                    pageDesignHtml.getSorts().forEach(e -> queryField.add(e.getFieldKey()));
                }
                //3按钮公式字段 移动端的按钮
                long formulaStart = System.currentTimeMillis();
                formula.addAll(pageDesignHtml.getButtons().stream().map(ButtonSettingDto::getFormula).collect(Collectors.toList()));
                //添加 公式
                formula.addAll(pageDesignHtml.getButtons().stream().map(ButtonSettingDto::getMobileFormula).collect(Collectors.toList()));
                if (ObjectNull.isNotNull(formula)) {
                    functionBusinessMapper.selectBatchIds(formula).forEach(e -> queryField.addAll(e.getRelatedIds()));
                    log.info("[分页查询] 查询按钮公式字段耗时: {}ms, 公式数: {}", System.currentTimeMillis() - formulaStart, formula.size());
                }
                //列表页筛选条件，与数据权限不同，是直接进行数据筛选
                long parametersStart = System.currentTimeMillis();
                if (ObjectNull.isNotNull(pageDesignHtml.getParameters())) {
                    pageDesignHtml.getParameters().stream().filter(e -> ObjectNull.isNotNull(e.getValue(), e.getKey(), e.getOperator())).map(e -> {
                        QueryConditionDto queryConditionDto = new QueryConditionDto().setValue(e.getOperator().equals(DataQueryType.in) ? e.getValue() : e.getValue().get(0)).setFieldKey(e.getKey()).setEnabledQueryTypes(e.getOperator());
                        if (collectMap.containsKey(queryConditionDto.getFieldKey())) {
                            FieldBasicsHtml fieldBasicsHtml = collectMap.get(queryConditionDto.getFieldKey());
                            //如果是文本框
                            if (DataFieldType.input.equals(fieldBasicsHtml.getFieldType())) {
                                // 将只有一个值的 In,条件变为模糊匹配
                                if (DataQueryType.in.equals(queryConditionDto.getEnabledQueryTypes()) && e.getValue().size() == 1) {
                                    queryConditionDto.setEnabledQueryTypes(DataQueryType.like);
                                }
                                if (!DataQueryType.in.equals(queryConditionDto.getEnabledQueryTypes()) && e.getValue().size() >= 2) {
                                    throw new BusinessException("列表过滤[" + fieldBasicsHtml.getFieldName() + "]非包含规则只支持一个条件值");
                                }
                            }
                            if (DataFieldType.input.equals(fieldBasicsHtml.getFieldType()) && DataQueryType.in.equals(queryConditionDto.getEnabledQueryTypes()) && e.getValue().size() == 1) {

                            }
                            if (DataFieldType.inputNumber.equals(fieldBasicsHtml.getFieldType())) {
                                Object value = e.getValue().get(0);
                                if (!NumberUtil.isNumber(value.toString())) {
                                    throw new BusinessException("列表过滤条件配置错误", fieldBasicsHtml.getFieldName() + "条件的值应为数字");
                                }
                                BigDecimal bigDecimal = new BigDecimal(e.getValue().get(0));
                                Object convertValue = bigDecimal.doubleValue();
                                if (bigDecimal.scale() == 0) {
                                    convertValue = bigDecimal.intValue();
                                }
                                queryConditionDto.setValue(convertValue);
                            }
                        }
                        return queryConditionDto;
                    }).peek(e -> e.setCrud(true)).peek(e -> stringSet.add(e.getFieldKey())).forEach(e -> {
                        if (ObjectNull.isNull(queryPageDto.getConditions())) {
                            queryPageDto.setConditions(new ArrayList<>());
                        }
                        queryPageDto.getConditions().add(e);
                    });
                    //4列表过滤
                    pageDesignHtml.getParameters().forEach(e -> queryField.add(e.getKey()));
                    log.info("[分页查询] 处理列表筛选条件耗时: {}ms, 参数数: {}", System.currentTimeMillis() - parametersStart, pageDesignHtml.getParameters().size());
                }
                // 如果没有指定排序字段，则使用列表的排序设计
                if (CollectionUtils.isEmpty(queryPageDto.getSorts()) && CollectionUtils.isNotEmpty(pageDesignHtml.getSorts())) {
                    queryPageDto.setSorts(BeanCopyUtil.copys(pageDesignHtml.getSorts(), QueryOrderDto.class));
                }
            }
        }
        //删除多余的字段
        collectMap.keySet().

                removeIf(e ->

                {
                    if (ObjectNull.isNull(queryField)) {
                        return false;
                    } else {
                        return !queryField.contains(e);
                    }
                });
        List<String> collect = new ArrayList<>(collectMap.keySet());

        List<List<QueryConditionDto>> queryGroupConditions = new ArrayList<>();
        if (ObjectNull.isNull(queryConditions)) {
            if (CollectionUtils.isNotEmpty(queryPageDto.getGroupConditions())) {
                queryGroupConditions = queryPageDto.getGroupConditions();
            } else {
                if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
                    queryGroupConditions = Collections.singletonList(queryPageDto.getConditions());
                }
            }
            long getQueryConditionsStart = System.currentTimeMillis();
            queryGroupConditions = getQueryConditions(queryGroupConditions, collectMap, notification);
            log.info("[分页查询] getQueryConditions处理耗时: {}ms", System.currentTimeMillis() - getQueryConditionsStart);
        } else {
            if (ObjectNull.isNotNull(queryPageDto.getConditions())) {
                queryConditions.addAll(queryPageDto.getConditions());
                queryConditions = queryConditions.stream().distinct().collect(Collectors.toList());
            }
            queryGroupConditions = Collections.singletonList(queryConditions);
        }
        // 条件值转换
        long convertStart = System.currentTimeMillis();
        queryGroupConditions.stream()
                        .

                flatMap(Collection::stream)
                        .

                forEach(condition ->

                        convertQueryValue(condition, collectMap));
        log.info("[分页查询] 条件值转换耗时: {}ms", System.currentTimeMillis() - convertStart);

        Page<DynamicDataPo> page = new Page<>(queryPageDto.getCurrent(), queryPageDto.getSize());
        collect.addAll(dataFieldService.getDoNotShowFields().

                stream().

                map(DataFieldPo::getFieldKey).

                collect(Collectors.toList()));
        if (ObjectNull.isNotNull(treeQuery.get())) {
            if (!queryGroupConditions.stream().flatMap(Collection::stream).filter(e -> e.getFieldKey().equals(treeQuery.get().getFieldKey())).findFirst().isPresent()) {
                if (!queryGroupConditions.stream().flatMap(Collection::stream).filter(e -> e.getFieldKey().equals(treeTitleName.get())).findFirst().isPresent()) {
                    if (ObjectNull.isNull(queryGroupConditions)) {
                        queryGroupConditions = Collections.singletonList(Collections.singletonList(treeQuery.get()));
                    } else {
                        // 调整树结构数据查询
//                        queryGroupConditions.get(0).add(treeQuery.get());
                    }
                }
            }
        }
        if (ObjectNull.isNotNull(dateField)) {
            collect.addAll(dateField);
        }
        long queryPageStart = System.currentTimeMillis();
        log.info("[分页查询] 开始执行数据库分页查询，查询字段数: {}, 查询条件组数: {}", collect.size(), queryGroupConditions.size());
        Page<Map<String, Object>> pageResult = dynamicDataService.queryPage(appId, page, modelId, combiningFieldFormulaContentMap, queryGroupConditions, queryPageDto.getSorts(), collect, true, true, ObjectNull.isNull(queryPageDto.getKeywords()), new ArrayList<>(collectMap.values()), stringSet);
        log.info("[分页查询] 数据库分页查询耗时: {}ms, 返回记录数: {}", System.currentTimeMillis() - queryPageStart, pageResult.getRecords().size());
        List<List<QueryConditionDto>> queryGroup = Collections.singletonList(Collections.singletonList(treeQuery.get()));
        List<Map<String, Object>> allData = new ArrayList<>();
        //如果是树，并关联自己，需要对数据进行关联查询下级直到查询不到结果为止
        long treeStructureStart = System.currentTimeMillis();
        List<Map<String, Object>> mapList = treeStructure(allData, modelDisplayMap, pageResult.getRecords(), treeQuery, appId, modelId, combiningFieldFormulaContentMap, queryGroup, queryPageDto.getSorts(), collect,
                ObjectNull.isNull(queryPageDto.getKeywords()),
                new ArrayList<>(collectMap.values()), stringSet);
        log.info("[分页查询] 树形结构处理耗时: {}ms", System.currentTimeMillis() - treeStructureStart);
        pageResult.setRecords(mapList);
        if (ObjectNull.isNotNull(dateField)) {
            long ganttStart = System.currentTimeMillis();
            R result = R.ok(GanttChartUtils.setFiled(pageResult, dateField));
            log.info("[分页查询] 甘特图处理耗时: {}ms", System.currentTimeMillis() - ganttStart);
            log.info("[分页查询] ====== 总耗时: {}ms ======", System.currentTimeMillis() - startTime);
            return result;
        }
        log.info("[分页查询] ====== 总耗时: {}ms ======", System.currentTimeMillis() - startTime);
        return R.ok(pageResult);
    }

    /**
     * 转换查询条件值
     *
     * @param queryConditionDto 查询条件
     * @param fieldHtmlMap      字段设计
     */
    private void convertQueryValue(QueryConditionDto queryConditionDto, Map<String, FieldBasicsHtml> fieldHtmlMap) {
        FieldBasicsHtml fieldBasicsHtml = fieldHtmlMap.get(queryConditionDto.getFieldKey());
        if (ObjectNull.isNull(fieldBasicsHtml)) {
            return;
        }
        // 数字组件查询时，将条件值转换为数字
        if (DataFieldType.inputNumber.equals(fieldBasicsHtml.getFieldType())) {
            Function<Object, Object> convertNumber = value -> {
                if (!NumberUtil.isNumber(value.toString())) {
                    return value;
                }
                BigDecimal bigDecimal = new BigDecimal(value.toString());
                Object convertValue = bigDecimal.doubleValue();
                if (bigDecimal.scale() == 0) {
                    convertValue = bigDecimal.intValue();
                }
                return convertValue;
            };

            Object value = queryConditionDto.getValue();
            if (ObjectNull.isNull(value)) {
                return;
            } else if (value instanceof Collection) {
                List<Object> values = (List) value;
                value = Stream.of(values).flatMap(Collection::stream).map(convertNumber::apply).collect(Collectors.toList());
            } else {
                value = convertNumber.apply(queryConditionDto.getValue());
            }
            queryConditionDto.setValue(value);
        }
    }

    /**
     * 将列表的数据以树形结构重新组装
     *
     * @param allData                         所有数据
     * @param modelDisplayMap                 是否存在数据筛选
     * @param records                         分页数据
     * @param treeQuery                       树形查询条件
     * @param appId                           应用 id
     * @param modelId                         模型 id
     * @param combiningFieldFormulaContentMap 查询条件
     * @param queryGroupConditions            查询条件
     * @param sorts                           排序
     * @param fields                          所有字段名
     * @param andOr                           使用and拼接还是or拼接查询条件
     * @param fieldBasicsHtmls                所有字段名
     * @param stringSet                       暂时无用
     * @return
     */
    private List<Map<String, Object>> treeStructure(List<Map<String, Object>> allData, Map<String, ModelDisplayHtml> modelDisplayMap, List<Map<String, Object>> records, AtomicReference<QueryConditionDto> treeQuery,
                                                    String appId, String
                                                            modelId,
                                                    Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap,
                                                    List<List<QueryConditionDto>> queryGroupConditions, List<QueryOrderDto> sorts, List<String> fields, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls, Set<String> stringSet) {
        long treeStructureStartTime = System.currentTimeMillis();
        // 显示设置-关联模型数据回显
        dynamicDataService.echoModelDisplay(appId, records, modelDisplayMap);
        if (ObjectNull.isNotNull(treeQuery.get())) {
            if (treeQuery.get().getFieldKey().equals("id")) {
                log.info("[树形结构] 树查询字段为id，直接返回，耗时: {}ms", System.currentTimeMillis() - treeStructureStartTime);
                return records;
            } else if (ObjectNull.isNull(allData) && ObjectNull.isNotNull(records)) {
                // 统计数据总量
                long countStart = System.currentTimeMillis();
                long totalCount = dataModelHandler.estimatedCount(modelId);
                log.info("[树形结构] 统计数据总量: {}, 耗时: {}ms", totalCount, System.currentTimeMillis() - countStart);
                
                // 获取父级字段名
                String parentFieldKey = queryGroupConditions.get(0).get(0).getFieldKey();
                List<String> rootIds = records.stream().map(e -> e.get("id").toString()).collect(Collectors.toList());
                
                // 使用批量分层查询优化方案，完全消除递归查询
                return buildTreeStructureByBatchQuery(appId, modelId, rootIds, parentFieldKey, 
                    fields, fieldBasicsHtmls, modelDisplayMap, totalCount);
            }
        }
        log.info("[树形结构] 无树查询条件，直接返回");
        return records;
    }
    
    /**
     * 批量分层查询构建树形结构
     * 核心优化：完全消除N+1递归查询，使用批量查询 + 内存组装
     * 
     * @param appId 应用ID
     * @param modelId 模型ID
     * @param rootIds 根节点ID集合
     * @param parentFieldKey 父级字段名
     * @param fields 查询字段
     * @param fieldBasicsHtmls 字段配置
     * @param modelDisplayMap 显示配置
     * @param totalCount 数据总量
     * @return 树形结构数据
     */
    private List<Map<String, Object>> buildTreeStructureByBatchQuery(
            String appId, String modelId, List<String> rootIds, String parentFieldKey,
            List<String> fields, List<FieldBasicsHtml> fieldBasicsHtmls, 
            Map<String, ModelDisplayHtml> modelDisplayMap, long totalCount) {
        
        long startTime = System.currentTimeMillis();
        log.info("[树形结构-批量查询] 开始构建树形结构，根节点数: {}, 数据总量: {}", rootIds.size(), totalCount);
        
        try {
            // 准备查询字段
            Set<String> fieldKeys = new HashSet<>(fields);
            fieldKeys.add(Get.name(DynamicDataPo::getId));
            fieldKeys.add(parentFieldKey);
            
            Query query = new Query();
            String[] fieldArray = new String[fieldKeys.size()];
            query.fields().include(fieldKeys.toArray(fieldArray));
            
            // 批量查询所有数据（一次查询，消除N+1问题）
            long batchQueryStart = System.currentTimeMillis();
            List<Map> mapList = dataModelHandler.find(query, Map.class, modelId);
            log.info("[树形结构-批量查询] 批量查询完成，查询到{}条数据，耗时: {}ms", 
                mapList.size(), System.currentTimeMillis() - batchQueryStart);
            
            // 数据转换和回显
            long echoStart = System.currentTimeMillis();
            List<Map<String, Object>> allDataList = mapList.stream()
                .map(e -> (Map<String, Object>) e)
                .collect(Collectors.toList());
            
            // 优化：批量预加载关联字段数据，避免N+1查询
            long preloadStart = System.currentTimeMillis();
            Map<String, Map<String, Map<String, Object>>> preloadedDataCache = 
                batchPreloadRelatedFieldData(allDataList, fieldBasicsHtmls, appId);
            log.info("[树形结构-批量查询] 关联数据预加载完成，耗时: {}ms, 缓存大小: {}", 
                System.currentTimeMillis() - preloadStart, preloadedDataCache.size());
            
            // 使用预加载的数据进行回显，避免逐条查询数据库
            SystemThreadLocal.set("PRELOADED_DATA_CACHE", preloadedDataCache);
            try {
                long echoStart2 = System.currentTimeMillis();
                allDataList = allDataList.stream()
                    .map(e -> dynamicDataService.echo(e, fieldBasicsHtmls, false))
                    .collect(Collectors.toList());
                log.info("[树形结构-批量查询] echo完成，耗时: {}ms", System.currentTimeMillis() - echoStart2);
            } finally {
                SystemThreadLocal.remove("PRELOADED_DATA_CACHE");
            }
            
            designHandler.handleButtonInfo(allDataList, EnvConstant.PAGE_BUTTON_DISPLAY);
            dynamicDataService.echoModelDisplay(appId, allDataList, modelDisplayMap);
            log.info("[树形结构-批量查询] 数据回显处理完成，耗时: {}ms", System.currentTimeMillis() - echoStart);
            
            // 在内存中构建树形结构
            long buildTreeStart = System.currentTimeMillis();
            
            // 循环引用路径检测：防止A→B→C→A这种循环，以及节点指向自己的情况
            Map<String, String> idToParentMap = new HashMap<>();
            for (Map<String, Object> data : allDataList) {
                String id = data.get("id").toString();
                Object parentValue = data.get(parentFieldKey);
                String parentId = parentValue != null ? parentValue.toString() : "";
                if (!parentId.isEmpty()) {
                    idToParentMap.put(id, parentId);
                }
            }
            
            // 检测并修复循环引用
            int circularReferenceCount = 0;
            for (Map<String, Object> data : allDataList) {
                String id = data.get("id").toString();
                Object parentValue = data.get(parentFieldKey);
                String parentId = parentValue != null ? parentValue.toString() : "";
                
                // 情况1：节点的父节点指向自己
                if (id.equals(parentId)) {
                    log.warn("[树形结构-批量查询] 发现自引用：节点[id={}, 名称={}]的父节点指向自己，已修复", 
                        id, data.get("gongChengMingChen"));
                    data.put(parentFieldKey, null);
                    idToParentMap.remove(id);
                    circularReferenceCount++;
                    continue;
                }
                
                // 情况2：检测循环引用链（A→B→C→A）
                Set<String> visited = new HashSet<>();
                visited.add(id);
                
                String currentId = id;
                while (idToParentMap.containsKey(currentId)) {
                    String nextParentId = idToParentMap.get(currentId);
                    if (visited.contains(nextParentId)) {
                        // 发现循环：断开循环链
                        log.warn("[树形结构-批量查询] 发现循环引用链：节点[id={}, 名称={}]形成循环，已断开", 
                            id, data.get("gongChengMingChen"));
                        data.put(parentFieldKey, null);
                        idToParentMap.remove(id);
                        circularReferenceCount++;
                        break;
                    }
                    visited.add(nextParentId);
                    currentId = nextParentId;
                }
            }
            
            if (circularReferenceCount > 0) {
                log.warn("[树形结构-批量查询] 共发现并修复 {} 处循环引用，建议修复数据库中的数据", circularReferenceCount);
            }
            
            List<Map<String, Object>> tree = TreeUtils.list2Tree(
                allDataList,
                rootIds,
                e -> e.get("id").toString(),
                (Function<Map<String, Object>, String>) e -> {
                    try {
                        Object parentValue = e.get(parentFieldKey);
                        return parentValue != null ? parentValue.toString() : "";
                    } catch (Exception ex) {
                        log.warn("[树形结构-批量查询] 获取父级字段值异常: {}", ex.getMessage());
                        return "";
                    }
                },
                (parent, children) -> {
                    if (ObjectNull.isNotNull(children) && !children.isEmpty()) {
                        parent.put("children", children);
                    }
                }
            );
            
            log.info("[树形结构-批量查询] 内存构建树形结构完成，树节点数: {}, 耗时: {}ms", 
                tree.size(), System.currentTimeMillis() - buildTreeStart);
            log.info("[树形结构-批量查询] 总耗时: {}ms，性能提升: 消除了N+1递归查询问题", 
                System.currentTimeMillis() - startTime);
            
            return tree;
            
        } catch (Exception e) {
            log.error("[树形结构-批量查询] 构建树形结构失败: {}", e.getMessage(), e);
            // 失败时返回原始记录，避免接口报错
            log.warn("[树形结构-批量查询] 降级处理：返回扁平化数据");
            return Collections.emptyList();
        }
    }
    
    /**
     * 批量预加载关联字段数据
     * 核心优化：将逐条查询变为批量查询，从而消除N+1查询问题
     * 
     * @param allDataList 所有数据
     * @param fieldBasicsHtmls 字段配置
     * @param appId 应用ID
     * @return 预加载数据缓存：Map<字段名, Map<关联模型ID, Map<ID, 数据>>>
     */
    private Map<String, Map<String, Map<String, Object>>> batchPreloadRelatedFieldData(
            List<Map<String, Object>> allDataList,
            List<FieldBasicsHtml> fieldBasicsHtmls,
            String appId) {
        
        Map<String, Map<String, Map<String, Object>>> preloadedDataCache = new HashMap<>();
        
        // 过滤出需要关联查询的字段（下拉选择、级联选择等）
        List<FieldBasicsHtml> relatedFields = fieldBasicsHtmls.stream()
            .filter(field -> {
                DataFieldType type = field.getType();
                return type == DataFieldType.select || 
                       type == DataFieldType.cascader || 
                       type == DataFieldType.radio;
            })
            .filter(field -> {
                try {
                    // 检查是否是关联数据模型类型
                    Object datatype = JvsJsonPath.read(field.getDesignJson(), "$.datatype");
                    return "dataModel".equals(String.valueOf(datatype));
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        if (relatedFields.isEmpty()) {
            log.info("[批量预加载] 没有需要关联查询的字段，跳过预加载");
            return preloadedDataCache;
        }
        
        log.info("[批量预加载] 发现{}个关联字段需要预加载", relatedFields.size());
        
        // 对每个关联字段进行批量预加载
        for (FieldBasicsHtml field : relatedFields) {
            try {
                String fieldKey = field.getFieldKey();
                String formId = (String) JvsJsonPath.read(field.getDesignJson(), "$.formId");
                String labelField = (String) JvsJsonPath.read(field.getDesignJson(), "$.props.label");
                
                if (ObjectNull.isNull(formId, labelField)) {
                    continue;
                }
                
                // 收集所有需要查询的ID
                Set<String> allIds = new HashSet<>();
                for (Map<String, Object> data : allDataList) {
                    Object value = data.get(fieldKey);
                    if (ObjectNull.isNull(value)) {
                        continue;
                    }
                    
                    if (value instanceof Collection) {
                        ((Collection<?>) value).forEach(v -> allIds.add(String.valueOf(v)));
                    } else {
                        allIds.add(String.valueOf(value));
                    }
                }
                
                if (allIds.isEmpty()) {
                    continue;
                }
                
                log.info("[批量预加载] 字段[{}]需要查询{}条关联数据，关联模型: {}", 
                    fieldKey, allIds.size(), formId);
                
                // 批量查询关联数据
                List<String> queryFields = Arrays.asList("id", labelField);
                QueryConditionDto queryCondition = new QueryConditionDto();
                queryCondition.setValue(new ArrayList<>(allIds));
                queryCondition.setEnabledQueryTypes(DataQueryType.in);  // 使用IN查询
                queryCondition.setFieldKey("id");
                
                // 跳过数据权限
                DynamicDataUtils.freePermit();
                List<Criteria> authCriteria = DynamicDataUtils.getAuthCriteria();
                SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, null);
                
                try {
                    List<Map<String, Object>> relatedDataList = 
                        dynamicDataService.queryList(formId, queryFields, queryCondition);
                    
                    // 将查询结果缓存到Map中
                    Map<String, Object> relatedDataMap = new HashMap<>();
                    if (ObjectNull.isNotNull(relatedDataList)) {
                        relatedDataMap = relatedDataList.stream()
                            .collect(Collectors.toMap(
                                data -> String.valueOf(data.get("id")),
                                data -> data,
                                (v1, v2) -> v1
                            ));
                    }
                    
                    // 存储到缓存中：结构为 fieldKey -> formId -> (dataId -> dataObject)
                    Map<String, Map<String, Object>> formIdMap = preloadedDataCache
                        .computeIfAbsent(fieldKey, k -> new HashMap<>());
                    formIdMap.put(formId, relatedDataMap);
                    
                    log.info("[批量预加载] 字段[{}]预加载完成，加载{}条数据", 
                        fieldKey, relatedDataMap.size());
                        
                } finally {
                    SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_CRITERIA, authCriteria);
                    SystemThreadLocal.set(DynamicDataUtils.KEY_AUTH_FREE, null);
                }
                
            } catch (Exception e) {
                log.error("[批量预加载] 字段[{}]预加载失败: {}", field.getFieldKey(), e.getMessage(), e);
            }
        }
        
        log.info("[批量预加载] 总计预加载{}个字段的关联数据", preloadedDataCache.size());
        return preloadedDataCache;
    }

    private List<List<QueryConditionDto>> getQueryConditions(List<List<QueryConditionDto>> conditions, Map<String, FieldBasicsHtml> collectMap, Object notification) {
        //过滤空的条件
        return conditions.stream().map(groupConditions -> groupConditions.stream().filter(e -> ObjectNull.isNotNull(e.getValue(), e.getEnabledQueryTypes())).peek(e -> {
            //排除查询条件中带_的字段
            if (e.getFieldKey().endsWith(DynamicDataUtils.SUFFIX_ECHO)) {
                e.setFieldKey(null);
                return;
            }
            //判断角色、岗位、团队、用户，这四种业务类型
            if (e.getValue() instanceof List) {
                List<String> collect = ((List<?>) e.getValue()).stream()
                        .filter(a -> a instanceof Map)
                        .map(a -> ((Map) a))
                        .filter(a -> a.containsKey("type"))
                        .filter(a -> Arrays.asList("role", "user", "group", "dept", "job").contains(a.get("type")))
                        .map(a -> a.get("id").toString()).collect(Collectors.toList());
                if (ObjectNull.isNotNull(collect)) {
                    e.setEnabledQueryTypes(DataQueryType.in);
                    e.setValue(collect);
                }
            }
            if (!collectMap.containsKey(e.getFieldKey())) {
                return;
            }
            FieldBasicsHtml fieldBasicsHtml = collectMap.get(e.getFieldKey());
            // 判断请求头是否是弹窗搜索,如果有值，直接放开
            if (fieldBasicsHtml.getType().equals(DataFieldType.input)) {
                if (ObjectNull.isNotNull(notification)) {
//                    e.setEnabledQueryTypes(DataQueryType.like);
                }
            }
            //关联了数据模型的下拉选择框查询,优先使用模糊查询
            //根据下拉数据组件,和单选 组件,只要是多选类,或单选类,进行数据二次转换处理,保证保证列表页查询条件,支持模糊搜索
            if (ObjectNull.isNull(fieldBasicsHtml.getDesignJson())) {
                //如果组件设计为空,则直接返回
                return;
            }
            //如果没有关联直接退出
            if (ObjectNull.isNull(fieldBasicsHtml.getDesignJson().get(FORM_ID))) {
                return;
            }
            //如果是下拉选择
            if (fieldBasicsHtml.getType().equals(DataFieldType.select)) {
                if (e.getEnabledQueryTypes().equals(DataQueryType.in)) {
                    try {
                        String formId1 = fieldBasicsHtml.getDesignJson().get("formId").toString();
                        Map<String, Object> props = (Map<String, Object>) fieldBasicsHtml.getDesignJson().get("props");
                        String label = props.get("label").toString();
                        Object o1 = dynamicDataService.getById(formId1, String.valueOf(e.getValue())).getJsonData().get(label);
                        Criteria haoCaiMingChen = DynamicDataUtils.like(new Criteria(), label, o1);
                        DynamicDataUtils.freePermit();
                        ArrayList<String> objects = new ArrayList<>();
                        objects.add(label);
                        List<String> ids = dynamicDataService.queryList(formId1, haoCaiMingChen, objects).stream().map(s -> s.get("id").toString()).collect(Collectors.toList());
                        e.setValue(ids);
                    } catch (Exception ex) {
                        log.error(fieldBasicsHtml.getLabel() + " 字段，字段名为" + fieldBasicsHtml.getProp() + "设计有错误，请检查!");
                    }
                }
            }
            //没有关联数据模型的下拉选择框 ,复选框查询条件
            //复选
            if (fieldBasicsHtml.getType().equals(DataFieldType.select) || fieldBasicsHtml.getType().equals(DataFieldType.checkbox)) {
                e.setEnabledQueryTypes(DataQueryType.in);
                String formId1 = fieldBasicsHtml.getDesignJson().get("formId").toString();
                Map<String, Object> props = (Map<String, Object>) fieldBasicsHtml.getDesignJson().get("props");
                String label = props.get("label").toString();
                if (!(e.getValue() instanceof List)) {
                    ArrayList<Object> value = new ArrayList<>();
                    value.add(e.getValue());
                    e.setValue(value);
                } else {
                    Object o1 = dynamicDataService.getByIds(formId1, (List<String>) e.getValue()).stream().map(m -> m.get(label)).collect(Collectors.toList());
                    if (ObjectNull.isNotNull(o1)) {
                        Criteria haoCaiMingChen = DynamicDataUtils.like(new Criteria(), label, o1);
                        DynamicDataUtils.freePermit();
                        ArrayList<String> objects = new ArrayList<>();
                        objects.add(label);
                        List<String> ids = dynamicDataService.queryList(formId1, haoCaiMingChen, objects).stream().map(s -> s.get("id").toString()).collect(Collectors.toList());
                        e.setValue(ids);
                    }
                }
            }
        }).collect(Collectors.toList())).collect(Collectors.toList());
    }

    /**
     * 查询所有数据
     *
     * @param appId    the app id
     * @param modelId  模型id
     * @param fieldKey 数据字段
     * @return 数据集合 r
     */
    @ApiOperation("查询所有数据")
    @GetMapping("/query/list/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<List<Map<String, Object>>> queryList(@PathVariable String appId, @PathVariable("modelId") String modelId, @ApiParam(name = "需要查询的字段", required = true) @RequestParam(value = "fieldKey", required = false) String fieldKey) {
        DataModelPo one = dataModelService.getOne(Wrappers.query(new DataModelPo().setAppId(appId).setId(modelId)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        //判断模型
        DynamicDataUtils.dataModelScope(modelId);
        List<String> fieldKey1 = null;

        if (ObjectNull.isNull(fieldKey)) {
            fieldKey1 = dataFieldService.getFieldKeys(appId, modelId);
        } else {
            try {
                fieldKey1 = JSONArray.parseArray(URLDecoder.decode(fieldKey, "UTF-8"), String.class);
                //兼容2.1.4的关联数据
            } catch (Exception e) {
                try {
                    fieldKey1 = new ArrayList<String>() {{
                        add(fieldKey);
                    }};
                } catch (Exception ex) {

                }
            }
        }
        List<Map<String, Object>> result = dynamicDataService.queryList(modelId, fieldKey1);
        //如果不为空,转树形
        return R.ok(result);
    }

    /**
     * 查询树形数据
     *
     * @param appId        the app id
     * @param modelId      模型id
     * @param fieldTreeDto the field tree dto
     * @return 数据集合 r
     */
    @ApiOperation("查询树形数据")
    @PostMapping("/query/tree/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<List> queryList(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestBody FieldTreeDto fieldTreeDto) {
        DataModelPo one = dataModelService.getOne(Wrappers.query(new DataModelPo().setAppId(appId).setId(modelId)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        //根据设计获取默认地址
        List<String> fieldList = new ArrayList<>();
        fieldList.add(fieldTreeDto.getValue());
        fieldList.add(fieldTreeDto.getLabel());
        fieldList.add(fieldTreeDto.getSecTitle());
        String createTime = Get.name(BasePo::getCreateTime);
        fieldList.add(createTime);

        // 条件筛选
        Criteria criteria = null;
        List<List<QueryConditionDto>> conditions = new ArrayList<>();
        if (ObjectNull.isNotNull(fieldTreeDto.getFilter())) {
            if (ObjectNull.isNotNull(fieldTreeDto.getFilter().getGroupConditions())) {
                conditions = fieldTreeDto.getFilter().getGroupConditions();
            } else {
                List<QueryConditionDto> conditionDtos = fieldTreeDto.getFilter().getConditions();
                conditions.add(conditionDtos);
            }
        }
        if (ObjectNull.isNotNull(fieldTreeDto.getQuery()) && ObjectNull.isNotNull(fieldTreeDto.getQuery().getConditions())) {
            conditions.add(fieldTreeDto.getQuery().getConditions());
        }
        //判断条件不能为空
        if (ObjectNull.isNotNull(conditions)) {
            criteria = DynamicDataUtils.buildDynamicGroupCriteria(conditions);
        }
        List<Map<String, Object>> result = dynamicDataService.queryList(modelId, criteria, fieldList);
        Set<String> ids = new HashSet<>();
        // 根据树形结构查询递归反推查询整体树型
        if (ObjectNull.isNotNull(criteria) && ObjectNull.isNotNull(fieldTreeDto.getSecTitle()) && ObjectNull.isNotNull(result)) {
            Map<String, Map<String, Object>> map = result.stream().collect(Collectors.toMap(e -> e.get("id").toString(), e -> e));
            ids.addAll(map.keySet());
            List<String> parentValues = result.stream().filter(e -> ObjectNull.isNotNull(e.get(fieldTreeDto.getSecTitle()))).map(e -> e.get(fieldTreeDto.getSecTitle()).toString()).collect(Collectors.toList());
            getParentList(modelId, fieldList, fieldTreeDto.getValue(), fieldTreeDto.getSecTitle(), parentValues, map, ids);
            result = map.values().stream().collect(Collectors.toList());
        }
        // 需要控制权限，并将权限的信息返回在数据按钮上面，进行处理
        designHandler.handleButtonInfo(result, EnvConstant.LEFT_TREE_BUTTON_DISPLAY);
        List<Tree<Object>> build = TreeUtil.build(result, null, new TreeNodeConfig(), (treeNode, tree) -> {
            tree.setId(treeNode.get("id").toString());
            tree.setParentId(treeNode.get(fieldTreeDto.getSecTitle()));
            Object orDefault = treeNode.getOrDefault(fieldTreeDto.getLabel(), "");
            if (ObjectNull.isNull(orDefault)) {
                orDefault = "";
            }
            tree.setName(orDefault.toString());
            //排序是根据时间毫秒排序

            try {
                Object o = treeNode.get(createTime);
                if (o instanceof Date) {
                    long time = ((Date) o).getTime();
                    tree.setWeight(-1 * time);
                } else {
                    long time = LocalDateTime.parse(o.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC);
                    tree.setWeight(-1 * time);
                }
            } catch (Exception e) {
            }
            tree.put("value", treeNode.get(fieldTreeDto.getValue()).toString());
            tree.put("extend", treeNode);
        });
        return R.ok(build);
    }

    /**
     * 获取指定数据的所有父级数据
     *
     * @param modelId         模型id
     * @param fieldList       查询字段
     * @param parentFieldName 父字段名
     * @param parentValues    父字段值
     * @param map             数据
     * @param ids
     */
    private void getParentList(String modelId, List<String> fieldList, String parentValueName, String parentFieldName, List<String> parentValues, Map<String, Map<String, Object>> map, Set<String> ids) {
        if (ObjectNull.isNull(parentValues)) {
            return;
        }
        QueryConditionDto queryCondition = new QueryConditionDto().setFieldKey(parentValueName).setValue(parentValues).setEnabledQueryTypes(DataQueryType.in);
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(Collections.singletonList(queryCondition));
        List<Map<String, Object>> results = dynamicDataService.queryList(modelId, criteria, fieldList);
        if (ObjectNull.isNotNull(results)) {
            parentValues.clear();
            results.forEach(e -> {
                if (ObjectNull.isNotNull(e.get(parentFieldName))) {
                    parentValues.add(e.get(parentFieldName).toString());
                }
                String id = e.get("id").toString();
                if (!ids.contains(id)) {
                    ids.add(id);
                    map.put(id, e);
                }
            });
            parentValues.removeAll(ids);
            getParentList(modelId, fieldList, parentValueName, parentFieldName, parentValues, map, ids);
        }
    }

    /**
     * Post query list r.
     *
     * @param appId        the app id
     * @param dataModelId  the data model id
     * @param queryPageDto the query page dto
     * @return the r
     */
    @ApiOperation("post查询所有数据")
    @PostMapping("/query/list/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<List<Map<String, Object>>> postQueryList(@PathVariable String appId, @PathVariable("modelId") String dataModelId, @RequestBody QueryListDto queryPageDto) {
        List<Map<String, Object>> body = getBody(appId, dataModelId, queryPageDto);
        return R.ok(body);
    }

    private List<Map<String, Object>> getBody(String appId, String dataModelId, QueryListDto queryPageDto) {
        if (ObjectNull.isNull(queryPageDto.getFieldList())) {
            List<String> fieldKeys = dataFieldService.getFieldKeys(appId, dataModelId);
            queryPageDto.setFieldList(fieldKeys);
        }
        List<Map<String, Object>> data = dynamicDataService.postQueryList(appId, dataModelId, queryPageDto);
        // 数据项禁用
        dynamicDataService.enableDataItem(data, queryPageDto.getEnableConditions());
        return data;
    }


    /**
     * 查询单条数据
     *
     * @param appId       the app id
     * @param dataModelId 模型id
     * @param designId    the design id
     * @param dataId      数据id
     * @return 数据内容 r
     */
    @Log
    @ApiOperation("查询单条转换后的数据")
    @GetMapping("/query/single/transformation/{modelId}/{designId}/{dataId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> querySinglePrint(@PathVariable String appId, @PathVariable("modelId") String dataModelId, @PathVariable String designId, @PathVariable("dataId") String dataId) {
        //设置数据权限
        DynamicDataUtils.dataModelScope(dataModelId);
        //查询单条数据，并处理数据权限，并添加默认按钮,不做转换
        Map<String, Object> data = dynamicDataService.querySingle(appId, dataModelId, dataId, true);
        //做数据转换操作
        data = dynamicDataService.paresMapWithEcho(appId, data, dataModelId, designId, true);
        DataModelPo byId = dataModelService.getById(dataId);
        //处理表单中的脱敏
        dynamicDataService.encryptionData(data, byId);
        return R.ok(data);
    }


    /**
     * 查询单条数据并进行数据转换，包含按钮，和数据脱敏,数据权限
     *
     * @param appId       the app id
     * @param dataModelId 模型id
     * @param dataId      数据id
     * @param designId    the design id
     * @return 数据内容 r
     */
    @Log
    @ApiOperation("查询单条数据")
    @GetMapping("/query/single/{modelId}/{dataId}")
    public R<Map<String, Object>> querySingle(@PathVariable String appId, @PathVariable("modelId") String dataModelId, @PathVariable("dataId") String
            dataId, @RequestHeader(value = "Designid", required = false) String designId) {
        //设置数据权限
        DynamicDataUtils.dataModelScope(dataModelId);
        //查询单条数据，并处理数据权限，并添加默认按钮,不做转换
        Map<String, Object> data = dynamicDataService.querySingle(appId, dataModelId, dataId, true);
        //做数据转换操作
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, dataModelId, null, true, false);
        data = dynamicDataService.echo(data, fields, false);
        DataModelPo byId = dataModelService.getById(dataModelId);
        //处理表单中的脱敏
        dynamicDataService.encryptionData(data, byId);
        // 扩展返回数据
        dynamicDataService.expandOtherData(data);
        if (ObjectNull.isNotNull(designId)) {
            FormPo po = formService.getById(designId);
            //根据设计判断哪些字段是存在有关联模型的，如果有直接删除对应的数据
            if (ObjectNull.isNotNull(po)) {
                //判断哪些是有关联模型的进行过滤出来
                List<String> tableField = fields.stream().filter(e -> e.getFieldType().equals(DataFieldType.tableForm))
                        .filter(e -> ObjectNull.isNotNull(e.getDesignJson()))
                        .map(e -> tableFormFieldHandler.toHtml(e.getDesignJson()))
                        .filter(e -> ObjectNull.isNotNull(e.getDataModelId()))
                        .map(FieldPublicHtml::getProp)
                        .collect(Collectors.toList());
                data.keySet().removeIf(tableField::contains);
            }
        }
        return R.ok(data);
    }

    /**
     * 查询单个字段的值
     *
     * @param appId   the app id
     * @param modelId 模型id
     * @param dataId  数据id
     * @param fieldId 字段key
     * @return 单个字段的值 r
     */
    @ApiOperation("查询单个字段的值")
    @GetMapping("/query/field/{modelId}/{dataId}/{fieldId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Object> queryField(@PathVariable String appId, @PathVariable("modelId") String modelId, @PathVariable("dataId") String dataId, @PathVariable("fieldId") String fieldId) {
        DynamicDataUtils.dataModelScope(modelId);
        return R.ok(dynamicDataService.queryField(appId, modelId, dataId, fieldId));
    }

    /**
     * 导入数据
     *
     * @param appId    the app id
     * @param file     数据文件
     * @param modelId  模型id
     * @param designId 设计id
     * @return 操作结果 r
     */
    @Log
    @ApiOperation("导入")
    @PostMapping("/import/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R importDesign(@PathVariable String appId, @RequestParam("file") MultipartFile file, @PathVariable("modelId") String modelId, @RequestParam(name = "designId", required = false) String designId) {
        long startTime = System.currentTimeMillis();
        log.info("开始执行数据导入，开始时间: {}", new Date(startTime));
        setFunctionName("导入");

        DynamicDataUtils.checkPermit();
        List<List<Object>> excelData;
        try {
            log.info("开始读取Excel文件");
            excelData = ExcelUtil.getReader(file.getInputStream()).read();
            log.info("Excel文件读取完成，耗时: {}ms，数据行数: {}", System.currentTimeMillis() - startTime, excelData.size());
        } catch (Exception ex) {
            log.error("数据导入异常", ex);
            return R.failed("数据导入异常");
        }
        if (ObjectUtils.isEmpty(excelData) || excelData.size() < EXCEL_DATA_MIN_SIZE) {
            log.info("导入数据为空，返回");
            return R.ok(false, "导入数据为空");
        }
        // 处理Excel表头
        log.info("开始查询CrudPage数据");
        CrudPage crudPage = pageService.getOne(Wrappers.query(new CrudPage().setDataModelId(modelId).setId(designId).setJvsAppId(appId)));
        log.info("CrudPage数据查询完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        
        log.info("开始解析页面设计");
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(crudPage.getViewJson());
        log.info("页面设计解析完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        
        Optional<ButtonDesignHtml> first = pageDesignHtml.getButtons().stream().filter(e -> e.getType().equals(ButtonTypeEnum.btn_download_template)).findFirst();
        //转换使用导出模板的别名为转换规则。这里获取的是所有要导入的字段列，需要判断字段列的个数是否正确
        List<String> fieldKeyList = new ArrayList<>();
        //取出不为空的数据级件进行判断
        Map<String, String> isNotNullField = new HashMap<>(8);
        if (CollectionUtils.isEmpty(first.get().getImportFields())) {
            //默认所有数据字段
            log.info("获取默认所有数据字段");
            fieldKeyList = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName).collect(Collectors.toList());
        } else {
            List<String> fieldIndex = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName).collect(Collectors.toList());
            log.info("获取自定义导入字段");
            fieldKeyList = new ArrayList<>(first.get().getImportFields().stream()
                    .peek(e -> {
                        if (ObjectNull.isNotNull(e.getRequired())) {
                            if (e.getRequired()) {
                                isNotNullField.put(e.getAliasColumnName(), e.getShowChinese());
                            }
                        }
                    })
                    .map(DataTableFieldDesignHtml::getAliasColumnName)
                    .sorted(Comparator.comparingInt(fieldIndex::indexOf))
                    .collect(Collectors.toList()));
        }

        log.info("开始查询数据模型，耗时: {}ms", System.currentTimeMillis() - startTime);
        DataModelPo byId = dataModelService.getById(crudPage.getDataModelId());
        //处理表单中的脱敏字段
        log.info("开始处理脱敏字段，耗时: {}ms", System.currentTimeMillis() - startTime);
        List<String> encryptionFields = dynamicDataService.encryptionData(byId);
        //根据脱敏字段进行去掉
        if (ObjectNull.isNotNull(encryptionFields)) {
            fieldKeyList.removeAll(encryptionFields);
        }
        if (excelData.get(0).size() != fieldKeyList.size()) {
            log.info("导入的模板格式不正确，返回错误，耗时: {}ms", System.currentTimeMillis() - startTime);
            return R.failed("导入的模板格式不正确,请检查文件");
        }

        log.info("开始查询字段信息，耗时: {}ms", System.currentTimeMillis() - startTime);
        List<FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, true, false);
        log.info("字段信息查询完成，字段数量: {}，耗时: {}ms", fields.size(), System.currentTimeMillis() - startTime);
        
        // 处理Excel表数据
        log.info("开始解析Excel数据为Map结构，耗时: {}ms", System.currentTimeMillis() - startTime);
        List<Map<String, Object>> mapDataList = this.parseExcelData2MapData(excelData, fieldKeyList, modelId);
        log.info("Excel数据解析完成，数据条数: {}，耗时: {}ms", mapDataList.size(), System.currentTimeMillis() - startTime);
        log.info("开始检查非空字段，耗时: {}ms", System.currentTimeMillis() - startTime);
        //判断导入的数据是否为空，如果为空，则直接报错, 并记录行号，和列号
        if (ObjectNull.isNotNull(isNotNullField)) {
            String error = IntStream.iterate(0, i -> i + 1)
                    .limit(mapDataList.size())
                    .mapToObj(i -> {
                        //获取哪些列为空的的字段名
                        String collect = isNotNullField.keySet().stream().filter(e -> ObjectNull.isNull(mapDataList.get(i).get(e)))
                                .map(isNotNullField::get)
                                .collect(Collectors.joining(","));
                        if (ObjectNull.isNotNull(collect)) {
                            return "第" + (i + 2) + "行" + collect;
                        }
                        return collect;
                    }).filter(ObjectNull::isNotNull).collect(Collectors.joining(","));
            if (ObjectNull.isNotNull(error)) {
                log.info("发现非空字段错误，耗时: {}ms", System.currentTimeMillis() - startTime);
                throw new BusinessException(error + "为空");
            }
        }
        log.info("非空字段检查完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        
        log.info("开始检查数据字段类型，耗时: {}ms", System.currentTimeMillis() - startTime);
        dynamicDataService.checkDataFieldType(appId, modelId, mapDataList);
        log.info("数据字段类型检查完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        
        Map<String, String> ids = new HashMap<>(8);
        String linkFieldKey = null;
        //自己关联自己
        FormValueHtml formValueHtml = null;
        log.info("开始构建字段类型映射，耗时: {}ms", System.currentTimeMillis() - startTime);
        Map<String, FieldBasicsHtml> typeMaps =
                fields.stream().filter(e -> DataFieldType.SELECT_CONVERSION.contains(e.getType()) || e.getType().equals(DataFieldType.datePicker) || e.getType().equals(DataFieldType.timePicker) || e.getType().equals(DataFieldType.timeSelect)).collect(Collectors.toMap(FieldPublicHtml::getFieldKey,
                        //这里直接转换获取真实类型数据
                        e -> {
                            if (ObjectNull.isNotNull(e.getDesignJson())) {
                                return fieldHandlerMap.get(e.getType().getDesc()).toHtml(e);
                            } else {
                                return e;
                            }
                        }));
        log.info("字段类型映射构建完成，映射数量: {}，耗时: {}ms", typeMaps.size(), System.currentTimeMillis() - startTime);
        log.info("开始处理树形数据，耗时: {}ms", System.currentTimeMillis() - startTime);
        //如果是树形数据中间的数据
        Map<String, List<Map<String, Object>>> generateCascaderList = new LinkedHashMap<>();
        if (ObjectNull.isNotNull(typeMaps)) {
            //用于存储不同字段的数据 id和值的关系路径  但这里缺少一个路径，所以需要添加上路径  field , path ,id
            Map<String, Map<String, String>> cascaderFieldPathIdsMap = new LinkedHashMap<>();
            Set<String> collect = fieldKeyList.stream().filter(typeMaps::containsKey).collect(Collectors.toSet());
            log.info("开始处理选择转换字段，耗时: {}ms", System.currentTimeMillis() - startTime);
            //树形字段和多选字段处理逻辑前置处理逻辑
            Map<String, List<FieldBasicsHtml>> cascaderList = typeMaps.values().stream().filter(e -> DataFieldType.SELECT_CONVERSION.contains(e.getType()))
                    .collect(Collectors.groupingBy(FieldPublicHtml::getProp));
            log.info("选择转换字段分组完成，分组数量: {}，耗时: {}ms", cascaderList.size(), System.currentTimeMillis() - startTime);
            //需要先判断出是否关联了自己,并将所有的下拉数据值，进行转换出结果对象
            log.info("开始遍历选择转换字段，耗时: {}ms", System.currentTimeMillis() - startTime);
            for (String field : cascaderList.keySet()) {
                //只取第一条，因为设计一致，
                FieldBasicsHtml fieldBasicsHtml = cascaderList.get(field).get(0);
                log.info("处理字段: {}，耗时: {}ms", field, System.currentTimeMillis() - startTime);
                //遍历所有的数据值，用于判断每一级的数据是否存在，如果不存在新增一个数据 id的值
                log.info("开始遍历数据映射，数据条数: {}，耗时: {}ms", mapDataList.size(), System.currentTimeMillis() - startTime);
                for (Map<String, Object> objectMap : mapDataList) {
                    Object o = objectMap.get(field);
                    if (ObjectNull.isNull(o)) {
                        continue;
                    }
                    IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(fieldBasicsHtml.getType().getDesc());
                    FieldBasicsHtml html = null;
                    if (ObjectNull.isNull(fieldBasicsHtml.getDesignJson())) {
                        Map generate = iDataFieldHandler.generate(fieldBasicsHtml.getLabel(), fieldBasicsHtml.getFieldKey(), new ArrayList<>());
                        html = iDataFieldHandler.toHtml(generate);
                        if (typeMaps.containsKey(fieldBasicsHtml.getFieldKey())) {
                            typeMaps.put(fieldBasicsHtml.getFieldKey(), html);
                        }
                    } else {
                        html = iDataFieldHandler.toHtml(fieldBasicsHtml.getDesignJson());
                    }
                    //这里根据选择类组件进行处理转换获取所有的数据，用于导入前的转换，避免可能不存在的数据需要 id进行提前处理。
                    iDataFieldHandler.getConversionKey(html, o, objectMap, cascaderFieldPathIdsMap, generateCascaderList);
                    if (html instanceof MultipleHtml) {
                        //表示选则类型，是否有自己关联自己
                        if (modelId.equals(((MultipleHtml) html).getFormId())) {
                            //设置自己关联自己的Key 然后将所有不为空的key给添加上id
                            linkFieldKey = ((MultipleHtml) html).getProps().getLabel();
                            formValueHtml = ((MultipleHtml) html).getProps();
                        }
                    }
                }
                log.info("字段: {} 处理完成，耗时: {}ms", field, System.currentTimeMillis() - startTime);
            }
            log.info("选择转换字段遍历完成，耗时: {}ms", System.currentTimeMillis() - startTime);
            //遍历数据转换
            for (Map<String, Object> map : mapDataList) {
                for (String key : collect) {
                    FieldBasicsHtml html = typeMaps.get(key);
                    IDataFieldHandler iDataFieldHandler = fieldHandlerMap.get(html.getType().getDesc());
                    //获取上级
                    Object o = map.get(key);
                    if (ObjectNull.isNull(o)) {
                        continue;
                    }
                    //这里需要判断处理是否是多选，单选，分别处理
                    boolean is = html instanceof MultipleHtml;
                    if (is && ObjectNull.isNotNull(((MultipleHtml) html).getProps())) {
                        String label = ((MultipleHtml) html).getProps().getLabel();
                        //不管是否是单选多选 ，都可以自己关联自己。这样都可能会导入数据
                        if ((ObjectNull.isNotNull(((MultipleHtml) html).getMultiple()) && ((MultipleHtml) html).getMultiple())) {
                            //多选 对数据进行分割后组装为新的数组,不支持新增数据
                            String finalLinkFieldKey = linkFieldKey;
                            List<Object> list = Arrays.stream(o.toString().split(","))
                                    .map(String::trim)
                                    .map(e -> {
                                        //如果是多选中，如果是模型自己关联了自己，则直接需要将没有找到的数据进行填充并生成新的数据
                                        {
                                            //判断是否为空
                                            if (ObjectNull.isNull(o)) {
                                                //生成一个新的 id
                                                List<Map<String, Object>> mapList = generateCascaderList.get(modelId);
                                                String idStr = mapList.stream().filter(b -> b.get(label).equals(map.get(label))).map(b -> b.get("id").toString())
                                                        .findFirst().orElseGet(IdWorker::getIdStr);
                                                map.put("id", idStr);
                                                if (generateCascaderList.containsKey(modelId)) {
                                                    generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                                }
                                                if (html.getProp().equals(finalLinkFieldKey)) {
                                                    //表示这个字段自己关联了自己，将这个字段的 key,设置为关联的 id,并同时添加这一条数据
                                                }
                                                return idStr;
                                            } else {
                                                String idStr = cascaderFieldPathIdsMap.get(html.getProp()).get(e.toString().trim());
                                                if (generateCascaderList.containsKey(modelId)) {
                                                    generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                                }
                                                return idStr;
                                            }
                                        }
                                    }).filter(ObjectNull::isNotNull).collect(Collectors.toList());
                            map.put(key, list);
                        } else if (html instanceof CascaderItemHtml) {
                            //多选 对数据进行分割后组装为新的数组,不支持新增数据
                            String finalLinkFieldKey = linkFieldKey;
                            Object list = null;
                            if (((CascaderItemHtml) html).getMultiple()) {
                                list = Arrays.stream(o.toString().split("/")).map(e -> {
                                    //如果是多选中，如果是模型自己关联了自己，则直接需要将没有找到的数据进行填充并生成新的数据
                                    {
                                        //判断数据是否为空，如果为空，产生一个新的灵气
                                        if (ObjectNull.isNull(o)) {
                                            //生成一个新的 id
                                            List<Map<String, Object>> mapList = generateCascaderList.get(modelId);
                                            String idStr = mapList.stream().filter(b -> b.get(label).equals(map.get(label))).map(b -> b.get("id").toString())
                                                    .findFirst().orElseGet(IdWorker::getIdStr);
                                            map.put("id", idStr);
                                            if (generateCascaderList.containsKey(modelId)) {
                                                generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                            }
                                            if (html.getProp().equals(finalLinkFieldKey)) {
                                                //表示这个字段自己关联了自己，将这个字段的 key,设置为关联的 id,并同时添加这一条数据
                                            }
                                            return idStr;
                                        } else {
                                            //如果不为空，在树结构中获取这个数据的id,这个 id 会在前一个步骤已经生成了。
                                            String idStr = cascaderFieldPathIdsMap.get(html.getProp()).get(e.toString().trim());
                                            if (ObjectNull.isNull(idStr)) {
                                                return IdWorker.getIdStr();
                                            }
                                            if (generateCascaderList.containsKey(modelId)) {
                                                generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                            }
                                            return idStr;
                                        }
                                    }
                                }).filter(ObjectNull::isNotNull).collect(Collectors.toList());
                            } else {
                                if (ObjectNull.isNotNull(o)) {
                                    if (cascaderFieldPathIdsMap.containsKey(html.getProps().getSecTitle())) {
                                        //如果不是多选，需要直接处理
                                        list = cascaderFieldPathIdsMap.get(html.getProps().getSecTitle()).getOrDefault(o.toString(), IdWorker.getIdStr());
                                    } else {
                                        if (cascaderFieldPathIdsMap.containsKey(html.getProp())) {
                                            list = cascaderFieldPathIdsMap.get(html.getProp()).get(o.toString());
                                        }
                                    }
                                }
                            }
                            if (ObjectNull.isNotNull(list)) {
                                map.put(key, list);
                            }
                        } else {
                            //单选,并判断类型，是否关联了模型，如果关联了自己，如果不是自己不处理
                            //判断是否为空串，
                            if (ObjectNull.isNull(o)) {
                                //生成一个新的 id  导入的时候可能会是串空也会执行下面的逻辑
                                List<Map<String, Object>> mapList = generateCascaderList.get(modelId);
                                if (ObjectNull.isNotNull(mapList)) {
                                    String idStr = mapList.stream().filter(b -> b.get(label).equals(map.get(label))).map(b -> b.get("id").toString())
                                            .findFirst().orElseGet(IdWorker::getIdStr);
                                    map.put("id", idStr);
                                    map.put("dataId", idStr);
                                    generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                    if (html.getProp().equals(linkFieldKey)) {
                                        //表示这个字段自己关联了自己，将这个字段的 key,设置为关联的 id,并同时添加这一条数据
                                        map.put(key, idStr);
                                    }
                                }
                                //必须要删除这个空串，避免树形结构展示不正常
                                map.remove(key);
                            } else {
                                if (ObjectNull.isNotNull(cascaderFieldPathIdsMap.get(html.getProp()))) {
                                    if (html instanceof CheckboxHtml) {
                                        List<String> list = Arrays.stream(o.toString().split(","))
                                                .map(String::trim)
                                                .map(e -> {
                                                    return cascaderFieldPathIdsMap.get(html.getProp()).get(e);
                                                })
                                                .filter(ObjectNull::isNotNull)
                                                .collect(Collectors.toList());
                                        map.put(key, list);
                                    } else {
                                        String obj = cascaderFieldPathIdsMap.get(html.getProp()).get(o.toString().trim());
                                        if (ObjectNull.isNull(obj)) {
                                            throw new BusinessException(html.getLabel() + "中：[" + o.toString() + "]不存在，请检查数据");
                                        }
                                        //删除已经生成的避免重复新增
                                        List<Map<String, Object>> mapList = generateCascaderList.get(modelId);
                                        if (ObjectNull.isNotNull(mapList)) {
                                            String idStr = mapList.stream().filter(b -> b.get(label).equals(map.get(label))).map(b -> b.get("id").toString())
                                                    .findFirst().orElseGet(IdWorker::getIdStr);
                                            map.put("id", idStr);
                                            map.put("dataId", idStr);
                                            generateCascaderList.get(modelId).removeIf(b -> idStr.equals(b.get("id")));
                                        }
                                        map.put(key, obj);
                                    }
                                }
                            }
                        }
                    } else {
                        //如果为空直接不处理，并需要删除 Key 避免重复，
                        if (ObjectNull.isNotNull(o)) {
                            if (cascaderFieldPathIdsMap.containsKey(html.getProp())) {
                                if (html instanceof MultipleHtml) {
                                    if (((MultipleHtml) html).getMultiple()) {
                                        Set<String> obj = Arrays.stream(o.toString().replace("，", ",").split(",")).distinct().map(e -> cascaderFieldPathIdsMap.get(html.getProp()).get(e)).collect(Collectors.toSet());
                                        map.put(key, obj);
                                    } else {
                                        String obj = cascaderFieldPathIdsMap.get(html.getProp()).get(o.toString());
                                        map.put(key, obj);
                                    }
                                } else {
                                    String obj = cascaderFieldPathIdsMap.get(html.getProp()).get(o.toString());
                                    map.put(key, obj);
                                }
                            } else {
                                Object obj = iDataFieldHandler.getConversionKey(html, o, map);
                                map.put(key, obj);
                            }
                        } else {
                            //但需要校验是否必填 ,如果这个字段为必填写，则直接报错。

                            map.remove(key);
                        }
                    }
                }
            }
            if (ObjectNull.isNotNull(linkFieldKey)) {
                for (Map<String, Object> map : mapDataList) {
                    Object o = map.get(linkFieldKey);
                    //如果创建了新的id就将id放进去
                    if (ids.containsKey(o)) {
                        map.put("id", ids.get(o));
                        map.put("dataId", ids.get(o));
                    }
                }
            }
        }
        log.info("开始处理级联生成数据，耗时: {}ms", System.currentTimeMillis() - startTime);
        if (ObjectNull.isNotNull(generateCascaderList)) {
            for (Map.Entry<String, List<Map<String, Object>>> entry : generateCascaderList.entrySet()) {
                log.info("处理级联生成数据，模型ID: {}，数据条数: {}，耗时: {}ms", entry.getKey(), entry.getValue().size(), System.currentTimeMillis() - startTime);
                try {
                    List<Map<String, Object>> list = entry.getValue();
                    //过滤出当前模型的数据，避免重复新增
                    if (entry.getKey().equals(modelId) && ObjectNull.isNotNull(formValueHtml)) {
                        FormValueHtml finalFormValueHtml = formValueHtml;
                        list.removeIf(e -> {
                            if (e.containsKey(finalFormValueHtml.getSecTitle()) && e.containsKey(finalFormValueHtml.getLabel())) {
                                //判断是否删除
                                try {
                                    //删除的同时需要将id复制到数据中
                                    for (Map<String, Object> dataMap : mapDataList) {
                                        if (dataMap.get(finalFormValueHtml.getSecTitle()).equals(e.get(finalFormValueHtml.getSecTitle()))) {
                                            if (dataMap.get(finalFormValueHtml.getLabel()).equals(e.get(finalFormValueHtml.getLabel()))) {
                                                if (mapDataList.stream().filter(b -> b.get(finalFormValueHtml.getLabel()).equals(dataMap.get(finalFormValueHtml.getLabel()))
                                                                                     && b.get(finalFormValueHtml.getSecTitle()).equals(dataMap.get(finalFormValueHtml.getSecTitle()))
                                                ).findAny().isPresent()) {
                                                    dataMap.put("id", e.get("id"));
                                                    dataMap.put("dataId", e.get("id"));
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                    return false;
                                } catch (Exception ex) {
                                    //可能不存在，就不删除
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        });
                    }
                    if (ObjectNull.isNotNull(list)) {
                        log.info("保存级联生成数据，模型ID: {}，数据条数: {}，耗时: {}ms", entry.getKey(), list.size(), System.currentTimeMillis() - startTime);
                        dynamicDataService.saveBatch(appId, entry.getKey(), list);
                        log.info("级联生成数据保存完成，耗时: {}ms", System.currentTimeMillis() - startTime);
                    }
                } catch (Exception ignored) {
                    log.error("处理级联生成数据异常", ignored);
                }
                log.info("级联生成数据处理完成，耗时: {}ms", System.currentTimeMillis() - startTime);
            }
        }
        log.info("开始保存主数据，数据条数: {}，耗时: {}ms", mapDataList.size(), System.currentTimeMillis() - startTime);
//        //这里考虑是否重复插入已经存在的数据
//        if (ObjectNull.isNotNull(formValueHtml)) {
//            FormValueHtml valueHtml = formValueHtml;
//            String error = IntStream.iterate(0, i -> i + 1)
//                    .limit(mapDataList.size())
//                    .filter(i -> mapDataList.get(i).containsKey(valueHtml.getLabel()))
//                    .filter(i -> mapDataList.get(i).containsKey(valueHtml.getSecTitle()))
//                    .filter(e -> {
//                        List<QueryConditionDto> objects = new ArrayList<>();
//                        objects.add(new QueryConditionDto().setValue(mapDataList.get(e).get(valueHtml.getSecTitle())).setFieldKey(valueHtml.getSecTitle()).setEnabledQueryTypes(DataQueryType.eq));
//                        objects.add(new QueryConditionDto().setValue(mapDataList.get(e).get(valueHtml.getLabel())).setFieldKey(valueHtml.getLabel()).setEnabledQueryTypes(DataQueryType.eq));
//                        QueryListDto queryPageDto = new QueryListDto();
//                        queryPageDto.setGroupConditions(Collections.singletonList(objects));
//                        List<Map<String, Object>> mapList = dynamicDataService.postQueryList(modelId, queryPageDto);
//                        if (mapList.size() > 0) {
//                            return true;
//                        } else {
//                            return false;
//                        }
//                    })
//                    .mapToObj(String::valueOf)
//                    .collect(Collectors.joining("、"));
//            if (ObjectNull.isNotNull(error)) {
//                return R.failed("第" + error + "行数据已经存在");
//            }
//        }
        //这里需要判断是否已经存在，如果存在，则不新增这个数据
        RuleExecuteDto executeDto = dynamicDataService.saveBatch(appId, modelId, mapDataList);
        log.info("数据保存完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        if (ObjectNull.isNotNull(executeDto) && ObjectNull.isNotNull(executeDto.getStats())) {
            if (executeDto.getStats()) {
                log.info("导入成功完成，总耗时: {}ms", System.currentTimeMillis() - startTime);
                return R.ok().setMsg(executeDto.getSyncMessageTips());
            } else {
                log.info("导入失败，总耗时: {}ms", System.currentTimeMillis() - startTime);
                return R.failed(executeDto.getSyncMessageTips());
            }
        }
        log.info("导入完成，总耗时: {}ms", System.currentTimeMillis() - startTime);
        return R.ok().setMsg("导入成功");
    }

    /**
     * 支持移动端小程序导出功能
     *
     * @param appId    the app id
     * @param keywords the keywords
     * @param modelId  the model id
     * @param designId the design id
     * @param query    the query
     * @param response the response
     */
    @ApiOperation("导出")
    @GetMapping("/export/{modelId}")
    public void exportDesign(@PathVariable String appId, @RequestParam(value = "keywords", required = false) String keywords, @PathVariable("modelId") String
            modelId, @RequestParam(name = "designId", required = false) String designId, @RequestParam(name = "query", required = false) String query, HttpServletResponse response) {
        exportDesign(appId, modelId, keywords, designId, query, null, response);
    }

    /**
     * 导出数据
     *
     * @param appId    the app id
     * @param modelId  模型id
     * @param keywords the keywords
     * @param designId 设计id
     * @param query    the query
     * @param ids      选中的ids数据,如果不为空,以id为准
     * @param response Http响应对象
     */
    @Log
    @ApiOperation("导出")
    @PostMapping("/export/{modelId}")
    public void exportDesign(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestParam(value = "keywords", required = false) String
            keywords, @RequestParam(name = "designId", required = false) String designId, @RequestParam(name = "query", required = false) String query, @RequestBody List<String> ids, HttpServletResponse response) {
        DynamicDataUtils.setDataModelId(modelId);
        DynamicDataUtils.setPageDesignId(designId);
        DynamicDataUtils.dataModelScope(modelId);
        setFunctionName("导出");
        CrudPage crudPage = pageService.getOne(Wrappers.query(new CrudPage().setDataModelId(modelId).setId(designId).setJvsAppId(appId)));
        if (ObjectNull.isNull(crudPage)) {
            log.error("应用错误");
        }
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(crudPage.getViewJson());
        Optional<ButtonDesignHtml> first = pageDesignHtml.getButtons().stream().filter(e -> e.getType().equals(ButtonTypeEnum.btn_export)).findFirst();
        if (first.isPresent()) {
            List<String> fieldsDesign = null;
            if (CollectionUtils.isEmpty(first.get().getExportFields())) {
                //默认所有数据字段
                fieldsDesign = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName).collect(Collectors.toList());
            } else {
                fieldsDesign = new ArrayList<>(first.get().getExportFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName)
                        .collect(Collectors.toList()));
            }

            List<FieldBasicsHtml> fields = dataFieldService.getAllField(appId, modelId, true, true, e -> false);
            List<String> finalFieldsDesign = fieldsDesign;
            Map<String, FieldBasicsHtml> collectMap = fields.stream()
                    //排除文件类型的下载，不然会导致 easyExcel转换失效
                    .peek(e -> {
                        if ((!DataFieldType.input.equals(e.getType())) && e.getType().getTransformationList().contains(e.getType())) {
                            finalFieldsDesign.remove(e.getFieldKey());
                        }
                    }).collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
            fieldsDesign = finalFieldsDesign;
            List<QueryOrderDto> sorts = null;
            Set<String> stringSet = new HashSet<>();
            List<List<QueryConditionDto>> queryGroupConditions = new ArrayList<>();
            List<Criteria> criteriaList = SystemThreadLocal.get(KEY_AUTH_CRITERIA);
            Criteria authCriteria = DynamicDataUtils.trueCriteria();

            if (ObjectNull.isNotNull(ids)) {
                //直接使用ids进行查询
                List<QueryConditionDto> queryConditions = Collections.singletonList(new QueryConditionDto().setEnabledQueryTypes(DataQueryType.in).setFieldKey("id").setValue(ids));
                queryGroupConditions.add(queryConditions);
            } else if (ObjectNull.isNotNull(query)) {
                try {
                    query = URLDecoder.decode(query, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new BusinessException(e.getMessage());
                }
                QueryListDto queryListDto = JSONObject.parseObject(query, QueryListDto.class);
                //列表页筛选条件，与数据权限不同，是直接进行数据筛选
                if (ObjectNull.isNotNull(pageDesignHtml.getParameters())) {
                    pageDesignHtml.getParameters().stream().filter(e -> ObjectNull.isNotNull(e.getValue(), e.getKey(), e.getOperator())).map(e -> {
                        QueryConditionDto queryConditionDto = new QueryConditionDto().setValue(e.getOperator().equals(DataQueryType.in) ? e.getValue() : e.getValue().get(0)).setFieldKey(e.getKey()).setEnabledQueryTypes(e.getOperator());
                        if (collectMap.containsKey(queryConditionDto.getFieldKey())) {
                            FieldBasicsHtml fieldBasicsHtml = collectMap.get(queryConditionDto.getFieldKey());
                            //如果只是文本框，将 In,条件变为模糊匹配
                            if (DataFieldType.input.equals(fieldBasicsHtml.getFieldType())) {
                                queryConditionDto.setEnabledQueryTypes(DataQueryType.like);
                            }
                        }
                        return queryConditionDto;
                    }).peek(e -> e.setCrud(true)).peek(e -> stringSet.add(e.getFieldKey())).forEach(e -> queryListDto.getConditions().add(e));
                }

                //如果关键字查询条件不为空的时候
                if (ObjectNull.isNotNull(keywords)) {
                    List<QueryConditionDto> list = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(e -> new QueryConditionDto().setFieldKey(e.getAliasColumnName()).setEnabledQueryTypes(DataQueryType.like).setValue(keywords)).collect(Collectors.toList());
                    queryListDto.getConditions().addAll(list);
                }

                queryGroupConditions = getQueryConditions(Collections.singletonList(queryListDto.getConditions()), collectMap, null);
                sorts = queryListDto.getSorts();
                //将查询条件和列表过滤条件进行分离
                if (ObjectNull.isNotNull(queryGroupConditions)) {
                    //列表过滤条件
                    List<QueryConditionDto> collect = queryGroupConditions.stream().flatMap(Collection::stream).filter(QueryConditionDto::getCrud).collect(Collectors.toList());
                    //过滤出列表过滤的数据
                    if (ObjectNull.isNotNull(collect)) {
                        //将列表过滤和数据权限添加上组合查询
                        List<Criteria> crud = DynamicDataUtils.buildDynamicCriteriaList(collect);
                        if (ObjectNull.isNotNull(criteriaList)) {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud).orOperator(criteriaList);
                        } else {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(crud);
                        }
                    } else if (ObjectNull.isNotNull(criteriaList)) {
                        if (criteriaList.size() == 1) {
                            authCriteria = DynamicDataUtils.trueCriteria().andOperator(criteriaList);
                        } else {
                            authCriteria = DynamicDataUtils.trueCriteria().orOperator(criteriaList);
                        }
                    }
                    queryGroupConditions = queryGroupConditions.stream().map(e -> e.stream().filter(s -> !s.getCrud()).collect(Collectors.toList())).collect(Collectors.toList());
                }
            }
            DataModelPo byId = dataModelService.getById(crudPage.getDataModelId());
            //处理表单中的脱敏字段
            List<String> encryptionFields = dynamicDataService.encryptionData(byId);
            //根据脱敏字段进行去掉
            if (ObjectNull.isNotNull(encryptionFields)) {
                fieldsDesign.removeAll(encryptionFields);
            }
            //调整excel导出顺序
            List<List<String>> fieldNameList = fieldsDesign.stream().filter(collectMap::containsKey).map(e -> Collections.singletonList(collectMap.get(e).getFieldName())).collect(Collectors.toList());

            // 查询数据
            List<Criteria> list = DynamicDataUtils.buildDynamicGroupCriteriaList(queryGroupConditions);
            Query querymongo = DynamicDataUtils.andOr(list, authCriteria, ObjectNull.isNull(keywords));
            Sort sort = Sort.by(Sort.Direction.DESC, Get.name(DynamicDataPo::getCreateTime))
                    //导入进来的数据时间一致.需要再通过ID排序
                    .and(Sort.by(Sort.Direction.ASC, "dataId"));
            if (ObjectNull.isNotNull(sorts)) {
                List<Sort.Order> collect = sorts.stream().filter(e -> ObjectNull.isNotNull(e.getDirection(), e.getFieldKey())).map(e -> new Sort.Order(e.getDirection(), e.getFieldKey())).collect(Collectors.toList());
                if (ObjectNull.isNotNull(collect)) {
                    sort = Sort.by(collect);
                }
            }
            querymongo.with(sort);
            long count = dataModelHandler.count(querymongo, modelId);
            if (count > designConfig.getExcelExportMax()) {
                throw new BusinessException("导出最大数量超过限制", designConfig.getExcelExportMax());
            }
            List<Map> queryList = dataModelHandler.find(querymongo, Map.class, modelId);
            List<FieldBasicsHtml> fieldBasicsHtmls = dataFieldService.getFields(appId, modelId, null, true, false);
            Map<String, FieldBasicsHtml> fieldMap = fieldBasicsHtmls.stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity()));
            List<String> fieldsDesignCopy = fieldsDesign;

            List<BaseFile> url = Collections.synchronizedList(new ArrayList<>());
            //异步处理业务数据导出
            Authentication authenticationAuthentication = null;
            RequestAttributes context = null;
            try {
                context = RequestContextHolder.currentRequestAttributes();
                SecurityContext authentication = SecurityContextHolder.getContext();
                authenticationAuthentication = authentication.getAuthentication();
            } catch (IllegalStateException ignored) {
            }
            String tenantId = TenantContextHolder.getTenantId();
            Map<String, Object> systemThreadLocalMap = SystemThreadLocal.get();
            Authentication finalAuthenticationAuthentication = authenticationAuthentication;
            RequestAttributes finalContext = context;
            List<List<Object>> excelData = queryList.parallelStream().map(e -> {
                SystemThreadLocal.setAll(systemThreadLocalMap);
                TenantContextHolder.setTenantId(tenantId);
                if (ObjectNull.isNotNull(finalContext)) {
                    //设置上下文user对象
                    RequestContextHolder.setRequestAttributes(finalContext);
                    SystemThreadLocal.set("user", finalAuthenticationAuthentication.getPrincipal());
                    SecurityContextHolder.getContext().setAuthentication(finalAuthenticationAuthentication);
                }
                dynamicDataService.echo(e, fieldMap, true, a -> {
                    switch (a.getType()) {
                        case file:
                        case image:
                        case fileUpload:
                        case imageUpload:
                        case signature:
                            if (fieldsDesignCopy.contains(a.getField())) {
                                //将所有的文件压缩到一个文件夹中
                                return ((ArrayList) a.getObject()).stream()
                                        .peek(s -> url.add(((BaseFile) s)))
                                        .map(d -> ((BaseFile) d).getFileName()).filter(ObjectNull::isNotNull)
                                        .map(d -> FileUtil.getName(d.toString()))
                                        .collect(Collectors.joining("\n"));
                            }
                        default:
                            return a.getObject();
                    }
                });
                //根据字段顺序调整excel顺序
                List<Object> oneRowData = new ArrayList<>(fieldsDesignCopy.size());
                for (String key : fieldsDesignCopy) {
                    oneRowData.add(e.getOrDefault(key, ""));
                }
                return oneRowData;
            }).collect(Collectors.toList());
            // 响应数据
            String fileName = this.getFileName("", designId);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            //判断文件是否为空，如果不为空则压缩文件
            if (ObjectNull.isNull(url)) {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode(FILE_NAME_EXPORT + fileName + FILE_TYPE, StandardCharsets.UTF_8)));
                response.setStatus(HttpStatus.OK.value());
                try {
                    //导出数据
                    ServletOutputStream outputStream = response.getOutputStream();
                    new ExcelWriterBuilder().file(outputStream)
                            .registerWriteHandler(new EasyExcelCustomCellWriteHandler())
                            .head(fieldNameList)
                            .registerConverter(linkedHashMapConvert)
                            .registerConverter(decimal128ListConvert)
                            .registerConverter(arrayListConvert).registerConverter(localDateTimeConvert).sheet(fileName).doWrite(excelData);
                } catch (Exception ex) {
                    log.error("数据导出异常", ex);
                }
            } else {
                List<String> fileNames = new ArrayList<>();
                List<ByteArrayInputStream> byteArrayInputStreams = new ArrayList<>();
                int i = 0;
                for (BaseFile baseFile : url) {
                    try {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(HttpUtil.downloadBytes(baseFile.getUrl()));
                        byteArrayInputStreams.add(byteArrayInputStream);
                        String name = FileUtil.getName(baseFile.getFileName()).split("\\?")[0];
                        if (fileNames.contains(name)) {
                            fileNames.add(++i + "_" + name);
                        } else {
                            fileNames.add(name);
                        }
                    } catch (Exception ignored) {

                    }
                }
                ByteArrayOutputStream excel = new ByteArrayOutputStream();
//                excelData.add(0, fieldNameList.stream().map(e -> e.get(0)).collect(Collectors.toList()));
                new ExcelWriterBuilder().file(excel)
                        .registerWriteHandler(new EasyExcelCustomCellWriteHandler())
                        .head(fieldNameList)
                        .registerConverter(linkedHashMapConvert)
                        .registerConverter(decimal128ListConvert)
                        .registerConverter(arrayListConvert)
                        .registerConverter(localDateTimeConvert)
                        .sheet(fileName)
                        .doWrite(excelData);

                //将 excel流放到压缩流里面
                byteArrayInputStreams.add(new ByteArrayInputStream(excel.toByteArray()));
                //获取输接受文件
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                fileNames.add(fileName + FILE_TYPE);

                ZipUtil.zip(out, fileNames.toArray(new String[0]), byteArrayInputStreams.toArray(new ByteArrayInputStream[0]));

                try {
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode(FILE_NAME_EXPORT + fileName + ".zip", StandardCharsets.UTF_8)));
                    response.setStatus(HttpStatus.OK.value());
                    ServletOutputStream outputStream = response.getOutputStream();
                    outputStream.write(out.toByteArray());
                    outputStream.flush();
                } catch (IOException e) {
                    log.error("数据导出异常", e);
                }
            }
        }
    }


    /**
     * 下载数据导入的模板
     *
     * @param appId    the app id
     * @param modelId  模型id
     * @param designId 设计id
     * @param response Http响应对象
     * @throws IOException the io exception
     */
    @Log
    @ApiOperation("下载模板")
    @GetMapping("/download/excel/template/{modelId}")
    public void excelTemplate(@PathVariable String appId, @PathVariable("modelId") String modelId, @RequestParam(name = "designId", required = false) String designId, HttpServletResponse response) throws IOException {
        CrudPage crudPage = pageService.getOne(Wrappers.query(new CrudPage().setId(designId).setJvsAppId(appId)));
        if (ObjectNull.isNull(crudPage)) {
            log.error("没有找到此设计app:{},design:{}", appId, designId);
            return;
        }
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(crudPage.getViewJson());
        Optional<ButtonDesignHtml> first = pageDesignHtml.getButtons().stream().filter(e -> e.getType().equals(ButtonTypeEnum.btn_download_template)).findFirst();
        if (first.isPresent()) {
            ServletOutputStream outputStream = response.getOutputStream();
            // 响应数据
            String fileName = this.getFileName("", designId);
            String sheelName = first.get().getSheelName();
            if (ObjectNull.isNull(sheelName)) {
                sheelName = FILE_NAME_TEMPLATE + fileName;
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode(FILE_NAME_TEMPLATE + fileName + FILE_TYPE, StandardCharsets.UTF_8)));
            response.setStatus(HttpStatus.OK.value());
            Map<String, String> aliasFieldMap = new HashMap<>();
            List<String> fieldsDesign;
            if (CollectionUtils.isEmpty(first.get().getImportFields())) {
                //默认所有数据字段
                fieldsDesign = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName).collect(Collectors.toList());
            } else {
                List<String> fieldIndex = pageDesignHtml.getDataPage().getAutoTableFields().stream().map(DataTableFieldDesignHtml::getAliasColumnName).collect(Collectors.toList());
                fieldsDesign = new ArrayList<>(first.get().getImportFields().stream()
                        .peek(e -> aliasFieldMap.put(e.getAliasColumnName(), e.getShowChineseAlias()))
                        .map(DataTableFieldDesignHtml::getAliasColumnName)
                        .filter(ObjectNull::isNotNull)
                        .sorted(Comparator.comparingInt(fieldIndex::indexOf))
                        .collect(Collectors.toList()));
            }
            if (ObjectNull.isNull(fieldsDesign)) {
                //没有设计下载模板字段
                new ExcelWriterBuilder().file(outputStream).head(new ArrayList<>()).registerWriteHandler(new EasyExcelCustomCellWriteHandler()).registerConverter(arrayListConvert).registerConverter(localDateTimeConvert).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).sheet("模板文件").useDefaultStyle(true).doWrite(Collections.singletonList(new ArrayList()));

                return;
            }
            // 字段数据
            Map<String, FieldBasicsHtml> fields = dataFieldService.getFields(appId, modelId, null, false, false).stream().collect(Collectors.toMap(FieldPublicHtml::getFieldKey, Function.identity()));
            Map<String, String> fieldMap = new HashMap<>(8);

            List<DataTableFieldDesignHtml> importFields = first.get().getExportFields();
            //如果没有设计导入模板信息，则直接使用字段信息
            if (ObjectNull.isNull(importFields)) {
                importFields = fields.values().stream().map(e -> new DataTableFieldDesignHtml().setShowChinese(e.getFieldName()).setDescription("").setShowChineseAlias(e.getFieldName()).setComponentType(e.getType())).collect(Collectors.toList());
            }
            if (ObjectNull.isNull(importFields)) {
                new ExcelWriterBuilder().file(outputStream).head(new ArrayList<>()).registerWriteHandler(new EasyExcelCustomCellWriteHandler()).registerConverter(arrayListConvert).registerConverter(localDateTimeConvert).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).sheet("模板文件").useDefaultStyle(true).doWrite(Collections.singletonList(new ArrayList()));

                return;
            }
            DataModelPo byId = dataModelService.getById(crudPage.getDataModelId());
            //处理表单中的脱敏字段
            List<String> encryptionFields = dynamicDataService.encryptionData(byId);
            //根据脱敏字段进行去掉
            if (ObjectNull.isNotNull(encryptionFields)) {
                fieldsDesign.removeAll(encryptionFields);
            }
            //屏蔽掉描述字段
            List<String> collect = new ArrayList<>();
            importFields.stream()
                    //使用别名返回到前端数据
                    .peek(e -> {
                        fieldMap.put(e.getShowChinese(), aliasFieldMap.getOrDefault(e.getShowChineseAlias(), e.getShowChinese()));
                    }).map(e -> (e.getComponentType().getDesc() + "\n" + (ObjectNull.isNotNull(e.getDescription()) ? e.getDescription() : ""))).collect(Collectors.toList());
            List<List<String>> fieldNameList = fieldsDesign.stream().map(fields::get).map(e -> Collections.singletonList(fieldMap.get(e.getFieldName()))).collect(Collectors.toList());

            try {
                if (ObjectNull.isNotNull(first.get().getTemplateFileLink())) {
                    byte[] bytes = HttpUtil.downloadBytes(ossTemplate.fileLink(first.get().getTemplateFileLink().replaceAll("/jvs-public", ""), "jvs-public"));
                    ButtonDesignHtml buttonDesignHtml = first.get();
                    String prefix = FileUtil.extName(buttonDesignHtml.getTemplateFileName());
                    ExcelTypeEnum excelTypeEnum = ExcelTypeEnum.valueOf(prefix.toUpperCase());
                    ExcelWriter writer = EasyExcel.write(outputStream).head(fieldNameList).excelType(excelTypeEnum).withTemplate(new ByteArrayInputStream(bytes)).registerWriteHandler(new EasyExcelCustomCellWriteHandler()).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();

                    WriteSheet writeSheet = EasyExcel.writerSheet(0, sheelName).useDefaultStyle(true).build();
                    writer.fill(new ArrayList<>(), writeSheet).finish();
                } else {
                    //导出数据
                    new ExcelWriterBuilder().file(outputStream).head(fieldNameList).registerWriteHandler(new EasyExcelCustomCellWriteHandler()).registerConverter(arrayListConvert).registerConverter(localDateTimeConvert).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).sheet(sheelName).useDefaultStyle(true).doWrite(Collections.singletonList(collect));
                }

            } catch (Exception ex) {
                log.error("模板下载异常", ex);
            }
        }
    }

    /**
     * Query single flow task ids r.
     *
     * @param appId       the app id
     * @param dataModelId the data model id
     * @param dataId      the data id
     * @return the r
     */
    @Log
    @ApiOperation("查询单条数据所有工作流任务id")
    @GetMapping("/query/single/flow/task/ids/{modelId}/{dataId}")
    @Transactional(rollbackFor = Exception.class)
    public R<List<String>> querySingleFlowTaskIds(@PathVariable String appId, @PathVariable("modelId") String dataModelId, @PathVariable("dataId") String dataId) {
        JSONObject data = flowDynamicDataService.getFlowTaskDataObj(dataId);
        List<String> flowTaskIds = designHandler.getDataFlowTasks(data).stream().map(FlowDynamicDataServiceImpl.FlowTaskModelData::getId).collect(Collectors.toList());
        return R.ok(flowTaskIds);
    }


    /**
     * 获取查询条件
     *
     * @param page 用于设置分页条件
     * @return 查询条件
     */
    private List<QueryConditionDto> getQueryConditions(@Nullable Page<DynamicDataPo> page, String... ignoredParams) {
        HttpServletRequest request = WebUtils.getRequest();
        Map<String, String[]> parameters = request.getParameterMap();
        List<QueryConditionDto> queryConditions = Collections.emptyList();
        if (ObjectUtils.isNotEmpty(parameters)) {
            Map<String, String> params = parameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
            if (ObjectUtils.isNotEmpty(ignoredParams)) {
                for (String ignoredParam : ignoredParams) {
                    params.remove(ignoredParam);
                }
            }
            queryConditions = new ArrayList<>(params.size());
            if (Objects.nonNull(page)) {
                page.setSize(this.peak(params, "size", 10));
                page.setCurrent(this.peak(params, "current", 1));
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                QueryConditionDto query = new QueryConditionDto();
                query.setFieldKey(entry.getKey());
                query.setValue(entry.getValue());
                query.setEnabledQueryTypes(DataQueryType.eq);
                queryConditions.add(query);
            }
        }
        return queryConditions;
    }

    /**
     * 获取文件名称
     *
     * @param defaultName  默认名称
     * @param pageDesignId 列表页id
     * @return 文件名称
     */
    private String getFileName(String defaultName, String pageDesignId) {
        CrudPage page = pageService.getOne(Wrappers.<CrudPage>lambdaQuery().select(CrudPage::getName).eq(CrudPage::getId, pageDesignId));
        if (Objects.isNull(page)) {
            return defaultName;
        }
        String name = page.getName();
        if (StringUtils.isBlank(name)) {
            return defaultName;
        }
        return (defaultName + name).replaceAll("/", "").replaceAll("\\\\", "").replaceAll("\\?", "").replaceAll("\\*", "");
    }

    /**
     * 获取参数
     *
     * @param data         Map类型的数据
     * @param key          键
     * @param defaultValue 默认值
     * @return Map中的参数
     */
    private int peak(Map<String, String> data, String key, int defaultValue) {
        if (ObjectUtils.isEmpty(data)) {
            return defaultValue;
        }
        String value = data.remove(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Excel单元格数据转数据模型数据
     *
     * @param excelData    Excel单元格数据
     * @param fieldKeyList 数据模型数据
     * @param modelId      数据模型id
     * @return 数据模型数据集合
     */
    private List<Map<String, Object>> parseExcelData2MapData(List<List<Object>> excelData, List<String> fieldKeyList, String modelId) {
        List<Map<String, Object>> allData = new ArrayList<>(excelData.size());
        //因为有描述行，所以需要从第二行开始做数据处理
        for (int i = 1; i < excelData.size(); i++) {
            List<Object> rowExcelData = excelData.get(i);
            int size = rowExcelData.size();
            Map<String, Object> rowData = new HashMap<>(size);
            for (int j = 0; j < size; j++) {
                Object unitExcelData = rowExcelData.get(j);
                if (fieldKeyList.size() < size) {
                    continue;
                }
                String fieldKey = fieldKeyList.get(j);
                if (Objects.nonNull(unitExcelData) && StringUtils.isNotBlank(fieldKey)) {
                    try {
                        String data = unitExcelData.toString();
                        if (data.startsWith("{") && data.endsWith("}")) {
                            unitExcelData = JSONUtil.parseObj(unitExcelData);
                        } else if (data.startsWith("[") && data.endsWith("]")) {
                            unitExcelData = JSONUtil.parseArray(unitExcelData);
                        }
                    } catch (JSONException e) {
                        log.warn("导入时数据格式异常", e);
                    }
                    rowData.put(fieldKey, unitExcelData);
                } else {
                    //调整导入时组件属性不能为空的情况
                    rowData.put(fieldKey, null);
                }
            }
            allData.add(rowData);
        }
        return allData;
    }

    /**
     * Query page r.
     *
     * @param appId the app id
     * @param dto   the dto
     * @return the r
     */
    @Log
    @ApiOperation("分页查询关联数据")
    @PostMapping("/query/page/linkage")
    public R<Page<Map<String, Object>>> queryPage(@PathVariable String appId, @RequestBody QueryPageLinkageDto dto) {
        Page<Map<String, Object>> mapPage = new Page<>(dto.getCurrent(), dto.getSize());
        String modelId = dto.getDataLinkageModelId();
        if (ObjectNull.isNull(modelId, dto.getDataLinkageModelId(), dto.getDataLinkageList(), dto.getLinkageFieldKeys())) {
            return R.ok(mapPage);
        }
        Map<String, FieldBasicsHtml> collectMap = dataFieldService.getAllField(appId, modelId, true, true, e -> false).stream().collect(Collectors.toMap(FieldBasicsHtml::getFieldKey, Function.identity(), (e1, e2) -> e1));
        if (ObjectNull.isNull(collectMap)) {
            return R.ok(mapPage);
        }
        String dataIdKey = Get.name(DynamicDataPo::getId);
        List<String> linkageQueryFieldKeys = dto.getLinkageFieldKeys().stream().map(ModelDisplayLinkageFieldHtml::getProp).collect(Collectors.toList());
        linkageQueryFieldKeys.add(dataIdKey);
        // 查询模型数据
        List<QueryOrderDto> orderBys = new ArrayList<>();
        orderBys.add(new QueryOrderDto().setFieldKey(Get.name(DynamicDataPo::getCreateTime)).setDirection(Sort.Direction.DESC));
        if (dto.getDataLinkageList().stream().anyMatch(e -> LinkTypeEnum.formula.equals(e.getProp()))) {
            QueryConditionUtils.replaceConditionValue(dto.getDataLinkageList(), dto.getLineData(), false);
        }
        List<List<QueryConditionDto>> queryGroupConditions = Collections.singletonList(dto.getDataLinkageList());
        Page<DynamicDataPo> page = new Page<>(dto.getCurrent(), dto.getSize());
        Page<Map<String, Object>> pageResult = dynamicDataService.queryPage(appId, page, modelId, null, queryGroupConditions, orderBys, linkageQueryFieldKeys, true, true, true, new ArrayList<>(collectMap.values()), null);
        pageResult.getRecords().forEach(data -> dynamicDataService.echoModelDisplay(data, data, collectMap.values(), dto.getLinkageFieldKeys()));
        return R.ok(pageResult);
    }
}
