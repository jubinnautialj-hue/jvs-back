package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 列表查询条件
 * <p>
 * 字段为空时, 默认返回空数据
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("查询条件")
public class QueryListDto {

    @ApiModelProperty("查询结果的字段")
    private List<String> fieldList;
    @ApiModelProperty("关键字搜索,如果不为空,有限使用关键字查询,否则使用查询条件")
    private String keywords;

    @ApiModelProperty("查询条件")
    private List<QueryConditionDto> conditions;

    @ApiModelProperty(value = "查询条件", notes = "组合查询条件")
    private List<List<QueryConditionDto>> groupConditions;

    @ApiModelProperty(value = "数据项可用条件", notes = "将不满足条件的数据标记为禁用")
    private List<List<QueryConditionDto>> enableConditions;

    @ApiModelProperty("自定义排序")
    private List<QueryOrderDto> sorts;

    @ApiModelProperty(value = "显示来源字段id", notes = "若存在，则用以替换显示字段的值")
    private String sourceFieldId;

}
