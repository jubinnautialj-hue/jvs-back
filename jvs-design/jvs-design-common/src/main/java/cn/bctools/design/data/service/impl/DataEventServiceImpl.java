package cn.bctools.design.data.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.data.entity.DataEventPo;
import cn.bctools.design.data.fields.dto.DataEventSettingDto;
import cn.bctools.design.data.fields.enums.DataEventType;
import cn.bctools.design.data.mapper.DataEventMapper;
import cn.bctools.design.data.service.DataEventService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.error.MessageTipsDto;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据事件
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataEventServiceImpl extends ServiceImpl<DataEventMapper, DataEventPo> implements DataEventService, IJvsDesigner {
    RuleDesignService ruleService;
    RuleRunService ruleRunService;
    DataModelService modelService;
    MapperMethodHandler mapperMethodHandler;

    @Override
    public DataEventSettingDto getEventList(String appId, String modelId, String designId) {
        List<DataEventPo> eventPoList = this.list(Wrappers.<DataEventPo>lambdaQuery()
                .eq(DataEventPo::getModelId, modelId)
                .eq(DataEventPo::getAppId, appId)
                .eq(DataEventPo::getDesignId, designId));
        DataEventSettingDto setting = new DataEventSettingDto();
        setting.setEventList(eventPoList);
        return setting;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataEventSettingDto updateEventList(String appId, String modelId, String designId, DataEventSettingDto settingDto) {
        String s = appId + modelId + designId;
        synchronized (s) {
            List<DataEventPo> result = settingDto.getEventList().stream().peek(e -> e.setAppId(appId).setModelId(modelId).setDesignId(designId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(result)) {
                result.forEach(e -> {
                    //如果为空，则保存，如果不为空则更新
                    if (ObjectNull.isNull(e.getId())) {
                        save(e);
                    } else {
                        updateById(e);
                    }
                });

                //禁用的逻辑引擎
                settingDto.getEventList().stream()
                        .forEach(e -> {
                            if (e.getBeforeRuleEnable()) {
                                ruleService.update(new UpdateWrapper<RuleDesignPo>().lambda().set(RuleDesignPo::getEnable, true).eq(RuleDesignPo::getSecret, e.getBeforeRuleId()));
                            } else {
                                ruleService.update(new UpdateWrapper<RuleDesignPo>().lambda().set(RuleDesignPo::getEnable, false).eq(RuleDesignPo::getSecret, e.getBeforeRuleId()));
                            }
                            if (e.getAfterRuleEnable()) {
                                ruleService.update(new UpdateWrapper<RuleDesignPo>().lambda().set(RuleDesignPo::getEnable, true).eq(RuleDesignPo::getSecret, e.getAfterRuleId()));
                            } else {
                                ruleService.update(new UpdateWrapper<RuleDesignPo>().lambda().set(RuleDesignPo::getEnable, false).eq(RuleDesignPo::getSecret, e.getAfterRuleId()));
                            }
                        });
            }
        }
        return null;
    }


    @Override
    public RuleExecuteDto callback(String modelId, String dataId, DataEventType type, Map<String, Object> data, boolean isBefore) {
        String designId = DynamicDataUtils.getDesignId();
        if (StringUtils.isBlank(designId)) {
            return null;
        }
        String ruleId;
        if (type.equals(DataEventType.DATA_DELETE)) {
            ruleId = this.getCallbackRuleId(modelId, null, type, isBefore);
        } else {
            ruleId = this.getCallbackRuleId(modelId, designId, type, isBefore);
        }
        if (StringUtils.isBlank(ruleId)) {
            return null;
        }
        // 一个请求最多只对一条数据执行一次前置、后置逻辑引擎。避免循环调用
        if (checkRequestExists(ruleId, modelId, type, isBefore)) {
            return null;
        }
        // 同步回调, 需要获取回调的返回值
        //回调时的id应该是原始id不应该是版本id号
        data.put("id", dataId);
        //事件回调.如果是成功, 则返回成功状态,如果是失败返回 失败状态和message
        RuleExecuteDto dto = Optional.ofNullable(ruleRunService.run(ruleId, JSONUtil.parseObj(data))).orElseGet(RuleExecuteDto::new);
        if (ObjectNull.isNotNull(dto.getEndResult())) {
            Object run = dto.getEndResult().getValue();
            if (ObjectNull.isNotNull(run)) {
                if (run instanceof Map) {
                    data.putAll((Map<? extends String, ?>) run);
                }
            }
        }
        if (!dto.getStats()) {
            throw new BusinessException(dto.getSyncMessageTips());
        }
        try {
            //处理前后置,如果发生异常后， 可能在开始线后， 直接返回了，并没有执行或设计
            if (ObjectNull.isNotNull(dto.getEndResult())) {
                if (ObjectNull.isNotNull(dto.getEndResult().getValue())) {
                    if (dto.getEndResult().getValue() instanceof MessageTipsDto) {
                        if (!((MessageTipsDto) dto.getEndResult().getValue()).getOnOff()) {
                            throw new BusinessException(((MessageTipsDto) dto.getEndResult().getValue()).getMessage());
                        }
                    }
                }
            }
        } catch (BusinessException e) {
            //处理返回类型可能为其它的情况导致前端报错
        }
        return dto;
    }

    /**
     * 获取回调事件的逻辑id
     *
     * @param modelId   模型id
     * @param designId  设计id
     * @param eventType 事件类型
     * @return 逻辑id
     */
    public String getCallbackRuleId(String modelId, String designId, DataEventType eventType, boolean isBefore) {
        List<DataEventPo> list = this.list(Wrappers.<DataEventPo>lambdaQuery()
                .eq(DataEventPo::getModelId, modelId)
                .eq(ObjectNull.isNotNull(designId), DataEventPo::getDesignId, designId)
                .eq(DataEventPo::getEventType, eventType));
        if (ObjectNull.isNull(list)) {
            return null;
        }
        //临时处理逻辑引擎删除前后置 ，后续会直接将事件设计到按钮上面
        switch (eventType) {
            case DATA_DELETE:
                // 根据eventType判断是数据删除事件
                if (isBefore) {
                    // 如果是在事件之前，过滤出beforeRuleEnable为true的数据，并获取beforeRuleId
                    return list.stream().filter(DataEventPo::getBeforeRuleEnable).map(DataEventPo::getBeforeRuleId).findAny().orElse(null);
                } else {
                    // 如果是在事件之后，过滤出afterRuleEnable为true的数据，并获取afterRuleId
                    return list.stream().filter(DataEventPo::getAfterRuleEnable).map(DataEventPo::getAfterRuleId).findAny().orElse(null);
                }
            default:
        }

        //临时处理事件重复保存的问题
        // 如果isBefore为true，且eventPo的beforeRuleEnable为true
        DataEventPo eventPo = list.get(0);
        if (isBefore && eventPo.getBeforeRuleEnable()) {
            // 返回eventPo的beforeRuleId
            return eventPo.getBeforeRuleId();
        } else if ((!isBefore) && eventPo.getAfterRuleEnable()) {
            // 如果isBefore为false，且eventPo的afterRuleEnable为true
            // 返回eventPo的afterRuleId
            return eventPo.getAfterRuleId();
        }
        return null;
    }

    /**
     * 校验当前数据是否已执行逻辑
     *
     * @param ruleId
     * @param modelId
     * @param dataId
     * @param type
     * @param isBefore
     * @return
     */
    private boolean checkRequestExists(String ruleId, String modelId, DataEventType type, boolean isBefore) {
        try {
            if (WebUtils.getRequest().getRequestURI().contains("/dynamic/data")) {
                return false;
            }
        } catch (Exception ignored) {

        }
        String key = ruleId + modelId + type.name() + isBefore;
        Boolean v = SystemThreadLocal.get(key);
        if (ObjectNull.isNull(v)) {
            SystemThreadLocal.set(key, Boolean.TRUE);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<DataEventPo>lambdaQuery().eq(DataEventPo::getAppId, appId));
    }
}
