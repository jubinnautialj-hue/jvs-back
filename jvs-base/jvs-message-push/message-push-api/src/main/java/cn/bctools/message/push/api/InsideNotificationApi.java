package cn.bctools.message.push.api;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.dto.messagepush.InsideNoticeDto;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内
 * @author czy
 */
@FeignClient(value = "message-push-mgr",contextId = "inside-notification")
public interface InsideNotificationApi {

    String PREFIX = "/inside/notification";

    /**
     * 站内信
     * @param messageDto 消息数据
     * @return 执行状态
     */
    @PostMapping(PREFIX +"/inside")
    R<Boolean> send(@RequestBody InsideNotificationDto messageDto);

    /**
     * 重新发送
     * @param pushHisId 消息数据
     * @return 执行状态
     */
    @GetMapping(PREFIX +"/inside/resend")
    R<Boolean> resend(String pushHisId);

    /**
     * 分页查询消息
     * @param current 当前页数
     * @param size 页 大小
     * @param dto 查询条件
     * @return 分页数据
     */
    @PostMapping(PREFIX +"/page")
    R<Page<InsideNoticeDto>> page(@RequestParam("current")Long current, @RequestParam("size")Long size, @RequestBody InsideNoticeDto dto);

    /**
     * 查询消息
     * @param dto 查询条件
     * @return 分页数据
     */
    @PostMapping(PREFIX +"/list")
    R<List<InsideNoticeDto>> list(@RequestBody InsideNoticeDto dto);

    /**
     * 查询详细
     * @param id 消息通知id
     * @return 消息通知详情
     */
    @GetMapping(PREFIX +"/{id}/detail")
    R<InsideNoticeDto> detail(@PathVariable("id") String id);

    /**
     * 设置消息通知已读
     * @param id 消息通知id
     * @return 消息通知详情
     */
    @GetMapping(PREFIX +"/{id}/read")
    R<Boolean> readIs(@PathVariable("id") String id);

    /**
     * 设置所有消息通知已读
     * @return 消息通知详情
     */
    @GetMapping(PREFIX +"/read")
    R<Boolean> allReadIs();
}
