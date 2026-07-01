package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyReqDto;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyResDto;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流代理 服务类
 */
public interface FlowTaskProxyService extends IService<FlowTaskProxy> {

    /**
     * 新增代理
     *
     * @param userDto       登录用户
     * @param flowTaskProxy 代理设置
     */
    void create(UserDto userDto, FlowTaskProxy flowTaskProxy);

    /**
     * 分页查询
     *
     * @param userDto 用户
     * @param page    分页条件
     * @param dto     筛选条件
     * @return 代理分页数据
     */
    Page<PageFlowTaskProxyResDto> page(UserDto userDto, Page<FlowTaskProxy> page, PageFlowTaskProxyReqDto dto);

    /**
     * 获取已生效的代理配置
     *
     * @return 被代理人已生效的代理配置
     */
    List<FlowTaskProxy> getEffectiveProxy();

    /**
     * 获取指定被代理人已生效的代理配置
     *
     * @param userId 被代理人
     * @return 代理配置
     */
    FlowTaskProxy getEffectiveProxyByUserId(String userId);

    /**
     * 批量获取指定被代理人已生效的代理配置
     *
     * @param userIds 被代理人集合
     * @return 代理配置
     */
    List<FlowTaskProxy> getEffectiveProxyByUserIds(Collection<String> userIds);


}
