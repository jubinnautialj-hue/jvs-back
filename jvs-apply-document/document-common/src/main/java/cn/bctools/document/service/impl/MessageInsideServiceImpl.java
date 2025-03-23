package cn.bctools.document.service.impl;

import cn.bctools.document.entity.MessageInside;
import cn.bctools.document.mapper.MessageInsideMapper;
import cn.bctools.document.service.MessageInsideService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageInsideServiceImpl extends ServiceImpl<MessageInsideMapper, MessageInside> implements MessageInsideService {
}
