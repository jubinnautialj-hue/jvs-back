package cn.bctools.document.service.impl;

import cn.bctools.document.entity.MessageTemplateVariable;
import cn.bctools.document.mapper.MessageTemplateVariableMapper;
import cn.bctools.document.service.MessageTemplateVariableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MessageTemplateVariableServiceImpl extends ServiceImpl<MessageTemplateVariableMapper, MessageTemplateVariable> implements MessageTemplateVariableService {
}
