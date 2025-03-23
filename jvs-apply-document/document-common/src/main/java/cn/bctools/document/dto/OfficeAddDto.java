package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 新建office文件
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("新建office文件")
@AllArgsConstructor
@NoArgsConstructor
public class OfficeAddDto {
    @ApiModelProperty("类型 文件后缀名")
    private String type;
    @ApiModelProperty("父级")
    private String parentId;
    @ApiModelProperty("知识库id")
    private String knowledgeId;

}
