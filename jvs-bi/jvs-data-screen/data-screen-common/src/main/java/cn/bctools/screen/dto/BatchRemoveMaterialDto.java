package cn.bctools.screen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("素材批量删除")
public class BatchRemoveMaterialDto {

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("素材")
    private List<String> materialIds;

}
