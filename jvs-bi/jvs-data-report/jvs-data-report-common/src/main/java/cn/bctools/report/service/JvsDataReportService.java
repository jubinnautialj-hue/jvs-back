package cn.bctools.report.service;

import cn.bctools.report.entity.JvsDataReport;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 报表设计 服务类
 * </p>
 *
 * @author admin
 * @since 2024-12-04
 */
public interface JvsDataReportService extends IService<JvsDataReport> {

    /**
     * 检查目录下是否有报表
     * @param menuId 目录id
     * @return true有 false没有
     */
    boolean existSource(String menuId);
}
