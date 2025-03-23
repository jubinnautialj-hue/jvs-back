package cn.bctools.document.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 知识库-文档位置参数
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("知识库-文档位置参数")
public class DcLocationOtherDto {

    @ApiModelProperty("移动的文档id")
    private String id;

    @ApiModelProperty("[移动后]父级id, 默认为顶级")
    private String parentId;

    @ApiModelProperty("[移动后]的知识库id")
    private String knowledgeId;

    @ApiModelProperty("[移动后]相邻的文档id")
    private String nextId;

}
