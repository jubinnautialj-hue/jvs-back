package cn.bctools.remote.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class JvsRemoteServerQueryDto {

    /**
     * 数据服务id
     */
    private String serverId;

    /**
     * 数据服务名称
     */
    private String serverName;

    /**
     * 查询起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime queryStartTime;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime queryEndTime;

    /**
     * 调用状态
     */
    private Boolean callStatus;

    /**
     * 调用ip
     */
    private String ip;

    /**
     * 调用方
     */
    private String invoker;

    /**
     * 数据查得状态
     */
    private Boolean dataGetStatus;
}
