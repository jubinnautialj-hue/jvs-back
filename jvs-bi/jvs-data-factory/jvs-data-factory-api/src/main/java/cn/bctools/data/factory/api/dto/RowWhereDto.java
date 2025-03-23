package cn.bctools.data.factory.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@ApiModel("数据集权限表达式获取返回值")
@Accessors(chain = true)
public class RowWhereDto {
    @ApiModelProperty("条件表达式")
    private String whereStr;
    @ApiModelProperty("动态入参")
    private List<Object> inParameter;
}
