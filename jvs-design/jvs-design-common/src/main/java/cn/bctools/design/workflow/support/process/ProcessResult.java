package cn.bctools.design.workflow.support.process;

import cn.bctools.design.workflow.model.Node;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class ProcessResult {
    private ProcessTypeEnum processType;
    private Node nextNode;


    public static ProcessResult buildEndProcess() {
        return new ProcessResult().setProcessType(ProcessTypeEnum.END);
    }
}
