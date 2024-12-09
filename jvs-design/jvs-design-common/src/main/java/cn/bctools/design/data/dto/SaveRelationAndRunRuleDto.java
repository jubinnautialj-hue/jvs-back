package cn.bctools.design.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 保存关联数据id集合并调用逻辑引擎入参
 */
@Data
@Accessors(chain = true)
@ApiModel("保存关联数据id集合并调用逻辑引擎入参")
public class SaveRelationAndRunRuleDto {

    @ApiModelProperty(value = "关联标识", required = true)
    @NotBlank(message = "关联标识不能为空")
    private String relationTag;
    @ApiModelProperty(value = "逻辑引擎key", required = true)
    @NotBlank(message = "逻辑引擎key不能为空")
    private String ruleKey;
    @ApiModelProperty(value = "数据")
    private Map<String, Object> data;
}
