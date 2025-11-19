package cn.bctools.design.permission.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class PermissionEndpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("标识名称")
    private String name;
    @ApiModelProperty("标识")
    private String permission;
    @ApiModelProperty("资源请求地址")
    private List<UrlMethodPair> url;

}
