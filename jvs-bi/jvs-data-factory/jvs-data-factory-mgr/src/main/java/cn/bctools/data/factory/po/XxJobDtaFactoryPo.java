package cn.bctools.data.factory.po;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * xxjob参数
 */
@Data
@Accessors(chain = true)
public class XxJobDtaFactoryPo {
    /**
     * 设计id
     */
    private String id;
    /**
     * 租户id
     */
    private String tenantId;
}
