package cn.bctools.design.notice.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.dto.VariableDto;
import cn.bctools.design.notice.handler.enums.DingNoticeEnum;
import cn.bctools.design.notice.handler.enums.WxEnterpriseNoticeEnum;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.WechatTemplateData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知模板变量工具类
 */
public class NoticeExtendUtils {
    private NoticeExtendUtils() {}

    /**
     * 得到所有平台的模板变量
     * @param templateDtos
     * @return
     */
    public static Map<String, Map<String, String>> getTemplateVariable(List<NoticeExtendTemplateDto> templateDtos) {
        if (CollectionUtils.isEmpty(templateDtos)) {
            return null;
        }
        Map<String, Map<String, String>> templateVariable = new HashMap<>(1);
        templateDtos.forEach(e -> {
            Map<String, String> variable = null;
            switch (e.getType()) {
                case SYSTEM:
                    break;
                case WECHAT_MP:
                    variable = getVariable(e.getWechatMp());
                    break;
                case SMS:
                    variable = getVariable(e.getVariables());
                default:
                    break;
            }
            if (MapUtils.isNotEmpty(variable)) {
                templateVariable.put(e.getType().getValue(), variable);
            }
        });
        return templateVariable;
    }

    /**
     * 微信公众号变量
     * @param wechatMp
     * @return
     */
    private static Map<String, String> getVariable(TemplateMessageDTO wechatMp) {
        if (CollectionUtils.isEmpty(wechatMp.getTemplateDataList())){
            return Collections.emptyMap();
        }
        return wechatMp.getTemplateDataList().stream().collect(Collectors.toMap(WechatTemplateData::getName, WechatTemplateData::getValue));
    }

    /**
     * 变量
     * @param variableDtos
     * @return
     */
    private static Map<String, String> getVariable(List<VariableDto> variableDtos) {
        if (CollectionUtils.isEmpty(variableDtos)){
            return Collections.emptyMap();
        }
        return variableDtos.stream().collect(Collectors.toMap(VariableDto::getName, VariableDto::getValue));
    }


    /**
     * 消息模板参数校验
     * @param templateDtos
     */
    public static void checkParam(List<NoticeExtendTemplateDto> templateDtos) {
        if (CollectionUtils.isEmpty(templateDtos)) {
            throw new BusinessException("请完善消息模板配置");
        }
        templateDtos.forEach(t -> {
            switch (t.getType()) {
                case SYSTEM:
                    checkParamSystem(t);
                    break;
                case WECHAT_MP:
                    checkParamWechatMp(t);
                    break;
                case WECHAT_ENTERPRISE:
                    checkParamWxEnterprise(t);
                    break;
                case DING:
                    checkParamDing(t);
                    break;
                case EMAIL:
                    checkParamEmail(t);
                    break;
                case SMS:
                    checkParamSms(t);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 校验站内信配置
     * @param templateDto
     */
    private static void checkParamSystem(NoticeExtendTemplateDto templateDto) {
        if (StringUtils.isBlank(templateDto.getTitleHtml())) {
            throw new BusinessException("未配置站内信标题");
        }
        if (StringUtils.isBlank(templateDto.getContentHtml())) {
            throw new BusinessException("未配置站内信内容");
        }
    }

    /**
     * 校验微信公众号配置
     * @param templateDto
     */
    private static void checkParamWechatMp(NoticeExtendTemplateDto templateDto) {
        TemplateMessageDTO templateMessage = templateDto.getWechatMp();
        if (ObjectNull.isNull(templateMessage) || StringUtils.isBlank(templateDto.getTemplateCode())) {
            throw new BusinessException("请完善微信公众号模板配置");
        }
    }

    /**
     * 校验企业微信配置
     * @param templateDto
     */
    private static void checkParamWxEnterprise(NoticeExtendTemplateDto templateDto) {
        Map<WxEnterpriseNoticeEnum, Object> templateMessage = templateDto.getWxEnterprise();
        if (MapUtils.isEmpty(templateMessage)) {
            throw new BusinessException("请完善企业微信模板配置");
        }
        templateMessage.forEach((key, value) -> {
            if (WxEnterpriseNoticeEnum.TEXT.equals(key) && StringUtils.isBlank(templateDto.getContentHtml())) {
                throw new BusinessException("请完善企业微信模板配置");
            }
        });

    }

    /**
     * 校验钉钉配置
     * @param templateDto
     */
    private static void checkParamDing(NoticeExtendTemplateDto templateDto) {
        Map<DingNoticeEnum, Object> templateMessage = templateDto.getDing();
        if (MapUtils.isEmpty(templateMessage)) {
            throw new BusinessException("请完善钉钉模板配置");
        }
        templateMessage.forEach((key, value) -> {
            if (DingNoticeEnum.TEXT.equals(key) && StringUtils.isBlank(templateDto.getContentHtml())) {
                throw new BusinessException("请完善钉钉模板配置");
            }
        });
    }

    /**
     * 校验邮件配置
     * @param templateDto
     */
    private static void checkParamEmail(NoticeExtendTemplateDto templateDto) {
        if (StringUtils.isBlank(templateDto.getTitleHtml())) {
            throw new BusinessException("未配置邮件标题");
        }
        if (StringUtils.isBlank(templateDto.getContentHtml())) {
            throw new BusinessException("未配置邮件内容");
        }
    }

    /**
     * 校验短信配置
     * @param templateDto
     */
    private static void checkParamSms(NoticeExtendTemplateDto templateDto) {
        if (StringUtils.isBlank(templateDto.getTemplateCode())) {
            throw new BusinessException("未选择短信模板");
        }
    }




}
