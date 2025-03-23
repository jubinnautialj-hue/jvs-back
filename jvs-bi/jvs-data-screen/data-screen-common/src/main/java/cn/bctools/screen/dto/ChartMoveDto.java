package cn.bctools.screen.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 移动
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("移动")
public class ChartMoveDto {

    @ApiModelProperty("移动的文档id")
    private String id;

    @ApiModelProperty("[移动后]父级id 目录移动时不需要")
    private String parentId;

    @ApiModelProperty("[移动后]相邻的id后面")
    private String nextId;

    @ApiModelProperty("[移动后]相邻的id前面")
    private String frontId;

}
