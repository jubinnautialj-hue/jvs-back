package cn.bctools.report.dto;

import cn.bctools.permission.dto.AuthRole;
import cn.bctools.permission.enums.OperationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("树状信息")
public class TreeDTO {

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("显示名称")
    private String name;

    @ApiModelProperty("上级id")
    private String pid;

    @ApiModelProperty("类型")
    private Type type;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("权限集")
    private List<AuthRole> role;

    @ApiModelProperty(value = "权限类型",notes = "true 自定义权限，false 公开权限")
    private Boolean roleType;

    @ApiModelProperty("权限标识")
    private List<OperationType> operationList;

    @ApiModelProperty("子项")
    private List<TreeDTO> children;

    @Getter
    public enum Type{
        menu,report
    }
}
