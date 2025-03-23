package cn.bctools.document.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("用户名称与头像数据")
public class DcAuthConfigHeadImg {
    @ApiModelProperty("头像")
    private String headImg;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("id")
    private String id;
}
