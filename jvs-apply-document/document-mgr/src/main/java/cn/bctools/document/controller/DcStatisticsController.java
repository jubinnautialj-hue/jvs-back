package cn.bctools.document.controller;


import cn.bctools.common.utils.R;
import cn.bctools.document.dto.IndexAddStatisticsDto;
import cn.bctools.document.dto.IndexStatisticsDto;
import cn.bctools.document.dto.IndexStatisticsQueryDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "统计")
@RestController
@RequestMapping(value = "/statistics")
public class DcStatisticsController {
    @Autowired
    DcLibraryService dcLibraryService;
    @Autowired
    DcLibraryLogService dcLibraryLogService;



    @Log
    @ApiOperation("首页统计")
    @GetMapping("/index/add/statistics")
    public R<IndexAddStatisticsDto> getIndexAddStatistics(IndexStatisticsQueryDto indexStatisticsQueryDto) {
        //判断是否存在查询条件
        DateTime beginOfYear;
        DateTime endOfYear;
        if (StrUtil.isAllNotBlank(indexStatisticsQueryDto.getStartTime(), indexStatisticsQueryDto.getEndTime())) {
            beginOfYear = DateUtil.parse(indexStatisticsQueryDto.getStartTime());
            endOfYear = DateUtil.parse(indexStatisticsQueryDto.getEndTime());
        } else {
            beginOfYear = DateUtil.beginOfYear(DateUtil.date());
            endOfYear = DateUtil.endOfYear(DateUtil.date());
        }
        //直接获取整个库的数据
        List<DcLibrary> list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getCreateTime)
                .between(DcLibrary::getCreateTime, DateUtil.format(beginOfYear, DatePattern.NORM_DATE_PATTERN), DateUtil.format(endOfYear, DatePattern.NORM_DATE_PATTERN)).orderByAsc(DcLibrary::getCreateTime));
        //统计每月新增的数据
        Map<String, Long> monthCount = list.stream()
                .peek(e -> {
                    DateTime dateTime = DateUtil.parse(DateUtil.format(e.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
                    e.setMonth(DateUtil.format(dateTime, DatePattern.NORM_MONTH_PATTERN));
                }).collect(Collectors.groupingBy(DcLibrary::getMonth, LinkedHashMap::new, Collectors.counting()));
        IndexAddStatisticsDto statisticsDto = new IndexAddStatisticsDto()
                .setMonthCount(monthCount);
        return R.ok(statisticsDto);
    }


    @Log
    @ApiOperation("首页统计")
    @GetMapping("/index")
    public R<IndexStatisticsDto> getIndexStatistics() {
        //直接获取整个库的数据
        List<DcLibrary> list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getType, DcLibrary::getShareRole, DcLibrary::getCreateTime));
        //文库分类统计
        Map<DcLibraryReadEnum, Long> countMap = list.parallelStream().filter(e -> e.getType().equals(DcLibraryTypeEnum.knowledge)).collect(Collectors.groupingBy(DcLibrary::getShareRole, Collectors.counting()));
        //获取文章
        List<DcLibrary> libraries = list.parallelStream().filter(e -> !(e.getType().equals(DcLibraryTypeEnum.knowledge) || e.getType().equals(DcLibraryTypeEnum.directory))).collect(Collectors.toList());
        //文章分组统计
        Map<DcLibraryTypeEnum, Long> map = libraries.parallelStream().collect(Collectors.groupingBy(DcLibrary::getType, Collectors.counting()));
        IndexStatisticsDto indexStatisticsDto = new IndexStatisticsDto()
                .setArticleCount(libraries.size())
                .setArticleGroupCount(map)
                .setKnowledgeCount(countMap);
        return R.ok(indexStatisticsDto);
    }

}
