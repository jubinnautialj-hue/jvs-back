package cn.bctools.design.tenant;

import cn.bctools.common.entity.dto.PlatformDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.DataModelDto;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.workflow.service.FlowDesignService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 配置短信，邮件，工作流等信息配置，和获取一些租户级参数配置
 *
 * @author guojing
 */
@Slf4j
@Api(tags = "租户管理员")
@RestController
@RequestMapping("/base/tenant/admin")
public class TenantAdminController {
    @Autowired
    CrudPageService crudPageService;
    @Autowired
    FormService formService;
    @Autowired
    FlowDesignService flowDesignService;
    @Autowired
    JvsAppService jvsAppService;
    @Autowired
    RuleDesignService ruleDesignService;
    @Autowired
    DataModelService dataModelService;
    @Autowired
    DataFieldService dataFieldService;
    @Autowired
    AppMenuService appMenuService;

    @ApiOperation(value = "基础信息")
    @GetMapping
    public R base() {
        List<PlatformDto.Block> blockList = new ArrayList<>();
        //只查询当前租户
        blockList.add(new PlatformDto.Block().setName("轻应用").setIcon("icon-icon_3-07").setSize(jvsAppService.count()).setMaxSize(10000));
        blockList.add(new PlatformDto.Block().setName("流程总数").setIcon("icon-icon_1-12").setSize(flowDesignService.count()).setMaxSize(10000));
        blockList.add(new PlatformDto.Block().setName("逻辑总数").setIcon("icon-liebiaoqingdan").setSize(ruleDesignService.count()).setMaxSize(10000));
        blockList.add(new PlatformDto.Block().setName("表单总数").setIcon("icon-icon_1-24").setSize(formService.count()).setMaxSize(20000));
        blockList.add(new PlatformDto.Block().setName("列表总数").setIcon("icon-fenxiqushi").setSize(crudPageService.count()).setMaxSize(10000));
        return R.ok(blockList);
    }

    /**
     * 查询所有模型
     *
     * @param page 分页数据
     * @return 模型基本数据集合
     */
    @ApiOperation("数据集")
    @GetMapping("/datamodel/page")
    public R<Page<DataModelDto>> modelPage(Page<DataModelPo> page, DataModelPo po) {
        // 查询模型数据
        dataModelService.page(page, Wrappers.<DataModelPo>lambdaQuery()
                .select(DataModelPo::getId, DataModelPo::getAppId, DataModelPo::getName, DataModelPo::getCreateTime)
                .eq(Objects.nonNull(po.getAppId()), DataModelPo::getAppId, po.getAppId()));
        Page<DataModelDto> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<DataModelPo> models = page.getRecords();
        if (ObjectUtils.isEmpty(models)) {
            return R.ok(resultPage);
        }
        List<DataModelDto> modelList = new ArrayList<>();
        for (DataModelPo model : models) {
            DataModelDto dataModelDto = BeanCopyUtil.copy(model, DataModelDto.class);
            List<FieldBasicsHtml> fields = dataFieldService.getFields(dataModelDto.getAppId(), model.getId(), false, false);
            dataModelDto.setFieldList(fields);
            modelList.add(dataModelDto);
        }
        resultPage.setRecords(modelList);
        return R.ok(resultPage);
    }

}
