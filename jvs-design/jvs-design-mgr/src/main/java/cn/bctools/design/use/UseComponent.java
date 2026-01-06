package cn.bctools.design.use;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.design.config.MultipleMongoConfig;
import cn.bctools.design.constant.AppConstant;
import cn.bctools.design.crud.entity.AppUrlPo;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.JvsTree;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.menu.util.JvsMenuVo;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.use.api.AppApi;
import cn.bctools.design.use.api.TreeApi;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.ModeDto;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
@Api(tags = "获取菜单功能")
@RestController
@AllArgsConstructor
public class UseComponent implements AppApi, TreeApi {

    AuthRoleServiceApi authRoleServiceApi;
    FormService formService;
    JvsAppService jvsAppService;
    JvsSystemConfig jvsSystemConfig;
    CrudPageService crudPageService;
    DataModelService dataModelService;
    AppUrlService appUrlService;
    EnvironmentVariableApi environmentVariableApi;
    RedisUtils redisUtils;
    JvsTreeService treeService;
    AppMenuTypeService appMenuTypeService;
    AppMenuService appMenuService;
    JvsAppVersionService appVersionService;
    OAuth2AuthorizationService authorizationService;
    DesignPermissionService designPermissionService;
    MultipleMongoConfig multipleMongoConfig;
    MongoProperties mongoProperties;
    IdentificationService identificationService;

    @Override
    public R<List<ModeDto>> mode() {
        MongoProperties beta = multipleMongoConfig.getBeta();
        List<ModeDto> objects = new ArrayList<>();
        // bi 不能配置开发模式的应用
        objects.add(new ModeDto().setMode(AppVersionTypeEnum.BETA.getMsg()).setDatasource(JSONObject.toJSONString(beta)));
        objects.add(new ModeDto().setMode(AppVersionTypeEnum.GA.getMsg()).setDatasource(JSONObject.toJSONString(mongoProperties)));
        return R.ok(objects);
    }

    @Override
    public R<List<DataModelDto>> apps(String mode) {
        List<String> versionTypeAppIds = appVersionService.getVersionTypeAppIds(AppVersionTypeEnum.getMsgType(mode));
//        String userId = UserCurrentUtils.getUserId();
        JSONObject conditionUser = new JSONObject();
        conditionUser.put("type", "user");
//        conditionUser.put("id", userId);
//        String conditionUserJson = JSON.toJSONString(conditionUser);
        LambdaQueryWrapper<JvsApp> in = new LambdaQueryWrapper<JvsApp>().select(JvsApp::getId, JvsApp::getName).in(ObjectNull.isNotNull(versionTypeAppIds), JvsApp::getId, versionTypeAppIds);
//        in.and(wrapper -> wrapper.eq(JvsApp::getCreateById, userId)
//                // 查询当前用户作为应用管理员的应用
//                .or(orUser -> orUser.apply(SqlFunctionUtil.jsonContainsObject(Get.name(JvsApp::getRole), "$.adminMember", conditionUserJson)))
//                // 查询当前用户作为应用开发人员的应用
//                .or(orUser -> orUser.apply(SqlFunctionUtil.jsonContainsObject(Get.name(JvsApp::getRole), "$.devMember", conditionUserJson)))
//        );
        List<DataModelDto> collect = jvsAppService.list(in)
                //这里需要处理添加应用的标示
                .stream().map(e -> {
                    Identification one = identificationService.getOne(Wrappers.query(new Identification().setDesignId(e.getId())));
                    if (ObjectNull.isNotNull(one)) {
                        return new DataModelDto().setAppCode(one.getIdentifier()).setAppName(e.getName() + "(" + one.getIdentifier() + ")");
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        return R.ok(collect);
    }

    @Override
    public R<DataModelDto> apps(String mode, String appCode) {
        List<DataModelDto> data = apps(mode).getData();
        return R.ok(data.stream().filter(e -> e.getAppCode().equals(appCode)).findFirst().orElse(null));
    }

    @Override
    public R<Long> dataSize() {
        return R.ok(dataModelService.getModeSize());
    }

    @Override
    public R<String> tree(String uniqueName) {
        //根据唯一标识确定
        JvsTree sysTree = treeService.getOne(Wrappers.query(new JvsTree().setUniqueName(uniqueName)));
        return R.ok(sysTree.getName());
    }

    @Override
    public R<List<String>> getByIds(List<String> uniqueNames) {
        //根据唯一标识确定
        List<JvsTree> list = treeService.list(Wrappers.lambdaQuery(JvsTree.class).in(JvsTree::getUniqueName, uniqueNames));
        return R.ok(list.stream().map(JvsTree::getName).collect(Collectors.toList()));
    }

    /**
     * {@link SysConstant#CACHE_LOGIN}
     * <p>
     * 获取用户的菜单信息
     * 主要分为有设计权限管理权限
     * 没有设计权限,但其它用户给自己授权了的应用和菜单
     * <p>
     * 移动端：
     * 不能切换模式，只显示已发布的应用
     * 正式模式：
     * - 所有已发布的应用，都标记为没有设计权限
     * 测试模式：
     * - 显示有设计权限的应用，都标记为没有设计权限
     * 开发模式：
     * - 显示有设计权限的应用，标记为有设计权限
     * 模拟用户：
     * -没有设计权限
     *
     * @param appId  指定应用id   如果指定了id的,只查询某一个轻应用下面的
     * @param userId 当前用户的id   根据用户id查询是否具有相关的权限
     * @param mobile 是否是移动端,如果是Pc端,根据状态显示移动端的或pc端的
     * @param app
     * @return
     */
    public Pair<List<Tree<Object>>, List<JvsMenuVo>> menu(String appId, String userId, boolean mobile, AppVersionTypeEnum mode, JvsApp app) {
        log.info("[UseComponent.menu] 开始构建菜单，appId={}, userId={}, mobile={}, mode={}, app={}", 
                appId, userId, mobile, mode, app != null ? app.getId() + "-" + app.getName() : "null");
        
        List<JvsMenuVo> list = new ArrayList<>();
        Map<String, JvsApp> allAppMap = new HashMap<>(2);
        // 有设计权限的应用
        Map<String, JvsApp> withDesignPermissionAppMap = new HashMap<>();
        // 无设计权限的应用
        Map<String, JvsApp> noDesignPermissionAppMap = new HashMap<>();
        
        if (ObjectNull.isNotNull(app)) {
            log.info("[UseComponent.menu] 指定了app参数，appId={}, appName={}", app.getId(), app.getName());
            allAppMap.put(app.getId(), app);
            noDesignPermissionAppMap.put(app.getId(), app);
        } else {
            log.info("[UseComponent.menu] app参数为null，开始查询应用列表");
            // 查询应用
            List<JvsApp> jvsApps = getJvsApps(appId, mode);
            log.info("[UseComponent.menu] 查询到的应用数量={}", jvsApps != null ? jvsApps.size() : 0);
            
            if (jvsApps != null && !jvsApps.isEmpty()) {
                log.info("[UseComponent.menu] 应用列表：{}", jvsApps.stream()
                        .map(a -> a.getId() + "-" + a.getName())
                        .collect(Collectors.joining(", ")));
            }
            
            // 有设计权限的应用
            withDesignPermissionAppMap = withDesignPermissionApp(userId, jvsApps);
            log.info("[UseComponent.menu] 有设计权限的应用数量={}", withDesignPermissionAppMap.size());
            if (!withDesignPermissionAppMap.isEmpty()) {
                log.info("[UseComponent.menu] 有设计权限的应用：{}", withDesignPermissionAppMap.values().stream()
                        .map(a -> a.getId() + "-" + a.getName())
                        .collect(Collectors.joining(", ")));
            }
            
            // 无设计权限的应用
            noDesignPermissionAppMap = noDesignPermissionApp(jvsApps, withDesignPermissionAppMap, mode);
            log.info("[UseComponent.menu] 无设计权限的应用数量={}, mode={}", noDesignPermissionAppMap.size(), mode);
            if (!noDesignPermissionAppMap.isEmpty()) {
                log.info("[UseComponent.menu] 无设计权限的应用：{}", noDesignPermissionAppMap.values().stream()
                        .map(a -> a.getId() + "-" + a.getName() + "(发布:" + a.getIsDeploy() + ",推荐:" + a.getRecommend() + ")")
                        .collect(Collectors.joining(", ")));
            }
            
            // 所有应用
            allAppMap.putAll(withDesignPermissionAppMap);
            allAppMap.putAll(noDesignPermissionAppMap);
        }
        
        log.info("[UseComponent.menu] 所有应用总数={}", allAppMap.size());
        // Map<应用id，版本号>
        Map<String, String> appVersionMap = allAppMap.values()
                .stream()
                .collect(Collectors.toMap(JvsApp::getId, jvsApp -> Optional.ofNullable(jvsApp.getUseVersion()).orElse("")));
        log.info("[UseComponent.menu] 应用版本信息：{}", appVersionMap.entrySet().stream()
                .map(e -> e.getKey() + ":版本" + (e.getValue().isEmpty() ? "未启用" : e.getValue()))
                .collect(Collectors.joining(", ")));

        //构建应用树
        // 有模拟用户，不论是否有应用权限，都设置为没有设计权限
        boolean whetherAnalogUser = ModeUtils.whetherAnalogUser();
        log.info("[UseComponent.menu] 是否模拟用户={}", whetherAnalogUser);
        
        appTree(withDesignPermissionAppMap.values(), list, Boolean.FALSE.equals(whetherAnalogUser));
        appTree(noDesignPermissionAppMap.values(), list, Boolean.FALSE);

        if (MapUtils.isEmpty(appVersionMap)) {
            log.warn("[UseComponent.menu] appVersionMap为空，直接返回");
            return Pair.of(JvsMenuVo.tree(list), list);
        }

        //构建目录树
        appDirectoryTree(appVersionMap, list, withDesignPermissionAppMap);
        log.info("[UseComponent.menu] 构建目录树完成，当前列表数量={}", list.size());

        //根据类型,判断是否开启快速创建,并支持哪些快速创建的类型
        Set<String> enableWorkflowMap = enableWorkflowMap(appVersionMap.keySet());

        //构建菜单树
        List<AppMenu> appMenus = getMenus(appVersionMap);
        log.info("[UseComponent.menu] 查询到的原始菜单数量={}", appMenus.size());
        
        if (!appMenus.isEmpty()) {
            // 按应用统计菜单
            Map<String, Long> menuCountByApp = appMenus.stream()
                    .collect(Collectors.groupingBy(AppMenu::getJvsAppId, Collectors.counting()));
            log.info("[UseComponent.menu] 每个应用的菜单数量：{}", menuCountByApp.entrySet().stream()
                    .map(e -> {
                        JvsApp jvsApp = allAppMap.get(e.getKey());
                        return (jvsApp != null ? jvsApp.getName() : e.getKey()) + ":" + e.getValue() + "个";
                    })
                    .collect(Collectors.joining(", ")));
            
            // 查找是否有“智慧技术监管”菜单
            List<AppMenu> targetMenus = appMenus.stream()
                    .filter(m -> m.getName() != null && m.getName().contains("智慧技术监管"))
                    .collect(Collectors.toList());
            if (!targetMenus.isEmpty()) {
                log.info("[UseComponent.menu] 找到包含'智慧技术监管'的菜单数量={}", targetMenus.size());
                targetMenus.forEach(m -> {
                    JvsApp jvsApp = allAppMap.get(m.getJvsAppId());
                    log.info("[UseComponent.menu] '智慧技术监管'菜单信息：菜单ID={}, 菜单名={}, 所属应用={}, 设计ID={}, 类型={}, 移动端显示={}, PC端显示={}, 菜单版本={}",
                            m.getId(), m.getName(), jvsApp != null ? jvsApp.getName() : m.getJvsAppId(), 
                            m.getDesignId(), m.getDesignType(), m.getMobileDisplay(), m.getPcDisplay(), m.getAppVersion());
                });
            } else {
                log.warn("[UseComponent.menu] 未找到包含'智慧技术监管'的菜单");
            }
        }
        List<String> designIds = appMenus.stream().map(AppMenu::getDesignId).collect(Collectors.toList());
        Map<String, List<DesignRole>> operationPermissionMap = designPermissionService.getBatchOperationPermission(designIds);
        SecurityContext context = SecurityContextHolder.getContext();
        Map<String, Object> threadMap = SystemThreadLocal.get();
        Map<String, JvsApp> finalWithDesignPermissionAppMap = withDesignPermissionAppMap;
        SwitchModeDto switchMode = ModeUtils.getSwitchMode();
        
        log.info("[UseComponent.menu] 开始过滤菜单，过滤前数量={}, mobile={}, mode={}", appMenus.size(), mobile, mode);
        
        List<JvsMenuVo> menus = appMenus
                .parallelStream()
                //判断是否是移动端,还是Pc显示
                .filter(e -> {
                    // 移动端根据配置显示隐藏
                    if (mobile) {
                        boolean mobileDisplay = e.getMobileDisplay();
                        if (!mobileDisplay && e.getName() != null && e.getName().contains("智慧技术监管")) {
                            log.warn("[UseComponent.menu] 移动端过滤：'智慧技术监管'菜单被过滤，移动端显示={}", mobileDisplay);
                        }
                        return mobileDisplay;
                    }
                    // PC端
                    SystemThreadLocal.setAll(threadMap);
                    SecurityContextHolder.setContext(context);
                    // 有设计权限，不判断是否隐藏
                    JvsApp jvsApp = allAppMap.get(e.getJvsAppId());
                    if (RoleUtils.checkAppDesignPermission(UserCurrentUtils.getUserId(), jvsApp)) {
                        return Boolean.TRUE;
                    }
                    // 无设计权限，根据配置显示隐藏
                    boolean pcDisplay = e.getPcDisplay();
                    if (!pcDisplay && e.getName() != null && e.getName().contains("智慧技术监管")) {
                        log.warn("[UseComponent.menu] PC端显示过滤：'智慧技术监管'菜单被过滤，PC端显示={}, 应用={}", pcDisplay, jvsApp != null ? jvsApp.getName() : e.getJvsAppId());
                    }
                    return pcDisplay;
                })
                //根据这些资源判断此用户是否包含权限,如果包含,则返回这些资源
                .filter(e -> {
                    ModeUtils.setSwitchModel(switchMode);
                    // 非模拟用户： 开发模式直接放行，其它模式都要校验资源权限
                    // 模拟用户： 所有模式都要校验资源权限
                    if (AppVersionTypeEnum.DEV.equals(mode) && Boolean.FALSE.equals(ModeUtils.whetherAnalogUser())) {
                        // 开发模式不判断权限
                        return Boolean.TRUE;
                    } else {
                        // 兼容新旧版本的权限配置
                        SystemThreadLocal.setAll(threadMap);
                        SecurityContextHolder.setContext(context);
                        JvsApp jvsApp = allAppMap.get(e.getJvsAppId());
                        List<DesignRole> roles = jvsApp.getEnableVersionFeature() ? operationPermissionMap.get(e.getDesignId()) : e.getRoles();
                        boolean hasPermit = RoleUtils.hasPermit(roles);
                        
                        if (!hasPermit && e.getName() != null && e.getName().contains("智慧技术监管")) {
                            log.warn("[UseComponent.menu] 权限过滤：'智慧技术监管'菜单被过滤，应用={}, 启用版本功能={}, 有权限={}, roles={}",
                                    jvsApp != null ? jvsApp.getName() : e.getJvsAppId(), 
                                    jvsApp != null && jvsApp.getEnableVersionFeature(), 
                                    hasPermit,
                                    roles != null ? roles.size() : "null");
                        }
                        
                        return hasPermit;
                    }
                })
                //转换为对象信息
                .map(e -> {
                    SystemThreadLocal.setAll(threadMap);
                    return transition(e, enableWorkflowMap, checkDesignRole(e.getJvsAppId(), finalWithDesignPermissionAppMap));
                })
                .peek(e -> {
                    if (ObjectNull.isNotNull(e.getUrl())) {
                        if (DesignType.URL.equals(e.getDesign())) {
                            //判断如果不是 http开头或不是/开头，则可能是环境变量，需要进行替换
                            if (!(e.getUrl().startsWith("http") || e.getUrl().startsWith("/"))) {
                                try {
                                    EnvironmentVariableDto data = environmentVariableApi.getByKey(e.getUrl(), ModeTypeEnum.getType(ModeUtils.getMode().getValue())).getData();
                                    if (ObjectNull.isNotNull(data)) {
                                        Map extend = (Map) e.getExtend();
                                        extend.put("variable", data.getValue());
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                })
                .collect(Collectors.toList());
        //资源转树
        log.info("[UseComponent.menu] 菜单过滤完成，过滤后数量={}", menus.size());
        
        // 检查过滤后是否还有“智慧技术监管”
        List<JvsMenuVo> targetMenusAfterFilter = menus.stream()
                .filter(m -> m.getName() != null && m.getName().contains("智慧技术监管"))
                .collect(Collectors.toList());
        if (!targetMenusAfterFilter.isEmpty()) {
            log.info("[UseComponent.menu] 过滤后仍然包含'智慧技术监管'的菜单数量={}", targetMenusAfterFilter.size());
            targetMenusAfterFilter.forEach(m -> {
                log.info("[UseComponent.menu] 过滤后'智慧技术监管'菜单：ID={}, 名称={}, 应用ID={}", 
                        m.getId(), m.getName(), m.getJvsAppId());
            });
        } else {
            log.warn("[UseComponent.menu] 过滤后'智慧技术监管'菜单已被完全过滤");
        }
        
        list.addAll(menus);
        
        log.info("[UseComponent.menu] 最终列表数量={}, 开始构建树结构", list.size());
        List<Tree<Object>> tree = JvsMenuVo.tree(list);
        log.info("[UseComponent.menu] 菜单构建完成，树节点数量={}", tree != null ? tree.size() : 0);

        return Pair.of(tree, list);
    }

    private List<AppMenu> getMenus(Map<String, String> appVersionMap) {
        Set<String> appIds = appVersionMap.keySet();
        List<AppMenu> menus = appMenuService.list(new LambdaQueryWrapper<AppMenu>()
                .select(AppMenu::getRole,
                        AppMenu::getRoleType,
                        AppMenu::getJvsAppId,
                        AppMenu::getId,
                        AppMenu::getDesignType,
                        AppMenu::getDesignId,
                        AppMenu::getName,
                        AppMenu::getType,
                        AppMenu::getSort,
                        AppMenu::getMobileDisplay,
                        AppMenu::getPcDisplay,
                        AppMenu::getDataModelId,
                        AppMenu::getAppVersion)
                .in(AppMenu::getJvsAppId, appIds)
                //菜单为空
                .isNotNull(AppMenu::getType));
        return menus.stream()
                .filter(menu -> filterMenu(appVersionMap, menu.getJvsAppId(), menu.getAppVersion()))
                .collect(Collectors.toList());
    }

    private JvsMenuVo transition(AppMenu e, Set<String> enableWorkflowMap, boolean type) {
        JvsMenuVo vo = (JvsMenuVo) new JvsMenuVo()
                .setDataModelId(e.getDataModelId())
                .setJvsAppId(e.getJvsAppId())
                .setPcDisplay(e.getPcDisplay())
                .setMobileDisplay(e.getMobileDisplay())
                .setSort(e.getSort())
                .setId(e.getDesignId())
                .setParentId(e.getType())
                .setName(e.getName());
        if (ObjectNull.isNotNull(e.getDesignType())) {
            vo.setType(e.getDesignType().name());
            vo.setDesign(e.getDesignType());
            switch (e.getDesignType()) {
                case form:
                    //如果包含则设置不同的快速创建
                    if (enableWorkflowMap.contains(e.getDataModelId())) {
                        vo.setDesignRole(true).setDesign(e.getDesignType()).setDesignTypess(DesignType.page);
                    } else {
                        vo.setDesignRole(true).setDesign(e.getDesignType()).setDesignTypess(DesignType.page, DesignType.workflow);
                    }
                    break;
                case page:
                    if (enableWorkflowMap.contains(e.getDataModelId())) {
                        vo.setDesignRole(true).setDesign(e.getDesignType()).setDesignTypess(DesignType.form, DesignType.page);
                    } else {
                        vo.setDesignRole(true).setDesign(e.getDesignType()).setDesignTypess(DesignType.form, DesignType.page, DesignType.workflow);
                    }
                    break;
                case URL:
                    AppUrlPo appUrlPo = appUrlService.getById(e.getDesignId());
                    if (ObjectNull.isNotNull(appUrlPo)) {
                        vo.setUrl(appUrlPo.getUrl());
                    }
                    break;
                default:
                    break;
            }
        }
        Map<String, Object> map = BeanToMapUtils.beanToMap(vo);
        map.put(AppConstant.DesignRole, type);
        vo.setExtend(map);
        return vo;
    }

    private Set<String> enableWorkflowMap(Set<String> keySet) {
        if (ObjectNull.isNull(keySet)) {
            return new HashSet<>();
        }
        return dataModelService.list(new LambdaQueryWrapper<DataModelPo>().select(DataModelPo::getId)
                        .in(ObjectNull.isNotNull(keySet), DataModelPo::getAppId, keySet)
                        .eq(DataModelPo::getEnableWorkflow, true))
                .stream().map(DataModelPo::getId).collect(Collectors.toSet());

    }

    /**
     * 校验是否有设计权限
     * <p>
     * 有模拟用户不能设计； 无模拟用户：有权限的可以设计
     *
     * @param appId                      应用id
     * @param withDesignPermissionAppMap 有设计权限的所有应用
     * @return true-有设计权限，false-无设计权限
     */
    private boolean checkDesignRole(String appId, Map<String, JvsApp> withDesignPermissionAppMap) {
        return ModeUtils.whetherAnalogUser() ? Boolean.FALSE : ObjectNull.isNotNull(withDesignPermissionAppMap.get(appId));
    }


    private void appDirectoryTree(Map<String, String> appVersionMap, List<JvsMenuVo> list, Map<String, JvsApp> withDesignPermissionAppMap) {
        //查询有权限的目录 和资源
        Set<String> appIds = appVersionMap.keySet();
        appMenuTypeService.list(new LambdaQueryWrapper<AppMenuType>().in(ObjectNull.isNotNull(appIds), AppMenuType::getJvsAppId, appIds))
                .stream()
                .filter(appMenuType -> filterMenu(appVersionMap, appMenuType.getJvsAppId(), appMenuType.getAppVersion()))
                .forEach(e -> addMenuType(e, list, checkDesignRole(e.getJvsAppId(), withDesignPermissionAppMap)));
    }

    /**
     * 筛选可返回的菜单/目录
     *
     * @param appVersionMap             应用版本号map
     * @param appId                     应用id
     * @param menuAffiliationAppVersion 菜单/目录所属应用版本号
     * @return true-可返回的菜单和目录，false-不能返回的菜单/目录
     */
    private boolean filterMenu(Map<String, String> appVersionMap, String appId, String menuAffiliationAppVersion) {
        // 开发模式，不需要校验版本号
        if (AppVersionTypeEnum.DEV.equals(ModeUtils.getMode())) {
            return Boolean.TRUE;
        }
        // 当前模式下目标应用没有版本号直接返回true（没有版本号，表示应用未启用版本功能，所以不需要校验版本号）
        String modeAppVersion = appVersionMap.get(appId);
        if (ObjectNull.isNull(modeAppVersion)) {
            return Boolean.TRUE;
        }
        return modeAppVersion.equals(menuAffiliationAppVersion);
    }

    private void addMenuType(AppMenuType e, List<JvsMenuVo> list, boolean designRole) {
        // 上级目录id不存在，则设置上级目录id为应用id
        String parentId = ObjectNull.isNull(e.getParentId()) ? e.getJvsAppId() : e.getParentId();
        TreePo directory = new JvsMenuVo().setIcon(e.getIcon()).setJvsAppId(e.getJvsAppId()).setType("directory").setId(e.getId()).setSort(e.getSort()).setParentId(parentId).setName(e.getType());
        Map<String, Object> map = BeanToMapUtils.beanToMap(directory);
        map.put("designRole", designRole);
        list.add((JvsMenuVo) directory.setExtend(map));
    }

    public void appTree(Collection<JvsApp> values, List<JvsMenuVo> list, Boolean designRole) {
        values.forEach(e -> {
            TreePo jvsapp = new JvsMenuVo().setIcon(e.getLogo()).setJvsAppId(e.getId()).setType("jvsapp").setId(e.getId()).setParentId("-1").setName(e.getName()).setSort(e.getSort());
            Map<String, Object> map = BeanToMapUtils.beanToMap(jvsapp);
            map.put("designRole", designRole);
            map.put("recommend", e.getRecommend());
            map.put("logo", e.getLogo());
            map.put("isDeploy", e.getIsDeploy());
            list.add((JvsMenuVo) jvsapp.setExtend(map));
        });
    }

    /**
     * 获取应用集合
     *
     * @param appId 应用id
     * @param mode  模式
     * @return 应用集合
     */
    private List<JvsApp> getJvsApps(String appId, AppVersionTypeEnum mode) {
        // 查询指定应用
        if (ObjectNull.isNotNull(appId)) {
            return Collections.singletonList(jvsAppService.getById(appId));
        }
        //查询所有的推荐应用
        if (mode.equals(AppVersionTypeEnum.DEV) && jvsSystemConfig.getDesignDefaultMode().equals(mode.getValue())) {
            List<JvsApp> appList = new ArrayList<>();
            //处理开发模式的访问速度
            List<JvsApp> list = jvsAppService.list(Wrappers.query(new JvsApp().setRecommend(true)));
            if (ObjectNull.isNotNull(list)) {
                appList.addAll(list);
            }
            //查询自己创建的应用
            JvsApp app = new JvsApp();
            app.setCreateById(UserCurrentUtils.getUserId());
            List<JvsApp> useApp = jvsAppService.list(Wrappers.query(app));
            if (ObjectNull.isNotNull(useApp)) {
                appList.addAll(useApp);
            }

            Set<String> appIds = appList.stream().map(JvsApp::getId).collect(Collectors.toSet());
            if (ObjectNull.isNotNull(appIds)) {
                // 查询指定模式下的应用id集合
                Set<String> ids = appVersionService.list(Wrappers.<JvsAppVersion>lambdaQuery()
                                .select(JvsAppVersion::getJvsAppId, JvsAppVersion::getAppVersion)
                                .eq(JvsAppVersion::getVersionType, AppVersionTypeEnum.DEV)
                                .in(JvsAppVersion::getJvsAppId, appIds)
                                .ne(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY))
                        .stream().map(JvsAppVersion::getJvsAppId).collect(Collectors.toSet());
                //删除不存在的那些应用
                appList.removeIf(e -> !ids.contains(e.getId()));
            }
            return appList.stream().peek(e -> e.setSort(-1)).distinct().collect(Collectors.toList());
        }
        // 查询模式下所有应用
        List<JvsAppVersion> appVersions = appVersionService.getVersionTypeApps(mode);
        if (ObjectNull.isNull(appVersions)) {
            return Collections.emptyList();
        }
        // Map<应用id，版本号>
        Map<String, String> appUseVersionMap = appVersions.stream()
                .collect(Collectors.toMap(JvsAppVersion::getJvsAppId, appVersion -> Optional.ofNullable(appVersion.getAppVersion()).orElse("")));
        Set<String> appIds = appUseVersionMap.keySet();
        return jvsAppService.list(Wrappers.<JvsApp>lambdaQuery().in(JvsApp::getId, appIds));
    }


    /**
     * 筛选有设计权限的应用
     *
     * @param userId  用户id
     * @param jvsApps 应用集合
     * @return Map<应用id ， 应用>
     */
    private Map<String, JvsApp> withDesignPermissionApp(String userId, List<JvsApp> jvsApps) {
        if (ObjectNull.isNull(jvsApps)) {
            return Collections.emptyMap();
        }
        // 筛选有权限的应用
        return jvsApps.stream()
                .filter(app -> RoleUtils.checkAppDesignPermission(userId, app))
                .collect(Collectors.toMap(JvsApp::getId, Function.identity()));
    }


    /**
     * 筛选无设计权限可返回的应用
     *
     * @param jvsApps              应用id集合
     * @param withPermissionAppMap 有权限的应用
     * @param mode                 模式
     * @return 无权限的应用
     */
    private Map<String, JvsApp> noDesignPermissionApp(List<JvsApp> jvsApps, Map<String, JvsApp> withPermissionAppMap, AppVersionTypeEnum mode) {
        // 非正式模式不能返回无权限的应用
        if (Boolean.FALSE.equals(AppVersionTypeEnum.GA.equals(mode))) {
            //需要返回推荐应用
            return jvsApps.stream()
                    .filter(app -> app.getRecommend() && Boolean.FALSE.equals(withPermissionAppMap.containsKey(app.getId())))
                    //将推荐放在前面
                    .peek(e -> e.setSort(-1))
                    .collect(Collectors.toMap(JvsApp::getId, Function.identity()));
        }
        // 正式模式可以返回无权限，但已发布的应用
        return jvsApps.stream()
                .filter(app -> app.getIsDeploy() && Boolean.FALSE.equals(withPermissionAppMap.containsKey(app.getId())))
                .collect(Collectors.toMap(JvsApp::getId, Function.identity()));

    }

}
