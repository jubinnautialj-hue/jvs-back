package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.dto.page.ModelDisplayLinkageFieldHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author jvs
 *
 */
@Slf4j
@Data
@Accessors(chain = true)
@ApiModel("关联数据分页查询")
public class QueryPageLinkageDto {
    @ApiModelProperty("每页数量")
    private Integer size = 10;

    @ApiModelProperty("查询页数")
    private Integer current = 1;

    @ApiModelProperty("数据联动模型id")
    private String dataLinkageModelId;
    @ApiModelProperty("开启数据联动条件")
    private Boolean dataLinkageEnable;
    @ApiModelProperty("数据联动条件")
    private List<QueryConditionDto> dataLinkageList;

    @ApiModelProperty("显示字段")
    private List<ModelDisplayLinkageFieldHtml> linkageFieldKeys;
}
