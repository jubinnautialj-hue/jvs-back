package cn.bctools.data.factory.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.data.factory.annotation.ExcelDataSource;
import cn.bctools.data.factory.enums.ExcelInterceptType;
import cn.bctools.data.factory.source.data.excel.ExcelExecuteImpl;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import cn.bctools.data.factory.source.entity.ExcelOperationLog;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.ExcelCommitLogService;
import cn.bctools.data.factory.source.service.ExcelOperationLogService;
import cn.bctools.data.factory.util.ExcelLock;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Api(tags = "[jvs-data-source]文件类型的数据源")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/data/source/file")
public class FileDataSourceController {
    private final ExcelExecuteImpl excelExecute;
    private final DataSourceService dataSourceService;
    private final OssTemplate ossTemplate;
    private final ExcelCommitLogService excelCommitLogService;
    private final ExcelOperationLogService excelOperationLogService;
    private final DataSourceStructureService dataSourceStructureService;
    private final ExcelLock excelLock;


    @Log(back = false)
    @ApiOperation("保存数据源获取修改")
    @PostMapping("/save/{addIs}")
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    @ExcelDataSource(type = ExcelInterceptType.新增)
    public R<DataSource> save(@RequestBody DataSource dataSource, @ApiParam("是否为追加") @PathVariable("addIs") Boolean addIs, @RequestParam Boolean updateFile) {
        //优先加锁 如果是新增就通过 oss 文件路径与桶名称的 md5 值加锁
        FileNameDto baseFile = dataSource.getCustomJson().toJavaObject(FileNameDto.class);
        String lockId = dataSource.getId();
        if (StrUtil.isBlank(lockId)) {
            lockId = SecureUtil.md5(baseFile.getFileName() + baseFile.getBucketName());
        }
        if (excelLock.tryLock(lockId)) {
            try {
                String url = null;
                //如果不是修改基本信息 就需要优先判断是否存在表头为空 如果为空本次修改作废
                if (updateFile) {
                    url = ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName());
                    excelExecute.checkTitleIsNotBlank(url);
                }
                //检查名称是否重复
                dataSourceService.duplicateName(dataSource.getSourceName(), dataSource.getId());
                dataSourceService.saveOrUpdate(dataSource);
                if (updateFile) {
                    dataSource.setSyncStructure(2);
                    ExcelReadDataPo excelReadDataPo = new ExcelReadDataPo().setBaseFile(baseFile).setDataSourceId(dataSource.getId()).setAddIs(addIs).setUrl(url);
                    //读取excel
                    excelExecute.read(excelReadDataPo);
                    //修改状态
                    dataSourceService.update(new UpdateWrapper<DataSource>().lambda().eq(DataSource::getId, dataSource.getId()).set(DataSource::getSyncStructure, 1));
                }
            } catch (Exception exception) {
                String errorMsg = "解析失败";
                log.info("解析失败", exception);
                if (exception instanceof BusinessException) {
                    errorMsg = exception.getMessage();
                }
                if (exception.getCause() instanceof BusinessException) {
                    errorMsg = exception.getCause().getMessage();
                }
                throw new BusinessException(errorMsg);
            } finally {
                //释放锁
                excelLock.unLock(lockId);
            }
        } else {
            String err = StrUtil.format("数据源：{} 正在操作中，请稍后再试", dataSource.getSourceName());
            return R.failed(err);
        }
        return R.ok(dataSource);
    }

    @Log(back = false)
    @ApiOperation("删除某一次提交的数据")
    @PostMapping("/del/{dataSourceId}/{submitId}")
    @ExcelDataSource(type = ExcelInterceptType.删除数据片段)
    public R delBySubmit(@ApiParam("数据源id") @PathVariable("dataSourceId") String dataSourceId, @ApiParam("提交记录") @PathVariable("submitId") String submitId) {
        ExcelCommitLog log = excelCommitLogService.getById(submitId);
        if (!log.getOperateStatus()) {
            return R.failed("本次提交记录无法删除,已被覆盖");
        }
        List<DataSourceStructure> list = dataSourceStructureService.list(Wrappers.lambdaQuery(DataSourceStructure.class).select(DataSourceStructure::getExecuteName).eq(DataSourceStructure::getDataSourceId, dataSourceId));
        list.forEach(e -> excelCommitLogService.delByLotNo(e.getExecuteName(), log.getLotNo()));
        excelCommitLogService.update(Wrappers.lambdaUpdate(ExcelCommitLog.class).set(ExcelCommitLog::getOperateStatus, Boolean.FALSE).eq(ExcelCommitLog::getId, submitId));
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("获取当前excel数据源组成部分")
    @GetMapping("/splicing/{dataSourceId}")
    public R<List<ExcelCommitLog>> getSplicing(@ApiParam("数据源id") @PathVariable("dataSourceId") String dataSourceId) {
        List<ExcelCommitLog> list = excelCommitLogService.list(Wrappers.lambdaQuery(ExcelCommitLog.class).eq(ExcelCommitLog::getDataSourceId, dataSourceId).eq(ExcelCommitLog::getOperateStatus, Boolean.TRUE).orderByAsc(BasalPo::getCreateTime));
        return R.ok(list);
    }

    @Log(back = false)
    @ApiOperation("删除当前提交记录")
    @DeleteMapping("/log/{dataSourceId}/{submitId}")
    @ExcelDataSource(type = ExcelInterceptType.删除提交记录)
    public R<Boolean> delLog(@ApiParam("提交记录") @PathVariable("dataSourceId") String dataSourceId, @ApiParam("提交记录") @PathVariable("submitId") String submitId) {
        return R.ok(excelCommitLogService.removeById(submitId));
    }

    @Log(back = false)
    @ApiOperation("分页获取操作记录")
    @GetMapping("/page/operation/log/{dataSourceId}")
    public R<Page<ExcelOperationLog>> pageOperationLog(Page<ExcelOperationLog> page, @ApiParam("数据源id") @PathVariable("dataSourceId") String dataSourceId) {
        LambdaQueryWrapper<ExcelOperationLog> queryWrapper = Wrappers.lambdaQuery(ExcelOperationLog.class).eq(ExcelOperationLog::getDataSourceId, dataSourceId).orderByDesc(BasalPo::getCreateTime);
        excelOperationLogService.page(page, queryWrapper);
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("下载excel全量数据")
    @PostMapping("/download/{structureId}")
    @SneakyThrows
    public void downLoad(@ApiParam("表id") @PathVariable("structureId") String structureId, HttpServletResponse response) {

        DataSourceStructure tableInfo = dataSourceStructureService.getById(structureId);
        if (tableInfo == null) {
            throw new BusinessException("表不存在");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode(tableInfo.getName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        String dataSourceId = tableInfo.getDataSourceId();
        DataSource dataSource = dataSourceService.getById(dataSourceId);

        //获取表名
        String executeName = tableInfo.getExecuteName();
        //获取表结构
        List<DataSourceStructure.Structure> columns = tableInfo.getStructure();
        //字段顺序
        List<String> collect = columns.stream().map(DataSourceStructure.Structure::getColumnName).collect(Collectors.toList());
        //表头
        List<List<String>> head = columns.stream().map(DataSourceStructure.Structure::getColumnCount).map(CollectionUtil::toList).collect(Collectors.toList());

        //获取总数 判断是否需要分页获取
        Long count = excelExecute.getCount(dataSource, tableInfo);
        long limit = 1000L;
        long page = new BigDecimal(count).divide(new BigDecimal(limit), RoundingMode.UP).longValue();

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).head(head).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(executeName).build();
        //循环写入本地文件 分批写入防止内存溢出
        for (long current = 1; current <= page; current++) {
            Page<Map<String, Object>> all = excelExecute.findAll(dataSource, tableInfo, limit, current);
            List<Map<String, Object>> records = all.getRecords();
            List<List<Object>> data = records.stream().map(e -> collect.stream().map(r -> Optional.ofNullable(e.get(r)).orElse("")).collect(Collectors.toList())).collect(Collectors.toList());
            excelWriter.write(data, writeSheet);
        }
        excelWriter.finish();
    }

    @Log(back = false)
    @ApiOperation("获取单次提交记录文件下载地址")
    @GetMapping("/get/submit/url/{submit}")
    public R<String> getUrl(@ApiParam("提交记录") @PathVariable("submit") String submit) {
        ExcelCommitLog commitLog = excelCommitLogService.getById(submit);
        FileNameDto fileConfig = commitLog.getFileConfig();
        String path = ossTemplate.fileLink(fileConfig.getFileName(), fileConfig.getBucketName());
        return R.ok(path);
    }

    @Log(back = false)
    @ApiOperation("根据批次号分页获取数据")
    @GetMapping("/page/data/{structureId}/{lotNo}")
    public R<Page<Map<String, Object>>> pageData(Page<Map<String, Object>> page, @ApiParam("表结构id") @PathVariable("structureId") String structureId, @ApiParam("批次号") @PathVariable("lotNo") String lotNo) {
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(structureId);
        Assert.notNull(dataSourceStructure, () -> new BusinessException("表不存在"));
        Page<Map<String, Object>> mapPage = excelExecute.pageByLotNo(page, dataSourceStructure.getExecuteName(), lotNo);
        return R.ok(mapPage);
    }
}
