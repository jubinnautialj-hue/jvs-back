package cn.bctools.design.workflow.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 * 审批节点记录字段
 */
@Data
@Accessors(chain = true)
public class ApprovalRecordFieldDto {
    /**
     * 记录字段key
     */
    private String fieldKey;

    /**
     * 记录字段名
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private DataFieldType dataFieldType;
}
