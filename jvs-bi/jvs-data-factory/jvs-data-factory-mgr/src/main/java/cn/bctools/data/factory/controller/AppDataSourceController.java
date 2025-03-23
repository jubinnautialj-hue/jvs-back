package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.source.dto.DataModelDataSourceDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.design.use.api.AppApi;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.ModeDto;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Api(tags = "低代码数据")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/data/source/app")
public class AppDataSourceController {
    private final AppApi appApi;
    private final DiscoveryClient discoveryClient;
    private final DataSourceService dataSourceService;

    @Log
    @ApiOperation("获取数据模型的所有模型")
    @GetMapping
    public R<List<DataModelDataSourceDto>> getAll(String mode) {
        List<String> services = SpringContextUtil.getBean(DiscoveryClient.class).getServices();
        String designServerName = AppApi.class.getAnnotation(FeignClient.class).value();
        if (!services.contains(designServerName)) {
            return R.ok(Collections.emptyList());
        }
        List<DataModelDataSourceDto> list = appApi.apps(mode).getData().stream()
                .map(e -> new DataModelDataSourceDto().setSourceType(DataSourceTypeEnum.dataModel)
                        .setName(e.getAppName())
                        .setSourceLibraryName(e.getAppCode())
                ).collect(Collectors.toList());
        return R.ok(list);
    }

    @Log
    @ApiOperation("获取模式")
    @GetMapping("/mode")
    public R<List<ModeDto>> getMode() {
        List<String> services = discoveryClient.getServices();
        String designServerName = AppApi.class.getAnnotation(FeignClient.class).value();
        if (!services.contains(designServerName)) {
            return R.ok(Collections.emptyList());
        }
        return R.ok(appApi.mode().getData());
    }

    @Log
    @ApiOperation("切换模式")
    @PutMapping("/switchover/mode/{id}")
    public R<Boolean> switchoverMode(@PathVariable("id") String id,@RequestBody ModeDto modeDto) {
        String mode = modeDto.getMode();
        DataSource dataSource = dataSourceService.getById(id);
        DataModelDataSourceDto dataModelDataSourceDto = dataSource.getCustomJson().toJavaObject(DataModelDataSourceDto.class);
        //切换时  校验此应用是否存在此模式
        DataModelDto data = appApi.apps(mode, dataModelDataSourceDto.getSourceLibraryName()).getData();
        if (dataSource.getSyncStructure() == BigDecimal.ROUND_CEILING) {
            return R.failed("同步中请稍等");
        }
        if (!Optional.ofNullable(data).isPresent()) {
            return R.failed("此应用没有" + mode + "模式");
        }
        dataModelDataSourceDto.setMode(mode)
                .setDatasource(modeDto.getDatasource());
        JSONObject customJson = JSONObject.parseObject(JSONObject.toJSONString(dataModelDataSourceDto));
        dataSource.setCustomJson(customJson)
                .setSyncStructure(BigDecimal.ROUND_CEILING);
        dataSourceService.updateById(dataSource);
        dataSourceService.syncTableStructure(dataSource, TenantContextHolder.getTenantId(), UserCurrentUtils.getCurrentUser());
        return R.ok(Boolean.TRUE);
    }
}
