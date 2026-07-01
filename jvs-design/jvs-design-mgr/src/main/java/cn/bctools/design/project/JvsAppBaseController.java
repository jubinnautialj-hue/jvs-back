package cn.bctools.design.project;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.entity.enums.DataRoleTypeEnum;
import cn.bctools.design.crud.service.AutoCreateCrudDesignService;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.enums.FieldTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormDataHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
import cn.bctools.design.data.fields.dto.page.*;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.fields.enums.FormTypeEnum;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.dto.*;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.dto.AppRoleDto;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppTemplateService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.config.JvsDefaultBearerTokenResolver;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Slf4j
@Api(tags = "应用基础信息")
@RestController
@AllArgsConstructor
@RequestMapping("/base/JvsApp")
public class JvsAppBaseController {

    JvsAppService service;
    IdentificationService identificationService;
    RedisUtils redisUtils;
    JvsAppTemplateService templateService;
    CrudPageService pageService;
    FormService formService;
    DataFieldService fieldService;
    AppMenuTypeService appMenuTypeService;
    JvsAppVersionService appVersionService;
    AuthUserServiceApi authUserServiceApi;
    Map<String, IDataFieldHandler> fieldHandlerMap;
    AutoCreateCrudDesignService autoCreateCrudDesignService;
    OAuth2AuthorizationService oAuth2AuthorizationService;
    static JvsDefaultBearerTokenResolver jvsDefaultBearerTokenResolver = new JvsDefaultBearerTokenResolver();


    @ApiOperation("应用-获取名称")
    @GetMapping("/name/{appId}")
    public R page(@PathVariable String appId) {
        JvsApp one = service.getOne(Wrappers.query(new JvsApp().setId(appId)).lambda().select(JvsApp::getName, JvsApp::getEnableVersionFeature));
        return R.ok(one);
    }

    @ApiOperation("应用-进度查询")
    @GetMapping("/schedule/{templateId}")
    public R schedule(@PathVariable String templateId) {
        ProgressDto o = (ProgressDto) redisUtils.get("jvsAppId::sync:progress" + UserCurrentUtils.getUserId() + UserCurrentUtils.getCurrentUser().getTenantId() + templateId);
        return R.ok(o);
    }

    @Log
    @ApiOperation("应用-新增")
    @PostMapping("/save")
    public R save(@RequestBody JvsAppSaveDto saveDto) {
        // 开发模式可以创建应用
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(ModeUtils.getMode()))) {
            throw new BusinessException("请切换到开发模式");
        }
        //如果模板ID不为空，则从模板ID创建应用
        String templateId = saveDto.getTemplateId();
        TenantContextHolder.setTenantId(UserCurrentUtils.getCurrentUser().getTenantId());
        if (ObjectNull.isNotNull(templateId)) {
            //直接解析
            templateService.createByTemplateId(templateId);
            return R.ok();
        }

        JvsApp app = BeanCopyUtil.copy(saveDto, JvsApp.class);
        app.setSecret(JvsAppSecretUtils.getAppSecret(IdGenerator.getIdStr()));
        //创建应用的时候默认为false
        app.setIsDeploy(false);
        // 设置默认权限
        app.setDefaultRole();
        // 新创建的应用默认启用轻应用版本功能
        app.setEnableVersionFeature(Boolean.TRUE);
        service.save(app);
        Identification entity = new Identification().setJvsAppId(app.getId()).setDesignId(app.getId()).setDesignType(DesignType.app).setIdentifier(IdGenerator.getIdStr(36));
        identificationService.save(entity);
        // 创建默认目录
        appMenuTypeService.save(new AppMenuType().setType("未命名目录").setSort(0).setJvsAppId(app.getId()));
        // 初始化版本
        templateService.initVersionDevTemplate(app);
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(app.getId()).setJvsAppName(app.getName()));
        return R.ok(app);
    }

    @Log
    @ApiOperation("应用-分页")
    @GetMapping("/page")
    public R<Page<AppDto>> page(Page<JvsApp> page, JvsApp dto) {
        // 获取模式相关的所有应用id
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (ObjectNull.isNull(appIds)) {
            return R.ok(new Page<>(page.getCurrent(), page.getSize()));
        }
        //必须是应用管理员
        LambdaQueryWrapper<JvsApp> queryWrapper = new LambdaQueryWrapper<JvsApp>()
                .select(JvsApp::getId,
                        JvsApp::getName,
                        JvsApp::getCreateTime,
                        JvsApp::getCreateById,
                        JvsApp::getIsDeploy,
                        JvsApp::getSecret,
                        JvsApp::getDescription,
                        JvsApp::getLogo,
                        JvsApp::getRole,
                        JvsApp::getRecommend
                )
                .in(JvsApp::getId, appIds)
                .eq(ObjectNull.isNotNull(dto.getIsDeploy()), JvsApp::getIsDeploy, dto.getIsDeploy())
                .like(ObjectNull.isNotNull(dto.getName()), JvsApp::getName, dto.getName());
        //不管是不是超级管理员,都只能看自己有权限的,其它的不能查看
        // 根据用户id查询
        String userId = ModeUtils.getRealUser().getId();
        JSONObject conditionUser = new JSONObject();
        conditionUser.put("type", "user");
        conditionUser.put("id", userId);
        String conditionUserJson = JSON.toJSONString(conditionUser);
        queryWrapper.and(wrapper -> wrapper.eq(JvsApp::getCreateById, userId)
                // 查询当前用户作为应用管理员的应用
                .or(orUser -> orUser.apply(SqlFunctionUtil.jsonContainsObject(Get.name(JvsApp::getRole), "$.adminMember", conditionUserJson)))
                // 查询当前用户作为应用开发人员的应用
                .or(orUser -> orUser.apply(SqlFunctionUtil.jsonContainsObject(Get.name(JvsApp::getRole), "$.devMember", conditionUserJson)))
        );
        service.page(page, queryWrapper.orderByDesc(JvsApp::getCreateTime));
        Page<AppDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        if (ObjectNull.isNull(page.getRecords())) {
            return R.ok(pageDto);
        }
        List<AppDto> pageDtos = page.getRecords().stream().map(app -> {
            AppDto appDto = convertAppRole(app);
            appDto.setRole(null);
            return appDto;
        }).collect(Collectors.toList());
        pageDto.setRecords(pageDtos);
        pageDto.setTotal(page.getTotal());
        return R.ok(pageDto);
    }

    @Log
    @ApiOperation("应用-详情")
    @GetMapping("/{appId}/detail")
    public R<AppDto> detail(@PathVariable String appId) {
        JvsApp one = service.getById(appId);
        AppDto appDto = convertAppRole(one);
        appDto.setSecret(null);
        return R.ok(appDto);
    }

    private AppDto convertAppRole(JvsApp app) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        AppDto appDto = BeanCopyUtil.copy(app, AppDto.class);
        AppRoleDto appRole = Optional.ofNullable(appDto.getRole()).orElseGet(AppRoleDto::new);
        // 当前用户是否是应用管理员
        if (userDto.getId().equals(appDto.getCreateById()) ||
            userDto.getAdminFlag() ||
            service.checkRole(appRole.getAdminMember(), UserCurrentUtils.getCurrentUser())) {
            appDto.getAppRoles().add(Get.name(AppRoleDto::getAdminMember));
        }
        // 当前用户是否是开发人员
        if (service.checkRole(appRole.getDevMember(), UserCurrentUtils.getCurrentUser())) {
            appDto.getAppRoles().add(Get.name(AppRoleDto::getDevMember));
        }
        return appDto;
    }

    @ApiOperation("获取用户信息,包含模拟用户")
    @GetMapping("/user/info")
    public R<UserDto> userInfo() {
        if (ModeUtils.whetherAnalogUser()) {
            return R.ok(ModeUtils.getSwitchMode().getAnalogUser());
        }
        return R.ok(UserCurrentUtils.getCurrentUser());
    }

    @ApiOperation("模式切换")
    @PostMapping("/switch/mode")
    public R<Boolean> submitBeta(@Validated @RequestBody SwitchModeDto dto, HttpServletRequest request) {
        String resolve = jvsDefaultBearerTokenResolver.resolve(request);
        CustomUser user = oAuth2AuthorizationService.findByToken(resolve, OAuth2TokenType.ACCESS_TOKEN).getAttribute("user");
        //没有设计权限
        if (user.getPermissions().contains("jvs_app")) {
            // 查询模拟用户信息
            String userId = Optional.ofNullable(dto.getAnalogUser()).map(UserDto::getId).orElse(null);
            if (ObjectNull.isNotNull(userId)) {
                //模拟用户不能设置为管理员
                dto.setAnalogUser(authUserServiceApi.getById(userId).getData().setAdminFlag(false).setPlatformAdmin(false));
            }
            ModeUtils.setModeCache(dto);
        }
        return R.ok();
    }

    @Log
    @ApiOperation("获取所有应用的json结构")
    @GetMapping("/app/json")
    public R saveJson() {
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        Map<String, List<CrudPage>> collect = pageService.list(new LambdaQueryWrapper<CrudPage>().in(CrudPage::getJvsAppId, appIds)).stream().collect(Collectors.groupingBy(CrudPage::getJvsAppId));
        List<AppJsonDto> jvsApps = service.listByIds(appIds)
                .parallelStream()
                .filter(e -> collect.containsKey(e.getId()))
                .map(e -> {
                    List<AppJsonDto.AppMenusDto> collect1 = collect.get(e.getId())
                            .stream()
                            .map(a -> {
                                AppJsonDto.AppMenusDto appMenusDto = new AppJsonDto.AppMenusDto().setMenu(a.getName());
                                List<AppJsonDto.AppJsonField> collect2 = fieldService.getAllField(a.getJvsAppId(), a.getDataModelId())
                                        .stream()
                                        .map(v -> {
                                            AppJsonDto.AppJsonField appJsonField = new AppJsonDto.AppJsonField().setField(v.getFieldKey()).setName(v.getName()).setType(v.getType().getDesc());
                                            if (v.getType().equals(DataFieldType.select) || v.getType().equals(DataFieldType.checkbox) || v.getType().equals(DataFieldType.radio)) {
                                                if ("option".equals(JvsJsonPath.read(v.getDesignJson(), "datatype"))) {
                                                    List<String> read = (List<String>) JvsJsonPath.read(v.getDesignJson(), "dicData.label");
                                                    appJsonField.setDicData(read);
                                                } else {
                                                    return null;
                                                }
                                            }
                                            return appJsonField;
                                        })
                                        .filter(ObjectNull::isNotNull)
                                        .collect(Collectors.toList());
                                appMenusDto.setFields(collect2);
                                return appMenusDto;
                            }).collect(Collectors.toList());
                    AppJsonDto appJsonDto = new AppJsonDto().setSystem(e.getName()).setChildren(collect1);
                    return appJsonDto;
                }).collect(Collectors.toList());
        return R.ok(jvsApps);
    }

    @Log
    @ApiOperation("应用-根据json直接创建")
    @PostMapping("/save/json")
    @Transactional(rollbackFor = Exception.class)
    public R saveJson(@RequestBody AppJsonDto app) {
        // 开发模式可以创建应用
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(ModeUtils.getMode()))) {
            throw new BusinessException("请切换到开发模式");
        }
        //查询应用名，和当前用户是否已经存在，存在则不再创建
        long count = service.count(new LambdaQueryWrapper<JvsApp>().eq(JvsApp::getName, app.getSystem()).eq(JvsApp::getCreateById, UserCurrentUtils.getUserId()));
        if (count > 0) {
            throw new BusinessException("您已经创建了该应用");
        }
        JvsApp entity = new JvsApp();
        entity.setName(app.getSystem());
        entity.setSecret(JvsAppSecretUtils.getAppSecret(IdGenerator.getIdStr()));
        //创建应用的时候默认为false
        entity.setIsDeploy(false);
        // 设置默认权限
        entity.setDefaultRole();
        // 新创建的应用默认启用轻应用版本功能
        entity.setEnableVersionFeature(Boolean.TRUE);
        service.save(entity);
        templateService.initVersionDevTemplate(entity);

        Map<String, DataFieldType> collect = Arrays.stream(DataFieldType.values()).collect(Collectors.toMap(DataFieldType::getDesc, Function.identity()));
        //填充字段的类型数据
        app.getChildren().forEach(b -> b.getFields().forEach(c -> c.setFieldType(collect.get(c.getType()))));
        //直接创建菜单， 不需要目录

        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(entity.getId()));
        for (AppJsonDto.AppMenusDto child : app.getChildren()) {
            String menu = child.getMenu();
            CrudPage crudPage = new CrudPage().setIsDeploy(true).setId(IdWorker.get32UUID()).setType(entity.getId()).setName(menu).setJvsAppId(entity.getId());

            //初始化按钮
            ArrayList<DesignRole> role = new ArrayList<>();
            role.add(new DesignRole());
            List<ButtonDesignHtml> buttons = new ArrayList<>();
            {
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("新增" + menu);
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("top");
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_add);
                    String stra = "{\n" +
                                  "        \"formdata\":[\n" +
                                  "          {\n" +
                                  "            \"forms\":[\n" +
                                  "            ],\n" +
                                  "            \"formsetting\":{\n" +
                                  "              \"labelposition\":\"top\",\n" +
                                  "              \"labelwidth\":80,\n" +
                                  "              \"formsize\":\"mini\",\n" +
                                  "              \"btnSetting\":[\n" +
                                  "              ],\n" +
                                  "              \"fullscreen\":false\n" +
                                  "            },\n" +
                                  "            \"autoTableFields\":[\n" +
                                  "            ]\n" +
                                  "          }],\n" +
                                  "        \"formType\":\"normalForm\"\n" +
                                  "      }";
                    buttonDesignHtml.setForm(JSONObject.parseObject(stra, HashMap.class));
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("修改");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("line");
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_modify);
                    String stra = "{\n" +
                                  "        \"formdata\":[\n" +
                                  "          {\n" +
                                  "            \"forms\":[\n" +
                                  "            ],\n" +
                                  "            \"formsetting\":{\n" +
                                  "              \"labelposition\":\"top\",\n" +
                                  "              \"labelwidth\":80,\n" +
                                  "              \"formsize\":\"mini\",\n" +
                                  "              \"btnSetting\":[\n" +
                                  "              ],\n" +
                                  "              \"fullscreen\":false\n" +
                                  "            },\n" +
                                  "            \"autoTableFields\":[\n" +
                                  "            ]\n" +
                                  "          }],\n" +
                                  "        \"formType\":\"normalForm\"\n" +
                                  "      }";
                    buttonDesignHtml.setForm(JSONObject.parseObject(stra, HashMap.class));
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("详情");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("line");
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_detail);
                    String stra = "{\n" +
                                  "        \"formdata\":[\n" +
                                  "          {\n" +
                                  "            \"forms\":[\n" +
                                  "            ],\n" +
                                  "            \"formsetting\":{\n" +
                                  "              \"labelposition\":\"top\",\n" +
                                  "              \"labelwidth\":80,\n" +
                                  "              \"formsize\":\"mini\",\n" +
                                  "              \"btnSetting\":[\n" +
                                  "              ],\n" +
                                  "              \"fullscreen\":false\n" +
                                  "            },\n" +
                                  "            \"autoTableFields\":[\n" +
                                  "            ]\n" +
                                  "          }],\n" +
                                  "        \"formType\":\"normalForm\"\n" +
                                  "      }";
                    buttonDesignHtml.setForm(JSONObject.parseObject(stra, HashMap.class));
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("导入");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("top");
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_import);
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("导出");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("top");
                    buttonDesignHtml.setExportFields(new ArrayList<>());
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_export);
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("下载模板");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("top");
                    buttonDesignHtml.setExportFields(new ArrayList<>());
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_download_template);
                    buttons.add(buttonDesignHtml);
                }
                {
                    ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                    buttonDesignHtml.setEnable(true);
                    buttonDesignHtml.setFormType("normalForm");
                    buttonDesignHtml.setIsDefault(true);
                    buttonDesignHtml.setMobileEnable(true);
                    buttonDesignHtml.setName("删除");
                    buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                    buttonDesignHtml.setPosition("line");
                    buttonDesignHtml.setExportFields(new ArrayList<>());
                    buttonDesignHtml.setType(ButtonTypeEnum.btn_delete);
                    buttons.add(buttonDesignHtml);
                }
            }
            List<DataTableFieldDesignHtml> autoTableFields = child.getFields().stream().map(e -> new DataTableFieldDesignHtml().setAliasColumnName(e.getField()).setColumnName(e.getField()).setDbJavaType(FieldTypeEnum.field_text).setShow(true).setShowChinese(e.getName()).setSupportShow(true).setSupportQuery(true).setSupportSettings(true).setSupportSort(false).setSupportStatistics(false)).collect(Collectors.toList());
            List<String> field = autoTableFields.stream().map(DataTableFieldDesignHtml::getFieldName).collect(Collectors.toList());
            DataPageDesignHtml datapage = new DataPageDesignHtml();
            datapage.setAutoTableFields(autoTableFields);
            datapage.setQueryJson("{\"size\":20,\"current\":1}");
            datapage.setDataPageJson("{\"records\":" + JSONObject.toJSONString(field) + ",\"current\":1,\"total\":0}");
            PageDesignHtml pageDesignHtml = new PageDesignHtml().setStepDataPermission(false).setSorts(new ArrayList<>()).setRole(role).setRoleType(true).setParameters(new ArrayList<>())
                    .setPageTableTitle(new ArrayList<>())
                    .setName(menu)
                    .setMenuWidth(150)
                    .setMenuFixed(false)
                    .setLeftTreeButton(new ArrayList<>())
                    .setIcon("icon-haxi")
                    .setDisplayType(LayoutEnum.table)
                    .setDataRoleType(DataRoleTypeEnum.data_model)
                    .setDataRole(new ArrayList<>())
                    .setDataPage(datapage)
                    .setButtons(buttons);
            crudPage.setViewJson(JSONObject.toJSONString(pageDesignHtml));
            //创建列表
            crudPage = pageService.create(crudPage);
            pageService.initButton(entity.getId(), crudPage.getDataModelId(), pageDesignHtml);
            pageService.updateDesign(crudPage.getId(), pageDesignHtml);
            {//根据表单信息创建对应的表单并更新
                String formId = pageDesignHtml.getButtons().get(0).getFormId();
                //新增的表单
                FormPo formPo = formService.getById(formId);
                FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
                //根据字段和类型，进行添加了初始化字段
                List<Map<String, Object>> formsList = child.getFields().stream().map(e -> {
                    return fieldHandlerMap.get(e.getType()).generate(e.getName(), e.getField(), e.getDicData());
                }).map(e -> (Map<String, Object>) e).collect(Collectors.toList());
                List<FormDataHtml> formdata = formDesignHtml.getFormdata();
                if (ObjectNull.isNull(formdata)) {
                    formdata = new ArrayList<>();
                    FormDataHtml e = new FormDataHtml();
                    formdata.add(e);
                    formDesignHtml.setFormdata(formdata);
                }
                List<String> fields = child.getFields().stream().map(AppJsonDto.AppJsonField::getField).collect(Collectors.toList());
                formDesignHtml.getFormdata().get(0).setForms(formsList);
                ArrayList<ButtonDesignHtml> btnSetting = new ArrayList<>();
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setName("提交");
                buttonDesignHtml.setButtonType("submit");
                buttonDesignHtml.setFlag(true);
                buttonDesignHtml.setPermissionFlag(formPo.getId() + "-" + buttonDesignHtml.getButtonType() + "-" + IdGenerator.getIdStr(36));
                btnSetting.add(buttonDesignHtml);
                formDesignHtml.getFormdata().get(0)
                        .setFormJson(JSONObject.toJSONString(fields))
                        .setFormsetting(new FormSettingHtml()
                                .setLabelposition("top")
                                .setLabelwidth(80)
                                .setFormsize("mini")
                                .setFullscreen(false)
                                .setSubmitBtn(true)
                                .setEmptyBtn(false)
                                .setCancal(false)
                                .setBtnSetting(btnSetting)
                                .setPopupWidth(50)
                                .setPopupType("drawer"));
                formDesignHtml.setFormType(FormTypeEnum.normalForm);
                formPo.setViewJson(JSONObject.toJSONString(formDesignHtml));
                List<DataFieldPo> datafields =
                        formsList.stream().map(e -> BeanCopyUtil.copy(e, DataFieldPo.class).setFieldKey(e.get("prop").toString()).setFieldName(e.get("label").toString()).setFieldType(DataFieldType.valueOf(e.get("type").toString())).setDesignJson(e)).collect(Collectors.toList());
                fieldService.updateFields(entity.getId(), formPo.getId(), DesignType.form, formPo.getDataModelId(), datafields);
                formService.updateById(formPo);
            }

            {//根据表单信息创建对应的表单并更新
                String formId = pageDesignHtml.getButtons().get(1).getFormId();
                //新增的表单
                FormPo formPo = formService.getById(formId);
                FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
                //根据字段和类型，进行添加了初始化字段
                List<Map<String, Object>> formsList = child.getFields().stream().map(e -> {
                    return fieldHandlerMap.get(e.getType()).generate(e.getName(), e.getField(), e.getDicData());
                }).map(e -> (Map<String, Object>) e).collect(Collectors.toList());
                List<FormDataHtml> formdata = formDesignHtml.getFormdata();
                if (ObjectNull.isNull(formdata)) {
                    formdata = new ArrayList<>();
                    FormDataHtml e = new FormDataHtml();
                    formdata.add(e);
                    formDesignHtml.setFormdata(formdata);
                }
                List<String> fields = child.getFields().stream().map(AppJsonDto.AppJsonField::getField).collect(Collectors.toList());
                formDesignHtml.getFormdata().get(0).setForms(formsList);
                ArrayList<ButtonDesignHtml> btnSetting = new ArrayList<>();
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setName("提交");
                buttonDesignHtml.setButtonType("submit");
                buttonDesignHtml.setFlag(true);
                buttonDesignHtml.setPermissionFlag(formPo.getId() + "-" + buttonDesignHtml.getButtonType() + "-" + IdGenerator.getIdStr(36));
                btnSetting.add(buttonDesignHtml);
                formDesignHtml.getFormdata().get(0)
                        .setFormJson(JSONObject.toJSONString(fields))
                        .setFormsetting(new FormSettingHtml()
                                .setLabelposition("top")
                                .setLabelwidth(80)
                                .setFormsize("mini")
                                .setFullscreen(false)
                                .setSubmitBtn(true)
                                .setEmptyBtn(false)
                                .setCancal(false)
                                .setBtnSetting(btnSetting)
                                .setPopupWidth(50)
                                .setPopupType("drawer"));
                formDesignHtml.setFormType(FormTypeEnum.normalForm);
                formPo.setViewJson(JSONObject.toJSONString(formDesignHtml));
                formService.updateById(formPo);
            }
            {//根据表单信息创建对应的表单并更新
                String formId = pageDesignHtml.getButtons().get(2).getFormId();
                //新增的表单
                FormPo formPo = formService.getById(formId);
                FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
                //根据字段和类型，进行添加了初始化字段
                List<Map<String, Object>> formsList = child.getFields().stream().map(e -> {
                    return fieldHandlerMap.get(e.getType()).generate(e.getName(), e.getField(), e.getDicData());
                }).map(e -> (Map<String, Object>) e).collect(Collectors.toList());
                List<FormDataHtml> formdata = formDesignHtml.getFormdata();
                if (ObjectNull.isNull(formdata)) {
                    formdata = new ArrayList<>();
                    FormDataHtml e = new FormDataHtml();
                    formdata.add(e);
                    formDesignHtml.setFormdata(formdata);
                }
                List<String> fields = child.getFields().stream().map(AppJsonDto.AppJsonField::getField).collect(Collectors.toList());
                formDesignHtml.getFormdata().get(0).setForms(formsList);
                ArrayList<ButtonDesignHtml> btnSetting = new ArrayList<>();
                formDesignHtml.getFormdata().get(0)
                        .setFormJson(JSONObject.toJSONString(fields))
                        .setFormsetting(new FormSettingHtml()
                                .setLabelposition("top")
                                .setLabelwidth(80)
                                .setFormsize("mini")
                                .setFullscreen(false)
                                .setSubmitBtn(true)
                                .setEmptyBtn(false)
                                .setCancal(false)
                                .setBtnSetting(btnSetting)
                                .setPopupWidth(50)
                                .setPopupType("drawer"));
                formDesignHtml.setFormType(FormTypeEnum.normalForm);
                formPo.setViewJson(JSONObject.toJSONString(formDesignHtml));
                formService.updateById(formPo);
            }

        }
        return R.ok();
    }

}
