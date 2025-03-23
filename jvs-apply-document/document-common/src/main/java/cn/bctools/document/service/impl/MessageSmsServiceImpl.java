package cn.bctools.document.service.impl;

import cn.bctools.document.entity.MessageSms;
import cn.bctools.document.mapper.MessageSmsMapper;
import cn.bctools.document.service.MessageSmsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageSmsServiceImpl extends ServiceImpl<MessageSmsMapper, MessageSms> implements MessageSmsService {
}
