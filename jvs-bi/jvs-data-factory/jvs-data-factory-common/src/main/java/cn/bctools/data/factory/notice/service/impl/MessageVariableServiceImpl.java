package cn.bctools.data.factory.notice.service.impl;

import cn.bctools.data.factory.notice.mapper.MessageVariableMapper;
import cn.bctools.data.factory.notice.po.MessageVariable;
import cn.bctools.data.factory.notice.service.MessageVariableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MessageVariableServiceImpl extends ServiceImpl<MessageVariableMapper, MessageVariable> implements MessageVariableService {
}
