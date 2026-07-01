package cn.bctools.design.template.dto;

import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.design.data.fields.enums.FormTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 表单模板数据
 *
 * @Author: GuoZi
 */
@Data
public class FormTemplateDto {

    @ApiModelProperty("表单id")
    private String id;
    @ApiModelProperty("表单名称")
    private String name;
    @ApiModelProperty("表单类型不能为空")
    private FormTypeEnum classify;
    @ApiModelProperty("归类")
    private String type;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计json对象")
    private String viewJson;
    @ApiModelProperty("支持的客户端类型")
    private SupportedClientType supportedClientType;
    @ApiModelProperty("是否校验登录, 默认为true")
    private Boolean checkLogin;
    @ApiModelProperty("回调地址")
    private String callbackUrl;
    @ApiModelProperty("回调方式(true:异步; false:同步)")
    private Boolean callbackAsync;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("数据模型ID")
    private String dataModelId;

}
