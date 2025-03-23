package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service;

import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelJobInfoReturnDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobDto;

/**
 * seaTunnel 数据同步接口的公共抽取
 *
 * @author Administrator
 */
public interface SeaTunnelApiService {

    /**
     * 单个任务提交
     *
     * @param seaTunnelSubmitJobDto 任务提交的配置信息
     * @return 任务id
     */
    String submitJob(SeaTunnelSubmitJobDto seaTunnelSubmitJobDto);

    /**
     * 单个任务明细查询
     *
     * @param id 任务id
     * @return 任务信息 {@link SeaTunnelJobInfoReturnDto}
     */
    SeaTunnelJobInfoReturnDto getJobInfo(String id);

    /**
     * 停止作业
     *
     * @param id 任务id
     */
    void stopJob(String id);


}
