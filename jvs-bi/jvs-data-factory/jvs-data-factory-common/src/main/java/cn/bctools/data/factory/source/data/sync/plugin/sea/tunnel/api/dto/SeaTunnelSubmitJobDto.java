package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 提交任务配置
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class SeaTunnelSubmitJobDto {
    /**
     * 引擎可选参数
     * 具体参数查看 <a href="https://seatunnel.apache.org/zh-CN/docs/2.3.8/concept/JobEnvConfig/">JobEnvConfig</a>
     */
    private JSONObject env;
    /**
     * 数据来源配置信息 不同数据源 配置可能存在差异
     * 具体参数查看 <a href="https://seatunnel.apache.org/zh-CN/docs/2.3.8/connector-v2/source">source</a>
     */
    private List<JSONObject> source;

    /**
     * 数据加工配置、
     * 具体参数查看 <a href="https://seatunnel.apache.org/zh-CN/docs/2.3.8/transform-v2">transform</a>
     */
    private List<JSONObject> transform;
    /**
     * 数据去向配置
     * 具体参数查看 <a href="https://seatunnel.apache.org/zh-CN/docs/2.3.8/connector-v2/sink">sink</a>
     */
    private List<JSONObject> sink;
}
