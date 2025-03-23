package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.source.dto.DataSourceApiDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 */
@Slf4j
@Component
@Api(tags = "[jvs-data-source]数据获取")
@RestController
@AllArgsConstructor
@RequestMapping("/data/source/acquire")
public class DataAcquireController {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;

    @GetMapping("/preview")
    @ApiOperation("数据预览")
    public R getData(Page page, DataSourceApiDto dataSourceApiDto) {
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.valueOf(dataSourceApiDto.getSourceType());
        //初始化来源类型
        //初始化入参
        DataSource dataSource = new DataSource();
        dataSource.setId(dataSourceApiDto.getDataSourceId())
                .setSourceType(dataSourceTypeEnum);
        //查询数据结构
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(dataSourceApiDto.getId());
        page = dataSourceService.findAll(dataSource, dataSourceStructure, page.getSize(), page.getCurrent());
        //获取结构
        dataSourceService.getStructure(dataSource, dataSourceStructure);
        return R.ok(page);
    }
}
