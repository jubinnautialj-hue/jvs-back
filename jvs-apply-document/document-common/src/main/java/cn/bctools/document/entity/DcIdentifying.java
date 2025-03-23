package cn.bctools.document.entity;

import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.entity.enums.IdentifyingTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("权限标识")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_identifying")
public class DcIdentifying implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("标识名称")
    @TableField("identifying_name")
    private String identifyingName;
    @ApiModelProperty("标识key")
    @TableField("identifying_key")
    private IdentifyingKeyEnum identifyingKey;
    @ApiModelProperty("标识类型")
    @TableField("identifying_type")
    private IdentifyingTypeEnum identifyingType;
    @ApiModelProperty("创建者id")
    @TableField("create_by_id")
    private String createById;
    @ApiModelProperty("更新人")
    @TableField("update_by")
    private String updateBy;
    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private String createTime;
    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private String updateTime;
    @ApiModelProperty("创建人")
    @TableField("create_by")
    private String createBy;
    @ApiModelProperty("是否可以选择 就是一些默认的权限 不用用户选择 系统直接默认的")
    @TableField("is_select")
    private Boolean isSelect;
    @ApiModelProperty("是否可以选择 就是一些默认的权限 不用用户选择 系统直接默认的")
    @TableField("possessor_is")
    private Boolean possessorIs;
    @ApiModelProperty("是否选中")
    @TableField(exist = false)
    private Boolean select;
    @ApiModelProperty("标识名称-用于适配国际化")
    @TableField(exist = false)
    private String name;

}
