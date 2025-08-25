package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.form.FormTipHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表单组件的基本设计数据
 *
 * @Author: GuoZi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "表格组件数据")
public class BaseItemHtml extends FieldBasicsHtml {


    @ApiModelProperty("组件类型")
    private DataFieldType type;
    @ApiModelProperty("组件对应的字段中文名")
    private String label;
    @ApiModelProperty("是否隐藏")
    private Boolean display;
    @ApiModelProperty("提示")
    private FormTipHtml tips;



    @ApiModelProperty("hasChildren")
    private Boolean hasChildren;
    @ApiModelProperty("可否多选")
    private Boolean multiple;
    @ApiModelProperty("collapsetags")
    private Boolean collapsetags;
    @ApiModelProperty("可否搜索")
    private Boolean filterable;
    @ApiModelProperty("可否创建选项")
    private Boolean allowcreate;
    @ApiModelProperty("显示路径")
    private Boolean showalllevels;
    @ApiModelProperty("传递路径")
    private Boolean emitPath;

    @ApiModelProperty("占位内容")
    private String placeholder;
    @ApiModelProperty("可否清空")
    private Boolean clearable;
    @ApiModelProperty("宽度比例")
    private int span;



}
