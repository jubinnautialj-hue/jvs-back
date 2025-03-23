package cn.bctools.document.service.impl;

import cn.bctools.document.entity.MessageEmail;
import cn.bctools.document.mapper.MessageEmailMapper;
import cn.bctools.document.service.MessageEmailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageEmailServiceImpl extends ServiceImpl<MessageEmailMapper, MessageEmail> implements MessageEmailService {
}
