package cn.bctools.report.model.univer.style;

import cn.bctools.report.model.univer.conf.UStyleN;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel("univer 单元格样式")
public class UStyle implements Serializable {

    private static final long serialVersionUID = 7272895391077262054L;
    @ApiModelProperty("字体")
    private String ff;

    @ApiModelProperty(value = "字体大小",notes = "单位pt")
    private Integer fs;

    @ApiModelProperty(value = "是否斜体",notes = "0 表示不斜体，1 表示斜体")
    private Integer it;

    @ApiModelProperty(value = "是否加粗",notes = "0 表示不加粗，1 表示加粗")
    private Integer bl;

    @ApiModelProperty("下划线")
    private LineStyle ul;

    @ApiModelProperty("删除线")
    private LineStyle st;

    @ApiModelProperty("上划线")
    private LineStyle ol;

    @ApiModelProperty("背景颜色")
    private RGB bg;

    @ApiModelProperty("边框")
    private BD bd;

    @ApiModelProperty("字体颜色")
    private RGB cl;

    @ApiModelProperty(value = "上标下标",notes = "1 表示正常，2 表示下标，3 表示上标")
    private Integer va;

    @ApiModelProperty("文字旋转")
    private TR tr;

    @ApiModelProperty(value = "文字方向",notes = "1 表示从左到右，2 表示从右到左")
    private Integer td;

    @ApiModelProperty(value = "水平对齐方式",notes = "1 表示左对齐，2 表示居中，3 表示右对齐")
    private Integer ht;

    @ApiModelProperty(value = "垂直对齐方式",notes = "1 表示顶部对齐，2 表示居中，3 表示底部对齐")
    private Integer vt;

    @ApiModelProperty(value = "截断溢出",notes = "1 表示溢出，2 表示截断，3 表示自动换行")
    private Integer tb;

    @ApiModelProperty("内边距")
    private PD pd;

    @ApiModelProperty("格式化")
    private UStyleN n;
}