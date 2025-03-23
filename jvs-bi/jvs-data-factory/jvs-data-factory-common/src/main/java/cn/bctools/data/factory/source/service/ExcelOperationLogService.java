package cn.bctools.data.factory.source.service;

import cn.bctools.data.factory.source.entity.ExcelOperationLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * excel数据源操作日志 服务类
 * </p>
 *
 * @author admin
 * @since 2024-06-17
 */
public interface ExcelOperationLogService extends IService<ExcelOperationLog> {

    void asyncSave(ExcelOperationLog log);

}
