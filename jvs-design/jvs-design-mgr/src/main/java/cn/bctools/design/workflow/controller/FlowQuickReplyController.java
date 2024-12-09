package cn.bctools.design.workflow.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.CreateFlowQuickReplyReqDto;
import cn.bctools.design.workflow.dto.UpdateFlowQuickReplyReqDto;
import cn.bctools.design.workflow.entity.FlowQuickReply;
import cn.bctools.design.workflow.service.FlowQuickReplyService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhuxiaokang
 * 工作流快捷回复配置
 */
@Api(tags = "[workflow]工作流快捷回复配置设计")
@RestController
@RequestMapping("/app/design/{appId}/workflow/quick/reply")
@AllArgsConstructor
public class FlowQuickReplyController {

    private FlowQuickReplyService service;

    @ApiOperation("创建快捷回复")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public R<FlowQuickReply> create(@Validated @RequestBody CreateFlowQuickReplyReqDto req, @PathVariable String appId) {
        if (ObjectNull.isNull(req.getContent())) {
            R.failed("快捷回复内容不能为空");
        }
        FlowQuickReply flowQuickReply = new FlowQuickReply()
                .setContent(req.getContent())
                .setJvsAppId(appId);
        service.save(flowQuickReply);
        return R.ok(flowQuickReply);
    }

    @ApiOperation("修改快捷回复")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping
    public R<FlowQuickReply> update(@Validated @RequestBody UpdateFlowQuickReplyReqDto req, @PathVariable String appId) {
        if (ObjectNull.isNull(req.getContent())) {
            R.failed("快捷回复内容不能为空");
        }
        FlowQuickReply flowQuickReply = new FlowQuickReply()
                .setContent(req.getContent());
        service.update(flowQuickReply, Wrappers.<FlowQuickReply>lambdaUpdate()
                .eq(FlowQuickReply::getId, req.getId())
                .eq(FlowQuickReply::getJvsAppId, appId));
        return R.ok(flowQuickReply);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("删除快捷回复")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "快捷回复id", required = true)
    })
    @DeleteMapping("/{id}")
    public R<String> del(@PathVariable String id, @PathVariable String appId) {
        service.remove(Wrappers.<FlowQuickReply>lambdaQuery().eq(FlowQuickReply::getId, id).eq(FlowQuickReply::getJvsAppId, appId));
        return R.ok();
    }
}
