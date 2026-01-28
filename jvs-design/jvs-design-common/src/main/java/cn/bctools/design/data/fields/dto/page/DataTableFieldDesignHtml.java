package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.dto.enums.ColumnTypeEnum;
import cn.bctools.design.data.fields.dto.enums.FieldTypeEnum;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <列表设计-数据设计-表设计-字段>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class DataTableFieldDesignHtml {

    @ApiModelProperty(value = "Java字段别名", notes = "列表页数据渲染字段匹配")
    private String aliasColumnName;
    @ApiModelProperty(value = "组件类型", notes = "表单表格里会用到")
    private DataFieldType componentType;
    @ApiModelProperty("后端数据类型")
    private FieldTypeEnum dbJavaType;
    @ApiModelProperty("是否为查询条件")
    private Boolean enableQuery;
    @ApiModelProperty("该字段支持的查询类型")
    private List<DataQueryType> enabledQueryTypes;
    @ApiModelProperty(value = "是否显示为行数据字段")
    private Boolean show;
    @ApiModelProperty(value = "快速查询,快速检索")
    private Boolean enableRetrieval;
    @ApiModelProperty("是否显示统计")
    private Boolean enableStatistics;
    @ApiModelProperty("显示中文名")
    private String showChinese;
    @ApiModelProperty("是否支持查询")
    private Boolean supportQuery;
    @ApiModelProperty("是否支持高级设置")
    private Boolean supportSettings;
    @ApiModelProperty("是否支持显示")
    private Boolean supportShow;
    @ApiModelProperty("是否支持排序")
    private Boolean supportSort;
    @ApiModelProperty("是否支持统计")
    private Boolean supportStatistics;
    @ApiModelProperty("列名")
    private String columnName;
    @ApiModelProperty("默认值")
    private String columnDefault;
    @ApiModelProperty("是否为空")
    private String isNullable;
    @ApiModelProperty("导入是否为空")
    private Boolean required;
    @ApiModelProperty("数据类型")
    private String dataType;
    @ApiModelProperty("排序")
    private Long ordinalPosition;
    @ApiModelProperty("注释")
    private String columnComment;
    @ApiModelProperty("键类型PRI URI MUL")
    private String columnKey;
    @ApiModelProperty("额外说明")
    private String extra;
    @ApiModelProperty("字符最大长度")
    private Long characterMaximumLength;
    @ApiModelProperty("数值精度(最大位数)")
    private Long numericPrecision;
    @ApiModelProperty("小数精度")
    private Long numericScale;
    @ApiModelProperty("枚举列表")
    private List<String> enumValues;
    @ApiModelProperty("显示宽度")
    private Integer showWidth;
    @ApiModelProperty("高级设置")
    private DataTableFieldAdvancedSettingsHtml advancedSettings;
    @ApiModelProperty("组件属性")
    private Map<String, Object> designJson;
    @ApiModelProperty("查询条件配置")
    private Map<String, Object> queryConditionConfig;
    @ApiModelProperty("是否为排序条件")
    private Boolean enableSort;
    @ApiModelProperty("是否为导出字段")
    private Boolean isExport;
    @ApiModelProperty("是否为导入字段")
    private Boolean isImport;
    @ApiModelProperty("模板字段格式描述")
    private String description;
    @ApiModelProperty(value = "模板字段别名")
    private String showChineseAlias;
    @ApiModelProperty("Java字段类型")
    private String javaType;
    @ApiModelProperty("是否固定列")
    private Boolean fixed;
    @ApiModelProperty("单个值支持范围查询")
    private Boolean enableQueryRange;
    @ApiModelProperty("快速检索配置")
    private RetrievalOptionHtml retrievalOption;


    public void setJavaType(String javaType) {
        this.javaType = StrUtil.blankToDefault(javaType, getJavaType());
    }

    public String getJavaType() {
        if (StrUtil.isNotBlank(this.javaType)) {
            return this.javaType;
        }
        Map<String, Class<?>> map = Arrays.stream(ColumnTypeEnum.values()).collect(Collectors.toMap(ColumnTypeEnum::name, ColumnTypeEnum::getJavaType));
        //如果dataType为空  用组件的数据类型
        String type = dataType;
        if (null != dbJavaType) {
            type = StrUtil.blankToDefault(dataType, dbJavaType.getDbType());
        }
        Class<?> clz = map.getOrDefault(StrUtil.blankToDefault(type, "varchar").toUpperCase(), String.class);
        return clz.getSimpleName();
    }

    public String getFieldName() {
        return StrUtil.lowerFirst(StrUtil.toCamelCase(StrUtil.blankToDefault(columnName, "")).replace("_", ""));
    }

}
