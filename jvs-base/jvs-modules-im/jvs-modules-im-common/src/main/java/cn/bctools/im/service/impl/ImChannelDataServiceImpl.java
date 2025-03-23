package cn.bctools.im.service.impl;

import cn.bctools.im.entity.ChannelData;
import cn.bctools.im.mapper.ChannelDataMapper;
import cn.bctools.im.service.ImChannelDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description IM服务连接相关的数据(服务关闭后，需要处理) 服务实现类
 * @author zhuxiaokang
 */
@Service
public class ImChannelDataServiceImpl extends ServiceImpl<ChannelDataMapper, ChannelData> implements ImChannelDataService {

}
