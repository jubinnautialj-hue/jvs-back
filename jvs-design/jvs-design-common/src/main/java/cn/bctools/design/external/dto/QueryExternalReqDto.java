package cn.bctools.design.external.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 查询外部页面配置入参
 */
@Data
@Accessors(chain = true)
@ApiModel("查询外部页面配置入参")
public class QueryExternalReqDto {

    @ApiModelProperty(value = "页面名")
    private String name;

    @ApiModelProperty(value = "外部页面地址")
    private String url;
}
