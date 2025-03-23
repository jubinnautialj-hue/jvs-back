package cn.bctools.data.factory.dto;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataSourceField {

    @ApiModelProperty("数据源ID")
    private String dataId;

    @ApiModelProperty("字段名称")
    private String fieldKey;

    @ApiModelProperty("显示名称")
    private String fieldName;

    @ApiModelProperty("是否显示")
    private Boolean isShow;


    @ApiModelProperty("字段名称 原来的名称")
    private String originalColumnName;

    /**
     * yyyy-MM-dd HH:mm:ss
     * 数字类型 M,D  m表示整数多少为，D精度位数 注意只有存在小数的才需要设置 其它的可以不用设置
     */
    @ApiModelProperty("格式类型例如时间类型:yyyy-MM-dd yyyy/MM/dd")
    private String format;

    @ApiModelProperty("格式的默认值")
    private String formatDefault;

    @ApiModelProperty("子集-子表单")
    private List<DataSourceField> items;

    @ApiModelProperty("字段类型-明细")
    private DataFieldTypeEnum fieldType;
    @ApiModelProperty("字段类型-分类")
    private DataFieldTypeClassifyEnum dataFieldTypeClassify;

    @ApiModelProperty("字段类型-数据库取出来的-不能为空")
    private String dataType;

    @ApiModelProperty("字段类型-doris数据库的类型")
    private String dorisType;
    @ApiModelProperty("长度-例如 varchar  datetime DECIMAL")
    private Integer length;
    @ApiModelProperty("精度-DECIMAL 类型")
    private Integer precision;

    @ApiModelProperty("部分功能节点 需要传入默认值-例如:列转行  可以设置为null字段的默认值")
    private Object defaultValue;


    @ApiModelProperty("数据源类型")
    private String dataSourceType;


}
