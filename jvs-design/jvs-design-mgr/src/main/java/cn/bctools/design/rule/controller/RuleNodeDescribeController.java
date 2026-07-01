package cn.bctools.design.rule.controller;


import cn.bctools.common.utils.R;
import cn.bctools.design.rule.entity.RunDescribePo;
import cn.bctools.design.rule.service.RuleDescribeService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author bctools.cn
 */
@Api(tags = "逻辑节点描述")
@Slf4j
@RestController
@RequestMapping("/app/design/{appId}/rule/desc")
@AllArgsConstructor
public class RuleNodeDescribeController {

    RuleDesignService designService;
    RuleDescribeService describeService;

    @Log(back = false)
    @ApiOperation("获取此逻辑下所有的节点描述")
    @GetMapping("/{id}")
    public R list(@PathVariable("id") String id, @PathVariable("appId") String appId) {
        List<RunDescribePo> list = describeService.list(Wrappers.query(new RunDescribePo().setReqRunKey(id).setJvsAppId(appId)));
        return R.ok(list);
    }

    @Log(back = false)
    @ApiOperation("新增或修改某些节点的描述信息")
    @PutMapping("/node/{id}")
    public R edit(@PathVariable("id") String id, @PathVariable("appId") String appId, @RequestBody Map<String, String> body) {
        Map<String, RunDescribePo> map = describeService.list(new LambdaQueryWrapper<RunDescribePo>()
                        .eq(RunDescribePo::getReqRunKey, id)
                        .eq(RunDescribePo::getJvsAppId, appId)
                        .in(RunDescribePo::getNodeId, body.keySet()))
                .stream()
                .collect(Collectors.toMap(RunDescribePo::getNodeId, Function.identity()));
        //删除已经有的
        body.keySet().removeIf(map::containsKey);
        List<RunDescribePo> list = new ArrayList<>();
        //保存新的
        for (String key : map.keySet()) {
            if (body.containsKey(key)) {
                list.add(map.get(key).setRemark(body.get(key)));
            } else {
                list.add(new RunDescribePo().setJvsAppId(appId).setReqRunKey(id).setNodeId(key).setRemark(body.get(key)));
            }
        }
        //保存或更新节点的描述
        describeService.saveOrUpdateBatch(list);
        return R.ok(list);
    }


    @Log(back = false)
    @ApiOperation("删除节点描述")
    @DeleteMapping("/node/{id}/{nodeId}")
    public R delete(@PathVariable("id") String id, @PathVariable("appId") String appId, @PathVariable("nodeId") String nodeId) {
        describeService.remove(Wrappers.query(new RunDescribePo().setNodeId(nodeId).setJvsAppId(appId).setReqRunKey(id)));
        return R.ok();
    }

}
