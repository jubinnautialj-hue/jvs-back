package cn.bctools.message.push.feign;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.message.push.api.MessagePushUtilsApi;
import cn.bctools.message.push.dto.messagepush.MessageBatchSendDto;
import cn.bctools.message.push.service.MessagePushUtilsService;
import cn.bctools.message.push.utils.JvsUserComponent;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author xh
 */
@Slf4j
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "[Feign] 消息发送工具")
public class MessagePushUtilsApiImpl implements MessagePushUtilsApi {

    private final MessagePushUtilsService messagePushUtilsService;
    public static final String DEFAULT_HDEAIMG = "http://jvsoss.bctools.cn/jvs-public/wx/avatar/2022-01-18-668103781807722496-520QT.jpg";

    @Override
    public R batchSend(MessageBatchSendDto batchSendDto) {
        messagePushUtilsService.batchSend(batchSendDto,getCurrentUser(batchSendDto.getClientCode()));
        return R.ok();
    }

    public UserDto getCurrentUser(String clientCode){
        UserDto userDto = new UserDto().setTenantId(JvsUserComponent.DEFAULT_TENANT_ID).setId(JvsUserComponent.DEFAULT_ID).setRealName(Optional.ofNullable(clientCode).orElse("系统消息")).setHeadImg(DEFAULT_HDEAIMG);
        try {
            UserDto currentUser = UserCurrentUtils.getCurrentUser();
            return Optional.ofNullable(currentUser).isPresent()?currentUser:userDto;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return userDto;
    }
}
