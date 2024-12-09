package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@ApiModel("列表页")
@NoArgsConstructor
public class CrudPageDto {
    public static final String URL_FORMAT = "crud-design-ui/#/show?id=%s";

    @ApiModelProperty("唯一标识")
    private String id;
    @ApiModelProperty("列表页唯一标识")
    private String code;
    @ApiModelProperty("列表名称")
    private String name;
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计数据")
    private String viewJson;
    @ApiModelProperty("是否已发布")
    private Boolean isDeploy;
    @ApiModelProperty("列表页地址")
    private String url;

}
