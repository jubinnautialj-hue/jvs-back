package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto;

import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.enums.JobInfoStatusEnums;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取任务详细的返回值
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class SeaTunnelJobInfoReturnDto {
    /**
     * 任务id
     */
    private String jobId;
    /**
     * 数据来源配置信息 不同数据源 配置可能存在差异
     */
    private String jobName;
    /**
     * 数据来源配置信息 不同数据源 配置可能存在差异
     */
    private JobInfoStatusEnums jobStatus;
    /**
     * 数据来源配置信息 不同数据源 配置可能存在差异
     */
    private String createTime;
    /**
     * 错误信息
     */
    private String errorMsg;
}
