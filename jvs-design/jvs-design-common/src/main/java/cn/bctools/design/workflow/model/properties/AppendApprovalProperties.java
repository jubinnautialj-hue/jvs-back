package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import lombok.Data;

import java.util.List;

/**
 * @author zhuxiaokang
 * 加签属性配置
 */
@Data
public class AppendApprovalProperties {

    /**
     * 审批结果是否生效：TRUE-生效(默认)，FALSE-不生效
     */
    private Boolean validApproval = Boolean.FALSE;

    /**
     * 加签位置
     */
    private List<AppendApprovalPointEnum> point;

    /**
     * 操作按钮配置
     */
    private List<FlowButton> btn;
}
