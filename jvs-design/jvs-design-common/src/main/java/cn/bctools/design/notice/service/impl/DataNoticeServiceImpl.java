package cn.bctools.design.notice.service.impl;

import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysNoticeConfig;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.dto.DataNoticeDto;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.notice.entity.enums.NoticeTemplateTypeEnum;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.mapper.DataNoticeMapper;
import cn.bctools.design.notice.service.DataNoticeService;
import cn.bctools.design.notice.util.NoticeExtendUtils;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.http.HtmlUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataNoticeServiceImpl extends ServiceImpl<DataNoticeMapper, DataNoticePo> implements DataNoticeService, IJvsDesigner {

    private final RedisUtils redisUtils;
    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void saveDataNotice(String appId, DataNoticeDto dto) {
        createOrUpdate(appId, dto, NoticeTemplateTypeEnum.MODEL);
    }

    public void createOrUpdate(String appId, DataNoticeDto dto, NoticeTemplateTypeEnum templateType) {
        // 校验配置
        NoticeExtendUtils.checkParam(dto.getExtend());
        // 保存
        DataNoticePo po = BeanCopyUtil.copy(dto, DataNoticePo.class);
        po.setExtend(dto.getExtend());
        po.setVariable(NoticeExtendUtils.getTemplateVariable(dto.getExtend()));
        po.setTemplateType(templateType);
        po.setTitleHtml(null);
        po.setContentHtml(null);
        po.setJvsAppId(appId);
        super.saveOrUpdate(po);
    }


    @Override
    public List<QueryDataNoticeRespDto> getAllByModelId(String appId, String modelId) {
        List<DataNoticePo> dataNoticePos = list(Wrappers.<DataNoticePo>lambdaQuery()
                .eq(DataNoticePo::getModelId, modelId)
                .eq(DataNoticePo::getJvsAppId, appId)
                .orderByAsc(DataNoticePo::getCreateTime));
        if (CollectionUtils.isEmpty(dataNoticePos)) {
            return Collections.emptyList();
        }
        return getTemplate(dataNoticePos);
    }

    @Override
    public List<QueryDataNoticeRespDto> getEffective(String appId, String modelId, FlowTask flowTask) {
        // 获取数据模型绑定的消息通知配置
        LambdaQueryWrapper<DataNoticePo> queryWrapper = Wrappers.<DataNoticePo>lambdaQuery();
        if (ObjectNull.isNull(flowTask)) {
            queryWrapper.eq(DataNoticePo::getModelId, modelId)
                    .eq(DataNoticePo::getJvsAppId, appId)
                    .eq(DataNoticePo::getEnabled, Boolean.TRUE);
        } else {
            queryWrapper.eq(DataNoticePo::getModelId, flowTask.getDesignModelId())
                    .eq(DataNoticePo::getJvsAppId, appId)
                    .eq(DataNoticePo::getEnabled, Boolean.TRUE);
        }
        queryWrapper.orderByAsc(DataNoticePo::getCreateTime);
        List<DataNoticePo> dataNoticePos = list(queryWrapper);

        // 获取租户级消息通知配置
        getAllTenantSystemConfig(dataNoticePos);
        if (CollectionUtils.isEmpty(dataNoticePos)) {
            return Collections.emptyList();
        }
        return getTemplate(dataNoticePos);
    }

    /**
     * 获取租户级消息通知配置，并转换为DataNoticePo
     *
     * @param dataNoticePos
     */
    private void getAllTenantSystemConfig(List<DataNoticePo> dataNoticePos) {
        SysNoticeConfig flowRemindConfig = getTenantSystemConfig(TriggerTypeEnum.FLOW_REMIND);
        SysNoticeConfig flowUrgeConfig = getTenantSystemConfig(TriggerTypeEnum.FLOW_URGE);
        if (Boolean.TRUE.equals(flowRemindConfig.getEnabled())) {
            dataNoticePos.add(JSONObject.parseObject(JSONObject.toJSONString(flowRemindConfig), DataNoticePo.class));
        }
        if (Boolean.TRUE.equals(flowUrgeConfig.getEnabled())) {
            dataNoticePos.add(JSONObject.parseObject(JSONObject.toJSONString(flowUrgeConfig), DataNoticePo.class));
        }
    }


    private List<QueryDataNoticeRespDto> getTemplate(List<DataNoticePo> dataNoticePos) {
        return dataNoticePos.stream()
                .map(d -> {
                    QueryDataNoticeRespDto dto = BeanCopyUtil.copy(d, QueryDataNoticeRespDto.class);
                    dto.setTitle(StringUtils.isNotBlank(dto.getTitleHtml()) ? HtmlUtil.cleanHtmlTag(dto.getTitleHtml()).replaceAll("&nbsp;", "") : null);
                    dto.setContent(StringUtils.isNotBlank(dto.getContentHtml()) ? HtmlUtil.cleanHtmlTag(dto.getContentHtml()).replaceAll("&nbsp;", "") : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SysNoticeConfig getTenantSystemConfig(TriggerTypeEnum triggerType) {
        ConfigsTypeEnum configsType = null;
        switch (triggerType) {
            case FLOW_REMIND:
                configsType = ConfigsTypeEnum.WORKFLOW_TODO_REMINDER;
                break;
            case FLOW_URGE:
                configsType = ConfigsTypeEnum.WORKFLOW_REMINDER;
                break;
            default:
                break;
        }
        if (ObjectNull.isNull(configsType)) {
            throw new BusinessException("不支持的消息触发类型");
        }
        String key = "config:" + TenantContextHolder.getTenantId() + ":" + configsType.name();
        Object data = redisUtils.get(key);
        if (ObjectNull.isNull(data)) {
            return new SysNoticeConfig();
        }
        return JSONObject.parseObject((String) data, SysNoticeConfig.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<DataNoticePo>lambdaQuery().eq(DataNoticePo::getJvsAppId, appId));
    }
}
