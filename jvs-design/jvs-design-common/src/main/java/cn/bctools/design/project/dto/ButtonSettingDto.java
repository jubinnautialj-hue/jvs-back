package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.form.AssociationHtml;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 设计的按钮配置
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class ButtonSettingDto {

    @ApiModelProperty("按钮名称")
    private String name;
    @ApiModelProperty("按钮类型")
    private ButtonTypeEnum type;
    @ApiModelProperty("是否启用")
    private Boolean enable;
    @ApiModelProperty("移动端是否启用")
    private Boolean mobileEnable;
    @ApiModelProperty("formType")
    private String formType;
    @ApiModelProperty("添加的自定义规则")
    private List<AssociationHtml> association;
    @ApiModelProperty("权限标识")
    private String permissionFlag;
    @ApiModelProperty("是否关闭窗口")
    private Boolean closeable;

    @ApiModelProperty("表达式id")
    private String formula;
    @ApiModelProperty("移动端的表达式id")
    private String mobileFormula;

    @ApiModelProperty(value = "是否启用表单校验")
    private Boolean validateable;

}
