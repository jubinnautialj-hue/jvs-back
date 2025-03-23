package cn.bctools.screen.dto;

import cn.bctools.screen.entity.JvsMaterial;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("素材批量保存")
public class BatchMaterialDto {

    @NotBlank(message = "类型不能为空")
    @ApiModelProperty("类型")
    private String type;

    @NotEmpty(message = "未上传素材")
    @ApiModelProperty("素材")
    private List<JvsMaterial> materialList;

}
