package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("模型分页查询入参")
public class DataModelPageReqDto {

    @ApiModelProperty("模型名称")
    private String name;

    @ApiModelProperty("模型id")
    private String id;

    @ApiModelProperty("数据集名")
    private String collectionName;

    @ApiModelProperty("true-绑定流程，false-未绑定流程")
    private Boolean enableWorkflow;
}
