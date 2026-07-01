package cn.bctools.design.template.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 模板保存
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("模板保存")
public class TemplateSaveDto {

    @NotBlank(message = "设计id不能为空")
    @ApiModelProperty("设计id")
    private String designId;

    @NotBlank(message = "标题不能为空")
    @Length(max = 64, min = 1, message = "标题最多64字")
    @ApiModelProperty("模板名称")
    private String name;

    @NotBlank(message = "模板分类不能为空")
    @ApiModelProperty("模板分类")
    private String category;

    @ApiModelProperty("封面图片")
    private String cover;

    @NotNull(message = "套件类型不能为空")
    @ApiModelProperty("套件类型")
    private DesignType type;

}
