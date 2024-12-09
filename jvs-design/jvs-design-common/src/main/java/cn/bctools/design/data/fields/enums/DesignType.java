package cn.bctools.design.data.fields.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设计套件类型
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum DesignType {

    /**
     * 现有设计套件
     */
    app("应用"),
    chart("图表"),
    form("表单"),
    h5("H5"),
    URL("自定义页面"),
    page("列表页"),
    pageToPage("列表页打开列表"),
    screen("大屏"),
    workflow("工作流"),
    data("数据模型"),
    report("报表"),
    rule("逻辑引擎"),
    other("其它"),
    ;

    private final String desc;

}
