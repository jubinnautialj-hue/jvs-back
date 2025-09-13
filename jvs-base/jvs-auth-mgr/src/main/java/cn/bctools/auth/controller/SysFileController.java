package cn.bctools.auth.controller;

import cn.bctools.auth.vo.FileVo;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.mapper.SysFileLabelMapper;
import cn.bctools.oss.mapper.SysFileMapper;
import cn.bctools.oss.po.OssFile;
import cn.bctools.oss.po.OssFileLabel;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.oss.utils.ThumbnailUtil;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "文件列表")
public class SysFileController {

    SysFileMapper fileMapper;
    SysFileLabelMapper labelMapper;
    OssTemplate ossTemplate;
    JvsSystemConfig jvsSystemConfig;

    @Log
    @ApiOperation("获取桶")
    @GetMapping("/buckets")
    public R<List<String>> buckets() {
        List<String> collect = ossTemplate.listBuckets();
        return R.ok(collect);
    }

    @ApiOperation("根据文件类型获取标签集")
    @GetMapping("/fileLabel")
    public R<Set<String>> fileLabel(@RequestParam(value = "fileLabel", required = false) String fileLabel) {
        Set<String> stringSet = labelMapper.selectList(Wrappers.query()).stream().map(OssFileLabel::getLabel).collect(Collectors.toSet());
        stringSet.add("系统");
        return R.ok(stringSet);
    }

    @Log
    @ApiOperation("文件列表")
    @GetMapping("/sys/file/list")
    public R<Page<OssFile>> list(Page<OssFile> page,
                                 @RequestParam(value = "fileType", required = false) String fileType,
                                 @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
                                 @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
                                 @RequestParam(value = "label", required = false) String label,
                                 @RequestParam(value = "bucketName", required = false) String bucketName,
                                 @RequestParam(value = "fileName", required = false) String fileName) {
        LambdaQueryWrapper<OssFile> queryWrapper = Wrappers.<OssFile>lambdaQuery()
                .eq(ObjectNull.isNotNull(label), OssFile::getLabel, label)
                .eq(ObjectNull.isNotNull(bucketName), OssFile::getBucketName, bucketName)
                .like(StrUtil.isNotBlank(fileType), OssFile::getFileType, fileType);
        if (ObjectNull.isNull(label)) {
            Set<String> stringSet = labelMapper.selectList(Wrappers.query()).stream().map(OssFileLabel::getLabel).collect(Collectors.toSet());
            stringSet.add("系统");
            queryWrapper.in(OssFile::getLabel, stringSet);
        }
        queryWrapper.like(StrUtil.isNotBlank(fileName), OssFile::getFileName, fileName);
        if (ObjectUtil.isNotNull(startTime) && ObjectUtil.isNotNull(endTime)) {
            queryWrapper.between(OssFile::getCreateTime, startTime.atTime(0,0,0), endTime.atTime(23,59,59));
        }
        if ("bctools.cn".equals(jvsSystemConfig.getDomain())) {
            queryWrapper.ne(OssFile::getLabel, "默认");
        }
        queryWrapper.orderByDesc(OssFile::getCreateTime);
        Page<OssFile> ossFilePage = fileMapper.selectPage(page, queryWrapper);
        ossFilePage.getRecords().forEach(e -> {
            String fileLink = ossTemplate.fileLink(e.getFilePath(), e.getBucketName());
            e.setFileLink(fileLink);
            e.setThumbnail(ThumbnailUtil.getImageBase64(e.getFileType()));
        });
        return R.ok(ossFilePage);
    }

    @Log
    @ApiOperation("删除文件")
    @DeleteMapping("/sys/file")
    public R delete(@RequestParam(value = "id") String id) {
        OssFile ossFile = fileMapper.selectById(id);
        fileMapper.deleteById(id);
        ossTemplate.removeFile(ossFile.getBucketName(), ossFile.getFileName());
        return R.ok();
    }

    @Log
    @ApiOperation("批量删除删除文件")
    @DeleteMapping("/sys/files")
    public R delete(@RequestBody Map<String, List<String>> obj) {
        obj.get("ids").forEach(e -> {
            OssFile ossFile = fileMapper.selectById(e);
            fileMapper.deleteById(e);
            ossTemplate.removeFile(ossFile.getBucketName(), ossFile.getFileName());
        });
        return R.ok();
    }

    @Log
    @ApiOperation("规则文件为新的标签")
    @PutMapping("/sys/fileLabel")
    public R fileLabel(@RequestBody FileVo vo) {
        fileMapper.update(new OssFile().setLabel(vo.getFileLabel()), new LambdaQueryWrapper<OssFile>().in(OssFile::getId, vo.getIds()));
        return R.ok();
    }

}
