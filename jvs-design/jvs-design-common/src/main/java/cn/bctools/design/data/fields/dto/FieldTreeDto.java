package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
/**
 * @author wl
 */
@Data
@Accessors(chain = true)
public class FieldTreeDto {

    @ApiModelProperty("数据值")
    private String value;

    @ApiModelProperty("显示数据")
    private String label;

    @ApiModelProperty("树形显示父级字段key")
    private String secTitle;

    @ApiModelProperty(value = "筛选条件", notes = "设计中配置的筛选条件")
    private QueryListDto filter;

    @ApiModelProperty(value = "搜索条件", notes = "用户使用时动态设置的条件")
    private QueryListDto query;

}
