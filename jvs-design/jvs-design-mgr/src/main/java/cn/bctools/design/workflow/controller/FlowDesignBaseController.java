package cn.bctools.design.workflow.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.FlowDesignNodeDto;
import cn.bctools.design.workflow.dto.permission.HavePermissionDesignResDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */

@Api(tags = "[workflow]工作流设计")
@RestController
@RequestMapping("/base/workflow/design")
@AllArgsConstructor
public class FlowDesignBaseController {

    private final FlowDesignService service;

    @Log
    @ApiOperation("获取工作流分组")
    @GetMapping("/groups")
    public R<List<String>> getDesignGroup() {
        //这接口屏蔽不再返回，2.2没有工作台所以 也没有工作流分组
        List<String> groups = new ArrayList<>();
        if (IpUtil.isMobile()) {
            Optional.ofNullable(service.list(Wrappers.<FlowDesign>lambdaQuery().groupBy(FlowDesign::getDesignGroup).select(FlowDesign::getDesignGroup)))
                    .orElse(new ArrayList<>())
                    .stream().map(FlowDesign::getDesignGroup).collect(Collectors.toList());
        }
        return R.ok(groups);
    }

    @Log
    @ApiOperation("获取有权启动的工作流")
    @GetMapping("/havePermissionDesign")
    public R<List<HavePermissionDesignResDto>> havePermissionDesign(String name) {
        if (IpUtil.isMobile()) {
            UserDto userDto = UserCurrentUtils.getCurrentUser();
            return R.ok(service.havePermissionDesign(userDto, name));
        }
        return R.ok(Collections.emptyList());
    }

    @Log
    @ApiOperation(value = "获取工作流审批节点基本信息")
    @GetMapping("/nodes/{designId}")
    public R<List<FlowDesignNodeDto>> getDesignNodes(@PathVariable String designId) {
        return R.ok(service.getNodesById(designId, Collections.singletonList(NodeTypeEnum.SP)));
    }

    @Log
    @ApiOperation("获取应用所有工作流")
    @GetMapping("/all")
    public R<List<FlowDesign>> getByAppId(String appId) {
        List<FlowDesign> flowDesigns = service.getByAppId(appId).stream()
                .peek(design -> {
                    design.setDesign(null);
                    design.setDesigning(null);
                }).collect(Collectors.toList());
        return R.ok(flowDesigns);
    }
}
