package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowTaskProxy;

/**
 * @author zhuxiaokang
 * 代理执行服务
 */
public interface ProxyService {

    /**
     * 立即执行代理
     *
     * @param flowTaskProxy 代理配置
     */
     void executeProxy(FlowTaskProxy flowTaskProxy);
}
