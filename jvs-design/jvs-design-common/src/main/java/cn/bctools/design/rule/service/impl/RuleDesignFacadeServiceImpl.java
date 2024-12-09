package cn.bctools.design.rule.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.component.XxlJobComponent;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.TaskCronDto;
import cn.bctools.design.rule.service.RuleDesignFacadeService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.dto.RuleFunctionDto;
import cn.bctools.rule.entity.enums.NodeType;
import cn.bctools.rule.utils.html.HtmlGraph;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class RuleDesignFacadeServiceImpl implements RuleDesignFacadeService {

    RuleDesignService ruleDesignService;
    XxlJobComponent xxlJobComponent;
    JvsAppService jvsAppService;
    IdentificationService identificationService;
    SwaggerRuleApiCacheService swaggerRuleApiCacheService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(RuleDesignPo ruleDesignPo, String appId) {
        JvsApp app = jvsAppService.getById(appId);
        RuleDesignPo dbpo = ruleDesignService.getOne(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setId(ruleDesignPo.getId())));
        if (ObjectNull.isNull(dbpo)) {
            throw new BusinessException("逻辑不存在");
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        HtmlGraph graph = JSONObject.parseObject(ruleDesignPo.getDesignDrawingJson(), HtmlGraph.class);
        //将节点和画布信息重写填充， 兼容老版本循环数据，和变量信息
        graph.getNodeList().forEach(e -> e.setCanvasId("main"));
        if (ObjectNull.isNotNull(graph.getErgodicCanvas())) {
            for (String canvasId : graph.getErgodicCanvas().keySet()) {
                if (ObjectNull.isNotNull(graph.getErgodicCanvas().get(canvasId).getNodeList())) {
                    graph.getErgodicCanvas().get(canvasId).getNodeList().stream().forEach(e -> e.setCanvasId(canvasId));
                }
            }
        }
        ruleDesignPo.setDesignDrawingJson(JSON.toJSONString(graph));

        List<String> icons = graph.getNodeList().stream()
                .map(e -> SystemInit.getFunctionsBase(e.getName()))
                .filter(Objects::nonNull)
                .map(RuleFunctionDto::getIcon)
                .filter(StringUtils::isNotBlank)
                .limit(3)
                .collect(Collectors.toList());
        List<String> ids = graph.getNodeList().stream()
                .filter(e -> e.getType().equals(NodeType.task))
                //只能是选择了OA流程  不管是不是同一个应用下
                .filter(e -> ObjectNull.isNotNull(e.getData(), e.getData().getFunctionName()) && "OA流程".equals(e.getData().getFunctionName()))
                //只能是选择了的结果值
                .filter(e -> ObjectNull.isNotNull(e.getData().getBody()))
                .map(e -> (String) e.getData().getBody().getOrDefault("workflow", ""))
                .filter(ObjectNull::isNotNull)
                .collect(Collectors.toList());
        //如果ID不为空，则直接修改
        ruleDesignPo.setUpdateTime(LocalDateTime.now());
        ruleDesignPo.setIcons(icons);
        //校验是否有重复使用
        if (ObjectNull.isNotNull(ids)) {
            ruleDesignPo.setFlowDesignIds(ids);
        } else {
            ruleDesignPo.setFlowDesignIds(new ArrayList<>(1));
        }
        if (ObjectNull.isNull(ruleDesignPo.getParameterPos())) {
            ruleDesignPo.setParameterPos(new HashMap<>(1));
        }
        TaskCronDto taskCronDto = Optional.ofNullable(ruleDesignPo.getTask()).orElseGet(dbpo::getTask);
        if (ObjectNull.isNotNull(taskCronDto) && ObjectNull.isNull(taskCronDto.getId())) {
            taskCronDto.setId(Optional.ofNullable(dbpo.getTask()).map(TaskCronDto::getId).orElseGet(() -> null));
        }
        xxlJobComponent.saveOrUpdateJob(taskCronDto, ruleDesignPo.getOnTask(), app.getName(), ruleDesignPo.getName(), ruleDesignPo.getSecret());
        ruleDesignService.updateById(ruleDesignPo);
        // 修改自定义标识冗余的设计名称
        if (Optional.of(ruleDesignPo).map(RuleDesignPo::getParameterIn).map(e -> ObjectNull.isNotNull(e.getUrl())).orElse(false)) {
            String url = ruleDesignPo.getParameterIn().getUrl();
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            // 发布逻辑API swagger缓存变更事件
            swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(false, appId, ruleDesignPo);
            Identification identification = new Identification().setJvsAppId(ruleDesignPo.getJvsAppId()).setDesignType(DesignType.rule).setIdentifier(url).setDesignName(ruleDesignPo.getName()).setDesignId(ruleDesignPo.getSecret());
            identificationService.saveOrUpdate(identification, Wrappers.<Identification>lambdaUpdate().eq(Identification::getDesignId, ruleDesignPo.getSecret()));
        }
    }
}
