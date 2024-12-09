package cn.bctools.design.workflow.controller;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.proxy.CreateFlowTaskProxyReqDto;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyReqDto;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyResDto;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.design.workflow.service.FlowTaskProxyService;
import cn.bctools.design.workflow.service.ProxyService;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 */
@Api(tags = "[workflow]工作流代理")
@RestController
@RequestMapping("/base/workflow/proxy")
@AllArgsConstructor
public class FlowTaskProxyController {
    private final FlowTaskProxyService service;
    private final ProxyService proxyService;

    @ApiOperation("新增工作流代理")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<String> create(@Validated @RequestBody CreateFlowTaskProxyReqDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        FlowTaskProxy flowTaskProxy = BeanCopyUtil.copy(dto, FlowTaskProxy.class);
        service.create(userDto, flowTaskProxy);
        // 立即执行代理
        proxyService.executeProxy(flowTaskProxy);
        return R.ok();
    }

    @ApiOperation("代理列表")
    @GetMapping("/page")
    public R<Page<PageFlowTaskProxyResDto>> page(Page<FlowTaskProxy> page, PageFlowTaskProxyReqDto dto) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.page(userDto, page, dto));
    }

    @ApiOperation("撤销")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "代理id", required = true),
    })
    @PutMapping("/revoke/{id}")
    public R<FlowTaskProxy> revoke(@PathVariable String id) {
        service.update(Wrappers.<FlowTaskProxy>lambdaUpdate()
                .set(FlowTaskProxy::getRevokeFlag, Boolean.TRUE)
                .eq(FlowTaskProxy::getId, id));
        return R.ok();
    }

    @ApiOperation("总数")
    @GetMapping("/total")
    public R<Long> total() {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.count(Wrappers.<FlowTaskProxy>lambdaQuery()
                .eq(Boolean.FALSE.equals(userDto.getAdminFlag()), FlowTaskProxy::getUserId, userDto.getId())));
    }

}
