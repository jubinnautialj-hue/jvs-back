package cn.bctools.document.dto;

import cn.bctools.document.entity.DcLibrary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 目录数 只有当前一级
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("获取目录树包括权限")
@AllArgsConstructor
@NoArgsConstructor
public class TreeAuthDto {
    @ApiModelProperty("当前目录的数据")
    private List<DcLibrary> data;
    @ApiModelProperty("当前数据信息")
    private DcLibrary dcLibrary;
    @ApiModelProperty("文库信息")
    private DcLibrary knowledge;
    @ApiModelProperty("当前目录的结构")
    private List<DcLibrary> directoryStructure;
    @ApiModelProperty("当前目录用户拥有的权限")
    private List<IdentifyingAuthDto> dcIdentifying;
    @ApiModelProperty("当前目录的文档数量包含子集")
    private Integer count;
    @ApiModelProperty("当前目录置顶数据")
    private List<DcLibrary> topList;

}
