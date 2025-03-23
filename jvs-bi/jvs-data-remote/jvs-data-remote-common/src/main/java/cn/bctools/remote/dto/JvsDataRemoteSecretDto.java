package cn.bctools.remote.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 数据服务-凭证
 * </p>
 *
 * @author admin
 * @since 2023-04-04
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "JvsDataRemoteSecret对象", description = "数据服务-凭证")
public class JvsDataRemoteSecretDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("凭证")
    private String secret;

    @ApiModelProperty("凭证说明")
    private String remark;
}
