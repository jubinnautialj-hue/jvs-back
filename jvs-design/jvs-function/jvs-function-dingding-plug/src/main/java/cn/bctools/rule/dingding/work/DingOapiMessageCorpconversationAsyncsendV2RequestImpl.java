package cn.bctools.rule.dingding.work;


import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dingding.utils.DingUtils;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.alibaba.fastjson2.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jvs
 * The type Ding oapi message corpconversation asyncsend v 2 request.
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "发送工作通知",
        group = RuleGroup.钉钉平台,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 4,
//        iconUrl = "rule-yinxingqiasanyaosurenzheng",
        explain = "工作通知消息是以某个应用的名义推送到员工的工作通知消息，例如生日祝福、入职提醒等。可以发送文本、语音、链接等，消息类型和样例可参考消息类型与数据格式。")
public class DingOapiMessageCorpconversationAsyncsendV2RequestImpl implements BaseCustomFunctionInterface<DingOapiMessageCorpconversationAsyncsendV2RequestDto> {


    /**
     * The User extension service api.
     */
    UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public Object execute(DingOapiMessageCorpconversationAsyncsendV2RequestDto dto, Map<String, Object> params) {
        //根据用户Id获取用户钉钉扩展uid
        List<UserExtensionDto> ding = userExtensionServiceApi.query(dto.getUsers(), "Ding").getData();
        if (ObjectNull.isNotNull(ding)) {
            String collect = ding.stream().map(e -> e.getExtension().getOrDefault("userid", null)).filter(ObjectNull::isNotNull).map(Object::toString).collect(Collectors.joining(","));
            if (ObjectNull.isNull(collect)) {
                throw new BusinessException("用户没有绑定钉钉");
            }


            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
            OapiMessageCorpconversationAsyncsendV2Request req = new OapiMessageCorpconversationAsyncsendV2Request();
            SysConfigDing sysConfigDing = DingUtils.getSysConfigDing(TenantContextHolder.getTenantId());
            req.setAgentId(Long.valueOf(sysConfigDing.getAgentId()));
            req.setUseridList(collect);
            OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();
            text.setContent(dto.getContent());
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setMsgtype(dto.getMsgType().value);
            switch (dto.getMsgType()) {
                case 文本消息:
                    msg.setText(text);
                    break;
                case 文件消息:
                    //设置资源标识
                    OapiMessageCorpconversationAsyncsendV2Request.File file = new OapiMessageCorpconversationAsyncsendV2Request.File();
                    file.setMediaId(JSONObject.parseObject(PasswordUtil.decodedPassword(dto.getMediaId(), SpringContextUtil.getKey()), DingMsMediaIdOption.class).mediaId);
                    msg.setFile(file);
                    break;
                case 图片消息:
                    OapiMessageCorpconversationAsyncsendV2Request.Image image = new OapiMessageCorpconversationAsyncsendV2Request.Image();
                    //设置资源标识
                    image.setMediaId(JSONObject.parseObject(PasswordUtil.decodedPassword(dto.getMediaId(), SpringContextUtil.getKey()), DingMsMediaIdOption.class).getMediaId());
                    msg.setImage(image);
                    break;
                case 链接消息:
                    OapiMessageCorpconversationAsyncsendV2Request.Link link = new OapiMessageCorpconversationAsyncsendV2Request.Link();
                    link.setText(dto.getText());
                    link.setTitle(dto.getTitle());
                    link.setPicUrl(dto.getPicUrl());
                    link.setMessageUrl(dto.getMessageUrl());
                    msg.setLink(link);
                    break;
                case Markdown消息:
                    OapiMessageCorpconversationAsyncsendV2Request.Markdown markdown = new OapiMessageCorpconversationAsyncsendV2Request.Markdown();
                    markdown.setText(dto.getText());
                    markdown.setTitle(dto.getTitle());
                    msg.setMarkdown(markdown);
                    break;
                case 卡片消息:
                    OapiMessageCorpconversationAsyncsendV2Request.ActionCard actionCard = new OapiMessageCorpconversationAsyncsendV2Request.ActionCard();
                    actionCard.setMarkdown(dto.getText());
                    actionCard.setTitle(dto.getTitle());
                    actionCard.setSingleUrl(dto.getMessageUrl());
                    actionCard.setSingleTitle(dto.getTitle());
                    msg.setActionCard(actionCard);
                default:

            }
            req.setMsg(msg);

            try {
                OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(req, DingUtils.getAccessToken(sysConfigDing));
                if (rsp.isSuccess()) {
                    return rsp.getBody();
                } else {
                    throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", rsp.getMessage(), rsp.getRequestId(), rsp.getCode()));
                }
            } catch (ApiException e) {
                log.error("钉钉三方调用异常", e);
                throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage()));

            }
        }
        throw new BusinessException("用户没有绑定钉钉");
    }
}
