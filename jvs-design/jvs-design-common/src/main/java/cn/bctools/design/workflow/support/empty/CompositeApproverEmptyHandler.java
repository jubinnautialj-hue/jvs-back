package cn.bctools.design.workflow.support.empty;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesUserEmptyEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 审批人为空处理策略
 */
@Component
public class CompositeApproverEmptyHandler {

    private final Map<String, ApproverEmptyInterface> approverEmptyInterfaceMap;

    @Autowired
    public CompositeApproverEmptyHandler(List<ApproverEmptyInterface> approverEmptyInterfaces) {
        this.approverEmptyInterfaceMap = approverEmptyInterfaces.stream().collect(Collectors.toMap(ApproverEmptyInterface::getType, Function.identity()));
    }

    /**
     * 审批人为空处理
     * @param userDtos 审批人集合
     * @param node 节点
     * @param runtimeData 运行时信息
     */
    public List<UserDto> execute(List<UserDto> userDtos, Node node, RuntimeData runtimeData) {
        if (CollectionUtils.isNotEmpty(userDtos)) {
            return userDtos;
        }

        NodePropertiesUserEmptyEnum emptyEnum = node.getProps().getUserEmpty();
        if (ObjectUtils.isEmpty(emptyEnum) || ObjectUtils.isEmpty(NodePropertiesUserEmptyEnum.getByValue(emptyEnum.getValue()))) {
            throw new BusinessException("环节未找到审批人任务不能继续流转", node.getName());
        }

        ApproverEmptyInterface approverEmptyInterface = Optional.ofNullable(approverEmptyInterfaceMap.get(node.getProps().getUserEmpty().getValue())).orElseThrow(() -> new BusinessException("审批人为空处理方式暂未支持"));
        return approverEmptyInterface.execute(node, runtimeData);
    }
}
