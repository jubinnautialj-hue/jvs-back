package cn.bctools.design.expression;

import cn.bctools.design.workflow.support.condition.impl.ConditionFunImpl;

/**
 * 表达式使用场景
 *
 * @Author: GuoZi
 */
public interface EnvConstant {

    /**
     * 表单设计-表单项-默认值
     */
    String FORM_ITEM_VALUE = "formItemValue";
    /**
     * 模型字段
     */
    String DATA_ITEM_VALUE = "dataItemValue";
    String THIS_DATA_ITEM_VALUE = "thisDataItemValue";
    /**
     * 表单设计-操作按钮-显隐控制
     */
    String FORM_BUTTON_DISPLAY = "formButtonDisplay";

    /**
     * 表单设计-二维码标签
     */
    String FORM_QR_CODE_TAG = "formQrCodeTag";
    /**
     * 左树结构公式处理
     */
    String LEFT_TREE_BUTTON_DISPLAY = "leftTreeButtonDisplay";
    /**
     * 列表页设计-操作按钮-显隐控制
     */
    String PAGE_BUTTON_DISPLAY = "pageButtonDisplay";

    /**
     * 工作流公式设计——字段
     */
    String FLOW_FUNCTION_ITEM_VALUE = ConditionFunImpl.FLOW_EXPR_USE_CASE;

    /**
     * 工作流自定义流程任务标题设计可用流程任务字段
     */
    String FLOW_TASK_TITLE_ITEM_VALUE = "flowTaskTitleItemValue";

    /**
     * 工作流自定义流程任务标题设计可用数据字段
     */
    String FLOW_TASK_TITLE_MODEL_ITEM_VALUE = "flowTaskTitleModelItemValue";

    /**
     * 表单打印模板在线设计-表单字段
     */
    String PRINT_FORM_ITEM_VALUE = "printFormItemValue";

    /**
     * 表单打印模板在线设计-其它字段
     */
    String PRINT_OTHER_ITEM_VALUE = "printOtherItemValue";

    /**
     * 表单打印模板设计（文件模板）字段对照-表单字段
     */
    String PRINT_FILE_FORM_ITEM_VALUE = "printFileFormItemValue";

    /**
     * 表单打印模板设计（文件模板）字段对照-其它字段
     */
    String PRINT_FILE_OTHER_ITEM_VALUE = "printFileOtherItemValue";

    /**
     * 表单打印模板设计（文件模板）字段对照-系统字段
     */
    String PRINT_SYS_ITEM_VALUE = "printSysItemValue";


}
