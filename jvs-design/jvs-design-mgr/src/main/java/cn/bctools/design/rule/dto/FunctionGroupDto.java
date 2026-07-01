package cn.bctools.design.rule.dto;

import cn.bctools.rule.dto.RuleFunctionDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FunctionGroupDto {
    @ApiModelProperty("分组")
    String groupName;
    @ApiModelProperty("方法")
    List<RuleFunctionDto> list;
}
