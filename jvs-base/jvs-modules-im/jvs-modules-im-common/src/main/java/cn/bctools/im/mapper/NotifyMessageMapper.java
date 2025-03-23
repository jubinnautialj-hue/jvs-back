package cn.bctools.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.bctools.im.entity.NotifyMessage;
import cn.bctools.im.entity.dto.NotifyHistoryMessageDto;
import org.apache.ibatis.annotations.Param;

/**
 * @author ZhuXiaoKang
 * @Description IM通知全量消息Mapper
 */
public interface NotifyMessageMapper  extends BaseMapper<NotifyMessage> {

    /**
     * 查询用户可接收的历史通知
     *
     * @param page
     * @param dto
     */
    Page<NotifyMessage> pageUserHistory(Page<NotifyMessage> page, @Param("dto") NotifyHistoryMessageDto dto);
}
