package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.config.CommonConfig;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.cofig.SeaTunnelApiConfig;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelJobInfoReturnDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelStopJobDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobReturnDto;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.service.SeaTunnelApiService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Service
@Slf4j
@AllArgsConstructor
public class SeaTunnelApiServiceImpl implements SeaTunnelApiService {
    SeaTunnelApiConfig seaTunnelApiConfig;
    CommonConfig commonConfig;

    @Override
    public String submitJob(SeaTunnelSubmitJobDto seaTunnelSubmitJobDto) {
        String value = HttpUtil.post(commonConfig.getSeaTunnelIpPort() + seaTunnelApiConfig.getSubmitJobUrl(), JSONObject.toJSONString(seaTunnelSubmitJobDto),10000);
        SeaTunnelSubmitJobReturnDto seaTunnelSubmitJobReturnDto = JSONObject.parseObject(value, SeaTunnelSubmitJobReturnDto.class);
        if (StrUtil.isNotBlank(seaTunnelSubmitJobReturnDto.getStatus())) {
            log.error("seaTunnel插件提交任务失败{},入参为{}", seaTunnelSubmitJobReturnDto.getMessage(),JSONObject.toJSONString(seaTunnelSubmitJobDto));
            throw new BusinessException("seaTunnel插件提交任务失败");
        }
        return seaTunnelSubmitJobReturnDto.getJobId();
    }

    @Override
    public void stopJob(String id) {
        SeaTunnelStopJobDto seaTunnelStopJobDto = new SeaTunnelStopJobDto()
                .setJobId(id)
                .setIsStopWithSavePoint(Boolean.FALSE);
        HttpUtil.post(commonConfig.getSeaTunnelIpPort() + seaTunnelApiConfig.getStopJobUrl(), JSONObject.toJSONString(seaTunnelStopJobDto),10000);
    }

    @Override
    public SeaTunnelJobInfoReturnDto getJobInfo(String id) {
        String getJobInfoUrl = String.format(seaTunnelApiConfig.getGetJobInfoUrl(), id);
        String value = HttpUtil.get(commonConfig.getSeaTunnelIpPort() + getJobInfoUrl,10000);
        return JSONObject.parseObject(value, SeaTunnelJobInfoReturnDto.class);
    }

}
