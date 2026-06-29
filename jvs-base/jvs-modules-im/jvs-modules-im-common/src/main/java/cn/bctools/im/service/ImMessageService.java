package cn.bctools.im.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.bctools.im.dto.history.HistoryMessageReqDto;
import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * @author ZhuXiaoKang
 * @Description IM消息处理接口
 */
@Validated
public interface ImMessageService {

    /**
     * 消息服务类型
     * @return
     */
    MessageTypeEnum getType();

    /**
     * 保存消息
     * @param syncMessageDto
     */
    void saveMessage(BaseSyncMessageDto syncMessageDto);


    /**
     * 分页查询历史消息
     *
     * @param historyMessageReqDto
     * @return
     */
    Page<String> pageMessageHistory(@Valid HistoryMessageReqDto historyMessageReqDto);
}
