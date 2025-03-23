package cn.bctools.document.dto;

import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.entity.enums.IdentifyingTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("权限标识")
@EqualsAndHashCode(callSuper = false)
public class IdentifyingAuthDto {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("标识名称")
    private String identifyingName;
    @ApiModelProperty("标识名称-适配国际化")
    private String name;
    @ApiModelProperty("标识key")
    private IdentifyingKeyEnum identifyingKey;
    @ApiModelProperty("标识类型")
    private IdentifyingTypeEnum identifyingType;
    @ApiModelProperty("是否可以选择 就是一些默认的权限 不用用户选择 系统直接默认的")
    private Boolean isSelect;
    @ApiModelProperty("所有者才会有的权限")
    private Boolean possessorIs;
    @ApiModelProperty("是否选中")
    private Boolean select;
}
