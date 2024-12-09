package cn.bctools.design.rule.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.rule.dto.RuleLogIndexDto;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.rule.entity.enums.RunType;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Api(tags = "日志服务")
@Slf4j
@RestController
@RequestMapping("/app/design/{appId}/rule/log")
@AllArgsConstructor
public class LogController {

    private static final int LIST_MAP_MAX_SIZE = 10;
    private static final String http = "http";

    RunLogService service;

    @Log(back = false)
    @ApiOperation("删除历史日志")
    @DeleteMapping("/{key}")
    public R delete(@PathVariable("key") String key, @PathVariable("appId") String appId) {
        service.remove(Wrappers.query(new RunLogPo().setReqRunKey(key).setJvsAppId(appId)));
        service.remove(new LambdaQueryWrapper<RunLogPo>().eq(RunLogPo::getReqRunKey, key));
        return R.ok();
    }

    @Log(back = false)
    @ApiOperation("批量删除历史日志")
    @DeleteMapping()
    public R delete(@RequestParam("ids") List<String> ids, @PathVariable("appId") String appId) {
        service.remove(new LambdaQueryWrapper<RunLogPo>().in(RunLogPo::getId, ids).eq(RunLogPo::getJvsAppId, appId));
        //删除子画布
        service.remove(new LambdaQueryWrapper<RunLogPo>().in(RunLogPo::getReqRunKey, ids));
        return R.ok();
    }

    @SneakyThrows
    @Log(back = false)
    @ApiOperation("分页查询某一个逻辑执行的日志服务")
    @GetMapping("/page/{key}")
    public R<Page<RunLogPo>> page(Page<RunLogPo> page, @PathVariable("key") String key, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @PathVariable("appId") String appId) {
        service.page(page, Wrappers.query(new RunLogPo().setJvsAppId(appId).setReqRunKey(key)).lambda()
                .select(RunLogPo.class, field -> !StrUtil.equalsAny(field.getProperty(), Get.name(RunLogPo::getResult), Get.name(RunLogPo::getReqData), Get.name(RunLogPo::getVariableMap), Get.name(RunLogPo::getLogs)))
                .between(RunLogPo::getStartTime, startDate, endDate)
                .orderByDesc(RunLogPo::getStartTime));
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation("获取具体某一条的详细数据")
    @GetMapping("/{id}")
    public R<RunLogPo> getTid(@PathVariable("id") String id, @PathVariable("appId") String appId) {
        RunLogPo one = service.getOne(Wrappers.query(new RunLogPo().setId(id).setJvsAppId(appId)));
        if (ObjectNull.isNotNull(one)) {
            if (ObjectNull.isNotNull(one.getLogs())) {
                if (one.getLogs().startsWith(http)) {
                    String s = HttpUtil.downloadString(one.getLogs(), Charset.defaultCharset());
                    one.setLogs(s);
                }
            }
        }
        return R.ok(one);
    }

    /**
     * * @param canvasId 画布id
     * * @param current  页码数与画布必须一周传递
     *
     * @param key
     * @param appId
     * @return
     */
    @SneakyThrows
    @Log(back = false)
    @ApiOperation("回放历史逻辑执行,最多只有7条")
    @GetMapping("/last/{key}")
    public R<List> last(@PathVariable("key") String key, @PathVariable("appId") String appId, @RequestParam(value = "tid", required = false) String tid) {
        List<RunLogPo> list = service.list(new LambdaQueryWrapper<RunLogPo>()
                .select(RunLogPo::getId, RunLogPo::getTotalExecutionTime, RunLogPo::getStatus, RunLogPo::getTid, RunLogPo::getStartTime)
                .eq(RunLogPo::getReqRunKey, key)
                .eq(RunLogPo::getParentId, key)
                .eq(ObjectNull.isNotNull(tid), RunLogPo::getTid, tid)
                .eq(RunLogPo::getJvsAppId, appId)
                .eq(RunLogPo::getTenantId, TenantContextHolder.getTenantId())
                .orderByDesc(RunLogPo::getStartTime)
                .last("limit 7")
        );
        return R.ok(list);
    }

    /**
     * @param canvasId   当前画布
     * @param canvasPage 画布信息
     * @param tid        链路信息
     * @return
     */
    @SneakyThrows
    @Log(back = false)
    @ApiOperation("回放历史逻辑执行")
    @PostMapping("/{key}/tid/{tid}/{canvasId}")
    public R<Page> tid(@PathVariable(value = "key", required = false) String key, @PathVariable(value = "canvasId", required = false) String canvasId,
                       @PathVariable(value = "tid", required = false) String tid,
                       @RequestBody Map<String, Integer> canvasPage) {
        //根据key 和tid 查询路径最长的那一条
        RunLogPo one = service.getOne(new LambdaQueryWrapper<RunLogPo>().select(RunLogPo::getPath, RunLogPo::getReqRunKey, RunLogPo::getTid)
                .eq(RunLogPo::getReqRunKey, key)
                .eq(RunLogPo::getTenantId, TenantContextHolder.getTenantId())
                .eq(RunLogPo::getTid, tid).last(" order by   " +
                        "LENGTH(path)  desc " +
                        "limit 1"));
        Page<RunLogPo> page = new Page<RunLogPo>().setSize(1).setCurrent(canvasPage.get(canvasId));
        if(ObjectNull.isNull(one)){
            return R.ok(page);
        }
        //为兼容老数据,不返回相关信息
        if (ObjectNull.isNull(one.getPath()) || one.getPath().indexOf(canvasId) < 0) {
            //判断是否是同级的
            one = service.getOne(new LambdaQueryWrapper<RunLogPo>().select(RunLogPo::getPath, RunLogPo::getReqRunKey, RunLogPo::getTid)
                    .eq(RunLogPo::getParentId, canvasId)
                    .eq(RunLogPo::getReqRunKey, key)
                    .eq(RunLogPo::getTid, tid).last(" order by   " +
                            "LENGTH(path)  desc " +
                            "limit 1"));
            if (ObjectNull.isNull(one)) {
                return R.ok();
            }
            if (ObjectNull.isNull(one.getPath())) {
                return R.ok(page);
            }
        }
        String path = one.getPath().substring(0, one.getPath().indexOf(canvasId));
        //获取某一个画布的相关信息
        LambdaQueryWrapper<RunLogPo> eq = new LambdaQueryWrapper<RunLogPo>()
                .eq(RunLogPo::getReqRunKey, key)
                .eq(RunLogPo::getParentId, canvasId)
                .eq(RunLogPo::getTid, tid)
                .orderByAsc(RunLogPo::getSort);
        //拼凑逻辑的路径信息
        for (String canvas : canvasPage.keySet()) {
            if (path.contains(canvas)) {
                eq.like(RunLogPo::getPath, "[" + canvas + "," + (canvasPage.get(canvas) - 1) + "]");
            }
        }
        service.page(page, eq);
        return R.ok(page);
    }

    /**
     * 返回逻辑的执行日志
     *
     * @param id    逻辑调用的id
     * @param appId 应用id
     * @return
     */
    @SneakyThrows
    @ApiOperation("获取数据ID")
    @GetMapping("/by/{id}")
    public R<RunLogPo> getById(@PathVariable("id") String id, @PathVariable("appId") String appId) {
        RunLogPo byId = service.getOne(Wrappers.query(new RunLogPo().setId(id).setJvsAppId(appId)));
        if (byId.getLogs().startsWith(http)) {
            String s = HttpUtil.downloadString(byId.getLogs(), Charset.defaultCharset());
            byId.setLogs(s);
        }
        return R.ok(byId);
    }

    @SneakyThrows
    @Log(back = false)
    @ApiOperation("日志统计")
    @GetMapping("/count/{key}")
    public R<Map<RunType, Integer>> page(@PathVariable("key") String key, @PathVariable("appId") String appId) {
        Map<RunType, Integer> map = new HashMap<>(3);
        for (RunType value : RunType.values()) {
            long count = service.count(Wrappers.query(new RunLogPo().setJvsAppId(appId).setReqRunKey(key).setRunType(value)));
            map.put(value, Long.valueOf(count).intValue());
        }
        return R.ok(map);
    }

    @SneakyThrows
    @Log(back = false)
    @ApiOperation("删除指定时间范围日志")
    @DeleteMapping("/delete/{key}")
    public R del(@PathVariable("key") String key, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @PathVariable("appId") String appId) {
        log.info("用户删除指定逻辑的操作日志 用户 {} ,应用 {}， 时间 {}, {} ", UserCurrentUtils.getAccountName(), appId, startDate, endDate);
        service.remove(Wrappers.query(new RunLogPo().setJvsAppId(appId).setReqRunKey(key))
                .lambda()
                .between(RunLogPo::getStartTime, startDate, endDate));
        return R.ok();
    }

    @SneakyThrows
    @Log(back = false)
    @ApiOperation("删除指定Id日志")
    @DeleteMapping("/delete/{key}/{id}")
    public R delId(@PathVariable("key") String key, @PathVariable("id") String id, @PathVariable("appId") String appId) {
        log.info("用户删除指定id的操作日志 用户 {} ,应用 {}， 日志Id {} ", UserCurrentUtils.getAccountName(), appId, id);
        service.remove(Wrappers.query(new RunLogPo().setJvsAppId(appId).setReqRunKey(key).setId(id)));
        return R.ok();
    }

    @SneakyThrows
    @Log(back = false)
    @ApiOperation("逻辑指标显示")
    @GetMapping("/index/{key}")
    public R index(@PathVariable("key") String key, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @PathVariable("appId") String appId) {
        Map<String, List<RunLogPo>> listMap = service.list(Wrappers.query(new RunLogPo().setJvsAppId(appId).setReqRunKey(key))
                .lambda()
                .select(RunLogPo::getRunType, RunLogPo::getStartTime, RunLogPo::getStatus, RunLogPo::getTotalExecutionTime)
                .between(RunLogPo::getStartTime, startDate, endDate)
                .orderByDesc(RunLogPo::getStartTime)
                //最多只显示近1000次数据
                .last(" limit 1000")
        ).stream().collect(Collectors.groupingBy(e -> e.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd"))));
        String name = "/日";
        //只有一天的数据
        if (!listMap.isEmpty() && listMap.size() <= 1) {
            listMap = listMap.values().stream().flatMap(e -> e.stream()).collect(Collectors.groupingBy(e -> e.getStartTime().format(DateTimeFormatter.ofPattern("HH"))));
            name = "/时";
            if (!listMap.isEmpty() && listMap.size() < LIST_MAP_MAX_SIZE) {
                listMap = listMap.values().stream().flatMap(e -> e.stream()).collect(Collectors.groupingBy(e -> e.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
                name = "/分";
            }
        }

        List<RuleLogIndexDto> indexDtos = new ArrayList<>(6);
        getCount(listMap, name, indexDtos);
        getTestCount(listMap, name, indexDtos);
        getRealCount(listMap, name, indexDtos, "正式调用(次数)", RunType.REAL);
        getJobCount(listMap, name, indexDtos);
        getAverageTime(listMap, name, indexDtos);
        getMaExcxTime(listMap, name, indexDtos);
        getSuccess(listMap, name, indexDtos);
        getError(listMap, name, indexDtos);
        return R.ok(indexDtos);
    }

    private void getError(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("失败次数" + name);
        Map<String, List<RunLogPo>> finalListMap7 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    long count = finalListMap7.get(e).stream().map(RunLogPo::getStatus).filter(v -> !v).count();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getSuccess(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("成功次数" + name);
        Map<String, List<RunLogPo>> finalListMap6 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    long count = finalListMap6.get(e).stream().map(RunLogPo::getStatus).filter(v -> v).count();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))
                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getMaExcxTime(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("最高消耗时间(毫秒)" + name);
        Map<String, List<RunLogPo>> finalListMap5 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    Long totalExecutionTime = finalListMap5.get(e).stream().sorted(Comparator.comparingLong(RunLogPo::getTotalExecutionTime).reversed()).findFirst().get().getTotalExecutionTime();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(totalExecutionTime);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getAverageTime(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("平均消耗时间(毫秒)" + name);
        Map<String, List<RunLogPo>> finalListMap4 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    double count = finalListMap4.get(e).stream().mapToLong(RunLogPo::getTotalExecutionTime).average().getAsDouble();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY((int) count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getJobCount(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("定时调用(次数)" + name);
        Map<String, List<RunLogPo>> finalListMap3 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    long count = finalListMap3.get(e).stream().filter(s -> s.getRunType().equals(RunType.JOB)).count();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getRealCount(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos, String s2, RunType real) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName(s2 + name);
        Map<String, List<RunLogPo>> finalListMap2 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    long count = finalListMap2.get(e).stream().filter(s -> s.getRunType().equals(real)).count();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getTestCount(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("测试调用(次数)" + name);
        Map<String, List<RunLogPo>> finalListMap1 = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> {
                    long count = finalListMap1.get(e).stream().filter(s -> s.getRunType().equals(RunType.TEST)).count();
                    RuleLogIndexDto.Index index = new RuleLogIndexDto.Index().setX(e).setY(count);
                    return index;
                })
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

    private void getCount(Map<String, List<RunLogPo>> listMap, String name, List<RuleLogIndexDto> indexDtos) {
        RuleLogIndexDto ruleLogIndexDto = new RuleLogIndexDto();
        ruleLogIndexDto.setName("调用(次数)" + name);
        Map<String, List<RunLogPo>> finalListMap = listMap;
        List<RuleLogIndexDto.Index> collect = listMap.keySet()
                .stream()
                .map(e -> new RuleLogIndexDto.Index().setX(e).setY(finalListMap.get(e).size()))
                .sorted(Comparator.comparing(RuleLogIndexDto.Index::getX))

                .collect(Collectors.toList());
        ruleLogIndexDto.setIndex(collect);
        indexDtos.add(ruleLogIndexDto);
    }

}
