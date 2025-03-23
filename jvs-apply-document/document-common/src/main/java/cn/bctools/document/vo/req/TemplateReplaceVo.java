package cn.bctools.document.vo.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: admin
 * @Description: 模板变量替换入参
 */

@Data
@Accessors(chain = true)
@ApiModel("模板变量替换入参")
public class TemplateReplaceVo {
    @ApiModelProperty(value = "模板id")
    @NotNull(message = "模板id不能为空")
    private String dcId;
    @ApiModelProperty(value = "变量对象")
    @NotNull(message = "变量对象不能为空")
    private List<TemplateReplaceDataVo> data;
}
