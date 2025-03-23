package cn.bctools.chart.chart;

import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.bo.YoYMoMComparisonsData;
import cn.bctools.chart.chart.config.CommonConfig;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.chart.po.GetDataParameter;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.chart.enums.YoYMoMCalculateModeEnums;
import cn.bctools.chart.enums.YoYMoMCalculateTypeEnums;
import cn.bctools.chart.enums.YoYMoMUnitEnums;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.RowWhereDto;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.doris.condition.DorisCollectCondition;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.dto.OrderField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.enums.SortTypeEnums;
import cn.bctools.oss.props.OssProperties;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 所有图表数据处理的抽象类
 *
 * @author xh
 */
public interface ChartElementInterface {

    /**
     * @param dataFactoryId 通过数据集id获取行权限
     * @return 行权限逻辑
     */
    default RowWhereDto getDataFactoryDataAuth(String dataFactoryId) {
        DataFactoryApi dataFactoryApi = SpringContextUtil.getBean(DataFactoryApi.class);
        return dataFactoryApi.getAuthRow(dataFactoryId).getData();
    }


    /**
     * 数据查询
     *
     * @param getDataParameter 获取数据的统一入参
     */
    default ChartReturnObj exec(GetDataParameter getDataParameter) {
        return null;
    }

    /**
     * 文件导出
     *
     * @param getDataParameter 获取数据的统一入参
     */
    default ChartReturnObj outFile(GetDataParameter getDataParameter, Long size) {
        ChartReturnObj chartReturnObj = new ChartReturnObj().setSeries(new ArrayList<>()).setCardContent(0).setYAxisData(new ArrayList<>()).setXAxisData(new ArrayList<>());
        StringBuilder sql = new StringBuilder();
        this.buildSql(getDataParameter, sql);
        DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        //防止 排序字段为空
        Function<ChartDesignInParameter, List<OrderField>> function = (x) -> {
            FieldsData fieldsData = Optional.ofNullable(x.getXAxis()).orElse(x.getYAxis()).get(0);
            OrderField orderField = new OrderField().setSortType(SortTypeEnums.desc).setFieldKey(fieldsData.getFieldKey());
            return Collections.singletonList(orderField);
        };
        ChartDesignInParameter logicSetting = getDataParameter.getLogicSetting();
        List<OrderField> sortFields = Optional.ofNullable(getDataParameter.getSortFields()).orElse(function.apply(logicSetting));
        //合并 xy等维度数据 用于生成 表格title
        List<FieldsData> fieldsData = new ArrayList<>();
        fieldsData.addAll(logicSetting.getXAxis());
        fieldsData.addAll(Optional.ofNullable(logicSetting.getYAxis()).orElse(new ArrayList<>()));
        fieldsData.addAll(Optional.ofNullable(logicSetting.getColour()).orElse(new ArrayList<>()));
        fieldsData.addAll(Optional.ofNullable(logicSetting.getDimension()).orElse(new ArrayList<>()));
        //导出数据sql 需要对title 重新定义
        StringBuilder outFileSql = new StringBuilder();
        outFileSql.append("SELECT ");
        //合并title
        fieldsData.forEach(e -> {
            String fileName = StrUtil.isNotEmpty(e.getAliasName()) ? e.getAliasName() : e.getFieldName();
            outFileSql.append("`").append(e.getFieldKey()).append("` as `").append(fileName).append("`,");
        });
        //s3相关信息
        OssProperties ossProperties = SpringContextUtil.getBean(OssProperties.class);
        String s3Url = ossProperties.getEndpoint().trim().startsWith("http") ? ossProperties.getEndpoint() : "http://" + ossProperties.getEndpoint();
        CommonConfig commonConfig = SpringContextUtil.getBean(CommonConfig.class);
        if (StrUtil.isBlank(commonConfig.getS3Ip())) {
            throw new BusinessException("s3内容ip为空无法导出");
        }
        String s3Ip = commonConfig.getS3Ip().trim().startsWith("http") ? commonConfig.getS3Ip() : "http://" + commonConfig.getS3Ip();
        outFileSql.delete(outFileSql.length() - 1, outFileSql.length());
        outFileSql.append(" from (").append(sql).append(") as out_file_table_chart ");
        if (!sortFields.isEmpty()) {
            outFileSql.append(" order by ");
            String string = sortFields.stream().map(e -> "`" + e.getFieldKey() + "` " + e.getSortType()).collect(Collectors.joining(","));
            outFileSql.append(string);
        }
        outFileSql
                .append(" limit ? into outfile \"s3://jvs-public/chart/outFile/\" ")
                .append(" format as csv_with_names ").append("properties(")
                //s3相关配置
                .append("\"s3.endpoint\" =\"").append(s3Ip).append("\",")
                .append(" \"s3.region\" = \"bd\",")
                .append("\"s3.secret_key\"=\"").append(ossProperties.getSecretKey()).append("\",")
                .append("\"s3.access_key\"=\"").append(ossProperties.getAccessKey()).append("\",")
                .append("\"column_separator\"=\",\",").append("\"line_delimiter\"=\"\\n\",").append("\"max_file_size\"=\"1024mb\")");
        //sql动态入参 需要添加数量限制
        List<Object> parameter = getDataParameter.getParameter();
        parameter.add(size);
        List<Map<String, Object>> list = dorisJdbcTemplate.queryForList(outFileSql.toString(), parameter.toArray());
        if (list.isEmpty()) {
            throw new BusinessException("下载文件错误");
        }
        String url = list.get(0).get("URL").toString();
        url = url.replace("s3:/", s3Ip);
        url = url.replace("*", "0.csv");
        url = url.replace(s3Ip, s3Url);
        chartReturnObj.setFilePath(url);
        return chartReturnObj;
    }

    /**
     * 表格模式下的数据查询
     *
     * @param getDataParameter 整个设计数据
     */
    default ChartReturnObj getTable(GetDataParameter getDataParameter, Long current, Long size) {
        ChartReturnObj chartReturnObj = new ChartReturnObj().setSeries(new ArrayList<>()).setCardContent(0).setYAxisData(new ArrayList<>()).setXAxisData(new ArrayList<>());
        StringBuilder sql = new StringBuilder();
        this.buildSql(getDataParameter, sql);
        DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        //防止 排序字段为空
        Function<ChartDesignInParameter, List<OrderField>> function = (x) -> {
            FieldsData fieldsData;
            if (!x.getXAxis().isEmpty()) {
                fieldsData = x.getXAxis().get(0);
            } else {
                fieldsData = x.getYAxis().get(0);
            }
            OrderField orderField = new OrderField().setSortType(SortTypeEnums.desc).setFieldKey(fieldsData.getFieldKey());
            return Collections.singletonList(orderField);
        };
        ChartDesignInParameter logicSetting = getDataParameter.getLogicSetting();
        List<OrderField> sortFields = Optional.ofNullable(getDataParameter.getSortFields()).orElse(function.apply(logicSetting));
        //这里会存在引用同一个地址 导致数据被修改 所以需要提前存储
        Object[] array = getDataParameter.getParameter().toArray();
        List<Map<String, Object>> page = dorisJdbcTemplate.getDataPage(size, current, sql.toString(), getDataParameter.getParameter(), sortFields);
        //获取条数
        String countSql = "SELECT COUNT(*) as count  FROM (" + sql + ") as a";
        String count = dorisJdbcTemplate.queryForList(countSql, array).get(0).get("count").toString();
        //合并 xy等维度数据 用于生成 表格title
        List<FieldsData> fieldsData = new ArrayList<>();
        fieldsData.addAll(logicSetting.getXAxis());
        fieldsData.addAll(Optional.ofNullable(logicSetting.getYAxis()).orElse(new ArrayList<>()));
        fieldsData.addAll(Optional.ofNullable(logicSetting.getColour()).orElse(new ArrayList<>()));
        fieldsData.addAll(Optional.ofNullable(logicSetting.getDimension()).orElse(new ArrayList<>()));
        //合并title
        List<ChartReturnObj.TableHeader> headers = fieldsData.stream().map(e -> {
            ChartReturnObj.TableHeader tableHeader = new ChartReturnObj.TableHeader();
            tableHeader.setLabel(StrUtil.isNotEmpty(e.getAliasName()) ? e.getAliasName() : e.getFieldName());
            tableHeader.setProp(e.getFieldKey());
            return tableHeader;
        }).collect(Collectors.toList());
        //组装数据
        ChartReturnObj.TableData tableData = new ChartReturnObj.TableData();
        tableData.setCurrent(current);
        tableData.setSize(size);
        tableData.setRecords(page);
        tableData.setTotal(Long.valueOf(count));
        return chartReturnObj.setHeader(headers).setData(tableData);
    }

    /**
     * 构建sql
     * 注意默认实现的方式
     * 都是 通过 维度指标 进行分组统计的  不同的图表 直接调用 父级方法 即可
     * 后续 二次数据的封装还是需要不同的图表类型自行维护
     *
     * @param getDataParameter 所有数据
     * @return 查询入参
     */
    default void buildSql(GetDataParameter getDataParameter, StringBuilder sql) {
        List<Object> parameter = getDataParameter.getParameter();
        RowWhereDto dataFactoryDataAuth = this.getDataFactoryDataAuth(getDataParameter.getDataFactoryId());
        ChartDesignInParameter chartDesignInParameter = getDataParameter.getLogicSetting();
        //构建基础sql
        sql.append("SELECT ");
        List<FieldsData> xAxis = Optional.ofNullable(chartDesignInParameter.getXAxis()).orElse(new ArrayList<>());
        List<FieldsData> yAxis = Optional.ofNullable(chartDesignInParameter.getYAxis()).orElse(new ArrayList<>());
        List<FieldsData> colour = Optional.ofNullable(chartDesignInParameter.getColour()).orElse(new ArrayList<>());
        List<FieldsData> dimension = Optional.ofNullable(chartDesignInParameter.getDimension()).orElse(new ArrayList<>());
        //颜色其实就是分组
        xAxis.addAll(colour);
        //尺寸就是 指标
        yAxis.addAll(dimension);
        if (!xAxis.isEmpty()) {
            String groupKey = xAxis.stream().map(e -> {
                //dateTime 时间格式 返回给前端需要转化为string
                if (e.getFieldType().equals(DataFieldTypeEnum.DATETIME)) {
                    return "cast(`" + e.getFieldKey() + "` as STRING) AS `" + e.getFieldKey() + "`";
                }
                return "`" + e.getFieldKey() + "`";
            }).collect(Collectors.joining(","));
            sql.append(groupKey).append(",");
        }
        //同环比数据
        List<FieldsData> YoYMoMList = new ArrayList<>();
        //指标数据
        for (FieldsData yAxi : yAxis) {
            //判断指标与维度是否重复如果重复需要取别名
            String asFieldKey = yAxi.getFieldKey();
            if (xAxis.stream().anyMatch(e -> e.getFieldKey().equals(yAxi.getFieldKey()))) {
                asFieldKey = yAxi.getFieldKey() + "1";
            }
            DorisCollectCondition condition = SpringContextUtil.getBean(yAxi.getCalculateType().getDorisClass());
            String conditionSql = condition.addCondition(yAxi.getFieldKey(), 0, Boolean.TRUE, asFieldKey);
            yAxi.setFieldKey(asFieldKey);
            sql.append(conditionSql).append(",");
            //同环比 sql 注入
            List<YoYMoMComparisonsData> children = Optional.ofNullable(yAxi.getChildren()).orElse(new ArrayList<>());
            if (!children.isEmpty()) {
                //本期数据获取的sql
                String currentPeriodSql = condition.addCondition(yAxi.getFieldKey(), 0, Boolean.TRUE, null);
                children.forEach(e -> {
                    //上期数据获取的sql
                    StringBuilder priorPeriodSql = new StringBuilder();
                    priorPeriodSql.append("( SELECT ").append(currentPeriodSql).append(" FROM ")
                            .append(getDataParameter.getTableName()).append(" where `")
                            .append(e.getPursuantKey()).append("` = ");
                    if (e.getCalculateMode().equals(YoYMoMCalculateModeEnums.immobilizationDate)) {
                        priorPeriodSql.append("'").append(e.getValue().toString()).append("'");
                    } else {
                        switch (e.getCalculateMode()) {
                            case YoYMonthGrowthRate:
                                e.setUnit(YoYMoMUnitEnums.MONTH).setValue(1);
                                break;
                            case YoYWeekGrowthRate:
                                e.setUnit(YoYMoMUnitEnums.WEEK).setValue(1);
                                break;
                            case YoYYearGrowthRate:
                                e.setUnit(YoYMoMUnitEnums.YEAR).setValue(1);
                                break;
                            case MoMGrowthRate:
                                e.setUnit(YoYMoMUnitEnums.DAY).setValue(1);
                                break;
                            default:
                        }
                        YoYMoMUnitEnums unit = e.getUnit();
                        String function = SpringContextUtil.getBean(unit.getGetFunctionClass()).functionSql(e.getPursuantFieldType(), e.getValue(), e.getPursuantKey(), unit);
                        priorPeriodSql.append(function);
                        if (!dataFactoryDataAuth.getWhereStr().isEmpty()) {
                            priorPeriodSql.append(" and (").append(dataFactoryDataAuth.getWhereStr()).append(")");
                            parameter.addAll(dataFactoryDataAuth.getInParameter());
                        }
                    }
                    priorPeriodSql.append(")");
                    sql.append("round(IFNULL((");
                    //如果存在同环比  需要把 同环比数据加入到指标中
                    FieldsData fieldsData = JSONObject.parseObject(JSONObject.toJSONString(e), FieldsData.class);
                    //根据不同的规则 拼接sql
                    if (e.getCalculateType().equals(YoYMoMCalculateTypeEnums.DIFF)) {
                        sql.append(currentPeriodSql).append(e.getCalculateType().getDorisSql())
                                .append(priorPeriodSql);
                    } else {
                        sql.append("(").append(currentPeriodSql).append("-")
                                .append(priorPeriodSql).append(")")
                                .append(e.getCalculateType().getDorisSql())
                                .append(priorPeriodSql)
                                .append(" * 100");
                        //因为这里是两个sql 所以需要重复添加一份入参
                        parameter.addAll(dataFactoryDataAuth.getInParameter());
                        //如果是百分比 需要设置单位 用于前端使用
                        fieldsData.setFormatParams(new FieldsData.FormatParams().setUnit(UnitEnums.perCent));
                    }
                    Integer decimals = Optional.ofNullable(e.getDecimals()).orElse(2);
                    sql.append("),0),").append(decimals).append(") as `").append(e.getFieldKey()).append("`,");
                    YoYMoMList.add(fieldsData);
                });
            }
        }
        getDataParameter.getLogicSetting().getYAxis().addAll(YoYMoMList);
        sql.delete(sql.length() - 1, sql.length());
        //是否存在同环比
        sql.append(" FROM ( SELECT * FROM ").append(getDataParameter.getTableName()).append(") as a ");
        //添加过滤条件
        sql.append(getDataParameter.getWhere());
        if (!dataFactoryDataAuth.getWhereStr().isEmpty()) {
            parameter.addAll(dataFactoryDataAuth.getInParameter());
            boolean b = getDataParameter.getWhere().isEmpty();
            if (b) {
                sql.append(" where ");
            } else {
                sql.append(" and (");
            }
            sql.append(dataFactoryDataAuth.getWhereStr());
            if (!b) {
                sql.append(")");
            }
        }
        if (!xAxis.isEmpty()) {
            String string = xAxis.stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
            //添加分组
            sql.append(" GROUP BY `").append(string).append("`");
        }
        getDataParameter.setParameter(parameter);
    }


}
