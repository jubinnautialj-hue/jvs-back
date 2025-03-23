package cn.bctools.design.crud.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : GaoZeXi
 */
@Data
@ApiModel("树形分类字典,excel解析类")
public class JvsTreeDto {

    @ExcelProperty(index = 0, value = "名称")
    @ApiModelProperty("名称, 同一层级不能重复")
    public String name;
    @ExcelProperty(index = 1, value = "显示值")
    @ApiModelProperty("显示值")
    @TableField("tree_value")
    public String value;
    @ExcelProperty(index = 2, value = "标识分组，默认和顶级标识一致")
    @ApiModelProperty("分组id, 区分不同的字典树")
    public String groupId;
    @ExcelProperty(index = 3, value = "唯一标识,不允许重复")
    @ApiModelProperty("唯一标识, 作为该字典被引用的key")
    public String uniqueName;
    @ExcelProperty(index = 4, value = "排序，值越小排越前")
    @ApiModelProperty("排序")
    public Integer sort;
    @ExcelProperty(index = 5, value = "备注信息,可以为空")
    @ApiModelProperty("备注信息")
    public String remarks;
    @ExcelProperty(index = 6, value = "父级标识,顶级为-1")
    @ApiModelProperty("上级Id, 根节点为-1")
    public String parentId;

}
