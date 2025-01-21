package cn.bctools.design.crud.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PinyinUtils;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.mapper.CrudPageMapper;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FormSelectItemDto;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.dto.page.DataPageDesignHtml;
import cn.bctools.design.data.fields.dto.page.DataTableFieldDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.impl.DataFieldServiceImpl;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.template.dto.PageTemplateDto;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 列表的列表配置项
 *
 * @author auto
 */
@Slf4j
@Service
@AllArgsConstructor
@Design(DesignType.page)
public class CrudPageServiceImpl extends ServiceImpl<CrudPageMapper, CrudPage> implements CrudPageService, IJvsDesigner {

    FormService formService;
    JvsAppService appService;
    DataModelService dataModelService;
    DataFieldService dataFieldService;
    AppMenuService appMenuService;
    Map<String, IDataFieldHandler> handlerMap;
    AppMenuHandler appMenuHandler;
    PermissionCompatibleService permissionCompatibleService;
    MapperMethodHandler mapperMethodHandler;

    @Override
    public CrudPage get(String pageId) {
        if (StringUtils.isBlank(pageId)) {
            throw new BusinessException("列表页id为空");
        }
        CrudPage page = this.getById(pageId);
        if (Objects.isNull(page)) {
            throw new BusinessException("列表页不存在");
        }
        return page;
    }

    @Override
    public CrudPage create(CrudPage crudPage) {
        String id = crudPage.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr(crudPage);
            crudPage.setId(id);
        }
        String appId = crudPage.getJvsAppId();
        // 初始化数据模型
        String dataModelId = crudPage.getDataModelId();
        if (StringUtils.isBlank(dataModelId)) {
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(crudPage.getViewJson());
            List<DataFieldPo> fields = DesignUtils.getFields(pageDesignHtml, dataModelId, id);
            dataModelId = dataModelService.createWithField(appId, id, DesignType.page, crudPage.getName(), fields);
            crudPage.setDataModelId(dataModelId);
        }
        crudPage.setIsDeploy(true);
        this.save(crudPage);
        appMenuHandler.addMenu(DesignType.page, crudPage.getId(), crudPage.getJvsAppId(), crudPage.getDataModelId(), crudPage.getName(), crudPage.getType());
        return crudPage;
    }

    @Override
    public CrudPage create(String dataModelId, String name, String type, String jvsAppId) {
        CrudPage crudPage = new CrudPage().setName(name).setType(type);
        crudPage.setJvsAppId(jvsAppId);
        crudPage.setDataModelId(dataModelId);
        this.create(crudPage);
        return crudPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initButton(String appId, String dataModelId, PageDesignHtml design) {
        for (ButtonDesignHtml button : design.getButtons()) {
            if (StringUtils.isBlank(button.getName())) {
                throw new BusinessException("请填写按钮名称");
            }
            //如果表单ID为空
            if (ObjectNull.isNull(button.getFormId())) {
                if (button.getType().equals(ButtonTypeEnum.btn_add)
                        || button.getType().equals(ButtonTypeEnum.btn_modify)
                        || button.getType().equals(ButtonTypeEnum.btn_detail)
                        || button.getType().equals(ButtonTypeEnum.btn_form)) {
                    FormPo formPo = formService.create(new FormPo().setJvsAppId(appId).setDataModelId(dataModelId).setName(design.getName() + "-" + button.getType().getDescription()));
                    button.setFormId(formPo.getId());
                }
            }
            if (button.getType().equals(ButtonTypeEnum.btn_export) && ObjectNull.isNotNull(button.getExportFields())) {
                List<String> collect = button.getExportFields().stream().map(e -> e.getAliasColumnName()).collect(Collectors.toList());
                //设置是否为导出字段
                design.getDataPage().getAutoTableFields().forEach(e -> e.setIsExport(collect.contains(e.getAliasColumnName())));
            }
        }
        DesignUtils.checkPageButton(design);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PageDesignHtml updateDesign(String id, PageDesignHtml init) {
        String name = init.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名称不能为空");
        }
        //设置统计标识
        DataPageDesignHtml dataPage = init.getDataPage();
        //字段配置
        if (dataPage != null && ObjectUtil.isNotEmpty(dataPage.getAutoTableFields())) {
            dataPage.getAutoTableFields().forEach(e -> e.setAliasColumnName(StrUtil.blankToDefault(e.getAliasColumnName(), PinyinUtils.getCameCasePinYin(StrUtil.blankToDefault(e.getShowChinese(), "a" + RandomUtil.randomString(6))))));
        }
        // 名称不能为空
        CrudPage crudPage = new CrudPage();
        crudPage.setId(id);
        crudPage.setName(name);
        crudPage.setDisplayType(init.getDisplayType());
        crudPage.setDescription(StringUtils.defaultString(init.getDescription(), ""));
        init.getDataRole().forEach(e -> e.put("id", IdWorker.getIdStr()));
        crudPage.setViewJson(JSONUtil.toJsonStr(init));
        crudPage.setStepDataPermission(init.getStepDataPermission());
        crudPage.setDataRole(JSONArray.parseArray(JSONObject.toJSONString(init.getDataRole())));
        crudPage.setDataRoleType(init.getDataRoleType());
        this.updateById(crudPage);

        // 修改菜单权限
        init.getButtons().addAll(Optional.ofNullable(init.getLeftTreeButton()).orElseGet(ArrayList::new));
        AppMenu appMenu = new AppMenu()
                .setName(crudPage.getName())
                .setDesignId(crudPage.getId())
                .setPermissionJson(JSONArray.parseArray(JSON.toJSONString(init.getButtons())))
                .setRole(JSONArray.parseArray(JSON.toJSONString(init.getRole())))
                .setRoleType(init.getRoleType())
                .setPermission(DesignPermissionUtil.parseDesign(DesignType.page, crudPage.getViewJson()))
                .setIcon(StringUtils.defaultString(init.getIcon(), ""));
        appMenuService.update(appMenu);
        return init;
    }

    @Override
    public List<PageTemplateDto> getTemplate(List<String> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<CrudPage> pages = this.listByIds(ids);
        if (ObjectUtils.isEmpty(pages)) {
            return Collections.emptyList();
        }
        List<PageTemplateDto> templateList = new ArrayList<>(pages.size());
        for (CrudPage page : pages) {
            PageTemplateDto templateDto = BeanCopyUtil.copy(page, PageTemplateDto.class);
            // 获取列表页的按钮数据
            String viewJson = page.getViewJson();
            PageDesignHtml designHtml = JSON.parseObject(viewJson, PageDesignHtml.class);
            List<ButtonDesignHtml> buttons = designHtml.getButtons();
            // 获取按钮的表单模板数据
            if (ObjectUtils.isNotEmpty(buttons)) {
                List<String> formIds = new ArrayList<>();
                for (ButtonDesignHtml button : buttons) {
                    String formId = button.getFormId();
                    if (StringUtils.isNotBlank(formId)) {
                        formIds.add(formId);
                    }
                }
                if (!formIds.isEmpty()) {
                    templateDto.setFormList(formService.getTemplate(formIds));
                }
            }
            templateList.add(templateDto);
        }
        return templateList;
    }

    @Override
    public List<FormSelectItemDto> getAllForm(String name, String jvsAppId) {
        List<CrudPage> pageList = this.list(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getName, CrudPage::getDataModelId, CrudPage::getViewJson)
                .like(StrUtil.isNotBlank(name), CrudPage::getName, name)
                .eq(StrUtil.isNotBlank(jvsAppId), CrudPage::getJvsAppId, jvsAppId)
                .orderByDesc(CrudPage::getCreateTime));
        return DesignUtils.getFormsFromPage(pageList);
    }

    @Override
    public DesignRoleSettingDto getDesignRole(String designId) {
        CrudPage page = this.getOne(Wrappers.<CrudPage>lambdaQuery().select(CrudPage::getDataModelId, CrudPage::getJvsAppId).eq(CrudPage::getId, designId));
        if (Objects.isNull(page)) {
            return null;
        }
        return permissionCompatibleService.getDesignPermission(page.getJvsAppId(), page.getId(), page.getDataModelId());
    }

    @Override
    public List<? extends ButtonSettingDto> getButtonSettings(String designId, String useCase) {
        CrudPage page = this.getOne(Wrappers.<CrudPage>lambdaQuery().select(CrudPage::getViewJson, CrudPage::getCreateById, CrudPage::getJvsAppId).eq(CrudPage::getId, designId));
        if (Objects.isNull(page)) {
            return Collections.emptyList();
        }
        String viewJson = page.getViewJson();
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(viewJson);
        List<ButtonDesignHtml> buttons = null;
        //判断上下文确定是请求的树，还是列表页
        if (EnvConstant.LEFT_TREE_BUTTON_DISPLAY.equals(useCase)) {
            buttons = pageDesignHtml.getLeftTreeButton().stream().peek(e -> e.setPosition("line")).collect(Collectors.toList());
        } else {
            buttons = pageDesignHtml.getButtons();
        }
        //
        //把删除按钮移到最后去
        Optional<ButtonDesignHtml> first = buttons.stream().filter(e -> e.getType().equals(ButtonTypeEnum.btn_delete)).findFirst();
        //添加默认办理按钮
        List<ButtonDesignHtml> defaultButtonDesignHtmls = getSystemDefaultButtons();

        boolean isAdmin = UserCurrentUtils.getCurrentUser().getAdminFlag() || page.getCreateById().equals(UserCurrentUtils.getUserId());
        if (isAdmin) {
            if (EnvConstant.LEFT_TREE_BUTTON_DISPLAY.equals(useCase)) {
                //树形结构不处理
            } else {
                buttons.addAll(defaultButtonDesignHtmls);
            }
            if (first.isPresent()) {
                ButtonDesignHtml o = first.get();
                buttons.remove(o);
                buttons.add(o);
            }
            return buttons;
        } else {
            //权限过滤
            //判断按钮权限进行解析排除
            List<DesignRole> designRoles = permissionCompatibleService.getOperationPermissions(page.getJvsAppId(), designId);
            boolean check = RoleUtils.hasPermit(designRoles);
            if (!check) {
                return new ArrayList<>();
            }
            Set<String> permit = RoleUtils.getPermitOperation(designRoles, buttons);
            // 操作按钮权限过滤
            buttons.removeIf(button -> !permit.contains(button.getName()));
            first = buttons.stream().filter(e -> e.getType().equals(ButtonTypeEnum.btn_delete)).findFirst();
            buttons.addAll(defaultButtonDesignHtmls);
            if (first.isPresent()) {
                ButtonDesignHtml o = first.get();
                buttons.remove(o);
                buttons.add(o);
            }
            return buttons;
        }
    }

    @Override
    public List<ButtonDesignHtml> getSystemDefaultButtons() {
        List<ButtonDesignHtml> buttons = new ArrayList<>();
        //添加默认工作流办理按钮
        ButtonDesignHtml workflowButton = new ButtonDesignHtml();
        workflowButton.setType(ButtonTypeEnum.oa_workflow);
        workflowButton.setEnable(true);
        workflowButton.setName(ButtonTypeEnum.oa_workflow.getDescription());
        workflowButton.setPermissionFlag(ButtonTypeEnum.oa_workflow.name());
        workflowButton.setPosition("line");
        buttons.add(workflowButton);

        //添加默认工作流查看进度按钮
        ButtonDesignHtml workflowProgressButtons = new ButtonDesignHtml();
        workflowProgressButtons.setType(ButtonTypeEnum.oa_workflow_progress);
        workflowProgressButtons.setEnable(true);
        workflowProgressButtons.setName(ButtonTypeEnum.oa_workflow_progress.getDescription());
        workflowProgressButtons.setPermissionFlag(ButtonTypeEnum.oa_workflow_progress.name());
        workflowProgressButtons.setPosition("line");
        buttons.add(workflowProgressButtons);

        //添加默认工作流重新发起按钮
        ButtonDesignHtml workflowRestartButtons = new ButtonDesignHtml();
        workflowRestartButtons.setType(ButtonTypeEnum.oa_workflow_restart);
        workflowRestartButtons.setEnable(true);
        workflowRestartButtons.setName(ButtonTypeEnum.oa_workflow_restart.getDescription());
        workflowRestartButtons.setPermissionFlag(ButtonTypeEnum.oa_workflow_restart.name());
        workflowRestartButtons.setPosition("line");
        buttons.add(workflowRestartButtons);

        //添加默认工作流撤回按钮
        ButtonDesignHtml workflowCancelButtons = new ButtonDesignHtml();
        workflowCancelButtons.setType(ButtonTypeEnum.oa_workflow_cancel);
        workflowCancelButtons.setEnable(true);
        workflowCancelButtons.setName(ButtonTypeEnum.oa_workflow_cancel.getDescription());
        workflowCancelButtons.setPermissionFlag(ButtonTypeEnum.oa_workflow_cancel.name());
        workflowCancelButtons.setPosition("line");
        buttons.add(workflowCancelButtons);
        return buttons;
    }

    @Override
    public void delete(String appId, String designId) {
        CrudPage page = this.getOne(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getIsDeploy, CrudPage::getViewJson)
                .eq(CrudPage::getJvsAppId, appId)
                .eq(CrudPage::getId, designId));
        if (Objects.isNull(page)) {
            throw new BusinessException("列表页不存在");
        }
        if (ObjectNull.isNotNull(page.getViewJson())) {
            //如果设计不为空的情况下继续向下执行
            //删除列表的同时删除关联的表单
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(page.getViewJson());
            List<String> form = pageDesignHtml.getButtons().stream().filter(e -> ObjectNull.isNotNull(e.getFormId())).map(ButtonDesignHtml::getFormId).collect(Collectors.toList());
            if (ObjectNull.isNotNull(form)) {
                form.add(designId);
                //删除列表页里面的表单
                formService.removeByIds(form);
                //根据form删除表单的字段
                dataFieldService.remove(new LambdaQueryWrapper<DataFieldPo>().in(DataFieldPo::getDesignId, form).eq(DataFieldPo::getJvsAppId, appId));
                // 删除菜单中的表单
                appMenuHandler.removeMenuByDesignIds(form);
            }
        }
        this.removeById(designId);
    }

    @Override
    public void updateName(String appId, @Nullable String designId, @Nullable String name) {
        if (ObjectNull.isNull(name)) {
            return;
        }
        this.update(Wrappers.<CrudPage>lambdaUpdate()
                .set(CrudPage::getName, name)
                .eq(CrudPage::getJvsAppId, appId)
                .eq(StringUtils.isNotBlank(designId), CrudPage::getId, designId));
    }

    @Override
    public String getDataModelId(String appId, String designId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(designId)) {
            return null;
        }
        return Optional.ofNullable(getOne(Wrappers.<CrudPage>lambdaQuery()
                        .eq(CrudPage::getJvsAppId, appId)
                        .eq(CrudPage::getId, designId))).orElseGet(CrudPage::new)
                .getDataModelId();
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<CrudPage>lambdaQuery().eq(CrudPage::getJvsAppId, appId));
    }


    @Override
    public void convertDesign(CrudPage po, PageDesignHtml design) {
        if (ObjectNull.isNull(design)) {
            return;
        }
        if (ObjectNull.isNull(design.getDataPage())) {
            return;
        }
        String buttonstr = JSONObject.toJSONString(design.getButtons()).toString();
        List<DataTableFieldDesignHtml> autoTableFields = design.getDataPage().getAutoTableFields();
        //调整支持如果查询列表页的查询条件，根据自己的列表页字段进行展示
        List<FieldBasicsHtml> fieldBasicsHtmls = dataFieldService.getFieldsCache(new DataFieldServiceImpl.FieldCacheKey()
                .setAppId(po.getJvsAppId())
                .setModelId(po.getDataModelId())
                .setFieldPredicate(e -> {
                    if (ObjectNull.isNotNull(e.getDesignId())) {
                        return buttonstr.contains(e.getDesignId());
                    } else {
                        return true;
                    }
                })
                .setGetDesignJson(true)
                .setAddDefaultFields(true));
        List<FieldBasicsHtml> list = JSON.parseArray(JSON.toJSONString(fieldBasicsHtmls), FieldBasicsHtml.class);

        autoTableFields.forEach(e -> {
            String aliasColumnName = e.getAliasColumnName();
            //类型转换  如果是需要转换的，且必须是查询字段的
            list.stream()
                    //字段匹配的
                    .filter(s -> s.getFieldKey().equals(aliasColumnName))
                    //控件类型不能为空
                    .filter(s -> ObjectNull.isNotNull(s.getFieldType()))
                    //获取第一个
                    .forEach(s -> {
                        if (s.getType().equals(e.getComponentType()) && DataFieldType.input.equals(e.getComponentType())) {
                            //排除不是输入框的
                            e.setDesignJson(s.getDesignJson());
                            if (ObjectNull.isNull(e.getEnabledQueryTypes())) {
                                e.setEnabledQueryTypes(new ArrayList<>());
                            }
                            if (!e.getEnabledQueryTypes().contains(DataQueryType.isNull)) {
                                e.getEnabledQueryTypes().add(DataQueryType.isNull);
                            }
                        } else {
                            IDataFieldHandler iDataFieldHandler = handlerMap.get(s.getType().getDesc());
                            if (ObjectNull.isNotNull(s.getDesignJson(), iDataFieldHandler)) {
                                FieldBasicsHtml publicHtml = iDataFieldHandler.toHtml(s);
                                if (ObjectNull.isNotNull(publicHtml)) {
                                    List enabledQueryTypes = iDataFieldHandler.getEnabledQueryTypes(publicHtml);
                                    if (!enabledQueryTypes.contains(DataQueryType.isNull)) {
                                        enabledQueryTypes.add(DataQueryType.isNull);
                                    }
                                    e.setEnabledQueryTypes(enabledQueryTypes);
                                }
                            }
                            e.setDesignJson(s.getDesignJson());
                            e.setComponentType(s.getType());
                        }
                    });
            if (ObjectNull.isNull(e.getEnabledQueryTypes())) {
                ArrayList<DataQueryType> dataQueryTypes = new ArrayList<>();
                dataQueryTypes.add(DataQueryType.isNull);
                dataQueryTypes.add(DataQueryType.like);
                dataQueryTypes.add(DataQueryType.eq);
                dataQueryTypes.add(DataQueryType.ne);
                e.setEnabledQueryTypes(dataQueryTypes);
                e.setComponentType(DataFieldType.input);
            }
            e.setEnabledQueryTypes(e.getEnabledQueryTypes().stream().distinct().collect(Collectors.toList()));
        });
    }
}
