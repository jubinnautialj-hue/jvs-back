package cn.bctools.auth.service.impl;

import cn.bctools.auth.mapper.SysLogMapper;
import cn.bctools.auth.service.SysLogService;
import cn.bctools.log.po.LogPo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 系统公告 服务实现类
 */
@Service
public class LogServiceImpl extends ServiceImpl<SysLogMapper, LogPo> implements SysLogService {

}
