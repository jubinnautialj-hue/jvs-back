package cn.bctools.design.workflow.dto.permission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 有权限启动的工作流列表响应
 */
@Data
@Accessors(chain = true)
@ApiModel("有权限启动的工作流列表")
public class HavePermissionDesignResDto {

    @ApiModelProperty(value = "分类")
    private String designGroup;

    @ApiModelProperty(value = "工作流设计列表")
    private List<PermissionDesignDto> flowDesigns;
}
