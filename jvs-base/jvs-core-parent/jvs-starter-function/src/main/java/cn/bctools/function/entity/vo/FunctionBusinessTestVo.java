package cn.bctools.function.entity.vo;

import cn.bctools.function.enums.JvsParamType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 新增函数时测试
 */
@Data
@Accessors(chain = true)
@ApiModel("系统函数测试实体类")
public class FunctionBusinessTestVo {
    private String id;
    @ApiModelProperty("函数体")
    private String body;
    @ApiModelProperty("参数")
    private List<Parameter> parameters;
    @ApiModelProperty("参数")
    private Map<String, Object> map = new HashMap<>();
    @ApiModelProperty("参数类型")
    private Map<String, JvsParamType> mapParamType = new HashMap<>();
    @ApiModelProperty("返回值类型")
    private JvsParamType jvsParamType;
    @ApiModelProperty("是否存在可变参数")
    private Boolean dynamicParam;
    @ApiModelProperty("返回入参类型")
    private List<JvsParamType> inParamTypes;
    @ApiModelProperty("函数名")
    String functionName;
    @ApiModelProperty("短名称")
    String shortName;
    @ApiModelProperty("解释")
    String info;
    @ApiModelProperty("分级")
    String type;

}
