package cn.bctools.report.feign;

import cn.bctools.common.utils.R;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.report.api.ReportLineageViewApi;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.service.JvsDataReportService;
import cn.bctools.report.utils.AuthUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Component
@Api(tags = "[feign] 血缘视图")
@RestController
@AllArgsConstructor
public class ReportLineageViewApiImpl implements ReportLineageViewApi {

    private final JvsDataReportService jvsReportService;

    @Override
    public R<Boolean> check(String id,String isTask) {
        String errorMsg = "此报表已经被删除";
        JvsDataReport byId = jvsReportService.getById(id);
        if (byId == null) {
            return R.failed(errorMsg);
        }
        if(!StrUtil.equals(isTask,"1")){
            List<OperationType> operationList = AuthUtils.getOperationList(byId);
            if(CollectionUtil.isEmpty(operationList)){
                return R.failed("您暂未无此报表权限");
            }
        }
        return R.ok(Boolean.TRUE);
    }
}
