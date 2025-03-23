package cn.bctools.report.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.query.dynamic.DynamicTimeExecuteFactory;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.bctools.data.factory.query.dynamic.enums.DynamicTimeTypeEnum;
import cn.bctools.report.enums.ESortType;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.USearchField;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * sql工具
 *
 * @author wl
 */
@Slf4j
public class SqlUtils {

    /**
     * 排序字段 完全限定列名
     */
    private static final String ORDER_BY_FULL_FORMAT = "`{}`.`{}` {}";
    /**
     * 排序字段 非完全限定列名
     */
    private static final String ORDER_BY_FORMAT = "`{}` {}";
    /**
     * 排序前缀
     */
    private static final String ORDER_BY_PREFIX = " ORDER BY ";

    private static final String WHERE_FORMAT = "`{}`.`{}` = '{}'";

    private static final String WHERE_BETWEEN_FORMAT = "`{}`.`{}` BETWEEN '{}' AND '{}'";

    private static final String WHERE_IN_FORMAT = "`{}`.`{}` IN ({})";

    private static List<String> SUPPORT_TIME_TYPE = new ArrayList<String>(){
        {
            add("yyyyMMdd");
            add("yyyy-MM-dd");
            add("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * 创建排序 包含表名
     * ORDER BY tableName.fieldName ASC
     * @param list 单元格列表
     * @return sql
     */
    public static String buildOrderBy(List<UCell> list){
        return buildOrderBy(list,true);
    }

    /**
     * 创建排序 不包含表名
     * ORDER BY fieldName ASC
     * @param list 单元格
     * @return sql
     */
    public static String buildOrderByNoTableName(List<UCell> list){
        return buildOrderBy(list,false);
    }

    /**
     * 创建排序sql
     * @param list 单元格
     * @param isFull 是否为完整字段名  true tableName.fieldName, false fieldName
     * @return sql
     */
    private static String buildOrderBy(List<UCell> list,boolean isFull){
        String orderBy = list.stream()
                .filter(e -> EValueType.数据集.equals(e.getCustom().getValueType()))
                .map(e -> {
                    ESortType sortType = e.getCustom().getSortType();
                    if(ESortType.NONE.equals(sortType)){
                        return null;
                    }
                    UField field = e.getCustom().getField();
                    if(isFull){
                        return StrUtil.format(ORDER_BY_FULL_FORMAT, field.getExecuteName(), field.getFieldKey(), sortType.name());
                    }
                    return StrUtil.format(ORDER_BY_FORMAT, field.getFieldKey(), sortType.name());
                }).filter(Objects::nonNull).collect(Collectors.joining(StringPool.COMMA));
        if(StrUtil.isNotBlank(orderBy)){
            return ORDER_BY_PREFIX + orderBy;
        }
        return StringPool.EMPTY;
    }

    /**
     * 创建字段名
     * 字段名 -> `字段名`
     * @param field 字段
     * @return 字段名
     */
    public static String buildFieldKey(UField field){
        return StrUtil.format("`{}`", field.getFieldKey());
    }

    /**
     * 创建 字段完全限定名
     * * 字段名 -> `表名`.`字段名`
     * @param field 字段
     * @return 字段名
     */
    public static String buildFullFieldKey(UField field){
        return StrUtil.format("`{}`.`{}`",field.getExecuteName(), field.getFieldKey());
    }

    /**
     * 获取查询字段
     * @param field
     * @return
     */
    public static String buildQueryField(UField field){
        Assert.notNull(field.getFieldType(),() -> new BusinessException("字段类型不确定"));
        switch (field.getFieldType().getClassifyEnum()){
            case 时间:
                /*
                只能转yyyyMMdd yyyy-MM-dd yyyy-MM-dd HH:mm:ss
                 */
                if(StrUtil.isNotBlank(field.getFormat()) && SUPPORT_TIME_TYPE.contains(field.getFormat())){
                    return StrUtil.format("DATE_FORMAT(`{}`.`{}`,\"{}\") AS \"{}\"", field.getExecuteName(), field.getFieldKey(), field.getFormat(), field.getFieldKey());
                }
            default:
                return StrUtil.format("`{}`.`{}` AS \"{}\"", field.getExecuteName(), field.getFieldKey(), field.getFieldKey());
        }
    }

    public static String buildQueryWhere(String executeName){
        Map<String, USearchField> searchFields = USheetContext.getSearchFields(executeName);
        if(searchFields==null){
            return "";
        }
        return searchFields.entrySet().stream().map(entry -> {
            USearchField field = entry.getValue();
            Object queryValue = field.getQueryValue() == null?field.getQueryDefaultValue():field.getQueryValue();
            if(queryValue == null){
                return null;
            }
            
            if(USearchField.EQueryType.quickDate.equals(field.getQueryType()) && queryValue instanceof String && StrUtil.isBlank(queryValue.toString())){
                DynamicTimeTypeEnum dynamicTimeType;
                String quickType = field.getQuickType();
                try {
                    dynamicTimeType = DynamicTimeTypeEnum.valueOf(quickType);
                } catch (IllegalArgumentException e) {
                    String err = StrUtil.format("不存在的快捷枚举:{}", quickType);
                    throw new BusinessException(err);
                }
                DynamicTimeValue dynamicTimeValue = SpringContextUtil.getBean(DynamicTimeExecuteFactory.class).execute(new DynamicTimeDto().setType(dynamicTimeType).setFormat(field.getFormat()));
                queryValue = CollectionUtil.toList(dynamicTimeValue.getStartTime(),dynamicTimeValue.getEndTime());

            }
            if(queryValue instanceof List){
                JSONArray arr = JSONUtil.parseArray(queryValue);
                if(USearchField.EQueryType.quickDate.equals(field.getQueryType()) || USearchField.EQueryType.dateRange.equals(field.getQueryType())){
                    if(arr.size()<2){
                        return null;
                    }
                    return StrUtil.format(WHERE_BETWEEN_FORMAT,executeName,entry.getKey(),arr.getStr(0),arr.getStr(1));
                }
                if(USearchField.EQueryType.selectMultiple.equals(field.getQueryType())){
                    String collect = arr.stream().map(StrUtil::toStringOrNull).filter(Objects::nonNull).map(e -> "'" + e + "'").collect(Collectors.joining(StringPool.COMMA));
                    return StrUtil.format(WHERE_IN_FORMAT,executeName,entry.getKey(),collect);
                }
            }
            if(queryValue instanceof String && StrUtil.isBlank(queryValue.toString())){
                return null;
            }
            return StrUtil.format(WHERE_FORMAT, executeName,entry.getKey(), queryValue);
        }).filter(Objects::nonNull).collect(Collectors.joining(" AND "));
    }
}
