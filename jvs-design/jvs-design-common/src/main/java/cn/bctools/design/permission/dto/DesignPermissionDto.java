package cn.bctools.design.permission.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("应用设计资源响应")
public class DesignPermissionDto {

    @ApiModelProperty("设计id")
    private String id;
    @ApiModelProperty("设计名称")
    private String name;
    @ApiModelProperty("设计类型")
    private DesignType designType;
    @ApiModelProperty("操作权限")
    private List<String> operation;
    @ApiModelProperty("树的操作权限")
    private List<String> treeOperation;
    @ApiModelProperty(value = "自定义页面的操作权限", notes = "key:标识，value：标识显示值")
    private List<Map<String, String>> urlOperation;
    @ApiModelProperty(value = "相关设计资源", notes = "同模型，且与菜单关联的设计资源")
    private List<DesignPermissionDto> relevant;
}
