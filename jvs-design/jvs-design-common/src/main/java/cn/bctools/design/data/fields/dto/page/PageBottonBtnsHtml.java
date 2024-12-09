package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
public class PageBottonBtnsHtml {

    @ApiModelProperty("名称")
    String name;
    @ApiModelProperty("逻辑")
    String rule;
    @ApiModelProperty("标识")
    String permissionFlag;

}
