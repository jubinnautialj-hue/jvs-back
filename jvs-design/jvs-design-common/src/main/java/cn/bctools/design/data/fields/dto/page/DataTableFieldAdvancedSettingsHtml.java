package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.dto.KvHtml;
import cn.bctools.design.data.fields.enums.DisplayFormType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <字段-高级设置>
 *
 * @author auto
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataTableFieldAdvancedSettingsHtml extends DataTableFieldDesignHtml {

    @ApiModelProperty("文本颜色")
    private String textcolor;
    /**
     * 公式id
     */
    private String formula;
    /**
     * 公式的内容如果公式为空,则直接获取值，如果公式不为空，直接执行公式,表示此字段为公式组合展示字段
     */
    private String combiningFieldFormulaContent;
    @ApiModelProperty("背景颜色")
    private String backColor;
    @ApiModelProperty("列表页字段打开的表单的id")
    private String openFormId;

    /**
     * --------------------链接----------------------
     **/
    @ApiModelProperty(value = "打开方式")
    private String openType;
    @ApiModelProperty(value = "链接显示文字", notes = "文本类型为链接时")
    private String text;

    /**
     * --------------------图片----------------------
     **/
    @ApiModelProperty(value = "图宽", notes = "文本为图片时")
    private Integer width;
    @ApiModelProperty(value = "图高", notes = "文本为图片时")
    private Integer height;

    /**
     * 字典
     */
    @ApiModelProperty(value = "方式", notes = "系统 配置 接口")
    private String dictSource;

    @ApiModelProperty(value = "字典", notes = "配置")
    private List<KvHtml> dictionary;
    @ApiModelProperty(value = "是否有排序,和复制的图标开关")
    private List<String> tools;
    @ApiModelProperty(value = "系统字典ID", notes = "系统")
    private String dictionaryUniqId;
    @ApiModelProperty(value = "字典label字段名称", notes = "显示配置中的类型为字典的时候，数据来源为api的时候，对应的取值字段名称")
    private String dictLabelFieldName;
    @ApiModelProperty(value = "字典value字段名称", notes = "显示配置中的类型为字典的时候，数据来源为api的时候，对应的取值字段名称")
    private String dictValueFieldName;
    @ApiModelProperty(value = "关联数据模型ID")
    private String dataModelId;
    @ApiModelProperty(value = "网络请求字段")
    private String url;
    @ApiModelProperty(value = "字段动态显示")
    private List<ConditionControlHtml> conditionControl;
    @ApiModelProperty(value = "查询条件宽占比")
    private Integer searchSpan;
    @ApiModelProperty(value = "是否使用动态样式")
    private boolean conditionControlEnable;
    @ApiModelProperty(value = "卡片样式-字段显示位置")
    private String cardPosition;
    /**
     * 动态socket数据绑定*
     */
    @ApiModelProperty(value = "是否绑定", notes = "如果绑定了，后续的数据全来至于socket ")
    Boolean dynamicRefresh;
    @ApiModelProperty(value = "显示类型")
    DisplayFormType displayFormType;

    @ApiModelProperty(value = "显示方式-配置显示关联模型")
    private ModelDisplayHtml modelDisplay;


}
