package cn.bctools.design.workflow.support.process.impl;

import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.process.ProcessInterface;
import cn.bctools.design.workflow.support.process.ProcessResult;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 流转进度暂停在当前节点。（不继续流转、不结束任务）
 */
@Slf4j
@Component
@AllArgsConstructor
public class HangProcess  implements ProcessInterface {

    @Override
    public FlowNextTypeEnum getType() {
        return FlowNextTypeEnum.HANG;
    }

    @Override
    public void execute(FlowResult flowResult) {
        log.debug("【挂起】节点：{}", FlowContextUtil.context().getRuntimeData().getNodeId());
        FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
    }
}
