package cn.bctools.design.external.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author zhuxiaokang
 * 外部页面授权
 */
@Data
@Accessors(chain = true)
@ApiModel("外部页面授权入参")
public class GrantExternalReqDto {
    @ApiModelProperty(value = "id", required = true)
    @NotBlank(message = "id不能为空")
    private String id;

    @ApiModelProperty("资源集")
    private List<JSONObject> resources;

    @ApiModelProperty("权限集")
    private List<JSONObject> permissions;
}
