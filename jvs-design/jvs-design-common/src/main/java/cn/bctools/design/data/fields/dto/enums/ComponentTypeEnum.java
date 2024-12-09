package cn.bctools.design.data.fields.dto.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <前端组件类型>
 *
 * @author auto
 **/
@ApiModel("前端组件类型")
@Getter
@AllArgsConstructor
public enum ComponentTypeEnum {
    /**
     * 前端组件类型
     */
    divider("分割线", false),
    p("文字", false),
    input("文本框", false),
    textarea("文本域", false),
    inputNumber("计数器", false),
    select("下拉框", false),
    cascader("级联选择", false),
    datasource("数据源", false),
    htmlEditor("富文本", false),
    button("按钮", false),
    SWITCH("开关", false),
    slider("滑块", false),
    timeSelect("固定时间", false),
    timePicker("任意时间", false),
    datePicker("日期", false),
    radio("单选", false),
    checkbox("多选", false),
    imageUpload("上传图片", false),
    fileUpload("上传文件", false),
    colorSelect("颜色选择器", false),
    iconSelect("图标选择器", false),
    tab("选项卡", false),
    tableForm("表格", false),
    pageTable("内嵌列表页", true),

    inputReadOnly("只读文本框", true),
    textareaReadOnly("只读文本域", true),
    image("图片", true),
    file("文件", true),
    box("描述框", true),
    link("链接", true),
    iframe("嵌套页面", true),

    user("选择用户", false),
    department("选择部门", false),
    role("选择角色", false),
    post("选择岗位", false),
    chinaArea("地区选择", false),
    ;

    /**
     * 解释
     */
    private final String description;
    /**
     * 是否只读
     */
    private final Boolean readOnly;
}
