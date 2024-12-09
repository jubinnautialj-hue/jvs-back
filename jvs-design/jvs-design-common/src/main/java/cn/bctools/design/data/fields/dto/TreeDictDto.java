package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 树形字典
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class TreeDictDto {

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("父级id")
    private String parentId;

}
