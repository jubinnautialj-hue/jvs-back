package cn.bctools.design.crud.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author : GaoZeXi
 */
@Data
@ApiModel("树形分类字典")
@Accessors(chain = true)
@TableName("jvs_tree")
public class JvsTree implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DICT_ID_ROOT = "-1";

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ExcelProperty("名称")
    @NotNull(message = "名称不能为空")
    @ApiModelProperty("名称, 同一层级不能重复")
    private String name;
    @ExcelProperty("值")
    @NotNull(message = "值不能为空")
    @ApiModelProperty("显示值")
    @TableField("tree_value")
    private String value;
    @NotNull(message = "分组不能为空")
    @ExcelProperty("分组，可为空")
    @ApiModelProperty("分组id, 区分不同的字典树")
    private String groupId;
    @NotNull(message = "唯一标识不能为空")
    @ExcelProperty("唯一标识")
    @ApiModelProperty("唯一标识, 作为该字典被引用的key")
    private String uniqueName;
    @ExcelProperty("排序")
    @NotNull(message = "排序不能为空")
    @ApiModelProperty("排序")
    private Integer sort;
    @ExcelProperty("备注信息")
    @ApiModelProperty("备注信息")
    private String remarks;
    @ExcelProperty("上级Id")
    @ApiModelProperty("上级Id, 根节点为-1")
    private String parentId;
    @ExcelIgnore
    @TableField(exist = false)
    @ApiModelProperty("子集")
    private List<JvsTree> children;

}
