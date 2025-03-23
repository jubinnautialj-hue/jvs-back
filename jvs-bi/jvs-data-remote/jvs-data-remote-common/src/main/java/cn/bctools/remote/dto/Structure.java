package cn.bctools.remote.dto;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("结构")
@Accessors(chain = true)
public class Structure implements Serializable {
    @ApiModelProperty("别名 统一通过id生成器生成")
    private String columnName;

    @ApiModelProperty("用于判断是否需要重新生成别名")
    private Boolean newColumnNameIs = true;

    @ApiModelProperty("字段名称 原来的名称")
    private String originalColumnName;

    @ApiModelProperty("来源id-可以是表名称 可以是数据源id根据不同的数据源需要的入参决定-注意 这里是返回才会存在")
    private String from;

    @ApiModelProperty("格式 例如时间:YYYY-MM-DD HH:MM:SS")
    private String format;

    @ApiModelProperty("默认format范围")
    private String formatDefault;

    @ApiModelProperty("对应java的类型字段类型")
    private DataFieldTypeEnum dataFieldTypeEnum;

    @ApiModelProperty("字段类型-数据库取出来的")
    private String dataType;
    @ApiModelProperty("dataX 对应的字段属性方便导入配置文件的编写")
    private String dataXType;

    @ApiModelProperty("对应doris的字段类型")
    private String dorisType;

    @ApiModelProperty("是否为主键")
    private Boolean primaryKey;

    @ApiModelProperty("子集数据")
    private List<Structure> items;

    @ApiModelProperty("字段解释")
    private String columnCount;
}