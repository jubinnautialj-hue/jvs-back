package cn.bctools.design.data.fields.dto.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 校验设置
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "校验设置")
public class FormRuleHtml {

    @ApiModelProperty("是否必填")
    private Boolean required;

    @ApiModelProperty("提示信息")
    private String message;

    @ApiModelProperty("[仅前端使用]触发事件")
    private String trigger;

}
