package cn.bctools.design.data.fields.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.*;

/**
 * 数据字段类型
 *
 * @Author: GuoZi
 */
@Getter
public enum DataFieldType {


    /**
     * Input data field type.
     */
    input("单行文本", FormComponentType.basic, true, String.class, true, true),
    /**
     * Textarea data field type.
     */
    textarea("多行文本", FormComponentType.basic, true, String.class, true, true),
    /**
     * Divider data field type.
     */
    divider("分割线", FormComponentType.basic, false, null, true, false),
    /**
     * P data field type.
     */
    p("小标题", FormComponentType.basic, false, null, true, false),
    /**
     * Select data field type.
     */
    select("下拉框", FormComponentType.basic, true, List.class, true, true),
    /**
     * Input number data field type.
     */
    inputNumber("计数器", FormComponentType.basic, true, Number.class, true, true),
    /**
     * Switch data field type.
     */
    SWITCH("开关", FormComponentType.basic, true, Boolean.class, true, true),
    /**
     * Date picker data field type.
     */
    datePicker("日期", FormComponentType.basic, true, Date.class, true, true),
    /**
     * Time select data field type.
     */
    timeSelect("固定时间", FormComponentType.basic, true, Date.class, true, true),
    /**
     * Time picker data field type.
     */
    timePicker("任意时间", FormComponentType.basic, true, Date.class, true, true),
    /**
     * Radio data field type.
     */
    radio("单选", FormComponentType.basic, true, String.class, true, true),
    /**
     * Checkbox data field type.
     */
    checkbox("多选", FormComponentType.basic, true, List.class, true, true),
    /**
     * Image data field type.
     */
    image("图片", FormComponentType.basic, false, String.class, true, true),
    /**
     * File data field type.
     */
    file("文件", FormComponentType.basic, false, String.class, true, true),
    /**
     * Color select data field type.
     */
    colorSelect("颜色选择", FormComponentType.basic, true, String.class, false, false),
    /**
     * Icon select data field type.
     */
    iconSelect("图标选择", FormComponentType.basic, true, String.class, false, false),
    /**
     * Button data field type.
     */
    button("按钮", FormComponentType.basic, false, Map.class, true, false),
    /**
     * Box data field type.
     */
    box("描述框", FormComponentType.basic, false, String.class, true, false),
    /**
     * Link data field type.
     */
    link("链接", FormComponentType.basic, true, String.class, true, true),
    /**
     * Iframe data field type.
     */
    iframe("网页", FormComponentType.basic, false, String.class, false, true),
    /**
     * Serial number data field type.
     */
    serialNumber("流水号", FormComponentType.basic, true, String.class, true, true),


    /**
     * Cascader data field type.
     */
    cascader("级联选择", FormComponentType.advance, true, Map.class, true, true),
    /**
     * Flow node data field type.
     */
    flowNode("动态流程", FormComponentType.advance, true, Map.class, true, true),
    /**
     * Dynamic form data field type.
     */
    dynamicForm("动态表单", FormComponentType.advance, true, List.class, true, false),
    /**
     * Html editor data field type.
     */
    htmlEditor("富文本", FormComponentType.advance, true, String.class, true, true),
    /**
     * Image upload data field type.
     */
//修改兼容
    imageUpload("上传图片", FormComponentType.advance, false, Map.class, true, true),
    /**
     * File upload data field type.
     */
    fileUpload("上传文件", FormComponentType.advance, false, Map.class, true, true),
    /**
     * Signature data field type.
     */
    signature("手写签名", FormComponentType.advance, false, String.class, true, true),
    /**
     * Bluetooth beacon data field type.
     */
    bluetoothBeacon("蓝牙信标", FormComponentType.advance, false, List.class, true, true),

    /**
     * 容器组件
     */
    tab("选项卡", FormComponentType.container, false, Map.class, true, false),
    /**
     * 此组件，只在后端选项卡解析时使用，其它时候不会使用
     */
    tabGenerate("选项卡生成组件", FormComponentType.container, false, Map.class, true, false),
    /**
     * Flow table data field type.
     */
    flowTable("流程设计", FormComponentType.container, false, Map.class, true, false),
    /**
     * Step data field type.
     */
//此组件展示不支持
    step("步骤条", FormComponentType.container, false, Map.class, false, false),

    /**
     * Table form data field type.
     */
    tableForm("表格", FormComponentType.container, false, List.class, true, false),
    /**
     * Report table data field type.
     */
    reportTable("静态表格", FormComponentType.container, false, Map.class, false, false),
    /**
     * Json editor data field type.
     */
    jsonEditor("JSON编译器", FormComponentType.container, false, String.class, false, false),
    /**
     * Page table data field type.
     */
    pageTable("内嵌列表页", FormComponentType.container, false, Map.class, true, false),


    /**
     * 扩展组件
     */
    department("部门选择", FormComponentType.extension, true, Map.class, true, true),

    /**
     * Role data field type.
     */
    role("角色选择", FormComponentType.extension, true, Map.class, true, true),

    /**
     * User data field type.
     */
    user("用户选择", FormComponentType.extension, true, Map.class, true, true),

    /**
     * Job data field type.
     */
    job("岗位选择", FormComponentType.extension, true, Map.class, true, true),

    /**
     * Timeline data field type.
     */
    timeline("时间线", FormComponentType.extension, false, Map.class, false, false),

    /**
     * Position map data field type.
     */
    positionMap("定位", FormComponentType.basic, false, String.class, true, true),

    ;

    @ApiModelProperty("描述")
    private final String desc;
    @ApiModelProperty("是否支持移动端")
    private final Boolean mobile;
    @ApiModelProperty("组件类别")
    private final FormComponentType itemType;
    @ApiModelProperty("是否支持导出")
    private final boolean enableExport;
    @ApiModelProperty("Java类型")
    private final Class<?> aClass;
    @ApiModelProperty("是否支持数据联动")
    private final boolean enableDataLinkage;

    /**
     * 为保证修改设计时不直接删除历史的数据。
     * 所有当修改设计时如果类型发生了变化我们将在界面上进行提示，如果用户确认修改后。我们可以进行强制转换操作，强制转换将会把历史数据清除掉。此属性用于类型转换范围确定
     */
    @ApiModelProperty("转换排序")
    private List<DataFieldType> transformationList;

    /**
     * 用于导入的时候，自动转换和用户转换信息
     * 比如用户导入时，excel为 张三，但数据库中存的为1
     *
     * @return
     */
    public static final List<DataFieldType> SELECT_CONVERSION = new ArrayList<DataFieldType>() {{
        add(DataFieldType.select);
        add(DataFieldType.radio);
        add(DataFieldType.role);
        add(DataFieldType.user);
        add(DataFieldType.department);
        add(DataFieldType.job);
        add(DataFieldType.cascader);
        add(DataFieldType.checkbox);
    }};


    /**
     * Gets transformation list.
     *
     * @return the transformation list
     */
    public List<DataFieldType> getTransformationList() {
        List<DataFieldType> transformationList = new ArrayList<DataFieldType>() {{
            add(DataFieldType.input);
        }};
        switch (this) {
            case signature:
                transformationList.add(DataFieldType.signature);
            case file:
                transformationList.add(DataFieldType.fileUpload);
                break;
            case fileUpload:
                transformationList.add(DataFieldType.file);
                break;
            case image:
                transformationList.add(DataFieldType.imageUpload);
                break;
            case imageUpload:
                transformationList.add(DataFieldType.image);
                break;
            default:
        }
        return transformationList;
    }

    /**
     * 容器组件
     */
    public static final List<DataFieldType> CONTAINER = new ArrayList<DataFieldType>() {{
        add(DataFieldType.tab);
        add(DataFieldType.step);
        add(DataFieldType.tableForm);
    }};


    /**
     * Gets by name.
     *
     * @param name the name
     * @return the by name
     */
    public static DataFieldType getByName(String name) {
        for (DataFieldType value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }

    DataFieldType(String desc, FormComponentType itemType, boolean enableExport, Class<?> aClass, Boolean mobile, Boolean enableDataLinkage) {
        this.desc = desc;
        this.itemType = itemType;
        this.enableExport = enableExport;
        this.aClass = aClass;
        this.mobile = mobile;
        this.enableDataLinkage = enableDataLinkage;
    }


    /**
     * 保留字不允许的名字
     */
    public static List RESERVED_WORDS = Arrays.asList("dataId", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default",
            "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof"
            , "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while");


    /**
     * 非模型字段类型
     */
    private static final List<DataFieldType> NOT_MODEL_FIELD_TYPES = new ArrayList<DataFieldType>() {{
        add(DataFieldType.divider);
        add(DataFieldType.p);
        add(DataFieldType.button);
        add(DataFieldType.tabGenerate);
        add(DataFieldType.step);
        add(DataFieldType.timeline);
        add(DataFieldType.flowTable);
        add(DataFieldType.reportTable);
        add(DataFieldType.colorSelect);

    }};


    /**
     * 获取非模型字段类型
     *
     * @return 字段类型
     */
    public static List<DataFieldType> notModelFieldTypes() {
        return NOT_MODEL_FIELD_TYPES;
    }
}
