package cn.bctools.data.factory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 字段设置
 * 函数对应关系
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("字段设置-函数对应关系")
@Accessors(chain = true)
public class FieldTypeFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("前端展示")
    private String label;
    @ApiModelProperty("前端展示-映射")
    private String value;
    @ApiModelProperty("字段格式")
    private String format;
    @ApiModelProperty("子级")
    private List<FieldTypeFunction> children;


}
