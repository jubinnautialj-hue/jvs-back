package cn.bctools.design.use;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.advance.HtmlEditorFieldHandler;
import cn.bctools.design.data.service.DataFieldDynamicService;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bctools.design.crud.utils.DesignUtils.parseField;


/**
 * @author Administrator
 */
@Api(tags = "[设计套件]获取设计渲染结构")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}")
public class AppUseController {
    DataFieldDynamicService dataFieldDynamicService;
    DesignHandler designHandler;
    OssProperties ossProperties;
    AuthRoleServiceApi roleServiceApi;
    UseComponent useComponent;
    FormService formService;
    DataModelService dataModelService;
    JvsAppService jvsAppService;
    CrudPageService crudPageService;
    FlowTaskService flowTaskService;
    DataFieldService dataFieldService;
    DynamicDataService dynamicDataService;
    DataFieldHandler dataFieldHandler;
    FunctionBusinessMapper businessMapper;
    Map<String, IDataFieldHandler> handlerMap;
    AppMenuService appMenuService;
    DesignPermissionService designPermissionService;
    PermissionCompatibleService permissionCompatibleService;

    @GetMapping
    @ApiOperation("获取的应用菜单信息")
    public R trees(@PathVariable String appId) {
        //只返回目录
        boolean mobile = IpUtil.isMobile();
        List<Tree<Object>> tree = useComponent.menu(appId, ModeUtils.getRealUser().getId(), mobile, ModeUtils.getMode(), null).getKey();
        if (ObjectNull.isNull(tree)) {
            return R.ok();
        }
        Tree trees = tree
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("应用不存在"));
        return R.ok(trees);
    }

    @Log(back = false)
    @GetMapping("/crudPage/{id}")
    @ApiOperation("列表页设计数据")
    public R<CrudPage> crud(@PathVariable("id") String id, @PathVariable("appId") String appId) {
        // 获取列表页设计
        CrudPage po = crudPageService.getById(id);
        if (Objects.isNull(po)) {
            throw new BusinessException("设计页面未找到请联系管理员");
        }
        if (!po.getJvsAppId().equals(appId)) {
            throw new BusinessException("应用错误或设计不存在");
        }

        boolean isAdmin = jvsAppService.userIsAppAdmin(po.getJvsAppId());
        PageDesignHtml design = DesignUtils.parsePage(po.getViewJson());
        if (!isAdmin) {
            //判断按钮权限进行解析排除
            List<DesignRole> designRoles = permissionCompatibleService.getOperationPermissions(appId, po.getId());
            if (ObjectNull.isNotNull(designRoles)) {
                boolean check = RoleUtils.hasPermit(designRoles);
                if (!check) {
                    return R.failed("没有权限请联系管理员");
                }
            }
            // 操作按钮权限过滤
            if (ObjectNull.isNotNull(design)) {
                List<ButtonDesignHtml> buttons = design.getButtons();
                if (ObjectNull.isNotNull(buttons)) {
                    Set<String> permit = RoleUtils.getPermitOperation(designRoles, buttons);
                    buttons.removeIf(button -> !permit.contains(button.getName()));
                    //移动端过滤
                    if (IpUtil.isMobile()) {
                        buttons.removeIf(e -> {
                            //如果移动端标识为空,取PC端标识
                            return ObjectNull.isNull(e.getMobileEnable()) ? !e.getEnable() : !e.getMobileEnable();
                        });
                    }
                }
            }
        }
        //当模型关联的工作流一条数据都没有时不加载默认工作流按钮
        if (0 != flowTaskService.countByModeId(po.getDataModelId())) {
            List<ButtonDesignHtml> defaultButtonDesignHtmls = crudPageService.getSystemDefaultButtons();
            defaultButtonDesignHtmls.forEach(button -> button.setPosition("line"));
            if (ObjectNull.isNotNull(design)) {
                if (ObjectNull.isNotNull(design.getButtons())) {
                    design.getButtons().addAll(defaultButtonDesignHtmls);
                } else {
                    design.setButtons(new ArrayList<>());
                    design.getButtons().addAll(defaultButtonDesignHtmls);
                }
            }
        }
        crudPageService.convertDesign(po, design);
        List<DataTableFieldDesignHtml> list = dataFieldDynamicService.getPageAutoTableFields(po.getJvsAppId(), po.getDataModelId(), po.getId());
        if (ObjectNull.isNotNull(list)) {
            design.getDataPage().getAutoTableFields().addAll(list);
        }
        if (ObjectNull.isNotNull(design)) {
            design.getDataPage().getAutoTableFields().forEach(e -> {
                //此处为了兼容老数据，将为空置为最后一个，避免查询出来结果不符合预期
                if (ObjectNull.isNull(e.getEnabledQueryTypes())) {
                    e.setEnabledQueryTypes(new ArrayList<>());
                }
                IDataFieldHandler handler = handlerMap.get(e.getComponentType().getDesc());
                FieldBasicsHtml html = handler.toHtml(e.getDesignJson());
                if (ObjectNull.isNotNull(html)) {
                    List enabledQueryTypes = handler.getEnabledQueryTypes(html);
                    e.setEnabledQueryTypes(enabledQueryTypes);
                    List<DataQueryType> collect = e.getEnabledQueryTypes().stream().distinct().collect(Collectors.toList());
                    e.setEnabledQueryTypes(collect);
                    e.getEnabledQueryTypes().remove(DataQueryType.isNull);
                    e.getEnabledQueryTypes().add(DataQueryType.isNull);
                }
            });
            //如果是选项卡类型，将不显示选项卡
            design.getDataPage().getAutoTableFields().removeIf(e -> e.getComponentType().equals(DataFieldType.tab));
            // 解析显示设置-关联模型，获取关联显示字段填充到列表页设计
            dataFieldDynamicService.parseModelDisplayFillFields(design.getDataPage().getAutoTableFields());
            return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
        } else {
            return R.ok(po);
        }
    }

    @GetMapping("/mobile")
    @ApiOperation("获取移动端的应用信息")
    public R mobile(@PathVariable String appId) {
        //只返回目录
        String userId = ModeUtils.getRealUser().getId();
        boolean mobile = IpUtil.isMobile();

        List<Tree<Object>> tree = useComponent.menu(appId, userId, mobile, ModeUtils.getMode(), null).getKey();
        List<Tree<Object>> trees = tree
                .stream()
                .findFirst()
                .map(Tree::getChildren)
                .orElseThrow(() -> new BusinessException("应用不存在"));
        return R.ok(trees);
    }


    @Log
    @GetMapping("/form/{id}")
    @ApiOperation("表单设计数据")
    @Transactional(rollbackFor = Exception.class)
    public R<FormPo> form(@PathVariable("id") String id, @PathVariable String appId) {
        FormPo po = formService.getById(id);
        if (Objects.isNull(po)) {
            return R.ok();
        }
        if (!po.getJvsAppId().equals(appId)) {
            return R.ok();
        }
        po = formService.getFormDetail(po, appId, id);
        if (ObjectNull.isNull(po)) {
            return R.ok();
        }

        FormDesignHtml design = DesignUtils.parseForm(po.getViewJson());
        if (ObjectNull.isNull(design.getFormdata())) {
            return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
        }
        //如果存在富文本和移动端，需要进行单独处理
        boolean mobile = IpUtil.isMobile();
        List<Map<String, Object>> forms = design.getFormdata().get(0).getForms();
        for (Map<String, Object> e : forms) {
            if (mobile) {
                if (DataFieldType.htmlEditor.name().equals(e.get("type"))) {
                    //如果是这富文本，需要对其内容进行重新处理
                    Object o = e.get("defaultValue");
                    if (ObjectNull.isNotNull(o)) {
                        //如果内容不为空
                        String s = HtmlEditorFieldHandler.replaceHtmlBodySrc((String) o, ossProperties.getEndpoint());
                        e.put("defaultValue", s);
                    }
                }
            }
            if (DataFieldType.tab.name().equals(e.get("type"))) {
                TabItemHtml tab = parseField(e, TabItemHtml.class);
                Set<Map.Entry<String, List<FieldBasicsHtml>>> entries = tab.getColumn().entrySet();
                for (Map.Entry<String, List<FieldBasicsHtml>> entry : entries) {
                    for (FieldBasicsHtml baseDto : entry.getValue()) {
                        if (DataFieldType.tableForm.name().equals(baseDto.getType())) {
                            if (ObjectNull.isNotNull(baseDto.getDataModelId())) {
                                TableFormItemHtml table = parseField(baseDto.getDesignJson(), TableFormItemHtml.class);
                                //判断是否有关联模型，关联模型中是否存在脱敏字段
                                List<String> fields = dynamicDataService.encryptionData(baseDto.getDataModelId());
                                //获取字段。将这表格内的字段进行删除掉
//                                table.getTableColumn().removeIf(a -> fields.contains(a.getProp()));
//                                baseDto = table;
                            }
                        }
                    }
                }
//                e.putAll(BeanToMapUtils.beanToMap(tab));
            }
            if (DataFieldType.tableForm.name().equals(e.get("type"))) {
                TableFormItemHtml baseDto = parseField(e, TableFormItemHtml.class);
                if (ObjectNull.isNotNull(baseDto.getDataModelId())) {
                    //判断是否有关联模型，关联模型中是否存在脱敏字段
                    List<String> fields = dynamicDataService.encryptionData(baseDto.getDataModelId());
                    //获取字段。将这表格内的字段进行删除掉
                    baseDto.getTableColumn().removeIf(a -> fields.contains(a.getProp()));
                    //todo 不处理表格脱敏
//                    e.putAll(BeanToMapUtils.beanToMap(baseDto));
                }
            }
        }

        DataModelPo byId = dataModelService.getById(po.getDataModelId());
        //处理表单中的脱敏
        List<String> fields = dynamicDataService.encryptionData(byId);
        design.getFormdata().get(0).getForms().removeIf(e -> {
            FieldPublicHtml baseDto = parseField(e, FieldPublicHtml.class);
            if (fields.contains(baseDto.getProp())) {
                if (!String.class.equals(baseDto.getType().getAClass())) {
                    return true;
                }
            }
            return false;
        });

        //操作按钮权限过滤目前只有一个表单，没有多个表单获取 0
        FormSettingHtml formsetting = design.getFormdata().get(0).getFormsetting();
        //设置是否开启数据和日志的默认值
        formsetting.setLogsEnable(ObjectNull.isNotNull(formsetting.getLogsEnable()) ? formsetting.getLogsEnable() : false);
        formsetting.setDataLogEnable(ObjectNull.isNotNull(formsetting.getDataLogEnable()) ? formsetting.getDataLogEnable() : false);
        List<ButtonDesignHtml> formButtonList = formsetting.getBtnSetting();
        //删除未应用的， 包括超级管理员
        formButtonList.removeIf(button -> {
            if (!Boolean.TRUE.equals(button.getEnable())) {
                return true;
            } else {
                //执行公式
                if (ObjectNull.isNotNull(button.getFormula())) {
                    return !designHandler.checkFormula(button.getFormula(), EnvConstant.FORM_BUTTON_DISPLAY);
                } else {
                    return false;
                }
            }
        });
        Set<String> prop = design.getFormdata().get(0).getForms().stream().map(e -> e.get("prop").toString()).collect(Collectors.toSet());

        List<Map> formAutoTableFields = dataFieldDynamicService.getFormAutoTableFields(appId, po.getDataModelId(), po.getId(), prop);
        if (ObjectNull.isNotNull(formAutoTableFields)) {
            formAutoTableFields.forEach(e -> {
                design.getFormdata().get(0).getForms().add(e);
            });
        }

        boolean isAdmin = jvsAppService.userIsAppAdmin(po.getJvsAppId());
        if (isAdmin) {
            return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
        }

        //判断是否有菜单权限，避免直接访问
        //判断按钮权限进行解析排除
        List<DesignRole> designRoleList = permissionCompatibleService.getOperationPermissions(appId, po.getId());
        //没有菜单的表单不判断权限
        if (ObjectNull.isNotNull(designRoleList)) {
            boolean hasPermit = RoleUtils.hasPermit(designRoleList);
            if (!hasPermit) {
                return R.failed("没有权限请联系管理员");
            }
        }

        Set<String> permit = RoleUtils.getPermitOperation(designRoleList, formButtonList);
        //删除没有权限的
        formButtonList.removeIf(button -> !permit.contains(button.getName()));
        return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
    }

    @Log
    @ApiOperation("获取表单")
    @GetMapping("/form/design/{resourceId}")
    public R<FormPo> getById(@PathVariable("resourceId") String id, @PathVariable String appId) {
        List<String> execs = new ArrayList<>();
        //根据结构添加公式组件数据
        List<FunctionBusinessPo> functionBusinessPos = businessMapper.selectList(Wrappers.query(new FunctionBusinessPo().setDesignId(id)));
        if (ObjectNull.isNotNull(functionBusinessPos)) {
            execs.addAll(functionBusinessPos
                    .stream()
                    .peek(e -> execs.add(e.getBusinessId()))
                    .filter(e -> ObjectNull.isNotNull(e.getRelatedIds()))
                    .flatMap(e -> e.getRelatedIds().stream())
                    .distinct()
                    .collect(Collectors.toList()));
        }

        FormPo one = formService.getOne(Wrappers.query(new FormPo().setJvsAppId(appId).setId(id)));
        FormDesignHtml design = DesignUtils.parseForm(one.getViewJson());
        design.setExecs(execs);
        one.setViewJson(JSONObject.toJSONString(design));
        return R.ok(one);
    }
}

