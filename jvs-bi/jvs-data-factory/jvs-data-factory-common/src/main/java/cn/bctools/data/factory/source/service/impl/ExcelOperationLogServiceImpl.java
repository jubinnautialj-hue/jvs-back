package cn.bctools.data.factory.source.service.impl;

import cn.bctools.data.factory.source.entity.ExcelOperationLog;
import cn.bctools.data.factory.source.mapper.ExcelOperationLogMapper;
import cn.bctools.data.factory.source.service.ExcelOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * <p>
 * excel数据源操作日志 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-06-17
 */
@Service
public class ExcelOperationLogServiceImpl extends ServiceImpl<ExcelOperationLogMapper, ExcelOperationLog> implements ExcelOperationLogService {

    @Async
    @Override
    public void asyncSave(ExcelOperationLog log) {
        Optional.ofNullable(log.getDataSourceId()).ifPresent(e -> this.save(log));
    }
}
