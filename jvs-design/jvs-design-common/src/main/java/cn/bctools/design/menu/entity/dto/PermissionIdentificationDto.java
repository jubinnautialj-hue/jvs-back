package cn.bctools.design.menu.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@ApiModel("设计的资源标识")
@Accessors(chain = true)
public class PermissionIdentificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "表单、列表页的资源标识")
    private List<String> operation;
    @ApiModelProperty(value = "列表页树的资源标识")
    private List<String> treeOperation;
    @ApiModelProperty(value = "自定义页面的资源标识", notes = "key:标识，value：标识显示值")
    private List<Map<String, String>> urlOperation;

}
