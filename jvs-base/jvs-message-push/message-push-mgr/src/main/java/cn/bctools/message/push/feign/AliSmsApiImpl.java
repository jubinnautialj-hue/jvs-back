package cn.bctools.message.push.feign;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.api.AliSmsApi;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.message.push.rabbitmq.Producer;
import cn.bctools.message.push.service.AliSmsService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guojing
 */
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "[Feign] 短信接口")
public class AliSmsApiImpl implements AliSmsApi {

    private final AliSmsService aliSmsService;
    private final Producer<AliSmsDto> producer;
    @Override
    public R<Boolean> sendAliSms(AliSmsDto dto) {
        producer.push2Mq(dto);
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<Boolean> sendAliSms(String pushHisId) {
        aliSmsService.resend(pushHisId);
        return R.ok(Boolean.TRUE);
    }

    @Override
    public List<AliSmsTemplateVo> getAllPrivateTemplate(Integer pageIndex, Integer pageSize) {
        return aliSmsService.getAllPrivateTemplate(pageIndex, pageSize);
    }
}
