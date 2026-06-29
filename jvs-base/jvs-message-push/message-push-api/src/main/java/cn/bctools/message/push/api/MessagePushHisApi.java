package cn.bctools.message.push.api;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.dto.messagepush.MessagePushHisDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 消息发送历史
 *
 * @author xh
 */
@FeignClient(value = "message-push-mgr", contextId = "push-his")
public interface MessagePushHisApi {

    String PREFIX = "/message/push/feign/his";

    /**
     * 发送信息
     *
     * @param dto
     * @return
     */
    @PostMapping(PREFIX)
    R<List<MessagePushHisDto>> getAllPushHis(@RequestBody MessagePushHisDto dto);

    /**
     * 获取发送信息
     *
     * @param current
     * @param size
     * @param dto
     * @return
     */
    @PostMapping(PREFIX + "/page")
    R<Page<MessagePushHisDto>> pagePushHis(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestBody MessagePushHisDto dto);

    /**
     * 重发消息
     *
     * @param hisId
     * @param msgType
     * @return
     */
    @PostMapping(PREFIX + "/resend")
    R<Boolean> resend(@RequestParam("hisId") String hisId, @RequestParam("msgType") String msgType);

}
