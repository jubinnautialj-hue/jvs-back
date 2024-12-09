package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <引用页面>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class ReferencePageDesignHtml {
    @ApiModelProperty("页面地址")
    private String address;
    @ApiModelProperty("位置")
    private String position;
    @ApiModelProperty("高度")
    private Integer height;
}
