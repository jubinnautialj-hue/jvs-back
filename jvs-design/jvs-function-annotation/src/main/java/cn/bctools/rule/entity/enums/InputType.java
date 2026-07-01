package cn.bctools.rule.entity.enums;

import cn.bctools.common.exception.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe
 */
public enum InputType {

    /**
     * 输入文本
     */
    input(true, true),
    /**
     * 文件类型
     */
    file(true, true),
    /**
     * 文件类型
     */
    files(true, true),
    /**
     * 变量输入框
     */
    variableInput(true, false),
    /**
     * 流程设置
     */
    flowTable(true, true),

    /**
     * 字段关联文本,可选择入参,和模型,和当前登陆用户的关联字段
     */
    linkInput(true, true),
    /**
     * 公式类型
     */
    expressionText(true, true),

    /**
     * 密码框
     */
    password(true, true),

    /**
     * 文本
     */
    text(true, true, String.class),

    /**
     * 数字
     */
    number(true, true, Number.class),

    /**
     * 长文本
     */
    longtext(true, true),

    /**
     * 数组
     */
    list(true, true, List.class),

    /**
     * 添加一个输入类型
     */
    listMap(true, true, List.class),

    /**
     * 对象
     */
    map(true, true, Map.class),

    /**
     * 筛选条件
     */
    filterMap(true, true, Map.class),

    /*
     *//**
     * 关联类型
     *//*
    link(true),*/
    /**
     * 数据模型字段
     */
    dataModelField(false),
    /**
     * 字段条件
     */
    dataModelFilterField(false),
    /**
     * 排序字段
     */
    dataModelOrderField(false),
    //    /**
//     * 字段单选
//     */
//    dataModelSelectField(true),
//    /**
//     * 字段多选
//     */
//    dataModelMultipleSelectedField(true),
    bind(false),
    /**
     * 变量赋值
     */
    assignment(false),

    /**
     * 选择框
     */
    selected(true),
    /**
     * 单个用户组件
     */
//    user(false),
    /**
     * 多选用户组件
     */
    userList(true),
    /**
     * 人员选择器组件,用户,部门,团队
     */
    PersonnelSelector(true),

    /**
     * 树型选择，都可选
     */
    treeSelected(false),

    /**
     * 树型选择，只选择子项
     */
    treeSelectedChild(false),

    /**
     * 多选
     */
    multipleSelected(false),
    /**
     * 动态添加表单或模型列表的字段
     */
    dynamicField(false),

    /**
     * 配置项，如模板，或三方 ，配置项必须提供配置服务接口地址，前端直接调用地址。返回配置ID(true),后端根据ID设置数据值
     */
    configuration(true),

    /**
     * 时间年月日时分秒
     */
    dateTime(true, true),

    /**
     * 时间范围
     */
    dateFrame(true, true),

    /**
     * 代码块
     */
    code(true),
    /**
     * groovy类型
     */
    groovy(true),
    /**
     * 公式类型
     */
    formula(true),

    /**
     * SQL
     */
    sql(true),

    /**
     * JSON
     */
    json(true, true),

    /**
     * html富文本
     */
    html(true, true),
    /**
     * 图片选择器
     */
    imageSelect(true, true),
    /**
     * markdown编辑器
     */
    markdown(true, true),

    /**
     * 请求类型
     */
    http(true),

    /**
     * 开关 值只能是true|null
     */
    onOff(true, true, Boolean.class),
    /**
     * 用户扩展
     */
    user(true, true),
    dept(true, true),
    role(true, true),
    group(true, true),
    job(true, true),
    /**
     * 工作流节点属性
     */
    flowNode(true, false, List.class),
    /**
     * value下拉选择
     */
    mapValueSelected(false, false),
    /**
     * key 下拉选择
     */
    mapKeySelected(false, false);


    InputType(boolean expression, boolean extend, Class<?> dataClass) {
        this.expression = expression;
        this.extend = extend;
        this.dataClass = dataClass;
    }

    InputType(boolean expression, boolean extend) {
        this.expression = expression;
        this.extend = extend;

    }

    /**
     * 是否支持公式， 如果支持公式
     */
    private Boolean expression;
    /**
     * 是否支持扩展
     */
    private Boolean extend = false;
    /**
     * 组件的数据类型，用于检查数据类型是否一致¬
     */
    private Class dataClass = String.class;

    public static InputType cls(Class<?> a) {
        for (InputType value : values()) {
            if (a.equals(value.getDataClass())) {
                return value;
            }
        }
        throw new BusinessException("未匹配类型");
    }

    InputType(Boolean expression) {
        this.expression = expression;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }

    public Boolean getExtend() {
        return extend;
    }

    public Boolean getExpression() {
        return expression;
    }

    public String get() {
        return this.name();
    }
}
