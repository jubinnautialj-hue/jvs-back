package cn.bctools.im.dto.sync;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ZhuXiaoKang
 * @Description IM同步消息DTO基类
 */
@Data
public class BaseSyncMessageDto {

    /**
     * 消息服务redis topic
     */
    private String topic;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户
     */
    private String tenantId;
}
