package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.function.Get;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.dto.*;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.enums.OperationEnum;
import cn.bctools.data.factory.job.XxlJobComponent;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.service.*;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.util.AuthUtil;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据etl
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Api(tags = "[jvs-data-factory]数据etl")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/factory")
@Slf4j
public class JvsDataFactoryController {

    JvsDataFactoryService jvsDataFactoryService;
    JvsDataFactoryLogService factoryLogService;
    XxlJobComponent xxlJobComponent;
    JvsDataFactoryOutService jvsDataFactoryOutService;
    JvsDataFactoryQueueService jvsDataFactoryQueueService;
    DorisJdbcTemplate dorisJdbcTemplate;
    private final DataSourceStructureService dataSourceStructureService;
    private final ConsanguinityAnalyseConsumer consanguinityAnalyseConsumer;
    private final ConsanguinityViewService consanguinityViewService;
    private final DataSourceService dataSourceService;
    private final AuthUtil<OperationEnum, JvsDataFactory> jvsDataFactoryAuthUtil;
    private final JvsDataAuthService jvsDataAuthService;

    @Log(back = false)
    @ApiOperation("分页数据")
    @GetMapping("/page")
    public R<Page<JvsDataFactory>> page(Page<JvsDataFactory> page, JvsDataFactory data) {
        LambdaQueryWrapper<JvsDataFactory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(JvsDataFactory::getCreateTime);
        if (StrUtil.isNotEmpty(data.getName())) {
            queryWrapper.like(JvsDataFactory::getName, data.getName());
        }
        if (ObjectUtil.isNotNull(data.getEnable())) {
            queryWrapper.eq(JvsDataFactory::getEnable, data.getEnable());
        }
        queryWrapper.select(JvsDataFactory.class, e -> !e.getProperty().contains(Get.name(JvsDataFactory::getViewJson)));
        page = jvsDataFactoryService.page(page, queryWrapper);
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("分页获取可选的数据集数据")
    @GetMapping("/table/page")
    public R<Page<DataSourceStructure>> pageGetExecute(Page<DataSourceStructure> page, DataSourceStructure data) {
        List<DataSource> list = dataSourceService.list(Wrappers.lambdaQuery(DataSource.class).select(DataSource::getId).eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        List<String> collect = list.stream().map(DataSource::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(collect)) {
            LambdaQueryWrapper<DataSourceStructure> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DataSourceStructure::getDataSourceId, collect).orderByDesc(DataSourceStructure::getCreateTime);
            if (StrUtil.isNotEmpty(data.getName())) {
                queryWrapper.like(DataSourceStructure::getName, data.getName());
            }
            dataSourceStructureService.page(page, queryWrapper);
        }
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("查询数据集基础信息")
    @GetMapping("/base/{id}")
    public R<JvsDataFactory> baseInfo(@ApiParam("数据集id") @PathVariable("id") String dataFactoryId) {
        LambdaQueryWrapper<JvsDataFactory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JvsDataFactory::getId, dataFactoryId);
        JvsDataFactory one = jvsDataFactoryService.getOne(queryWrapper);
        if (one == null) {
            return R.failed("未找到此设计");
        }
        //权限判断
        JvsDataFactory auth = jvsDataFactoryAuthUtil.auth(one, null, Arrays.asList(OperationEnum.values()));
        if (auth.getOperationList().isEmpty()) {
            return R.failed("暂无权限");
        }
        return R.ok(auth);
    }

    @Log(back = false)
    @ApiOperation("获取数据")
    @GetMapping("/get/select/{id}")
    public R<List<JvsDataFactory>> getSelect(@ApiParam("数据集id此数据需要被排除") @PathVariable("id") String dataFactoryId) {
        List<JvsDataFactory> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().select(JvsDataFactory::getId, JvsDataFactory::getName, JvsDataFactory::getRoleType, JvsDataFactory::getEnable, JvsDataFactory::getCreateById, JvsDataFactory::getRole).ne(JvsDataFactory::getId, dataFactoryId).eq(JvsDataFactory::getEnable, Boolean.TRUE)).stream().peek(e -> jvsDataFactoryAuthUtil.auth(e, OperationEnum.编辑, Arrays.asList(OperationEnum.values()))).collect(Collectors.toList());
        //设置权限
        return R.ok(list);
    }

    @Log(back = false)
    @ApiOperation("预览处理完成的数据")
    @GetMapping("/preview/{id}")
    public R<JvsDataFactoryOut> preview(Page<Map<String, Object>> page, @ApiParam("数据集id") @PathVariable("id") String dataFactoryId) {
        JvsDataFactoryOut one = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataFactoryId).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        if (one == null) {
            return R.ok();
        }
        //列权限
        List<DataSourceField> column = jvsDataFactoryService.getColumn(dataFactoryId);
        Page<Map<String, Object>> data = jvsDataFactoryOutService.getData(dataFactoryId, page.getSize(), page.getCurrent());
        one.setPageData(data);
        one.setFields(JSONObject.parseArray(JSONObject.toJSONString(column), JSONObject.class));
        return R.ok(one);
    }

    /**
     * 原来的接口目前不使用了
     */
    @Log
    @ApiOperation("修改数据集基础信息")
    @PutMapping("/edit/{id}/base")
    @Deprecated
    public R<JvsDataFactory> editBaseInfo(@ApiParam("数据集id") @PathVariable("id") String dataFactoryId, @RequestBody JvsDataFactory dto) {
        JvsDataFactory old = jvsDataFactoryService.getById(dataFactoryId);
        if (ObjectUtil.isNull(old)) {
            return R.failed("数据集不存在");
        }
        dto.setId(dataFactoryId).setViewJson(old.getViewJson());
        jvsDataFactoryService.updateById(dto);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("复制数据集")
    @PostMapping("/copy")
    public R<JvsDataFactory> copy(@RequestBody CopyDto dto) {
        JvsDataFactory factory = jvsDataFactoryService.copy(dto);
        return R.ok(factory);
    }

    @Log
    @ApiOperation("日志分页数据")
    @GetMapping("/log/page")
    public R<Page<JvsDataFactoryLog>> page(Page<JvsDataFactoryLog> page, StatisticsQueryDto statisticsQueryDto) {
        page = factoryLogService.page(page, new LambdaQueryWrapper<JvsDataFactoryLog>().between(StrUtil.isNotBlank(statisticsQueryDto.getEndTime()), JvsDataFactoryLog::getCreateTime, statisticsQueryDto.getStartTime(), statisticsQueryDto.getEndTime()).eq(JvsDataFactoryLog::getDataId, statisticsQueryDto.getId()).orderByDesc(JvsDataFactoryLog::getCreateTime));
        return R.ok(page);
    }

    @Log
    @ApiOperation("获取所有启用的数据集-过滤节点使用")
    @GetMapping("/get/start/using")
    public R<List<StartUsingDto>> getStartUsing() {
        List<JvsDataFactory> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getEnable, Boolean.TRUE).select(JvsDataFactory::getId, JvsDataFactory::getName, JvsDataFactory::getCreateById, JvsDataFactory::getRole, JvsDataFactory::getRoleType).orderByDesc(JvsDataFactory::getCreateTime));
        if (list.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        //权限过滤
        list = jvsDataFactoryAuthUtil.auth(list, null, Arrays.asList(OperationEnum.values()));
        if (list.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        Map<String, JvsDataFactoryOut> outMap = jvsDataFactoryOutService.list(new LambdaQueryWrapper<JvsDataFactoryOut>().in(JvsDataFactoryOut::getDataId, list.stream().map(JvsDataFactory::getId).collect(Collectors.toList()))).stream().collect(Collectors.toMap(JvsDataFactoryOut::getDataId, Function.identity()));
        //组装前端需要的数据格式
        List<StartUsingDto> usingDtoList = list.stream().map(e -> {
            StartUsingDto startUsingDto = new StartUsingDto();
            startUsingDto.setName(e.getName()).setId(e.getId());
            if (outMap.containsKey(e.getId())) {
                JvsDataFactoryOut factoryOut = outMap.get(e.getId());
                startUsingDto.setKey(factoryOut.getDocumentName());
                List<StartUsingDto> children = factoryOut.getFieldList().stream().map(v -> new StartUsingDto().setName(v.getFieldName()).setKey(v.getFieldKey()).setFormat(v.getFormat()).setFieldType(v.getFieldType())).collect(Collectors.toList());
                startUsingDto.setChildren(children);
            }
            return startUsingDto;
        }).collect(Collectors.toList());
        return R.ok(usingDtoList);
    }


    @Log
    @ApiOperation("保存数据")
    @PostMapping("/save")
    public R<JvsDataFactory> save(@Validated @RequestBody JvsDataFactory dto) {
        dto.setPrefixTaskId(new ArrayList<>()).setRearTaskId(new ArrayList<>());
        long count = jvsDataFactoryService.count(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getType, dto.getType()));
        dto.setSort((int) (count + 1));
        jvsDataFactoryService.save(dto);
        //添加血缘视图
        ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse().setDataFactoryId(dto.getId()).setTenantId(TenantContextHolder.getTenantId()).setViewType(ConsanguinityViewTypeEnum.dataFactoryDataSource).setType(2).setDesignId(dto.getId()).setDesignName(dto.getName());
        consanguinityAnalyseConsumer.send(consanguinityAnalyse);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("编辑数据")
    @PutMapping("/edit/sql")
    public R<JvsDataFactory> editSql(@RequestBody JvsDataFactory dto) {
        JvsDataFactory byId = jvsDataFactoryService.getById(dto.getId());
        if (byId.getEnable()) {
            return R.failed("当前数据处于启动状态,无法保存");
        }
        jvsDataFactoryService.updateById(dto);
        //添加血缘视图
        ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse().setDataFactoryId(dto.getId()).setTenantId(TenantContextHolder.getTenantId()).setViewType(ConsanguinityViewTypeEnum.dataFactoryDataSource).setType(2).setDesignId(dto.getId()).setDesignName(dto.getName());
        //先删除本身重新入库
        consanguinityViewService.deleteSource(dto.getId(), 2);
        consanguinityAnalyseConsumer.send(consanguinityAnalyse);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("编辑数据")
    @PutMapping("/edit")
    public R<JvsDataFactory> edit(@RequestBody JvsDataFactory dto) {
        //判断队列中是否存在此任务 如果存在就无法保存
        String taskExec = jvsDataFactoryQueueService.isTaskExec(dto);
        if (StrUtil.isNotBlank(taskExec)) {
            return R.failed(taskExec);
        }
        JvsDataFactory byId = jvsDataFactoryService.getById(dto.getId());
        if (byId.getEnable()) {
            if (dto.getEnable()) {
                return R.failed("当前数据处于启动状态,无法保存");
            } else {
                //关闭应用
                disabled(dto.getId());
            }
        }
        jvsDataFactoryService.updateById(dto);
        if (dto.getEnable()) {
            R<Boolean> enable = enable(dto.getId());
            if (enable.getCode() != BigDecimal.ROUND_UP) {
                return R.failed(enable.getMsg());
            }
        }
        //删除历史数据
        if (!dto.getDeleteNode().isEmpty()) {
            dto.getDeleteNode().stream().map(e -> "ods_" + e.getDataId() + "_" + e.getId() + "_" + Boolean.FALSE).forEach(e -> {
                boolean b = dorisJdbcTemplate.ifNotExistsTable(e);
                if (b) {
                    dorisJdbcTemplate.dropForce(e);
                }
            });
        }
        //添加血缘视图
        ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse().setDataFactoryId(dto.getId()).setTenantId(TenantContextHolder.getTenantId()).setViewType(ConsanguinityViewTypeEnum.dataFactoryDataSource).setType(2).setDesignId(dto.getId()).setDesignName(dto.getName());
        //先删除本身重新入库
        consanguinityViewService.deleteSource(dto.getId(), 2);
        consanguinityAnalyseConsumer.send(consanguinityAnalyse);
        return R.ok(dto);
    }

    @Log
    @ApiOperation(value = "禁用应用")
    @DeleteMapping("/disabled/{id}")
    public R disabled(@PathVariable String id) {
        JvsDataFactory byId = jvsDataFactoryService.getById(id);
        //修改定时任务
        xxlJobComponent.saveOrUpdateJob(byId.getTask(), Boolean.FALSE, byId.getName(), byId.getId(), byId.getTaskType());
        byId.setEnable(Boolean.FALSE);
        jvsDataFactoryService.updateById(byId);
        return R.ok();
    }

    @Log
    @ApiOperation(value = "启用应用")
    @DeleteMapping("/enable/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> enable(@PathVariable String id) {
        JvsDataFactory byId = jvsDataFactoryService.getById(id);
        if (!byId.getIntegrityIs()) {
            return R.failed("请检查设计是否正确");
        }

        LambdaUpdateWrapper<JvsDataFactory> updateWrapper = Wrappers.lambdaUpdate();
        //启用定时任务
        xxlJobComponent.saveOrUpdateJob(byId.getTask(), Boolean.TRUE, byId.getName(), byId.getId(), byId.getTaskType());
        updateWrapper.set(JvsDataFactory::getTask, JSONObject.toJSONString(byId.getTask())).set(JvsDataFactory::getEnable, Boolean.TRUE).eq(JvsDataFactory::getId, id);
        jvsDataFactoryService.update(updateWrapper);
        //通知数据源同步结构
        jvsDataFactoryService.syncTableStructure(byId);
        return R.ok();
    }

    @Log
    @ApiOperation("删除数据")
    @DeleteMapping("/del/{id}")
    public R<Boolean> del(@ApiParam("id") @PathVariable("id") String id) {
        JvsDataFactory dataFactory = jvsDataFactoryService.delete(id);
        xxlJobComponent.stop(dataFactory.getTask());
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("获取数据id")
    @GetMapping("/{id}")
    public R<JvsDataFactory> byId(@ApiParam("id") @PathVariable("id") String id) {
        JvsDataFactory byId = jvsDataFactoryService.getById(id);
        return R.ok(byId);
    }

    @Log
    @ApiOperation("导出设计")
    @PostMapping("/export")
    public void byId(@RequestBody List<String> ids) {
        String path = "etl" + DateUtil.format(DateUtil.date(), DatePattern.CHINESE_DATE_TIME_PATTERN) + ".jvs";
        List<JvsDataFactory> list = jvsDataFactoryService.listByIds(ids);
        String encodePassword = PasswordUtil.encodePassword(JSONObject.toJSONString(list));
        File file = FileUtil.file(path);
        //写入内容
        FileUtil.writeString(encodePassword, file, "utf-8");
        fileOutput(path, FileUtil.readBytes(file));
        //删除文件
        file.delete();
    }


    @Log
    @ApiOperation("导入设计")
    @PostMapping("/import")
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public R<List<JvsDataFactory>> byId(@RequestParam("file") MultipartFile file) {
        File file1 = FileUtil.file(file.getName());
        FileUtil.writeFromStream(file.getInputStream(), file1);
        String string = FileUtil.readUtf8String(file1);
        file1.delete();
        if (StrUtil.isBlank(string)) {
            return R.failed("文件内容不能为空!");
        }
        try {
            string = PasswordUtil.decodedPassword(string);
        } catch (Exception e) {
            log.error("数据导入失败!", e);
            return R.failed("文件解密失败!");
        }
        List<JvsDataFactory> list = JSONObject.parseArray(string, JvsDataFactory.class);
        //删除id重置状态 清空taskid与状态
        list = list.stream().peek(e -> {
            e.setSourceId(e.getId());
            e.setId(null).setEnable(Boolean.FALSE).setIntegrityIs(Boolean.FALSE);
            e.setCreateBy(null);
            e.setCreateTime(null);
            e.setCreateById(null);
            e.setUpdateTime(null);
            e.setUpdateBy(null);
            e.getTask().setId(null).setOnTask(Boolean.FALSE);
        }).collect(Collectors.toList());
        jvsDataFactoryService.saveBatch(list);
        //更改渲染数据
        list = list.stream().peek(e -> e.getViewJson().replaceAll(e.getSourceId(), e.getId())).collect(Collectors.toList());
        jvsDataFactoryService.updateBatchById(list);
        return R.ok(list);
    }

    @Log
    @ApiOperation("移动不管是调整顺序还是移动到其它目录")
    @PutMapping("/move")
    @Transactional(rollbackFor = Exception.class)
    public R<List<JvsDataFactory>> move(@RequestBody FactoryMoveDto dto) {
        //获取数据
        JvsDataFactory byId = jvsDataFactoryService.getById(dto.getId());
        //获取当前目录下面的所有文档
        List<JvsDataFactory> list = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getType, dto.getParentId())
                //.ne(JvsDataFactory::getId, dto.getId())
                .orderByAsc(JvsDataFactory::getSort)).stream().filter(e -> !e.getId().equals(dto.getId())).collect(Collectors.toList());
        int indexOf = 0;
        if (StrUtil.isNotBlank(dto.getNextId())) {
            indexOf = list.stream().map(JvsDataFactory::getId).collect(Collectors.toList()).indexOf(dto.getNextId());
        }
        Boolean aBoolean = Optional.ofNullable(dto.getIsFront()).orElse(false);
        if (aBoolean) {
            indexOf += 1;
        }
        //插入数据
        byId.setType(dto.getParentId());
        list.add(indexOf, byId);
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort(i);
        }
        jvsDataFactoryService.updateBatchById(list);
        return R.ok(list);
    }


    /***
     * 功能描述: <br>
     * 〈输出流文件〉
     * @Param: [response]
     * @Return: void
     * @Author:
     * @Date: 2021/11/18 21:54
     */
    @SneakyThrows
    private static void fileOutput(String fileName, byte[] bytes) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        IoUtil.close(outputStream);
    }

}
