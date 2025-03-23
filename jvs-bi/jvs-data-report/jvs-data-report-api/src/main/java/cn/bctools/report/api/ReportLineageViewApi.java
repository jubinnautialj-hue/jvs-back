package cn.bctools.report.api;

import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Component
@FeignClient(value = "jvs-data-report-mgr", contextId = "lineageView")
public interface ReportLineageViewApi {

    @GetMapping("/jvs/data/report/check/{id}")
    R<Boolean> check(@ApiParam("设计id") @PathVariable("id") String id, @RequestHeader(value = "isTask",required = false,defaultValue = "0")String isTask);
}
