package cn.bctools.design.permission.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class UrlMethodPair implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("请求地址")
    private String url;
    @ApiModelProperty("请求方式")
    private String type;
}
