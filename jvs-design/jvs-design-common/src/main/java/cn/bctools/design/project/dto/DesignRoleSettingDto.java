package cn.bctools.design.project.dto;

import cn.bctools.design.crud.entity.DesignRole;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 设计的权限设置
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class DesignRoleSettingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("权限设置")
    private List<DesignRole> role = new ArrayList<>(1);
    @ApiModelProperty("应用id")
    private String jvsAppId;
    @ApiModelProperty("应用名称")
    private String jvsAppName;
    @ApiModelProperty("模型id")
    private String modeId;
    @ApiModelProperty("应用创建人")
    private String jvsAppCreateById;
    @ApiModelProperty("是否启用工作流")
    private Boolean enableWorkflow;
    @ApiModelProperty("数据权限")
    private List<DesignRole> dataModelRole = new ArrayList<>(1);
    @ApiModelProperty("是否跳过数据权限")
    private Boolean stepDataPermission;
    @ApiModelProperty("是否启用轻应用版本功能")
    private Boolean enableVersionFeature;

}
