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
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
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
import cn.bctools.design.h5.entity.H5Design;
import cn.bctools.design.h5.service.H5DesignService;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.dto.AppRoleDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.screen.entity.ScreenPo;
import cn.bctools.design.screen.service.ScreenService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
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
    OssProperties ossProperties;
    AuthRoleServiceApi roleServiceApi;
    UseComponent useComponent;
    FormService formService;
    ScreenService screenService;
    DataModelService dataModelService;
    JvsAppService jvsAppService;
    CrudPageService crudPageService;
    H5DesignService h5DesignService;
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
        List<Tree<Object>> tree = useComponent.menu(appId, ModeUtils.getRealUser().getId(), mobile, ModeUtils.getMode());
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
        CrudPage po = crudPageService.getById(id);
        if (Objects.isNull(po)) {
            throw new BusinessException("设计页面未找到请联系管理员");
        }
        if (!po.getJvsAppId().equals(appId)) {
            throw new BusinessException("应用错误或设计不存在");
        }

        JvsApp app = jvsAppService.getById(po.getJvsAppId());
        AppRoleDto appRole = Optional.ofNullable(app.getRole()).orElseGet(AppRoleDto::new);
        boolean isAdmin = UserCurrentUtils.getCurrentUser().getAdminFlag()
                || jvsAppService.checkRole(appRole.getAdminMember(), UserCurrentUtils.getCurrentUser())
                || jvsAppService.checkRole(appRole.getDevMember(), UserCurrentUtils.getCurrentUser());
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
        //TODO 对属性字段 进行类型自适应处理
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
                } else {

                }
            });
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

        List<Tree<Object>> tree = useComponent.menu(appId, userId, mobile, ModeUtils.getMode());
        List<Tree<Object>> trees = tree
                .stream()
                .findFirst()
                .map(e -> e.getChildren())
                .orElseThrow(() -> new BusinessException("应用不存在"));
        return R.ok(trees);
    }

    @Log
    @GetMapping("/h5/{id}")
    @ApiOperation("H5设计数据")
    @Transactional(rollbackFor = Exception.class)
    public R h5(@PathVariable("id") String id, @PathVariable String appId) {
        H5Design po = h5DesignService.getById(id);
        if (!po.getJvsAppId().equals(appId)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        JvsApp byId = jvsAppService.getById(po.getJvsAppId());
        boolean isAdmin = UserCurrentUtils.getCurrentUser().getAdminFlag() || byId.getCreateById().equals(UserCurrentUtils.getUserId()) || po.getCreateById().equals(UserCurrentUtils.getUserId());
        if (isAdmin) {
            return R.ok(po);
        }
        return R.ok(po);
    }

    @Log
    @GetMapping("/screen/{resourceId}")
    @ApiOperation("大屏获取")
    @Transactional(rollbackFor = Exception.class)
    public R screen(@PathVariable("resourceId") String id, @PathVariable String appId) {
        ScreenPo one = screenService.getOne(Wrappers.query(new ScreenPo().setId(id).setJvsAppId(appId)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        return R.ok(one);
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
        JvsApp byId = jvsAppService.getById(po.getJvsAppId());
        boolean isAdmin = UserCurrentUtils.getCurrentUser().getAdminFlag() || byId.getCreateById().equals(UserCurrentUtils.getUserId()) || po.getCreateById().equals(UserCurrentUtils.getUserId());
        FormDesignHtml design = DesignUtils.parseForm(po.getViewJson());
        if (ObjectNull.isNull(design.getFormdata())) {
            return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
        }
        //如果存在富文本和移动端，需要进行单独处理
        boolean mobile = IpUtil.isMobile();
        if (mobile) {
            design.getFormdata().get(0).getForms().forEach(e -> {
                if (DataFieldType.htmlEditor.name().equals(e.get("type"))) {
                    //如果是这富文本，需要对其内容进行重新处理
                    Object o = e.get("defaultValue");
                    if (ObjectNull.isNotNull(o)) {
                        //如果内容不为空
                        String s = HtmlEditorFieldHandler.replaceHtmlBodySrc((String) o, ossProperties.getEndpoint());
                        e.put("defaultValue", s);
                    }
                }
            });
        }
        //操作按钮权限过滤目前只有一个表单，没有多个表单获取 0
        FormSettingHtml formsetting = design.getFormdata().get(0).getFormsetting();
        //设置是否开启数据和日志的默认值
        formsetting.setLogsEnable(ObjectNull.isNotNull(formsetting.getLogsEnable()) ? formsetting.getLogsEnable() : false);
        formsetting.setDataLogEnable(ObjectNull.isNotNull(formsetting.getDataLogEnable()) ? formsetting.getDataLogEnable() : false);
        List<ButtonDesignHtml> formButtonList = formsetting.getBtnSetting();
        //删除未应用的， 包括超级管理员
        formButtonList.removeIf(button -> !Boolean.TRUE.equals(button.getEnable()));
        Set<String> prop = design.getFormdata().get(0).getForms().stream().map(e -> e.get("prop").toString()).collect(Collectors.toSet());

        List<Map> formAutoTableFields = dataFieldDynamicService.getFormAutoTableFields(appId, po.getDataModelId(), po.getId(), prop);
        if (ObjectNull.isNotNull(formAutoTableFields)) {
            formAutoTableFields.forEach(e -> {
                design.getFormdata().get(0).getForms().add(e);
            });
        }
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
        return R.ok(formService.getOne(Wrappers.query(new FormPo().setJvsAppId(appId).setId(id))));
    }
}

