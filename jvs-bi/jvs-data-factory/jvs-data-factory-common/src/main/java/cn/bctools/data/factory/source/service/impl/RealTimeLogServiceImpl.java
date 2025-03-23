package cn.bctools.data.factory.source.service.impl;

import cn.bctools.data.factory.source.entity.RealTimeLog;
import cn.bctools.data.factory.source.mapper.RealTimeLogMapper;
import cn.bctools.data.factory.source.service.RealTimeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author admin
 * @description 实时任务日志
 */
@Service
@Slf4j
@AllArgsConstructor
public class RealTimeLogServiceImpl extends ServiceImpl<RealTimeLogMapper, RealTimeLog> implements RealTimeLogService {

}
