package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.design.workflow.entity.enums.ProxyTypeEnum;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.ProxyService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 代理执行服务
 */
@Service
@AllArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    private final FlowTaskService flowTaskService;

    @Async
    @Override
    public void executeProxy(FlowTaskProxy flowTaskProxy) {
        // 离职代理
        if (ProxyTypeEnum.DEPART.equals(flowTaskProxy.getProxyType())) {
            // 立即代理被代理人未完成的工作流任务
            flowTaskService.departTransfer(flowTaskProxy);
        }
    }
}
