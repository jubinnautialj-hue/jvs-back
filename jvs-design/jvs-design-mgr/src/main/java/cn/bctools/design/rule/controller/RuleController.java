package cn.bctools.design.rule.controller;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.api.dto.RuleDto;
import cn.bctools.design.rule.component.XxlJobComponent;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RuleType;
import cn.bctools.design.rule.entity.TaskCronDto;
import cn.bctools.design.rule.expression.RuleItemParam;
import cn.bctools.design.rule.service.RuleDescribeService;
import cn.bctools.design.rule.service.RuleDesignFacadeService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.feign.config.SwaggerProperties;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.handler.IJvsFunction;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author guojing
 */
@Api(tags = "逻辑列表页")
@Slf4j
@RestController
@RequestMapping("/app/design/{appId}/rule")
@AllArgsConstructor
public class RuleController {

    RuleDesignService designService;
    RuleDesignFacadeService ruleDesignFacadeService;
    RuleDescribeService describeService;
    FlowDesignService flowDesignService;
    XxlJobComponent xxlJobComponent;
    RunLogService runLogService;
    JvsAppService jvsAppService;
    RuleItemParam ruleItemParam;
    IdentificationService identificationService;
    SwaggerProperties swaggerProperties;
    SwaggerRuleApiCacheService swaggerRuleApiCacheService;
    FunctionBusinessService functionBusinessService;
    RedisUtils redisUtils;

    private static final String RULE_DESIGN_EDIT_LOCK = "rule:design:edit:lock";
    private static final Integer LOCK_EXPIRE_TIME = 30;

    /**
     * 根据应用获取所有逻辑
     *
     * @param appId 应用id
     * @return 逻辑列表
     */
    @Log(back = false)
    @ApiOperation("获取所有逻辑")
    @GetMapping("/all")
    public R<List<RuleDesignPo>> page(@PathVariable String appId, @RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "identification", required = false, defaultValue = "false") Boolean identification) {
        List<RuleDesignPo> ruleList = designService.list(Wrappers.<RuleDesignPo>lambdaQuery()
                .select(
                        RuleDesignPo::getId,
                        RuleDesignPo::getName,
                        RuleDesignPo::getDescription,
                        RuleDesignPo::getSecret,
                        RuleDesignPo::getIcons,
                        RuleDesignPo::getComponentType,
                        RuleDesignPo::getComponentId,
                        RuleDesignPo::getCreateTime,
                        RuleDesignPo::getUpdateTime,
                        RuleDesignPo::getEnable
                )
                .like(ObjectNull.isNotNull(name), RuleDesignPo::getName, name)
                //如果有标识，则只有这两种
                .in(identification, RuleDesignPo::getReqType, RuleType.External_API_logic, RuleType.Source_code_development_docking_logic)
                .eq(RuleDesignPo::getJvsAppId, appId)
//                .eq(RuleDesignPo::getEnable, true)
                .orderByDesc(RuleDesignPo::getCreateTime));
        return R.ok(ruleList);
    }

    /**
     * 逻辑分页查询
     * <p>
     * 模糊查询平台和简介
     *
     * @param page         分页信息
     * @param ruleDesignPo 查询条件
     * @return 逻辑列表
     */
    @Log(back = false)
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public R<Page<RuleDesignPo>> page(Page<RuleDesignPo> page, RuleDesignPo ruleDesignPo, @PathVariable String appId) {
        String name = ruleDesignPo.getName();
        String content = ruleDesignPo.getDescription();
        page = designService.page(page, Wrappers.<RuleDesignPo>lambdaQuery()
                .select(
                        RuleDesignPo::getId,
                        RuleDesignPo::getName,
                        RuleDesignPo::getDescription,
                        RuleDesignPo::getSecret,
                        RuleDesignPo::getIcons,
                        RuleDesignPo::getCreateTime,
                        RuleDesignPo::getUpdateTime,
                        RuleDesignPo::getEnable
                )
                //通过应用创建的，不显示在列表页中
                .eq(RuleDesignPo::getJvsAppId, appId)
                .eq(ObjectNull.isNotNull(ruleDesignPo.getReqType()), RuleDesignPo::getReqType, ruleDesignPo.getReqType())
                .isNotNull(RuleDesignPo::getName)
                .isNull(RuleDesignPo::getComponentId)
                .like(ObjectUtil.isNotEmpty(name), RuleDesignPo::getName, name)
                .like(ObjectUtil.isNotEmpty(content), RuleDesignPo::getDescription, content)
                .orderByDesc(RuleDesignPo::getCreateTime));
        return R.ok(page);
    }

    /**
     * 逻辑分页查询
     * <p>
     * 模糊查询平台和简介
     *
     * @param page         分页信息
     * @param ruleDesignPo 查询条件
     * @return 逻辑列表
     */
    @Log(back = false)
    @ApiOperation("分页查询")
    @GetMapping("/page/list")
    public R<Page<RuleDesignPo>> list(Page<RuleDesignPo> page, RuleDesignPo ruleDesignPo, @PathVariable String appId) {
        String name = ruleDesignPo.getName();
        boolean notNull = ObjectUtil.isNotNull(ruleDesignPo.getReqType());
        List<RuleType> list = new ArrayList<>();
        if (notNull) {
            list.add(RuleType.Source_code_development_docking_logic);
            if (RuleType.Source_code_development_docking_logic.equals(ruleDesignPo.getReqType())) {
                list.add(RuleType.Low_code_logic);
            }
        }
        page = designService.page(page, Wrappers.<RuleDesignPo>lambdaQuery()
                .select(
                        RuleDesignPo::getId,
                        RuleDesignPo::getName,
                        RuleDesignPo::getDescription,
                        RuleDesignPo::getSecret,
                        RuleDesignPo::getIcons,
                        RuleDesignPo::getComponentType,
                        RuleDesignPo::getComponentId,
                        RuleDesignPo::getReqType,
                        RuleDesignPo::getCreateTime,
                        RuleDesignPo::getUpdateTime,
                        RuleDesignPo::getEnable
                )
                .eq(RuleDesignPo::getJvsAppId, appId)
                .in(notNull, RuleDesignPo::getReqType, list)
                .like(ObjectUtil.isNotNull(name), RuleDesignPo::getName, name)
                .orderByDesc(RuleDesignPo::getCreateTime));
        return R.ok(page);
    }

    /**
     * 新建一个逻辑设计, 后面如果需要调用，直接使用业务名进行调用
     * <p>
     * 默认启用
     *
     * @param name 名称
     * @return 保存后的逻辑对象, 包含逻辑的远程调用key, 为了方便使用，以后直接使用Key进行业务数据调用
     */
    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("新建逻辑设计")
    @PutMapping
    public R<String> save(@RequestParam(value = "name", required = false, defaultValue = "未命名逻辑") String name,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam(value = "componentType", required = false) DesignType componentType,
                          @RequestParam(value = "reqType", required = false) RuleType reqType,
                          @RequestParam(value = "designId", required = false) String designId,
                          @RequestParam(value = "componentId", required = false) String componentId,
                          @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        //数据转换
        String realName = UserCurrentUtils.getRealName();
        RuleDesignPo ruleDesign = new RuleDesignPo()
                .setName(name)
                .setSecret(JvsAppSecretUtils.getAppSecret(IdGenerator.getIdStr()))
                .setCreateBy(realName)
                .setEnable(true)
                .setReqType(reqType)
                .setDescription(description)
                .setJvsAppId(appId)
                .setComponentDesignId(designId)
                .setComponentId(componentId)
                .setComponentType(componentType)
                .setDesignDrawingJson(JSONObject.toJSONString(new HtmlGraph()))
                .setUpdateBy(realName);

        ruleDesign.setParameterPos(new HashMap<>(1));
        designService.save(ruleDesign);
        return R.ok(ruleDesign.getSecret());
    }

    @Log
    @ApiOperation("修改逻辑基本信息")
    @PutMapping("/{id}")
    public R<RuleDesignPo> update(@RequestBody RuleDesignPo ruleDesignPo, @PathVariable("id") String id, @PathVariable String appId) {
        // 更新一个逻辑
        RuleDesignPo byId = designService.getById(id);
        //校验是否一致
        if (byId.getJvsAppId().equals(appId)) {
            ruleDesignPo.setId(id);
            designService.updateById(ruleDesignPo);
        }
        return R.ok(ruleDesignPo);
    }

    /**
     * 获取所有的模板信息
     *
     * @return
     * @throws Exception
     */
    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("根据凭证删除逻辑")
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/secret/{secret}")
    public R<Boolean> secret(@PathVariable("secret") String secret, @PathVariable String appId) {
        RuleDesignPo ruleDesignPo = designService.getOne(Wrappers.query(new RuleDesignPo().setSecret(secret).setJvsAppId(appId)));
        if (ObjectNull.isNull(ruleDesignPo)) {
            return R.ok();
        }
        designService.removeById(ruleDesignPo.getId());
        // 发布逻辑API swagger缓存变更事件
        swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(true, appId, ruleDesignPo);
        return R.ok();
    }

    /**
     * 将源设计的数据拷贝一份到新设计里面去
     *
     * @param secret     当前设计
     * @param formSecret 源设计
     * @return
     */
    @Log
    @ApiOperation("复制整个逻辑")
    @PutMapping("/copy/{secret}/{formSecret}")
    @Transactional(rollbackFor = Exception.class)
    public R copy(@PathVariable("secret") String secret, @PathVariable("formSecret") String formSecret) {
        RuleDesignPo designPo = designService.getEnableDesign(secret);
        RuleDesignPo formDesign = designService.getEnableDesign(formSecret);
        //将当前设计的公式 id 全部拿出来。
        //将所有 id与这个设计 json做匹配关系
        functionBusinessService.remove(new LambdaQueryWrapper<FunctionBusinessPo>().eq(FunctionBusinessPo::getDesignId, designPo.getId()));
        //如果 id相同 则表示存在，并将原有 id的内容进行复制。形成新的公式对象。将新的 id 复制为老的 id值。
        Map<String, FunctionBusinessPo> map = functionBusinessService.list(Wrappers.query(new FunctionBusinessPo().setDesignId(formDesign.getId())))
                .stream().collect(Collectors.toMap(FunctionBusinessPo::getId, Function.identity()));
        List<FunctionBusinessPo> list = new ArrayList<>();
        String jsonString = formDesign.getDesignDrawingJson();
        if (ObjectNull.isNotNull(map)) {
            for (String oldId : map.keySet()) {
                if (jsonString.contains(oldId)) {
                    FunctionBusinessPo functionBusinessPo = map.get(oldId).setId(IdWorker.getIdStr());
                    functionBusinessPo.setCreateTime(LocalDateTime.now());
                    functionBusinessPo.setUpdateTime(LocalDateTime.now());
                    list.add(functionBusinessPo);
                    //产生新的对象数据
                    jsonString = jsonString.replaceAll(oldId, functionBusinessPo.getId());
                }
            }
        }
        designPo.setDesignDrawingJson(jsonString);
        designPo.setParameterIn(formDesign.getParameterIn());
        designPo.setParameterOut(formDesign.getParameterOut());
        designPo.setTask(formDesign.getTask());
        functionBusinessService.saveBatch(list);
        designService.updateById(designPo);
        return R.ok(designPo);
    }

    /**
     * 获取所有的模板信息
     *
     * @return
     * @throws Exception
     */
    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("删除逻辑")
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        //判断是否为启用状态，如果有启用状态，则不能删除,
        RuleDesignPo byId = designService.getById(id);
        describeService.deleteRuleDescribe(id, appId);
        //TODO 类型校验,判断是否有引用关系未被删除 .如工作流,h5 ,或模型还存在 .后续将在应用概览图进行体现
        if (byId.getJvsAppId().equals(appId)) {
            designService.removeById(id);
            // 发布逻辑API swagger缓存变更事件
            swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(true, appId, byId);
        }
        return R.ok();
    }

    /**
     * 根据逻辑标识获取逻辑设计数据
     *
     * @param key 逻辑标识
     * @return 逻辑设计数据
     */
    @Log(back = false)
    @ApiOperation("根据key获取一个逻辑的设计信息")
    @GetMapping("/{key}")
    public R<RuleDesignPo> getDesign(@PathVariable("key") String key, @PathVariable String appId) {
        // 查询所有已经设计好的数据
        RuleDesignPo designPo = designService.getOne(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setSecret(key)));
        if (ObjectNull.isNull(designPo)) {
            return R.failed("没有找到集成设计");
        }
        //判断类型，根据类型获取应用标识信息,并同时返回
        Identification one = identificationService.getOne(new LambdaQueryWrapper<Identification>().eq(Identification::getDesignId, designPo.getId()).eq(Identification::getJvsAppId, appId));
        if (ObjectNull.isNotNull(one)) {
            designPo.setAppIdentifier(one.getIdentifier());
        } else {
            Identification entity = new Identification().setJvsAppId(appId).setDesignId(designPo.getId()).setDesignType(DesignType.app).setIdentifier(IdGenerator.getIdStr(36));
            identificationService.save(entity);
            designPo.setAppIdentifier(entity.getIdentifier());
        }
        JvsApp app = jvsAppService.getById(appId);
        //根据类型判断是否是 api类型，并拼凑 api的 url的地址
        if (RuleType.External_API_logic.equals(designPo.getReqType()) && ObjectNull.isNotNull(designPo.getParameterIn()) && ObjectNull.isNotNull(designPo.getParameterIn().getUrl())) {
            designPo.setApi("/doc.html#" + URLEncodeUtil.encode(String.format("/[%s]" + swaggerProperties.getTitle() + "%s/%s[%s]/%s",
                    SpringContextUtil.getVersion(),
                    Optional.ofNullable(ModeUtils.getMode()).orElse(AppVersionTypeEnum.GA).getMsg(),
                    app.getName(),
                    appId,
                    designPo.getId()
            )));
        }
        return R.ok(designPo);
    }

    @ApiOperation("根据key获取一个逻辑的设计信息")
    @GetMapping("/bind/{id}")
    public R bind(@PathVariable("id") String id, @PathVariable String appId, @RequestParam(value = "extendJson", required = false) String extendJson) {
        // 查询所有已经设计好的数据
        SystemThreadLocal.set(RuleItemParam.KEY_DESIGN_ID, id);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_EXTEND_JSON, extendJson);
        //获取入参对象集合
        List<ElementVo> collect = ruleItemParam.getAllElements()
                .stream()
//                .filter(e -> e.getType().equals("请求参数"))
                .peek(e -> e.setName(e.getType() + "-" + e.getName()))
                .collect(Collectors.toList());
        //将 path 清除掉
        collect.forEach(this::clean);
        return R.ok(collect);
    }

    private void clean(ElementVo vo) {
        if (ObjectNull.isNotNull(vo.getChildren())) {
            vo.getChildren().forEach(this::clean);
        } else {
            vo.setPath(null);
        }
    }


    /**
     * 新增、修改逻辑设计
     *
     * @param ruleDesignPo 逻辑设计数据
     * @return 修改之后的设计数据
     */
    @Log
    @ApiOperation("修改逻辑设计数据")
    @PostMapping("/design")
    public R<RuleDesignPo> update(@RequestBody RuleDesignPo ruleDesignPo, @PathVariable String appId) {
        String lockKey = null;
        Integer cronId = Optional.ofNullable(ruleDesignPo.getTask()).map(TaskCronDto::getId).orElseGet(() -> null);
        if (RuleType.Timing_logic.equals(ruleDesignPo.getReqType()) && ObjectNull.isNull(cronId)) {
            lockKey = SysConstant.redisKey(RULE_DESIGN_EDIT_LOCK, ruleDesignPo.getSecret());
        }
        try {
            if (ObjectNull.isNotNull(lockKey)) {
                boolean lock = redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME);
                if (Boolean.FALSE.equals(lock)) {
                    throw new BusinessException("正在启动定时任务");
                }
            }
            ruleDesignFacadeService.update(ruleDesignPo, appId);
        } catch (Exception e) {
            log.error("异常：", e);
            throw new BusinessException(e.getMessage());
        } finally {
            if (ObjectNull.isNotNull(lockKey)) {
                redisUtils.unLock(lockKey);
            }
        }
        return R.ok(ruleDesignPo);
    }

    /**
     * 美化图,
     * 美化top|false
     *
     * @param graph
     * @param id
     * @param left
     * @param top
     */
    private void findNextTopLeft(HtmlGraph graph, List<String> id, AtomicInteger left, AtomicInteger top) {
        for (String s : id) {
            graph.getNodeList().forEach(e -> {
                if (s.equals(e.getId())) {
                    e.setTop(top.addAndGet(70) + "px");
                    e.setLeft(left.addAndGet(70) + "px");
                }
            });
        }
        //找下级节点
        List<String> collect = graph.getLineList()
                .stream()
                .filter(e -> id.contains(e.getFrom()))
                .map(e -> e.getTo())
                .collect(Collectors.toList());
        if (ObjectNull.isNotNull(collect)) {
            findNextTopLeft(graph, collect, left, top);
        }
    }


    /**
     * 获取所有逻辑设计的名称与标识
     *
     * @return 逻辑集合
     * @author xh
     */
    @Log(back = false)
    @ApiOperation("获取所有逻辑名称与key")
    @GetMapping("/get/select")
    public R<List<RuleDto>> getSelect(@PathVariable String jvsApp) {
        // 查询所有已经启用的逻辑
        List<RuleDesignPo> ruleDesignPoList = designService.list(Wrappers.<RuleDesignPo>lambdaQuery()
                .select(RuleDesignPo::getSecret, RuleDesignPo::getName)
                .eq(RuleDesignPo::getJvsAppId, jvsApp)
                .eq(RuleDesignPo::getEnable, true));
        List<RuleDto> ruleDtoList = ruleDesignPoList.stream().map(e -> new RuleDto(e.getSecret(), e.getName())).collect(Collectors.toList());
        return R.ok(ruleDtoList);
    }

    @ApiOperation("获取变量")
    @GetMapping("/expression/{id}")
    public R expression(@PathVariable("id") String id, @PathVariable String appId) {
        // 查询所有已经设计好的数据
        SystemThreadLocal.set(RuleItemParam.KEY_DESIGN_ID, id);
        List<ElementVo> collect = ruleItemParam.getAllElements()
                .stream()
                .filter(e -> !"节点".equals(e.getType()))
                .peek(e -> e.setName(e.getType() + "-" + e.getName()))
                .collect(Collectors.toList());
        return R.ok(collect);
    }
}
