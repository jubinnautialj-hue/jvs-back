package cn.bctools.chart.api;

import cn.bctools.chart.api.dto.ChartPageDto;
import cn.bctools.chart.api.dto.DeleteChartPageDto;
import cn.bctools.chart.api.dto.IsDeleteDto;
import cn.bctools.common.utils.R;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Administrator
 */
@Component
@FeignClient(value = "jvs-chart-mgr", contextId = "jvsChart")
public interface JvsChartApi {

    String PREFIX = "/api/chart/design";

    @ApiOperation("创建了图表")
    @PostMapping(PREFIX + "/save/{jvsAppId}")
    R<ChartPageDto> save(@ApiParam("应用id") @PathVariable("jvsAppId") String jvsAppId);

    /**
     * 修改 图表名称
     *
     * @param name 图表名称
     * @param id   图表id
     * @return 用户信息集合
     */
    @GetMapping(value = PREFIX + "/update/{id}/{name}")
    R updateByName(@PathVariable("id") String id, @PathVariable("name") String name);


    @ApiOperation("获取某一个应用的所有图表")
    @GetMapping(PREFIX + "/{jvsAppId}")
    R<List<ChartPageDto>> get(@ApiParam("应用id") @PathVariable("jvsAppId") String jvsAppId);

    @ApiOperation("获取应用的所有图表")
    @PostMapping(PREFIX + "/get/by/ids")
    R<List<ChartPageDto>> getAll(@ApiParam("通过id集合查询") @RequestBody List<String> jvsAppId);


    @ApiOperation("删除-注意优先应用id")
    @PostMapping(PREFIX + "/delete")
    R delete(@RequestBody DeleteChartPageDto deleteChartPageDto);

    @ApiOperation("通过图表id 获取此图表下面所有的数据来源id")
    @PostMapping(PREFIX + "/get/data/factory/id/{chartId}")
    R<List<String>> getDataFactoryId(@PathVariable("chartId") String chartId);

    @ApiOperation("图表的子集是否被删除目前用于数据集")
    @PostMapping(PREFIX + "/is/delete")
    R<Boolean> isDelete(@RequestBody IsDeleteDto isDeleteDto);

    @ApiOperation("返回设计json")
    @GetMapping(PREFIX + "/down/file/{id}/{isMock}")
    R<JSONObject> downFile(@PathVariable("id") String id,@PathVariable("isMock") Boolean isMock);

    @ApiOperation("上传数据")
    @PostMapping(PREFIX + "/up/{id}")
    R<Boolean> up(@RequestBody JSONObject jsonObject, @PathVariable("id") String id);

}
