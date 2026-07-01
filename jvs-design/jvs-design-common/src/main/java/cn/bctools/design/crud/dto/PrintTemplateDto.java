package cn.bctools.design.crud.dto;

import cn.bctools.design.crud.entity.enums.DesignTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("打印模板")
public class PrintTemplateDto {

    @ApiModelProperty(value = "打印模板id", notes = "修改时必传")
    private String id;

    @ApiModelProperty(value = "关联设计id", required = true)
    @NotBlank(message = "设计id不能为空")
    private String designId;

    @ApiModelProperty(value = "模板名称", required = true)
    @NotBlank(message = "请填写模板名称")
    private String name;

    @ApiModelProperty(value = "模板类型", required = true)
    @NotNull(message = "请选择模板类型")
    private DesignTypeEnum designType;

    @ApiModelProperty(value = "模板设计JSON", notes = "模板类型为自定义时保存模板设计")
    private String design;

    @ApiModelProperty(value = "模板文件地址", notes = "模板类型为word模板时保存word文件地址")
    private String fileUrl;

    @ApiModelProperty(value = "文件类型", notes = "模板为上传文件时保存文件类型")
    private String fileType;

    @ApiModelProperty(value = "FALSE-禁用，TRUE-启用")
    private Boolean enableFlag;
}
