package cn.bctools.report.service.impl;

import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.mapper.JvsDataReportMapper;
import cn.bctools.report.service.JvsDataReportService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 报表设计 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-12-04
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JvsDataReportServiceImpl extends ServiceImpl<JvsDataReportMapper, JvsDataReport> implements JvsDataReportService {

    @Override
    public boolean existSource(String menuId) {
        return this.count(Wrappers.lambdaQuery(JvsDataReport.class).eq(JvsDataReport::getMenuId, menuId)) > 0;
    }
}
