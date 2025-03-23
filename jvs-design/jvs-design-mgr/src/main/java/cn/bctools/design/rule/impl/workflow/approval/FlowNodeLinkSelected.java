package cn.bctools.design.rule.impl.workflow.approval;


import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;
import cn.bctools.design.workflow.service.ApprovalRecordService;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowNodeLinkSelected implements LinkFieldSelected<String> {

    private final ApprovalRecordService approvalRecordService;

    @Override
    public Object link(String id, String field) {
        List<ApprovalRecordFieldDto> approvalRecordFields = approvalRecordService.getApprovalRecordFields(id);
        return approvalRecordFields.stream().map(e -> {
            return new ParameterOption().setLabel(e.getFieldName()).setValue(e.getFieldKey());
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> fields() {
        List<String> objects = new ArrayList<>();
        objects.add("nodeId");
        return objects;
    }
}
