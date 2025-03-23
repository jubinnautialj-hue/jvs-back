package cn.bctools.document.service.impl;

import cn.bctools.document.entity.MessageWechat;
import cn.bctools.document.mapper.MessageWechatMapper;
import cn.bctools.document.service.MessageWechatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageWechatServiceImpl extends ServiceImpl<MessageWechatMapper, MessageWechat> implements MessageWechatService {
}
