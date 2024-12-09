package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.workflow.entity.FlowQuickReply;
import cn.bctools.design.workflow.mapper.FlowQuickReplyMapper;
import cn.bctools.design.workflow.service.FlowQuickReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 工作流快捷回复配置
 */
@Service
@AllArgsConstructor
public class FlowQuickReplyServiceImpl extends ServiceImpl<FlowQuickReplyMapper, FlowQuickReply> implements FlowQuickReplyService {
}
