package cn.bctools.screen.dto;

import cn.bctools.screen.entity.JvsMaterial;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("素材类型")
public class MaterialTypeDto {

    @ApiModelProperty("唯一值 类型名称的md5值")
    private String id;

    @ApiModelProperty("类型名称")
    private String name;

    @ApiModelProperty("素材")
    private List<JvsMaterial> materialList;

}
