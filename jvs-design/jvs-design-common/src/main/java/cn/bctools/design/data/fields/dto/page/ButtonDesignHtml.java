package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.project.dto.ButtonSettingDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * <按钮设计>
 *
 * @author auto
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ButtonDesignHtml extends ButtonSettingDto {

    @ApiModelProperty("表单ID")
    private String formId;
    @ApiModelProperty("按钮位置")
    private String position;
    @ApiModelProperty("按钮类型")
    private ButtonTypeEnum type;
    @ApiModelProperty("表单的基础信息")
    private Map form;
    @ApiModelProperty("是否为默认")
    private Boolean isDefault;

    @ApiModelProperty(value = "外链地址或内嵌地址", notes = "按钮类型为内嵌地址或外链地址时填写")
    private String address;
    @ApiModelProperty(value = "调逻辑的时的自定义参数", notes = "当同一个列表的不同按钮走同一个逻辑时配置的额外参数")
    private String customParameterIn;

    @ApiModelProperty(value = "导入选中字段", notes = "按钮类型为导入时有")
    private List<DataTableFieldDesignHtml> importFields;
    @ApiModelProperty(value = "导出选中字段", notes = "按钮类型为导出时有")
    private List<DataTableFieldDesignHtml> exportFields;
    @ApiModelProperty(value = "动态控制按钮显隐", notes = "表达式控制按钮动态显示隐藏")
    private String expressControl;

    @ApiModelProperty(value = "逻辑引擎标识")
    private String secret;

    @ApiModelProperty(value = "逻辑引擎按钮二次确认文本")
    private String confirm;

    @ApiModelProperty(value = "按钮打开列表页的查询条件")
    private List<PageQueryHtml> pageQuery;

    @ApiModelProperty(value = "打开方式", notes = "列表页按钮打开列表页或表单")
    private String openType;

    @ApiModelProperty(value = "打开列表的模型id")
    private String dataModelId;

    @ApiModelProperty(value = "按钮名称拼音")
    private String namePinYin;
    @ApiModelProperty(value = "按钮类型")
    private String buttonType;
    @ApiModelProperty(value = "是否为默认按钮")
    private Boolean flag;

    @ApiModelProperty(value = "按钮打开的列表页的查询条件绑定关系")
    private List<PageSearchHtml> pageSearch;
    @ApiModelProperty(value = "按钮打开的列表页的底部按钮")
    private List<PageBottonBtnsHtml> pageBottomBtns;
    @ApiModelProperty(value = "按钮打开列表页的弹框标题")
    private String dialogTitle;
    @ApiModelProperty(value = "按钮打开列表页的弹框宽度")
    private Integer dialogWidth;
    @ApiModelProperty(value = "excel解析sheel名称")
    private String sheelName;
    @ApiModelProperty(value = "文件连接地址")
    private String templateFileLink;
    @ApiModelProperty(value = "文件名")
    private String templateFileName;
}
