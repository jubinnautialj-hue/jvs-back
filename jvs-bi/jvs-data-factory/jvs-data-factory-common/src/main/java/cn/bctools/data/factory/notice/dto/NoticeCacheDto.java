package cn.bctools.data.factory.notice.dto;

import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.notice.enums.TriggerTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class NoticeCacheDto {

    /**
     * 数据集id
     */
    private String jvsDataFactoryId;

    /**
     * 租户id
     */
    private String tenantId;
    /**
     * 执行人
     */
    private String operateUser;
    /**
     * 发起类型
     */
    private OperateMethodEnum operateMethod;

    /**
     * 本次消息触发的事件
     */
    private List<TriggerTypeEnum> triggerTypeList;
}
