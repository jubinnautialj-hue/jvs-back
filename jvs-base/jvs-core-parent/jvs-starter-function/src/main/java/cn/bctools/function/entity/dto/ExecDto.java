package cn.bctools.function.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 函数计算
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("函数计算")
public class ExecDto {

    @ApiModelProperty("如果没有值，则表示这个控件在最外层,[tab,table]")
    private List<String> parentKey;
    @ApiModelProperty("如果是表格需要传递具体的参数值 1 ")
    private Integer index;
    @ApiModelProperty("内容变动的字段, 为空时会尝试计算所有字段,触发可能会有两种， 容器内触发， 容器外触发，容器内触发时包含key的路径  name")
    private String modifiedField;
    @ApiModelProperty("所有参数 前端 整个表单对象 ")
    private Map<String, Object> params;

}
