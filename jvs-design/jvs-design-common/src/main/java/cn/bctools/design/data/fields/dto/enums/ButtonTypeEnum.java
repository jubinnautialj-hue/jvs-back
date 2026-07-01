package cn.bctools.design.data.fields.dto.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <按钮类型>
 *
 * @author auto
 **/
@ApiModel("按钮类型")
@Getter
@AllArgsConstructor
public enum ButtonTypeEnum {

    /**
     * 按钮类型
     */
    btn_add("新增", true, true, new String[]{"top"}),
    btn_modify("修改", true, true, new String[]{"line"}),
    btn_delete("删除", false, false, new String[]{"line"}),
    btn_detail("详情", true, true, new String[]{"line"}),
    oa_workflow("流程办理", false, false, new String[]{"line"}),
    oa_workflow_progress("流程进度", true, false, new String[]{"line"}),
    oa_workflow_restart("重新发起", true, false, new String[]{"line"}),
    oa_workflow_cancel("撤回", true, false, new String[]{"line"}),
    btn_import("导入", false, false, new String[]{"top"}),
    btn_export("导出", false, false, new String[]{"top"}),
    btn_download_template("下载模板", false, true, new String[]{"top"}),
    btn_form("表单", false, true, new String[]{"top", "line"}),
    btn_page("列表", false, false, new String[]{"line", "top"}),
    btn_rule_design("逻辑引擎", false, false, new String[]{"line", "top"}),
    btn_external_link_address("外链地址", false, false, new String[]{"line", "top"}),
    btn_embedded_address("内嵌地址", false, false, new String[]{"line", "top"}),

    ;
    /**
     * 解释
     */
    private final String description;
    /**
     * 是否为默认按钮
     */
    private final Boolean isDefault;
    /**
     * 是否校验表单有配置值
     */
    private final Boolean checkConfig;
    /**
     * 按钮支持位置 顶部 行内
     */
    private final String[] position;
}
