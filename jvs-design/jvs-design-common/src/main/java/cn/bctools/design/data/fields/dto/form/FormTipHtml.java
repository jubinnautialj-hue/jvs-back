package cn.bctools.design.data.fields.dto.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 提示信息
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "提示信息")
public class FormTipHtml {

    @ApiModelProperty("提示描述")
    private String text;

    @ApiModelProperty("描述位置")
    private String position;

}
