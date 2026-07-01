package cn.bctools.design.data.fields.dto.form;


import cn.bctools.design.data.fields.dto.enums.FormDataStatusEnum;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <表单设置>
 *
 * @author auto
 **/
@ApiModel(value = "表单设置")
@Data
@Accessors(chain = true)
public class FormSettingHtml {

    @ApiModelProperty(value = "按钮设置")
    private List<ButtonDesignHtml> btnSetting;
    @ApiModelProperty(value = "对齐方式")
    private String labelposition;
    @ApiModelProperty(value = "请求回显逻辑")
    private String dataEchoRequest;
    @ApiModelProperty(value = "字段宽度")
    private Integer labelwidth;
    @ApiModelProperty(value = "组件尺寸")
    private String formsize;
    @ApiModelProperty("是否全屏")
    private Boolean fullscreen;
    @ApiModelProperty("表单数据状态")
    private FormDataStatusEnum formDataStatus;
    @ApiModelProperty("表单显示方式")
    private String popupType;
    @ApiModelProperty("表单显示宽度")
    private Integer popupWidth;
    @ApiModelProperty("是否显示变更记录")
    private Boolean logsEnable;
    @ApiModelProperty("数据日志跟踪记录")
    private Boolean dataLogEnable;
    @ApiModelProperty("提交按钮")
    private Boolean submitBtn;
    @ApiModelProperty("清空按钮")
    private Boolean emptyBtn;
    @ApiModelProperty("关闭")
    private Boolean cancal;
    @ApiModelProperty("表单选择一个字段的值作为标题显示")
    private String title;

}
