package cn.bctools.im.message.dto;

import lombok.Data;

/**
 * @author ZhuXiaoKang
 * @Description IM同步消息DTO基类
 */
@Data
public class BaseSyncMessageDto {

    /**
     * 消息服务topic
     */
    private String topic;

    /**
     * 创建时间 时间戳
     */
    private Long createTime;

    /**
     * 租户
     */
    private String tenantId;
}
