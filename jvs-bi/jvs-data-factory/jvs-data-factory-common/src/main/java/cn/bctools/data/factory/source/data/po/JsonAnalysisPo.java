package cn.bctools.data.factory.source.data.po;

import cn.bctools.data.factory.enums.ApiDataFieldTypeEnum;
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
public class JsonAnalysisPo implements Serializable {

    private static final long serialVersionUID = -296426201835087400L;
    @ApiModelProperty("字段名称")
    private String columnName;

    @ApiModelProperty("来源id-可以是表名称 可以是数据源id根据不同的数据源需要的入参决定-注意 这里是返回才会存在")
    private String from;

    @ApiModelProperty("格式 例如时间:YYYY-MM-DD HH:MM:SS")
    private String format;

    @ApiModelProperty("是否选中")
    private Boolean isCheck;

    @ApiModelProperty("jsonKey值的路径")
    private String jsonPath;

    @ApiModelProperty(value = "jsonKey拆分后的路径后端用于中间计算", hidden = true)
    private List<String> jsonPathList;
    @ApiModelProperty(value = "jsonKey的路径长度", hidden = true)
    private Integer jsonPathLength;

    @ApiModelProperty("对应java的类型字段类型")
    private DataFieldTypeEnum dataFieldTypeEnum;

    @ApiModelProperty("api实时数据的时候使用")
    private ApiDataFieldTypeEnum apiDataFieldTypeEnum;

    @ApiModelProperty("结果集")
    private List<Object> values;

    @ApiModelProperty("子集数据")
    private List<JsonAnalysisPo> items;

    @ApiModelProperty("字段解释")
    private String columnCount;
}
