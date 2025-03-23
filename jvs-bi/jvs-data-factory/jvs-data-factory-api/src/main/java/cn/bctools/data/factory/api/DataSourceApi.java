package cn.bctools.data.factory.api;

import cn.bctools.common.utils.R;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Component
@FeignClient(value = "jvs-data-factory-mgr", contextId = "dataFactory")
public interface DataSourceApi {
    String PREFIX = "/api/data/source";

    @ApiOperation("获取数据源")
    @PostMapping(PREFIX + "/get/by/list")
    R<List<JSONObject>> getByList(@RequestBody List<String> ids);

    @ApiOperation("上传数据")
    @PostMapping(PREFIX + "/up")
    R<Boolean> up(@RequestBody JSONObject jsonObject);

    @ApiOperation("获取数据集在数据源中的id-方便导入时进行替换")
    @PostMapping(PREFIX + "/get/data/factory/source/id")
    R<String> getDataFatcorySourceId();
}
