package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.workflow.entity.FlowTaskNoticeLog;
import cn.bctools.design.workflow.mapper.FlowTaskNoticeLogMapper;
import cn.bctools.design.workflow.service.FlowTaskNoticeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author wayne
 * 待办提醒日志 服务实现类
 */
@Service
@AllArgsConstructor
public class FlowTaskNoticeLogServiceImpl extends ServiceImpl<FlowTaskNoticeLogMapper, FlowTaskNoticeLog> implements FlowTaskNoticeLogService {

}
