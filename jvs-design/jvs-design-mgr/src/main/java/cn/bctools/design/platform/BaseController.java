package cn.bctools.design.platform;


import cn.bctools.auth.api.api.AuthTenantServiceApi;
import cn.bctools.common.entity.dto.PlatformDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RuleExternalService;
import cn.bctools.design.screen.service.ScreenService;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.oss.mapper.SysFileMapper;
import cn.bctools.redis.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "[app]应用基础信息")
@RestController
@RequestMapping("/platform/base")
public class BaseController {

    @Autowired
    ScreenService screenService;
    @Autowired
    CrudPageService crudPageService;
    @Autowired
    FormService formService;
    @Autowired
    FlowDesignService flowDesignService;
    @Autowired
    JvsAppService jvsAppService;
    @Autowired
    AuthTenantServiceApi authTenantServiceApi;
    @Autowired
    SysFileMapper sysFileMapper;
    @Autowired
    RuleDesignService ruleDesignService;
    @Autowired
    RuleExternalService ruleExternalService;
    @Autowired
    RedisUtils redisUtils;

    @ApiOperation(value = "基础信息")
    @GetMapping
    public R base() {
        //返回设备号
        List<PlatformDto.Block> blockList = new ArrayList<PlatformDto.Block>();
        //查询所有租户的
        TenantContextHolder.clear();
        blockList.add(new PlatformDto.Block().setName("轻应用").setIcon("icon-icon_3-07").setSize(jvsAppService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("流程设计").setIcon("icon-icon_1-12").setSize(flowDesignService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("逻辑设计").setIcon("icon-liebiaoqingdan").setSize(ruleDesignService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("表单设计").setIcon("icon-icon_1-24").setSize(formService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("列表设计").setIcon("icon-fenxiqushi").setSize(crudPageService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("大屏设计").setIcon("icon-jingyingfenxisvg").setSize(screenService.count()).setMaxSize(0));
        blockList.add(new PlatformDto.Block().setName("集成&自动化扩展").setIcon("icon-jingyingfenxisvg").setSize(ruleExternalService.count()).setMaxSize(0));
        return R.ok(blockList);
    }

}
