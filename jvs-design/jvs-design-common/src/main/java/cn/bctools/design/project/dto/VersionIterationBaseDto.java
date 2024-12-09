package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("版本迭代入参")
public class VersionIterationBaseDto {

    @ApiModelProperty(value = "是否将数据发布到模板")
    private Boolean deployData = Boolean.FALSE;

    @ApiModelProperty(value = "要将数据发布到模板的模型id集合")
    private List<String> deployDataModelIds;
}
