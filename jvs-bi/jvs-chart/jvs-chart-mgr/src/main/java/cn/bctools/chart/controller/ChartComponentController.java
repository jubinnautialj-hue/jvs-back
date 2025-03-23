package cn.bctools.chart.controller;

import cn.bctools.chart.chart.po.ChartReturnObj;
import cn.bctools.chart.dto.MonomerDto;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * @author Administrator
 */
@Api(tags = "[chart]组件")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/component")
public class ChartComponentController {
    private final static String DESIGN_KEY = "json";

    ChartPageService chartPageService;

    @Log(back = false)
    @ApiOperation("单个图表数据获取")
    @PostMapping("/monomer/{name}")
    public R<ChartReturnObj> componentPreview(@RequestBody MonomerDto monomerDto, @ApiParam("当前设计的名称") @PathVariable("name") String chartName) {
        //获取json
        String json = monomerDto.getQueryData().get(DESIGN_KEY);
        //刪除设计数据
        monomerDto.getQueryData().remove(DESIGN_KEY);
        ChartReturnObj chartReturnObj = chartPageService.dataTranslation(json, monomerDto, chartName);
        return R.ok(chartReturnObj);
    }


}
