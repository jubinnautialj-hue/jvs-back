package cn.bctools.data.factory.dto;

import cn.bctools.data.factory.entity.SysFunction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("获取函数返回值dto")
public class GetFunctionDto {
    @ApiModelProperty("分类名称")
    private String name;
    @ApiModelProperty("子集")
    private List<SysFunction> children;
}
