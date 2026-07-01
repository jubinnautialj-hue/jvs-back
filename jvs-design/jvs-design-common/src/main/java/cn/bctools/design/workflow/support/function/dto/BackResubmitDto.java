package cn.bctools.design.workflow.support.function.dto;

import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import cn.bctools.design.workflow.model.Node;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class BackResubmitDto {

    /**
     * 是否是回退后第一次提交
     * true-是，false-不是
     */
    private Boolean whetherResubmit;

    /**
     * 根据“被回退的数据重新提交配置”得到的下一步流转节点
     */
    private Node nextNode;

    /**
     * 重新提交规则
     */
    private BackTaskResubmitEnum backTaskResubmit;
}
