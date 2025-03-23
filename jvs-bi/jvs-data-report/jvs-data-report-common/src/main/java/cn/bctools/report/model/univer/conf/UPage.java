package cn.bctools.report.model.univer.conf;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@ApiModel("分页设置")
public class UPage {

    @ApiModelProperty("是否开启分页")
    private boolean pageIs;

    @ApiModelProperty("当前页码")
    private Integer current;

    @ApiModelProperty("每页数量")
    private Integer size;

    private Long count;

}
