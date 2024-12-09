package cn.bctools.design.component;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.enums.DataConditionType;
import cn.bctools.design.data.fields.dto.form.html.DatePickerHtml;
import cn.bctools.design.data.fields.dto.form.html.InputNumberHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.fields.impl.basic.DatePickerFieldHandler;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.use.api.DataModelApi;
import cn.bctools.design.use.api.dto.DataFiledDto;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.DataModelSearchDto;
import cn.bctools.design.use.api.enums.DataFieldTypeDataEnum;
import cn.bctools.design.use.api.enums.DataFieldTypeEnum;
import cn.bctools.design.use.api.enums.DataModelQueryType;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;
import static cn.hutool.core.date.DatePattern.NORM_DATE_PATTERN;

/**
 * 数据模型
 *
 * @Author: GuoZi
 */
@Slf4j
@RestController
@Api(tags = "[Feign]数据模型")
public class DataModelComponent implements DataModelApi {
    @Autowired
    DataModelService dataModelService;
    @Autowired
    DataFieldService dataFieldService;
    @Autowired
    JvsAppService appService;
    @Autowired
    DataModelHandler dataModelHandler;
    @Autowired
    Map<String, IDataFieldHandler> handlerMap;

    @Override
    public R<Long> getCount(String dataModelId, String mode) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        Query query = new Query(Criteria.where(DynamicDataService.MONGO_ID).exists(true).andOperator(Criteria.where("delFlag").is(false)));
        long count = dataModelHandler.count(query, Map.class, dataModelId);
        return R.ok(count);
    }

    @Override
    public R<List> list(String dataModelId, long size, long current, String mode) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        Query query = new Query(Criteria.where(DynamicDataService.MONGO_ID).exists(true).andOperator(Criteria.where("delFlag").is(false)));
        //这里的数量如果为0 就表示获取全部数据
        if (size > BigDecimal.ROUND_UP) {
            long skip = size * (current - 1);
            query.skip(skip).limit((int) size);
        }
        List mapList = dataModelHandler.find(query, Map.class, dataModelId);
        return R.ok(mapList);
    }

    @Override
    public R<List> search(DataModelSearchDto searchDto) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(searchDto.getMode())));
        Criteria criteria = buildQuery(searchDto);
        Query query = new Query(criteria);
        //这里的数量如果为0 就表示获取全部数据
        if (searchDto.getSize() > BigDecimal.ROUND_UP) {
            long skip = searchDto.getSize() * (searchDto.getCurrent() - 1);
            query.skip(skip).limit((int) searchDto.getSize());
        }
        List mapList = dataModelHandler.find(query, Map.class, searchDto.getId());
        return R.ok(mapList);
    }

    @Override
    public R<Long> countBySearch(DataModelSearchDto searchDto) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(searchDto.getMode())));

        Criteria criteria = buildQuery(searchDto);
        Query query = new Query(criteria);
        long count = dataModelHandler.count(query, Map.class, searchDto.getId());
        return R.ok(count);
    }

    @Override
    public R<List<DataFiledDto>> fieldMapData(String appId, String dataModelId, String mode) {
        log.info("获取数据应用的信息,{},{},{}", appId, dataModelId, mode);
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        List<DataFiledDto> collect = dataFieldService.getAllField(appId, dataModelId).stream()
                .filter(e -> !DesignType.data.equals(e.getDesignType()))
                .map(e -> {
                    DataFiledDto dataFiledDto =
                            new DataFiledDto().setDataType(DataFieldTypeDataEnum.STRING).setCls(e.getFieldType().getAClass()).setColumnCount(e.getFieldName()).setColumnName(e.getFieldKey()).setModelId(e.getModelId());
                    //此处需要对类型进行转换 bi 的特殊类型处理
                    switch (e.getFieldType()) {
                        case SWITCH:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.BOOLEAN);
                            return dataFiledDto;
                        case inputNumber:
                            //如果是数字，需要明确整数位数，和小数位数
                            InputNumberHtml html = (InputNumberHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            dataFiledDto.setFormat("38," + html.getPrecision());
                            DataFieldTypeDataEnum fieldType = DataFieldTypeDataEnum.INT;
                            if (Optional.ofNullable(html.getPrecision()).orElseGet(() -> 0) >= 1) {
                                fieldType = DataFieldTypeDataEnum.DECIMAL;
                            }
                            dataFiledDto.setLength(38);
                            dataFiledDto.setPrecision(html.getPrecision());
                            dataFiledDto.setDataType(fieldType);
                            return dataFiledDto;
                        case datePicker:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.DATETIME);
                            if (ObjectNull.isNull(e.getDesignJson())) {
                                dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                return dataFiledDto;
                            }
                            dataFiledDto.setPrecision(0);
                            DatePickerHtml datePickerHtml = (DatePickerHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            DatePickerFieldHandler.DateType dateType = datePickerHtml.getDatetype();
                            if (ObjectNull.isNull(dateType)) {
                                return dataFiledDto;
                            }
                            switch (dateType) {
                                case date:
                                    dataFiledDto.setFormat(NORM_DATE_PATTERN);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.DATE);
                                    return dataFiledDto;
                                case datetime:
                                    dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.DATETIME);
                                    return dataFiledDto;
                                default:
                                    dataFiledDto.setCls(String.class);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.STRING);
                                    return dataFiledDto;
                            }
                        case timeSelect:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.STRING);
                        case timePicker:
                            dataFiledDto.setCls(String.class);
                            return dataFiledDto;
                    }
                    return dataFiledDto;
                })
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    public R<List<DataFiledDto>> fieldMap(String appId, String dataModelId, String mode) {
        log.info("获取数据应用的信息,{},{},{}", appId, dataModelId, mode);
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        List<DataFiledDto> collect = dataFieldService.getAllField(appId, dataModelId).stream()
                .filter(e -> !DesignType.data.equals(e.getDesignType()))
                .map(e -> {
                    DataFiledDto dataFiledDto =
                            new DataFiledDto().setType(DataFieldTypeEnum.字符串).setCls(e.getFieldType().getAClass()).setColumnCount(e.getFieldName()).setColumnName(e.getFieldKey()).setModelId(e.getModelId());
                    //此处需要对类型进行转换 bi 的特殊类型处理
                    switch (e.getFieldType()) {
                        case SWITCH:
                            dataFiledDto.setType(DataFieldTypeEnum.布尔);
                            return dataFiledDto;
                        case inputNumber:
                            //如果是数字，需要明确整数位数，和小数位数
                            InputNumberHtml html = (InputNumberHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            dataFiledDto.setFormat("38," + html.getPrecision());
                            dataFiledDto.setType(DataFieldTypeEnum.数字);
                            return dataFiledDto;
                        case datePicker:
                            dataFiledDto.setType(DataFieldTypeEnum.时间);
                            if (ObjectNull.isNull(e.getDesignJson())) {
                                dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                return dataFiledDto;
                            }
                            DatePickerHtml datePickerHtml = (DatePickerHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            DatePickerFieldHandler.DateType dateType = datePickerHtml.getDatetype();
                            if (ObjectNull.isNull(dateType)) {
                                return dataFiledDto;
                            }
                            switch (dateType) {
                                case date:
                                    dataFiledDto.setFormat(NORM_DATE_PATTERN);
                                    return dataFiledDto;
                                case datetime:
                                    dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                    return dataFiledDto;
                                default:
                                    dataFiledDto.setCls(String.class);
                                    return dataFiledDto;
                            }
                        case timeSelect:
                            dataFiledDto.setType(DataFieldTypeEnum.字符串);
                        case timePicker:
                            dataFiledDto.setCls(String.class);
                            return dataFiledDto;
                        default:
                    }
                    return dataFiledDto;
                })
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    @Override
    public R<List<DataFiledDto>> dataFieldMap(String tableCode, String mode) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        // 根据表标识和模式获取模型id和应用id
        DataModelPo dataModel = dataModelService.getOne(Wrappers.<DataModelPo>lambdaQuery().eq(DataModelPo::getTableCode, tableCode).eq(DataModelPo::getBelongMode, AppVersionTypeEnum.getMsgType(mode)));
        if (ObjectNull.isNull(dataModel)) {
            return R.ok(Collections.emptyList());
        }

        List<DataFiledDto> collect = dataFieldService.getAllField(dataModel.getAppId(), dataModel.getId()).stream()
                .filter(e -> !DesignType.data.equals(e.getDesignType()))
                .map(e -> {
                    DataFiledDto dataFiledDto =
                            new DataFiledDto().setDataType(DataFieldTypeDataEnum.STRING).setCls(e.getFieldType().getAClass()).setColumnCount(e.getFieldName()).setColumnName(e.getFieldKey()).setModelId(e.getModelId());
                    //此处需要对类型进行转换 bi 的特殊类型处理
                    switch (e.getFieldType()) {
                        case SWITCH:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.BOOLEAN);
                            return dataFiledDto;
                        case inputNumber:
                            //如果是数字，需要明确整数位数，和小数位数
                            InputNumberHtml html = (InputNumberHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            dataFiledDto.setFormat("38," + html.getPrecision());
                            DataFieldTypeDataEnum fieldType = DataFieldTypeDataEnum.INT;
                            if (Optional.ofNullable(html.getPrecision()).orElseGet(() -> 0) >= 1) {
                                fieldType = DataFieldTypeDataEnum.DECIMAL;
                            }
                            dataFiledDto.setLength(38);
                            dataFiledDto.setPrecision(html.getPrecision());
                            dataFiledDto.setDataType(fieldType);
                            return dataFiledDto;
                        case datePicker:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.DATETIME);
                            if (ObjectNull.isNull(e.getDesignJson())) {
                                dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                return dataFiledDto;
                            }
                            dataFiledDto.setPrecision(0);
                            DatePickerHtml datePickerHtml = (DatePickerHtml) handlerMap.get(e.getType().getDesc()).toHtml(e.getDesignJson());
                            DatePickerFieldHandler.DateType dateType = datePickerHtml.getDatetype();
                            if (ObjectNull.isNull(dateType)) {
                                return dataFiledDto;
                            }
                            switch (dateType) {
                                case date:
                                    dataFiledDto.setFormat(NORM_DATE_PATTERN);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.DATE);
                                    return dataFiledDto;
                                case datetime:
                                    dataFiledDto.setFormat(NORM_DATETIME_PATTERN);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.DATETIME);
                                    return dataFiledDto;
                                default:
                                    dataFiledDto.setCls(String.class);
                                    dataFiledDto.setDataType(DataFieldTypeDataEnum.STRING);
                                    return dataFiledDto;
                            }
                        case timeSelect:
                            dataFiledDto.setDataType(DataFieldTypeDataEnum.STRING);
                        case timePicker:
                            dataFiledDto.setCls(String.class);
                            return dataFiledDto;
                    }
                    return dataFiledDto;
                })
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    @Override
    public R<List<DataModelDto>> dataModelList(String appId, String mode) {
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(AppVersionTypeEnum.getMsgType(mode)));
        JvsApp byId = appService.getById(appId);
        List<DataModelDto> collect = dataModelService.list(Wrappers.query(new DataModelPo().setAppId(appId)))
                .stream()
                //数据必须要大于 0
                .filter(e -> dataModelHandler.estimatedCount(ObjectNull.isNull(e.getCollectionName()) ? e.getId() : e.getCollectionName()) > 0)
                .map(e -> new DataModelDto().setTableCode(e.getTableCode()).setAppId(e.getAppId()).setAppName(byId.getName()).setTableName(ObjectNull.isNull(e.getCollectionName()) ? e.getId() : e.getCollectionName()).setTableNameDesc(e.getName()))
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    private Criteria buildQuery(DataModelSearchDto searchDto) {
        Criteria criteria = Criteria.where(DynamicDataService.MONGO_ID)
                .exists(true);
        Criteria childCriteria = Criteria.where("delFlag").is(false);

        List<DataModelSearchDto.SearchGroup> group = searchDto.getGroup();

        List<Criteria> and = new ArrayList<>();
        List<Criteria> or = new ArrayList<>();
        for (DataModelSearchDto.SearchGroup searchGroup : group) {
            List<DataModelSearchDto.SearchItem> items = searchGroup.getItems();
            Criteria currentCriteria = buildDynamicCriteriaList(items);
            if (searchGroup.getAndOr()) {
                and.add(currentCriteria);
            } else {
                or.add(currentCriteria);
            }
        }
        if (CollectionUtil.isNotEmpty(and)) {
            childCriteria.andOperator(and);
        }
        if (CollectionUtil.isNotEmpty(or)) {
            childCriteria.orOperator(or);
        }
        criteria.andOperator(childCriteria);
        return criteria;
    }

    /**
     * 动态查询条件
     *
     * @param conditions
     * @return
     */
    private Criteria buildDynamicCriteriaList(List<DataModelSearchDto.SearchItem> conditions) {
        if (ObjectUtils.isEmpty(conditions)) {
            return null;
        }
        conditions = conditions.stream().filter(e -> ObjectNull.isNotNull(e.getKey(), e.getQueryType())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(conditions)) {
            return null;
        }
        Criteria criteria = new Criteria();

        List<Criteria> and = new ArrayList<>();
        List<Criteria> or = new ArrayList<>();
        for (DataModelSearchDto.SearchItem condition : conditions) {
            Criteria criteria1 = buildCriteria(condition);
            if (condition.getAndOr()) {
                and.add(criteria1);
            } else {
                or.add(criteria1);
            }
        }

        if (CollectionUtil.isNotEmpty(and)) {
            criteria.andOperator(and);
        }
        if (CollectionUtil.isNotEmpty(or)) {
            criteria.orOperator(or);
        }
        return criteria;
    }

    private Criteria buildCriteria(DataModelSearchDto.SearchItem condition) {
        String key = condition.getKey();
        Object queryValue = condition.getValue();
        DataModelQueryType queryType = condition.getQueryType();
        if (!DataModelQueryType.isNull.equals(queryType) && !DataModelQueryType.between.equals(queryType)) {
            if (StringUtils.isBlank(key) || ObjectUtils.isEmpty(queryValue) || Objects.isNull(queryType)) {
                return null;
            }
            if (queryValue instanceof List) {
                //有间隔查询,
                //下拉多选查询,
                queryType = DataModelQueryType.in;
                //如果是多值为查询条件,则直接改变查询的类型为in 或 not in
            } else {
                queryValue = DataConditionType.get(queryValue);
            }
        }

        Criteria lower = Criteria.where(key);
        switch (queryType) {
            case eq:
                //做字符串拼接处理，如果是包含,则进行分隔处理
                if (queryValue instanceof String) {
                    if (queryValue.toString().contains(",")) {
                        String[] split = queryValue.toString().split(",");
                        lower.in(new ArrayList<String>(Arrays.asList(split)));
                        break;
                    }
                }
                if (queryValue instanceof List) {
                    lower.in(queryValue);
                } else {
                    lower.is(queryValue);
                }
                break;
            case ne:
                lower.ne(queryValue);
                break;
            case gt:
                lower.gt(queryValue);
                break;
            case ge:
                lower.gte(queryValue);
                break;
            case lt:
                lower.lt(queryValue);
                break;
            case le:
                lower.lte(queryValue);
                break;
            case allin:
                if (queryValue instanceof List) {
                    List value = (List) condition.getValue();
                    lower.regex(DynamicDataUtils.parseRegular(value.get(0).toString()));
                    for (int i = 1; i < value.size(); i++) {
                        //添加其它的
                        Criteria regex = Criteria.where(key).regex(DynamicDataUtils.parseRegular(value.get(i).toString()));
                        lower.andOperator(regex);
                    }
                } else {
                    //不是数组不处理
                    lower.regex(DynamicDataUtils.parseRegular(queryValue.toString()));
                }
                break;
            case in:
                if (queryValue instanceof List) {
                    lower.in((List) condition.getValue());
                } else {
                    if (JSONUtil.isTypeJSONObject(condition.getValue().toString())) {
                        if (JSONUtil.isTypeJSONArray(condition.getValue().toString())) {
                            lower.in(JSONArray.parseArray(JSONObject.toJSONString(condition.getValue())));
                        }
                    } else {
                        lower.is(condition.getValue());
                    }
                }
                break;
            case notIn:
                if (queryValue instanceof List) {
                    lower.nin((List) queryValue);
                } else {
                    Pattern compile = Pattern.compile("^((?!" + queryValue.toString() + ").)*$", Pattern.CASE_INSENSITIVE);
                    lower.regex(compile);
                }
                break;
            case like:
                // 模糊查询, mongoTemplate只支持正则匹配
                lower.regex(DynamicDataUtils.parseRegular(queryValue.toString()));
                break;
            case isNull:
                lower.is("");
                break;
            case between:
                // 范围查询可参考官方文档: https://www.mongodb.com/docs/manual/reference/operator/query/elemMatch/
                List queryValues = null;
                if (queryValue instanceof List) {
                    queryValues = (List) condition.getValue();
                } else {
                    if (JSONUtil.isTypeJSON(condition.getValue().toString())) {
                        if (JSONUtil.isTypeJSONArray(condition.getValue().toString())) {
                            queryValues = JSONArray.parseArray(JSONObject.toJSONString(condition.getValue()));
                        }
                    } else {
                        queryValues = Collections.emptyList();
                    }
                }
                if (ObjectNull.isNotNull(queryValues) && queryValues.size() == 2) {
                    lower = new Criteria().andOperator(
                            Criteria.where(key).gte(queryValues.get(0)).lte(queryValues.get(1))
                    );
                }
                break;
            default:
                break;
        }
        return lower;
    }
}
