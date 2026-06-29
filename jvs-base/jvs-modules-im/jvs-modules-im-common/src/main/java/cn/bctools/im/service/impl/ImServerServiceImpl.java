package cn.bctools.im.service.impl;

import cn.bctools.im.entity.ServerInfo;
import cn.bctools.im.mapper.ServerInfoMapper;
import cn.bctools.im.service.ImServerInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description im服务信息 服务实现类
 * @author zhuxiaokang
 */
@Service
public class ImServerServiceImpl extends ServiceImpl<ServerInfoMapper, ServerInfo> implements ImServerInfoService {

}
