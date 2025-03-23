package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 提交任务 返回值
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class SeaTunnelSubmitJobReturnDto {
    /**
     * 任务提交状态 提交成功这个值 为null
     */
    private String status;
    /**
     * 提交错误时的信息
     */
    private String message;
    /**
     * 提交成功后的id
     */
    private String jobId;
}
