package cn.bctools.design.workflow.model;

import lombok.Data;

/**
 * @author zhuxiaokang
 * 表单配置
 */
@Data
public class NodeForm {

    /**
     * 表单id
     */
    private String formId;

    /**
     * true-使用发起人表单，false-不使用发起人表单
     */
    private Boolean sendUserForm = Boolean.TRUE;

    /**
     * 表单版本
     */
    private String version;

    /**
     * 生成默认的表单配置
     * @return
     */
    public static NodeForm buildDefault() {
        NodeForm nodeForm = new NodeForm();
        nodeForm.setFormId("");
        nodeForm.setVersion("");
        return nodeForm;
    }
}
