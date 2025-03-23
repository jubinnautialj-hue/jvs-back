package cn.bctools.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("移动")
public class MoveDTO {

    @ApiModelProperty("目录或子级id")
    private String id;

    @ApiModelProperty("拖拽方向 true向上 false向下")
    private Boolean isFront;

    @ApiModelProperty("涉及id")
    private String nextId;

    @ApiModelProperty("父级id")
    private String parentId;
}
