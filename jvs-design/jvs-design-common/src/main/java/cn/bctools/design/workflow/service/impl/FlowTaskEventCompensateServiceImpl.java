package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.workflow.entity.FlowTaskEventCompensate;
import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import cn.bctools.design.workflow.mapper.FlowTaskEventCompensateMapper;
import cn.bctools.design.workflow.service.FlowTaskEventCompensateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 事件补偿服务
 */
@Slf4j
@Service
public class FlowTaskEventCompensateServiceImpl extends ServiceImpl<FlowTaskEventCompensateMapper, FlowTaskEventCompensate> implements FlowTaskEventCompensateService {

    @Override
    public void save(FlowTaskEventTypeEnum eventType, String eventBody) {
        FlowTaskEventCompensate flowTaskEventCompensate = new FlowTaskEventCompensate()
                .setEventType(eventType)
                .setEventBody(eventBody);
        save(flowTaskEventCompensate);
    }
}
