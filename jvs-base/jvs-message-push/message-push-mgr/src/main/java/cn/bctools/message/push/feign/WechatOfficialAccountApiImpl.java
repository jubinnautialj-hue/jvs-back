package cn.bctools.message.push.feign;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.vo.WxMpTemplateVo;
import cn.bctools.message.push.rabbitmq.Producer;
import cn.bctools.message.push.service.WechatOfficialAccountService;
import cn.bctools.redis.utils.RedisUtils;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xh
 */
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "[Feign] 微信公众号接口")
public class WechatOfficialAccountApiImpl implements WechatOfficialAccountApi {

    private final WechatOfficialAccountService wechatOfficialAccountService;
    private final RedisUtils redisUtils;
    private final Producer<TemplateMessageDTO> producer;

    @Override
    public R<Boolean> sendWebChatTemplateMessage(TemplateMessageDTO messageDto) {
        producer.push2Mq(messageDto);
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<Boolean> resendWebChatTemplateMessage(String pushHisId) {
        wechatOfficialAccountService.resendWebChatTemplateMessage(pushHisId);
        return R.ok(Boolean.TRUE);
    }

    @Override
    public List<WxMpTemplateVo> getAllPrivateTemplate() {
        return wechatOfficialAccountService.getAllPrivateTemplate();
    }

    @Override
    public List<WxMpTemplateVo> getAllPrivateTemplate(Boolean sync) {
        String key = "message:push:WECHAT_MP_MESSAGE" + TenantContextHolder.getTenantId();
        //如果是不是最新的，则直接返回缓存数据
        if (redisUtils.hasKey(key) && !sync) {
            return (List<WxMpTemplateVo>) redisUtils.get(key);
        } else {
            List<WxMpTemplateVo> allPrivateTemplate = wechatOfficialAccountService.getAllPrivateTemplate();
            redisUtils.set(key, allPrivateTemplate);
            return allPrivateTemplate;
        }
    }
}
