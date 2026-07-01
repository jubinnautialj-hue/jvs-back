package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowTaskEventCompensate;
import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zhuxiaokang
 */
public interface FlowTaskEventCompensateService extends IService<FlowTaskEventCompensate> {

    /**
     * 保存补偿事件
     *
     * @param eventType 事件类型
     * @param eventBody 事件内容
     */
    void save(FlowTaskEventTypeEnum eventType, String eventBody);
}
