package cn.bctools.design.workflow.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.workflow.dto.*;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.utils.FlowDataFieldUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.handler.IJvsFunction;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "[workflow]工作流设计-应用相关")
@RestController
@RequestMapping("/app/design/{appId}/workflow")
@AllArgsConstructor
public class FlowDesignController {

    private final FlowDesignService service;
    private final DataModelService dataModelService;
    private final FlowDataFieldUtil flowDataFieldUtil;
    private final CrudPageService crudPageService;
    private final AppMenuService appMenuService;
    private final ExpressionHandler handler;

    @Log(callBackClass = JvsLogServiceImpl.class, back = false)
    @ApiOperation("创建工作流")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public R<FlowDesign> create(@Validated @RequestBody CreateFlowDesignReqDto dto, @PathVariable String appId) {
        if (Boolean.FALSE.equals(dto.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        FlowDesign flowDesign = BeanCopyUtil.copy(dto, FlowDesign.class);
        String id = UUID.randomUUID().toString().replace("-", "");
        flowDesign.setId(id);
        // 创建数据模型，并保存模型id
        flowDesign.setDataModelId(dataModelService.create(dto.getJvsAppId(), id, DesignType.workflow, flowDesign.getName()));
        service.save(flowDesign);

        // 将工作流默认字段保存到数据模型
        flowDataFieldUtil.updateFlowDefaultFields(appId, flowDesign.getId(), flowDesign.getDataModelId());
        return R.ok(flowDesign);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("快捷创建工作流")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/quick-create")
    public R<FlowDesign> quickCreation(@Validated @RequestBody QuickCreationFlowDesignReqDto dto, @PathVariable String appId) {
        if (Boolean.FALSE.equals(dto.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        DataModelPo dataModelPo = Optional.ofNullable(dataModelService.getById(dto.getDataModelId())).orElseThrow(() -> new BusinessException("模型不存在"));
        FlowDesign design = service.quickCreation(dto);
        dataModelPo.setWorkflowId(design.getId());
        dataModelPo.setEnableWorkflow(true);
        //更新模型id值
        dataModelService.updateById(dataModelPo);
        // 将工作流默认字段保存到数据模型
        flowDataFieldUtil.updateFlowDefaultFields(appId, design.getId(), dto.getDataModelId());
        return R.ok(design);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("修改工作流")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping
    public R<String> edit(@Validated @RequestBody UpdateFlowDesignReqDto dto, @PathVariable String appId) {
        if (Boolean.FALSE.equals(dto.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        service.update(dto);
        return R.ok();
    }

    @Deprecated
    @Log
    @ApiOperation("删除工作流")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    public R<String> del(@PathVariable String id, @PathVariable String appId) {
        FlowDesign flowDesign = service.delete(id, appId);
        // 删除成功后的处理：修改数据模型启用工作流状态为false
        if (ObjectNull.isNotNull(flowDesign)) {
            dataModelService.updateEnableWorkflow(flowDesign.getDataModelId(), Boolean.FALSE);
        }
        return R.ok();
    }

    @Log
    @ApiOperation("工作流列表")
    @GetMapping("/page")
    public R<Page<FlowDesign>> page(Page<FlowDesign> page, PageFlowDesignReqDto dto, @PathVariable String appId) {
        return R.ok(service.page(page, Wrappers.<FlowDesign>lambdaQuery()
                .like(ObjectNull.isNotNull(dto.getName()), FlowDesign::getName, dto.getName())
                .eq(ObjectNull.isNotNull(dto.getDesignGroup()), FlowDesign::getDesignGroup, dto.getDesignGroup())
                .eq(FlowDesign::getJvsAppId, appId)
                .orderByDesc(FlowDesign::getCreateTime)));
    }

    @Log
    @ApiOperation("获取应用所有工作流")
    @GetMapping
    public R<List<FlowDesign>> getByAppId(@PathVariable String appId) {
        List<FlowDesign> flowDesigns = service.getByAppId(appId).stream()
                .peek(design -> {
                    design.setDesign(null);
                    design.setDesigning(null);
                }).collect(Collectors.toList());
        return R.ok(flowDesigns);
    }

    @Log
    @ApiOperation("工作流详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true)
    })
    @GetMapping("/{id}")
    public R<FlowDesignDetailDto> detail(@PathVariable String id, @PathVariable String appId) {
        FlowDesign flowDesign = service.getById(id);
        if (Boolean.FALSE.equals(flowDesign.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }

        FlowDesignDetailDto detail = BeanCopyUtil.copy(flowDesign, FlowDesignDetailDto.class);
        // 填充变量得到完整的设计
        detail.setDesignBody(service.getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.DESIGNING).getDesignBody());
        // 若列表设计id存在，且列表页已删除，则置空列表设计id
        if (ObjectNull.isNotNull(flowDesign.getPageId()) && ObjectNull.isNull(crudPageService.getOne(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getId).eq(CrudPage::getId, flowDesign.getPageId())))) {
            flowDesign.setPageId(null);
            service.updateById(flowDesign);
            detail.setPageId(null);
        } else {
            // 存在列表页设计，则返回该列表页的菜单信息
            AppMenu appMenu = Optional.ofNullable(appMenuService.getDesignMenu(flowDesign.getPageId(), appId)).orElseGet(AppMenu::new);
            detail.setMenuId(appMenu.getType());
        }
        return R.ok(detail);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("保存设计")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/save")
    public R<String> saveDesign(@Validated @RequestBody SaveFlowReqDesign dto, @PathVariable String appId) {
        if (Boolean.FALSE.equals(dto.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        // 保存流程设计
        FlowDesign flowDesign = service.saveDesign(dto);
        // 返回表单id
        String formId = flowDesign.getFormId();
        // 若有自定义表单节点id，则返回该节点的表单id
        if (StringUtils.isNotBlank(dto.getNodeId())) {
            // 节点表单id
            formId = FlowUtil.getNodeForm(JSON.toJSONString(flowDesign.getDesignBody()), dto.getNodeId()).getFormId();
        }
        return R.ok(formId);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("保存并发布设计")
    @PutMapping("/saveAndPublish")
    public R<String> saveAndPublish(@Validated @RequestBody SaveFlowReqDesign dto, @PathVariable String appId) {
        if (Boolean.FALSE.equals(dto.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        // 保存流程设计
        FlowDesign flowDesign = service.saveDesign(dto);
        // 发布流程设计
        service.published(dto.getId(), appId);
        return R.ok(flowDesign.getDesignBody());
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("发布")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true),
    })
    @PutMapping("/publish/{id}")
    public R<FlowDesign> publish(@PathVariable String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        service.published(id, appId);
        return R.ok();
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("停用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true),
    })
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/stop/{id}")
    public R<FlowDesign> stop(@PathVariable String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        service.stop(id, appId);
        return R.ok();
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("创建工作流列表设计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true),
    })
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create/page/{id}")
    public R<CrudPage> createPage(@PathVariable String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        FlowDesign flowDesign = Optional.ofNullable(service.getById(id)).orElseThrow(() -> new BusinessException("工作流不存在"));
        if (Boolean.FALSE.equals(flowDesign.getJvsAppId().equals(appId))) {
            throw new BusinessException("应用错误或设计不存在");
        }
        CrudPage crudPage = crudPageService.create(flowDesign.getDataModelId(), flowDesign.getName() + "-数据管理", null, flowDesign.getJvsAppId());
        flowDesign.setPageId(crudPage.getId());
        service.updateById(flowDesign);
        return R.ok(crudPage);
    }

    @Log
    @ApiOperation("获取自定义流程任务标题可用字段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "工作流id", required = true),
    })
    @GetMapping("/custom/title/fields/{id}")
    public R<Map<String, List<ElementVo>>> getCustomTitleField(@PathVariable String id) {
        FlowDesign flowDesign = Optional.ofNullable(service.getById(id)).orElseThrow(() -> new BusinessException("工作流不存在"));
        // 记录设计id
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, flowDesign.getDataModelId());
        Map<String, Map<String, List<ElementVo>>> params = handler.getAllParamElement(EnvConstant.FLOW_TASK_TITLE_ITEM_VALUE + StrUtil.DOT + EnvConstant.FLOW_TASK_TITLE_MODEL_ITEM_VALUE,"");
        if (MapUtils.isEmpty(params)) {
            return R.ok(Collections.emptyMap());
        }
        Map<String, List<ElementVo>> param = params.get(IJvsParam.NAME);
        if (MapUtils.isEmpty(params)) {
            return R.ok(param);
        }
        Map<String, List<ElementVo>> resultParams = param.entrySet()
                .stream()
                .filter(e -> "流程字段".equals(e.getKey()) || "模型字段".equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return R.ok(resultParams);
    }

    @Log(callBackClass = JvsLogServiceImpl.class, back = false)
    @ApiOperation("流程设计节点复制")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/node/copy")
    public R<JSONObject> copyNode(@RequestBody JSONObject node, @PathVariable String appId) {
        if (ObjectNull.isNull(node)) {
            return R.ok(node);
        }
        return R.ok(service.copyNode(node));
    }
}
