package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author zhuxiaokang
 * 排序
 */
@Data
@Accessors(chain = true)
@ApiModel("排序入参")
public class SortDto {

    @ApiModelProperty(value = "上级目录id", required = true)
    @NotBlank(message = "上级目录id不能为空")
    private String toMenuCode;

    @ApiModelProperty(value = "菜单排序集合")
    private List<String> sortList;
}
