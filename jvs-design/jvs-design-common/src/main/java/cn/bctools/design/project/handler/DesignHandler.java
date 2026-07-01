package cn.bctools.design.project.handler;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.config.DesignConfig;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.dto.DesignUpdateDto;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.impl.FlowDynamicDataServiceImpl;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.function.utils.ExpressionParam;
import cn.bctools.function.utils.ExpressionUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.web.utils.IpUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 设计器统一处理
 * <p>
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
public class DesignHandler {

    @Lazy
    @Autowired
    List<IJvsDesigner> designers;
    @Lazy
    @Autowired
    DataModelService modelService;
    @Lazy
    @Autowired
    FunctionBusinessMapper functionMapper;
    @Lazy
    @Autowired
    DesignConfig designConfig;
    @Lazy
    @Autowired
    ExpressionHandler expressionHandler;
    @Lazy
    @Autowired
    FlowTaskService flowTaskService;
    @Lazy
    @Autowired
    FlowDynamicDataService flowDynamicDataService;
    @Lazy
    @Autowired
    IdentificationService identificationService;
    @Lazy
    @Autowired
    PermissionCompatibleService permissionCompatibleService;


    /**
     * 按钮的操作权限匹配规则
     */
    public static final String JVS_ENABLED_BUTTONS = "jvsEnabledButtons";

    @Data
    @Accessors(chain = true)
    public static class LoadingCacheDto {
        /**
         * 模型Id
         */
        String dataModelId;
        /**
         * 设计Id
         */
        String pageDesignId;
    }

    private final LoadingCache<LoadingCacheDto, DesignRoleSettingDto> localDesignRoleSettingCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(new CacheLoader<LoadingCacheDto, DesignRoleSettingDto>() {
                @Override
                public DesignRoleSettingDto load(LoadingCacheDto data) throws Exception {
                    return getRoleSettingCache(data);
                }
            });


    /**
     * 添加设计的数据
     *
     * @param data
     * @param useCase
     */
    public void handleButtonInfo(Map<String, Object> data, String useCase) {
        this.handleButtonInfo(Collections.singletonList(data), useCase);
    }

    public void handleButtonInfo(List<Map<String, Object>> dataList, String useCase) {
        long handleStart = System.currentTimeMillis();
        log.info("[DesignHandler-handleButtonInfo] 开始处理按钮信息 - 数据条数: {}, useCase: {}", 
            dataList != null ? dataList.size() : 0, useCase);
        
        if (ObjectUtils.isEmpty(dataList)) {
            log.info("[DesignHandler-handleButtonInfo] 数据列表为空，直接返回");
            return;
        }

        // 1. 获取设计ID和用户信息
        long step1Start = System.currentTimeMillis();
        String designId = DynamicDataUtils.getDesignId();
        if (StringUtils.isBlank(designId)) {
            log.warn("[DesignHandler-handleButtonInfo] designId为空，直接返回");
            return;
        }

        String userId = UserCurrentUtils.getUserId();
        boolean mobile = IpUtil.isMobile();
        log.info("[DesignHandler-handleButtonInfo] 获取基础信息完成，耗时: {}ms - designId: {}, userId: {}, mobile: {}", 
            System.currentTimeMillis() - step1Start, designId, userId, mobile);

        // 2. 获取并过滤按钮设置
        long step2Start = System.currentTimeMillis();
        List<ButtonDesignHtml> buttonSetting = this.getButtonSetting(designId, useCase)
                .stream()
                .filter(b -> StringUtils.isNotBlank(b.getPermissionFlag()))
                .map(b -> (ButtonDesignHtml) b)
                //如果是移动端，返回 移动端启用的，否则返回 pc端启用的
                .filter(b -> {
                    if (mobile) {
                        if (ObjectNull.isNotNull(b.getMobileEnable())) {
                            return b.getMobileEnable();
                        } else {
                            return true;
                        }
                    } else {
                        return b.getEnable();
                    }
                })
                .collect(Collectors.toList());
        log.info("[DesignHandler-handleButtonInfo] 获取并过滤按钮设置完成，耗时: {}ms - 按钮数量: {}", 
            System.currentTimeMillis() - step2Start, buttonSetting != null ? buttonSetting.size() : 0);
        
        //如果没有按钮直接返回
        if (ObjectNull.isNull(buttonSetting)) {
            log.info("[DesignHandler-handleButtonInfo] 按钮设置为空，直接返回");
            return;
        }

        // 3. 批量预加载formula配置，避免N+1查询
        long step3PreloadStart = System.currentTimeMillis();
        Map<String, FunctionBusinessPo> formulaCache = new HashMap<>();
        Set<String> formulaIds = new HashSet<>();
        
        // 收集所有需要的formulaId
        buttonSetting.forEach(button -> {
            if (ObjectNull.isNotNull(button.getFormula())) {
                formulaIds.add(button.getFormula());
            }
            if (ObjectNull.isNotNull(button.getMobileFormula())) {
                formulaIds.add(button.getMobileFormula());
            }
        });
        
        // 批量查询formula配置
        if (!formulaIds.isEmpty()) {
            List<FunctionBusinessPo> formulas = functionMapper.selectBatchIds(formulaIds);
            if (ObjectNull.isNotNull(formulas)) {
                formulas.forEach(f -> formulaCache.put(f.getId(), f));
            }
        }
        
        log.info("[DesignHandler-handleButtonInfo] 批量预加载formula完成，耗时: {}ms - formula数量: {}", 
            System.currentTimeMillis() - step3PreloadStart, formulaCache.size());
        
        // 将formula缓存放入ThreadLocal，供checkFormula方法使用
        SystemThreadLocal.set("FORMULA_CACHE", formulaCache);
        
        try {
            // 4. 获取流程任务信息
            long step4Start = System.currentTimeMillis();
            List<String> dataIds = dataList.stream().map(dataMap -> String.valueOf(dataMap.get("id"))).collect(Collectors.toList());
            Map<String, JSONObject> dataFlowMap = flowDynamicDataService.getMltipleFlowTaskData(dataIds);
            log.info("[DesignHandler-handleButtonInfo] 获取流程任务信息完成，耗时: {}ms - 数据ID数量: {}, 流程数据数量: {}", 
                System.currentTimeMillis() - step4Start, dataIds.size(), dataFlowMap != null ? dataFlowMap.size() : 0);

            // 5. 处理每条数据的按钮
            long step5Start = System.currentTimeMillis();
        int processedCount = 0;
        long maxItemDuration = 0;
        String slowestItemId = null;
        
        for (Map<String, Object> data : dataList) {
            long itemStart = System.currentTimeMillis();
            
            // 将工作流的信息加入数据
            JSONObject flowData = dataFlowMap.get(String.valueOf(data.get("id")));
            if (ObjectNull.isNotNull(flowData)) {
                data.putAll(flowData);
            }
            
            List list = new ArrayList<>(buttonSetting)
                    .stream()
                    .filter(e -> {
                        //如果是表单按钮场景
                        if (useCase.equals(EnvConstant.FORM_BUTTON_DISPLAY) && e.getEnable()) {
                            return true;
                        }
                        // 只返已启用的回行内按钮
                        if (ObjectNull.isNotNull(e.getType()) && e.getEnable()) {
                            return "line".equals(e.getPosition());
                        }
                        return false;
                    })
                    .filter(e -> {
                        if (mobile) {
                            //判断公式
                            if (ObjectNull.isNotNull(e.getMobileFormula())) {
                                return checkFormula(e.getMobileFormula(), data, useCase);
                            } else if (ObjectNull.isNotNull(e.getType()) && e.getType().name().startsWith("oa_workflow")) {
                                //判断是否有工作流按钮
                                return checkJvsFlowTask(e.getType(), data, userId);
                            } else if (ObjectNull.isNull(e.getMobileFormula())) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            //判断公式
                            if (ObjectNull.isNotNull(e.getFormula())) {
                                return checkFormula(e.getFormula(), data, useCase);
                            } else if (ObjectNull.isNotNull(e.getType()) && e.getType().name().startsWith("oa_workflow")) {
                                //判断是否有工作流按钮
                                return checkJvsFlowTask(e.getType(), data, userId);
                            } else if (ObjectNull.isNull(e.getFormula())) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    })
                    .map(ButtonSettingDto::getPermissionFlag)
                    .collect(Collectors.toList());
            //添加按钮
            if (ObjectNull.isNull(list)) {
                //用于占位不然会显示所有按钮
                list.add(IdGenerator.getIdStr());
            }
            data.put(JVS_ENABLED_BUTTONS, list);
            
            long itemDuration = System.currentTimeMillis() - itemStart;
            if (itemDuration > maxItemDuration) {
                maxItemDuration = itemDuration;
                slowestItemId = String.valueOf(data.get("id"));
            }
            
            // 单条数据处理超过50ms记录警告
            if (itemDuration > 50) {
                log.warn("[DesignHandler-handleButtonInfo] 单条数据处理耗时过长: {}ms - 数据ID: {}, 按钮数量: {}", 
                    itemDuration, data.get("id"), list.size());
            }
            
            processedCount++;
        }
        
            log.info("[DesignHandler-handleButtonInfo] 处理所有数据的按钮完成，耗时: {}ms - 处理条数: {}, 最慢记录ID: {}, 最慢记录耗时: {}ms", 
                System.currentTimeMillis() - step5Start, processedCount, slowestItemId, maxItemDuration);

            long totalDuration = System.currentTimeMillis() - handleStart;
            log.info("[DesignHandler-handleButtonInfo] 处理完成，总耗时: {}ms", totalDuration);
            
            if (totalDuration > 1000) {
                log.warn("[DesignHandler-handleButtonInfo] 处理超时预警！总耗时: {}ms - 数据条数: {}", totalDuration, dataList.size());
            }
        } finally {
            // 清理ThreadLocal，避免内存泄漏
            SystemThreadLocal.remove("FORMULA_CACHE");
        }
    }


    /**
     * 校验规则
     *
     * @param e       按钮
     * @param data
     * @param useCase
     * @return
     */
    private boolean checkFormula(String formulaId, Map<String, Object> data, String useCase) {
        // 优先从threadlocal缓存中获取
        @SuppressWarnings("unchecked")
        Map<String, FunctionBusinessPo> formulaCache = 
            (Map<String, FunctionBusinessPo>) SystemThreadLocal.get("FORMULA_CACHE");
        
        FunctionBusinessPo function = null;
        if (ObjectNull.isNotNull(formulaCache)) {
            function = formulaCache.get(formulaId);
        }
        
        // 如果缓存未命中，则查询数据库（兜底逻辑）
        if (ObjectNull.isNull(function)) {
            function = functionMapper.selectById(formulaId);
        }
        
        if (ObjectNull.isNull(function)) {
            return true;
        }
        String expression = function.getBody();
        List<ExpressionParam> params = ExpressionUtils.parsePostfixExpression(expression);
        if (ObjectUtils.isEmpty(params)) {
            return true;
        }
        try {
            Object result = expressionHandler.calculate(params, data, useCase);
            if (result instanceof Boolean) {
                if (Boolean.parseBoolean(result.toString())) {
                    return true;
                } else {
                    if (designConfig.getPageButtonFormulaChinese()) {
                        HashMap<String, Object> body = new HashMap<>(data);
                        //将转换的key全部再放回到新的对象中进行匹配是
                        data.keySet().stream().filter(s -> s.endsWith(DynamicDataUtils.SUFFIX_ECHO)).forEach(s -> body.put(s.substring(0, s.length() - DynamicDataUtils.SUFFIX_ECHO.length()), data.get(s)));
                        result = expressionHandler.calculate(params, body, useCase);
                        return Boolean.parseBoolean(result.toString());
                    }
                }
            }
        } catch (Exception exception) {
            log.error("按钮公式配置错误", exception);
        }
        return false;
    }

    /**
     * 按钮的操作，默认都显示，执行执行为 false是才不显示
     *
     * @param formulaId
     * @param useCase
     * @return
     */
    public boolean checkFormula(String formulaId, String useCase) {
        HashMap<String, Object> data = new HashMap<>();
        FunctionBusinessPo function = functionMapper.selectById(formulaId);
        if (ObjectNull.isNull(function)) {
            return true;
        }
        String expression = function.getBody();
        List<ExpressionParam> params = ExpressionUtils.parsePostfixExpression(expression);
        if (ObjectUtils.isEmpty(params)) {
            return true;
        }
        try {
            Object result = expressionHandler.calculate(params, data, useCase);
            if (result instanceof Boolean) {
                if (Boolean.parseBoolean(result.toString())) {
                    return true;
                } else {
                    if (designConfig.getPageButtonFormulaChinese()) {
                        HashMap<String, Object> body = new HashMap<>(data);
                        //将转换的key全部再放回到新的对象中进行匹配是
                        data.keySet().stream().filter(s -> s.endsWith(DynamicDataUtils.SUFFIX_ECHO)).forEach(s -> body.put(s.substring(0, s.length() - DynamicDataUtils.SUFFIX_ECHO.length()), data.get(s)));
                        result = expressionHandler.calculate(params, body, useCase);
                        return Boolean.parseBoolean(result.toString());
                    } else {
                        return Boolean.parseBoolean(result.toString());
                    }
                }
            }
        } catch (Exception exception) {
            //如果报错了，默认也给显示
            return true;
        }
        return true;
    }

    public Object runFormula(FunctionBusinessPo expression, Map<String, Object> data, String useCase) {
        try {
            List<ExpressionParam> params = ExpressionUtils.parsePostfixExpression(expression.getBody());
            if (ObjectUtils.isEmpty(params)) {
                return "公式配置错误";
            }
            Object result = expressionHandler.calculate(params, data, useCase);
            return result;
        } catch (Exception exception) {
            log.error("按钮公式配置错误", exception);
            return "数据转换异常";
        }
    }

    /**
     * 获取数据中所有工作流任务
     *
     * @param data 数据
     * @return 工作流任务
     */
    public List<FlowDynamicDataServiceImpl.FlowTaskModelData> getDataFlowTasks(Map<String, Object> data) {
        // 获取历史工作流任务
        Object taskHistoryObj = data.get(FlowDataFieldEnum.TASK_HISTORY.getFieldKey());
        List<FlowDynamicDataServiceImpl.FlowTaskModelData> taskHistoryList = Optional.ofNullable(taskHistoryObj)
                .map(obj -> BeanCopyUtil.copys((List) obj, FlowDynamicDataServiceImpl.FlowTaskModelData.class))
                .orElseGet(ArrayList::new);
        // 获取最终执行的工作流任务
        FlowDynamicDataServiceImpl.FlowTaskModelData lastFlowTask = Optional.ofNullable(data.get(FlowDataFieldEnum.TASK.getFieldKey()))
                .map(taskObj -> BeanCopyUtil.copy(taskObj, FlowDynamicDataServiceImpl.FlowTaskModelData.class))
                .orElseGet(FlowDynamicDataServiceImpl.FlowTaskModelData::new);
        // 因为任务结束，就会保存到历史记录，重启后还会保存到FlowDataFieldEnum.TASK，所以移除同一个任务已保存的历史信息，再重新加入
        taskHistoryList.removeIf(task -> task.getId().equals(lastFlowTask.getId()));
        taskHistoryList.add(lastFlowTask);
        return taskHistoryList;
    }

    private boolean checkJvsFlowTask(ButtonTypeEnum buttonType, Map<String, Object> data, String userId) {
        Object jvsFlowTaskObj = data.get(FlowDataFieldEnum.TASK.getFieldKey());
        if (ObjectNull.isNull(jvsFlowTaskObj)) {
            return false;
        }

        // oa_workflow_progress按钮的权限
        if (ButtonTypeEnum.oa_workflow_progress.equals(buttonType)) {
            // 遍历所有工作流任务信息，只要有一个条件满足，都可以看进度
//            List<FlowDynamicDataServiceImpl.FlowTaskModelData> jvsFlowTasks = getDataFlowTasks(data);
//            for (FlowDynamicDataServiceImpl.FlowTaskModelData jvsFlowTask : jvsFlowTasks) {
//                if (userId.equals(jvsFlowTask.getCreateById())) {
//                    return true;
//                }
//                // 是审批人，可看进度
//                if (ObjectNull.isNotNull(jvsFlowTask.getFlowTaskPersons()) && jvsFlowTask.getFlowTaskPersons().contains(userId)) {
//                    return true;
//                }
//                // 是抄送人，可看进度
//                if (ObjectNull.isNotNull(jvsFlowTask.getCarbonCopyPersons()) && jvsFlowTask.getCarbonCopyPersons().contains(userId)) {
//                    return true;
//                }
//                // 历史审批人，可看进度
//                if (ObjectNull.isNotNull(jvsFlowTask.getHistoryPersons()) && jvsFlowTask.getHistoryPersons().contains(userId)) {
//                    return true;
//                }
//            }
            return true;
        }

        FlowDynamicDataServiceImpl.FlowTaskModelData jvsFlowTask = BeanCopyUtil.copy(jvsFlowTaskObj, FlowDynamicDataServiceImpl.FlowTaskModelData.class);
        String createUserId = jvsFlowTask.getCreateById();
        // 审核人才有这个oa_workflow按钮的权限
        if (ButtonTypeEnum.oa_workflow.equals(buttonType)) {
            if (ObjectNull.isNotNull(jvsFlowTask.getFlowTaskPersons())) {
                if (jvsFlowTask.getFlowTaskPersons().contains(userId)) {
                    return true;
                }
            }
        }

        // 任务发起人才能显示的按钮
        if (userId.equals(createUserId)) {
            String flowTaskId = jvsFlowTask.getId();
            if (ButtonTypeEnum.oa_workflow_cancel.equals(buttonType)) {
                return flowTaskService.checkCancel(userId, flowTaskId);
            }
            // 发起人表单不为空的校验是否可显示重启按钮
            if (ButtonTypeEnum.oa_workflow_restart.equals(buttonType) && ObjectNull.isNotNull(jvsFlowTask.getSendFormId())) {
                return flowTaskService.checkCanRestart(userId, flowTaskId);
            }
        }
        return false;
    }


    private DesignRoleSettingDto getRoleSettingCache(LoadingCacheDto data) {
        return permissionCompatibleService.getRoleSetting(data.dataModelId, data.getPageDesignId());
    }

    /**
     * 获取数据权限配置
     *
     * @param dataModelId 模型id
     * @return 设计的权限设置
     */
    @SneakyThrows
    public DesignRoleSettingDto getRoleSetting(String dataModelId) {
        String pageDesignId = DynamicDataUtils.getPageDesignId();
        LoadingCacheDto loadingCacheDto = new LoadingCacheDto().setDataModelId(dataModelId).setPageDesignId(pageDesignId);
        return localDesignRoleSettingCache.get(loadingCacheDto);
    }

    /**
     * 根据设计id获取权限配置
     *
     * @param designId 设计id
     * @return 权限配置
     */
    public DesignRoleSettingDto getDesignRole(String designId) {
        if (StringUtils.isBlank(designId)) {
            return null;
        }
        IJvsDesigner formDesigner = this.getDesigner(DesignType.form);
        IJvsDesigner pageDesigner = this.getDesigner(DesignType.page);
        DesignRoleSettingDto designRole = formDesigner.getDesignRole(designId);
        DynamicDataUtils.setDto(designRole);
        if (Objects.isNull(designRole)) {
            designRole = pageDesigner.getDesignRole(designId);
            if (ObjectNull.isNull(designRole) || ObjectNull.isNull(designRole.getModeId())) {
                throw new BusinessException("找不到数据_source");
            }
        }
        return designRole;
    }

    /**
     * 根据设计id获取按钮配置
     *
     * @param designId 设计id
     * @param useCase
     * @return 权限配置
     */
    public List<? extends ButtonSettingDto> getButtonSetting(String designId, String useCase) {
        IJvsDesigner formDesigner = this.getDesigner(DesignType.form);
        IJvsDesigner pageDesigner = this.getDesigner(DesignType.page);
        List<? extends ButtonSettingDto> buttonSettings = formDesigner.getButtonSettings(designId, useCase);
        if (CollectionUtils.isEmpty(buttonSettings)) {
            buttonSettings = pageDesigner.getButtonSettings(designId, useCase);
        }
        return buttonSettings;
    }

    /**
     * 删除应用所有数据
     *
     * @param versionApps 版本应用
     */
    @Async
    public void appDataDeleted(List<JvsAppVersion> versionApps) {
        if (ObjectNull.isNull(versionApps)) {
            return;
        }
        versionApps.forEach(app -> {
            log.info("开始删除应用[{}]所有数据", app.getJvsAppId());
            ModeUtils.setSwitchModel(new SwitchModeDto().setMode(app.getVersionType()));
            for (IJvsDesigner handler : designers) {
                handler.beforeAppDeleted(app.getJvsAppId());
            }
            log.info("删除应用[{}]所有数据完成", app.getJvsAppId());
        });
    }

    /**
     * 删除设计
     *
     * @param appId      应用id
     * @param designId   设计id
     * @param designType 设计类型
     */
    public void removeDesign(String appId, String designId, DesignType designType) {
        this.getDesigner(designType).delete(appId, designId);
    }

    public void display(String appId, String designId, DesignType designType, Boolean mobileDisplay, Boolean pcDisplay) {
        getDesigner(designType).display(appId, designId, mobileDisplay, pcDisplay);
    }

    /**
     * 修改指定应用设计基本信息
     * <p>
     * 设计名称, 应用目录
     *
     * @param updateDto 应用设计修改信息
     */
    public void updateDesign(@NotNull DesignUpdateDto updateDto) {
        String name = updateDto.getName();
        String designId = updateDto.getDesignId();
        IJvsDesigner designer = this.getDesigner(updateDto.getDesignType());
        designer.updateName(updateDto.getAppId(), designId, name);
        // 修改自定义标识冗余的设计名称
        identificationService.updateDesignName(designId, name);
        // 修改数据模型名称
        String dataModelId = designer.getDataModelId(updateDto.getAppId(), designId);
        modelService.updateName(dataModelId, name);
    }

    /**
     * 获取指定类型的设计器
     *
     * @param type 设计类型
     * @return 设计器
     */
    private IJvsDesigner getDesigner(DesignType type) {
        if (Objects.isNull(type)) {
            throw new BusinessException("设计类型不能为空");
        }
        List<IJvsDesigner> handlerList = this.designers.stream().filter(e -> {
            Design annotation = AnnotationUtils.findAnnotation(e.getClass(), Design.class);
            return Objects.nonNull(annotation) && type.equals(annotation.value());
        }).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(handlerList)) {
            throw new BusinessException("套件不支持模板功能", type);
        }
        int size = handlerList.size();
        if (size != 1) {
            log.warn("Found {} JvsDesigner of '{}', but expect 1.", size, type);
        }
        return handlerList.get(0);
    }

}
