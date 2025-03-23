package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 提交任务配置
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class SeaTunnelStopJobDto {
    /**
     * 任务id
     */
    private String jobId;
    /**
     * 是否为检查点停止
     */
    private Boolean isStopWithSavePoint;
}
