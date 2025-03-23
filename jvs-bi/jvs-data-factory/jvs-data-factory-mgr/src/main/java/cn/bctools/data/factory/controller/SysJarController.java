package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.source.dto.ApiDataSourceDto;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.SysJar;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Slf4j
@Api(tags = "[jvs-data-source]jar包信息")
@RestController
@AllArgsConstructor
@RequestMapping("/data/source/sys/jar")
public class SysJarController {
    private final SysJarService sysJarService;
    private final DataSourceService dataSourceService;

    @Log(back = false)
    @ApiOperation("新增或修改jar包")
    @PostMapping("/update")
    public R<SysJar> save(@RequestBody SysJar sysJar) {
        sysJarService.saveOrUpdate(sysJar);
        return R.ok(sysJar);
    }

    @Log(back = false)
    @ApiOperation("获取jar包")
    @GetMapping("/page")
    public R<Page<SysJar>> page(Page<SysJar> page, SysJar sysJar) {
        LambdaQueryWrapper<SysJar> queryWrapper = new LambdaQueryWrapper<SysJar>()
                .eq(StrUtil.isNotBlank(sysJar.getJarName()), SysJar::getJarName, sysJar.getJarName());
        sysJarService.page(page, queryWrapper);
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("删除")
    @DeleteMapping("/id/{id}")
    public R delete(@ApiParam("id") @PathVariable String id) {
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.apiDataSource))
                .stream()
                .filter(e -> {
                    ApiDataSourceExecDto apiDataSourceDto = e.getCustomJson().toJavaObject(ApiDataSourceExecDto.class);
                    return StrUtil.isNotBlank(apiDataSourceDto.getJarId()) && apiDataSourceDto.getJarId().equals(id);
                })
                .collect(Collectors.toList());
        if (!list.isEmpty()) {
            List<String> name = list.stream().map(DataSource::getSourceName).collect(Collectors.toList());
            return R.failed(name, "此协议存在绑定,请先解除绑定");
        }
        sysJarService.removeById(id);
        return R.ok();
    }

}
