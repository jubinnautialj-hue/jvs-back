package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.dto.ApiDataSourceDto;
import cn.bctools.data.factory.source.dto.DataSourceQueryDto;
import cn.bctools.data.factory.source.dto.MenuDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.data.factory.source.util.AuthUtil;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Api(tags = "[jvs-data-source]菜单管理")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/data/source")
public class DataSourceMenuController {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final SysJarService sysJarService;
    private final JvsDataFactoryService jvsDataFactoryService;

    @Log
    @ApiOperation("菜单")
    @GetMapping("/menu")
    public R<List<MenuDto>> menu(DataSource dto) {
        dataSourceService.saveDataFactory();
        //先获取一级
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>()
                .like(StrUtil.isNotBlank(dto.getSourceName()), DataSource::getSourceName, dto.getSourceName())
                .orderByAsc(DataSource::getCreateTime));
        //权限认证
        list = AuthUtil.auth(list);
        Map<DataSourceTypeEnum, List<DataSource>> hashMap = list
                .stream()
                .collect(Collectors.groupingBy(DataSource::getSourceType, LinkedHashMap::new, Collectors.toList()));
        List<MenuDto> menuDto = new ArrayList<>();
        if (hashMap.isEmpty()) {
            return R.ok(menuDto);
        }
        //组装数据
        hashMap.forEach((k, v) -> {
            //id使用枚举值进行md5加密  保证id一致 用于前端做缓存使用
            MenuDto menu = new MenuDto().setId(SecureUtil.md5(k.getValue()))
                    .setSourceType(k.getValue())
                    .setLabel(k.getDesc())
                    .setSourceName(k.getDesc())
                    .setChildren(v);
            menuDto.add(menu);
        });
        return R.ok(menuDto);
    }

    @Log
    @ApiOperation("只显示需要的类型菜单")
    @GetMapping("/filter/menu")
    public R<List<MenuDto>> filterMenu(DataSourceQueryDto dto) {
        //先获取一级
        List<DataSource> list = dataSourceService.list(new LambdaQueryWrapper<DataSource>()
                .like(StrUtil.isNotBlank(dto.getSourceName()), DataSource::getSourceName, dto.getSourceName())
                .orderByDesc(DataSource::getCreateTime));
        //权限认证
        list = AuthUtil.auth(list);

        List<DataSourceTypeEnum> dataSourceTypeEnums = Optional.ofNullable(dto.getTypeEnumList())
//                .map(e -> e.stream().map(DataSourceTypeEnum::valueOf).collect(Collectors.toList()))
                .orElse(CollectionUtil.toList(DataSourceTypeEnum.values()));

        Map<DataSourceTypeEnum, List<DataSource>> hashMap = list
                .stream()
                .filter(e -> dataSourceTypeEnums.contains(e.getSourceType()))
                .collect(Collectors.groupingBy(DataSource::getSourceType, LinkedHashMap::new, Collectors.toList()));
        List<MenuDto> menuDto = new ArrayList<>();
        if (hashMap.isEmpty()) {
            return R.ok(menuDto);
        }
        //组装数据
        hashMap.forEach((k, v) -> {
            //id使用枚举值进行md5加密  保证id一致 用于前端做缓存使用
            MenuDto menu = new MenuDto().setId(SecureUtil.md5(k.getValue()))
                    .setSourceType(k.getValue())
                    .setLabel(k.getDesc())
                    .setSourceName(k.getDesc())
                    .setChildren(v);
            menuDto.add(menu);
        });
        return R.ok(menuDto);
    }

    @Log
    @ApiOperation("校验某个数据源是否还存在")
    @GetMapping("/check/{id}/{subId}")
    public R<Boolean> check(@ApiParam("设计id") @PathVariable String id, @ApiParam("下级id") @PathVariable String subId) {
        String errorMsg = "此数据源已经被删除";
        DataSource byId = dataSourceService.getById(id);
        if (byId == null) {
            return R.failed(errorMsg);
        }
        //判断下级是否存在
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(subId);
        if (dataSourceStructure != null) {
            return R.failed(errorMsg);
        }
        //校验当前用户是否有权限
        DataSource auth = AuthUtil.auth(byId);
        if (auth.getOperationList().isEmpty()) {
            return R.failed("此数据源您暂无权限");
        }
        return R.ok(Boolean.TRUE);
    }

    @Log(back = false)
    @ApiOperation("获取某个数据源的数据表")
    @GetMapping("/get/table/{id}")
    public R<List<DataSourceStructure>> getTable(@ApiParam("数据源id") @PathVariable String id) {
        List<DataSourceStructure> list = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, id));
        DataSource dataSource = dataSourceService.getById(id);
        //权限过滤 数据集
        if (dataSource.getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource)) {
            list = list.stream().filter(v -> !jvsDataFactoryService.auth(v.getExecuteName()).getOperationList().isEmpty()).collect(Collectors.toList());
            //列权限过滤
            list = list.stream().peek(e -> {
                List<String> keys = jvsDataFactoryService.getColumn(e.getExecuteName()).stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
                List<DataSourceStructure.Structure> structureList = e.getStructure().stream().filter(v -> keys.contains(v.getColumnName())).collect(Collectors.toList());
                e.setStructure(structureList);
                e.setFieldCount(structureList.size());
            }).collect(Collectors.toList());
        }
        return R.ok(list);
    }

    @Log
    @ApiOperation("通过id获取数据")
    @GetMapping("/get/by/{id}")
    public R<DataSource> getId(@ApiParam("数据源id") @PathVariable String id) {
        DataSource byId = dataSourceService.getById(id);
        if (byId == null) {
            return R.failed("未找到此数据");
        }
        if (byId.getSourceType().equals(DataSourceTypeEnum.apiDataSource) && byId.getCustomJson() != null) {
            ApiDataSourceDto apiDataSourceDto = byId.getCustomJson().toJavaObject(ApiDataSourceDto.class);
            if (StrUtil.isNotBlank(apiDataSourceDto.getSysJarId())) {
                String jarName = sysJarService.getById(apiDataSourceDto.getSysJarId()).getJarName();
                byId.setSysJarName(jarName);
            }
        }
        return R.ok(AuthUtil.auth(byId));
    }
}
