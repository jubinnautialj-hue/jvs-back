package cn.bctools.design.workflow.support.function.dto;

import cn.bctools.design.workflow.support.RuntimeData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 自动审批
 */
@Data
@Accessors(chain = true)
public class AutoApprovalDto {
    /**
     * TRUE-有自动审批任务，FALSE-无自动审批任务
     */
    private Boolean enable = Boolean.FALSE;

    /**
     * 自动审批任务运行时参数
     */
    private List<RuntimeData> autoTasks = new ArrayList<>();
}
