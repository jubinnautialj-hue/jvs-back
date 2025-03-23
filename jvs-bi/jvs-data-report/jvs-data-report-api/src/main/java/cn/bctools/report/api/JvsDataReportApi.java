package cn.bctools.report.api;

import cn.bctools.common.utils.R;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@FeignClient(value = "jvs-data-report-mgr", contextId = "jvsReport")
public interface JvsDataReportApi {

    String PREFIX = "/report/api";

    @GetMapping(PREFIX + "/check/{id}")
    R<Boolean> check(@ApiParam("设计id") @PathVariable("id") String id, @RequestHeader(value = "isTask",required = false,defaultValue = "0")String isTask);

    /*导入导出*/
    @ApiOperation("返回设计json")
    @GetMapping(PREFIX + "/down/file/{id}/{isMock}")
    R<JSONObject> downFile(@PathVariable("id") String id, @PathVariable("isMock") Boolean isMock);

    @ApiOperation("上传数据")
    @PostMapping(PREFIX + "/up/{id}")
    R<Boolean> up(@RequestBody JSONObject jsonObject, @PathVariable("id") String id);

    @ApiOperation("通过图表id 获取此图表下面所有的数据来源id")
    @PostMapping(PREFIX + "/get/data/factory/id/{id}")
    R<List<String>> getDataFactoryId(@PathVariable("id") String id);
}
