package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 重置排序
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("重置排序")
@AllArgsConstructor
@NoArgsConstructor
public class ResetSortDto {
    @ApiModelProperty("排序字段 name 名称  createTime 创建时间")
    private String sortName;
    @ApiModelProperty("文库id")
    private String id;
    @ApiModelProperty("是否为降序排序")
    private Boolean isSortDesc;
}
