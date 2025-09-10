package cn.bctools.design.taskNotice.service.impl;

import cn.bctools.design.taskNotice.entity.FlowTaskNoticeLog;
import cn.bctools.design.taskNotice.mapper.FlowTaskNoticeLogMapper;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeLogService;
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
