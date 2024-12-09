package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.design.workflow.entity.dto.ProxyDto;
import cn.bctools.design.workflow.entity.dto.TransferDto;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.service.FlowTaskProxyService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.TransferRuntimeDto;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageHandler;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 任务转交（用户可将当前节点的任务转交给其他人，只转交当前节点的任务）
 */
@Slf4j
@Component
@AllArgsConstructor
public class TransferFunction extends AbstractFunctionHandler<List<ProxyDto>, TransferRuntimeDto> {
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskProxyService flowTaskProxyService;
    private final TimeLimitMessageHandler timeLimitMessageHandler;
    private final FlowTaskNodeService flowTaskNodeService;

    /**
     * 任务转交
     *
     * @param node 节点
     * @param transferRuntime 转交数据
     * @return 任务移交记录
     */
    @Override
    public List<ProxyDto> invoke(Node node, TransferRuntimeDto transferRuntime) {
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN);
        TransferDto transferDto = transferRuntime.getTransfer();
        // 有转交配置，则转交给指定代理人（审核人选择指定人员转交任务）
        if (ObjectNull.isNotNull(transferDto)) {
            List<FlowTaskPerson> flowTaskPersons = transferUpdatePerson(time, transferRuntime, node.getId());
            // 发送延时任务（校验审核是否超时等功能）
            timeLimitMessageHandler.delayedTask(node, transferRuntime.getFlowTask(), flowTaskPersons);
            return Collections.emptyList();
        } else {
            // 无转交配置，且待自动选择代理人的被代理人id集合不为空时，需要根据代理设置，自动选择任务转交人
            if (CollectionUtils.isEmpty(transferRuntime.getUserIds())) {
                return Collections.emptyList();
            }
            return proxyTransfer(time, transferRuntime.getFlowTask().getId(), node.getId(), transferRuntime.getUserIds());
        }
    }

    /**
     * 根据代理配置，自动选择转交人
     *
     * @param proxyTime  代理时间
     * @param nodeId     节点id
     * @param flowTaskId 任务id
     * @param userIds    待转交用户id集合
     */
    private List<ProxyDto> proxyTransfer(String proxyTime, String flowTaskId, String nodeId, List<String> userIds) {
        List<FlowTaskProxy> flowTaskProxys = flowTaskProxyService.getEffectiveProxy();
        List<FlowTaskProxy> currentUserProxys = flowTaskProxys.stream().filter(proxy -> userIds.contains(proxy.getUserId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(currentUserProxys)) {
            return Collections.emptyList();
        }
        // 获取用户的最终代理信息
        for (FlowTaskProxy flowTaskProxy : currentUserProxys) {
            getEndProxy(flowTaskProxy, flowTaskProxy, flowTaskProxys);
        }
        List<ProxyDto> transfers = new ArrayList<>();
        currentUserProxys.forEach(proxy -> {
            ProxyDto proxyDto = new ProxyDto();
            proxyDto.setProxy(Boolean.TRUE);
            proxyDto.setUserId(proxy.getUserId());
            proxyDto.setUserName(proxy.getUserName());
            proxyDto.setProxyUserId(proxy.getProxyUserId());
            proxyDto.setProxyUserName(proxy.getProxyUserName());
            proxyDto.setTime(proxyTime);
            proxyDto.setDirections(StringUtils.isBlank(proxy.getDescription()) ? "代理设置" : proxy.getDescription());
            transfers.add(proxyDto);
        });

        // 保存转交记录
        flowTaskNodeService.saveTransfer(transfers, flowTaskId, nodeId);
        return transfers;
    }

    /**
     * 获取指定用户的最终代理信息
     * <p>
     * 可能出现多层代理设置。
     * 如：被代理人A > 代理人B
     * 被代理人B > 代理人C
     * 被代理人C > 代理人D
     * 应该获取最终代理人D
     *
     * @param currentProxy   当前代理配置
     * @param flowTaskProxy  比较的代理配置
     * @param flowTaskProxys 生效的代理配置集合
     * @return 用户的最终代理信息
     */
    private FlowTaskProxy getEndProxy(FlowTaskProxy currentProxy, FlowTaskProxy flowTaskProxy, List<FlowTaskProxy> flowTaskProxys) {
        FlowTaskProxy flowTaskProxies = flowTaskProxys.stream().filter(proxy -> flowTaskProxy.getProxyUserId().equals(proxy.getUserId())).findFirst().orElse(null);
        if (ObjectNull.isNull(flowTaskProxies)) {
            // 指定用户最终代理
            // 如：上述示例中的被代理人A，直接指向代理人D
            currentProxy.setProxyUserId(flowTaskProxy.getProxyUserId());
            currentProxy.setProxyUserName(flowTaskProxy.getProxyUserName());
            return currentProxy;
        }
        return getEndProxy(currentProxy, flowTaskProxies, flowTaskProxys);
    }


    /**
     * 转交审核人
     *
     * @param transferTime 转交时间
     * @param dto 转交参数
     * @param nodeId 转交节点
     */
    private List<FlowTaskPerson> transferUpdatePerson(String transferTime, TransferRuntimeDto dto, String nodeId) {
        UserDto userDto = dto.getUserDto();
        TransferDto transfer = dto.getTransfer();
        transfer.setTime(transferTime);
        String flowTaskId = dto.getFlowTask().getId();
        if (StringUtils.isBlank(transfer.getProxyUserId()) || StringUtils.isBlank(transfer.getProxyUserName()) ) {
            throw new BusinessException("请选择转交对象");
        }
        // 被转交人默认为当前用户
        if (ObjectNull.isNull(transfer.getUserId())) {
            transfer.setUserId(userDto.getId());
            transfer.setUserName(userDto.getRealName());
        }
        List<FlowTaskPerson> flowTaskPersons = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getFlowTaskId, flowTaskId)
                .eq(FlowTaskPerson::getNodeId, nodeId)
                .eq(FlowTaskPerson::getUserId, transfer.getUserId())
                .ne(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PROCESSED));
        if (CollectionUtils.isEmpty(flowTaskPersons)) {
            return Collections.emptyList();
        }
        // 若转交人已存在代理配置，则根据代理配置设置转交人
        FlowTaskProxy proxy = flowTaskProxyService.getEffectiveProxyByUserId(transfer.getProxyUserId());
        if (ObjectNull.isNotNull(proxy)) {
            throw new BusinessException("您选择的用户已将任务转交他人请重新选择");
        }

        // 任务转交
        flowTaskPersons.forEach(person -> {
            person.setUserId(transfer.getProxyUserId());
            person.setUserName(transfer.getProxyUserName());
        });
        flowTaskPersonService.updateBatchById(flowTaskPersons);
        // 保存转交记录
        ProxyDto proxyDto = BeanCopyUtil.copy(transfer, ProxyDto.class);
        proxyDto.setProxy(Boolean.FALSE);
        flowTaskNodeService.saveTransfer(Collections.singletonList(proxyDto), flowTaskId, nodeId);
        return flowTaskPersons;
    }
}
