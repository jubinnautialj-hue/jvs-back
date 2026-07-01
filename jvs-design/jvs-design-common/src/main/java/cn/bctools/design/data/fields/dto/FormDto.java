package cn.bctools.design.data.fields.dto;

import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.design.data.fields.enums.FormTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 表单配置项
 *
 * @author auto
 */
@Data
@ApiModel("表单页")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FormDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("表单id")
    private String id;
    @ApiModelProperty("表单名称")
    private String name;
    @ApiModelProperty("应用id")
    private String jvsAppId;
    @ApiModelProperty("表单类型不能为空")
    private FormTypeEnum classify;
    @ApiModelProperty("归类")
    private String type;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计json对象")
    private String viewJson;
    @ApiModelProperty("是否已发布")
    private Boolean isDeploy;
    @ApiModelProperty("支持的客户端类型")
    private SupportedClientType supportedClientType;
    @ApiModelProperty("是否校验登录, 默认为true")
    private Boolean checkLogin;
    @ApiModelProperty("回调地址")
    private String callbackUrl;
    @ApiModelProperty("回调方式(true:异步; false:同步)")
    private Boolean callbackAsync;

    @ApiModelProperty("权限设置")
    private List<JSONObject> role;
    @ApiModelProperty("权限类型,true 应用 权限，false 自定义权限")
    private Boolean roleType;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("数据模型ID")
    private String dataModelId;

    @ApiModelProperty("数据模型ID")
    private List<FieldBasicsHtml> deleteFieldsKey;

    @ApiModelProperty("二维码标签设置")
    private JSONObject tagSetting;

}
