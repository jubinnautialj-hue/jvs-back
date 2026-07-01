package cn.bctools.design.workflow.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.entity.FlowQuickReply;
import cn.bctools.design.workflow.service.FlowQuickReplyService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流快捷回复配置
 */
@Api(tags = "[workflow]工作流快捷回复配置")
@RestController
@RequestMapping("/base/workflow/quick/reply")
@AllArgsConstructor
public class FlowQuickReplyBaseController {

    private FlowQuickReplyService flowQuickReplyService;

    @ApiOperation("获取应用所有快捷回复")
    @GetMapping("/{appId}")
    public R<List<FlowQuickReply>> getAppAll(@PathVariable String appId) {
        return R.ok(flowQuickReplyService.list(Wrappers.<FlowQuickReply>lambdaQuery()
                .eq(FlowQuickReply::getJvsAppId, appId)
                .orderByAsc(FlowQuickReply::getCreateTime)));
    }
}
