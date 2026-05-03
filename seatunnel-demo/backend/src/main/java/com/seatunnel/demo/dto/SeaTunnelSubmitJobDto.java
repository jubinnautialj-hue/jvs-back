package com.seatunnel.demo.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class SeaTunnelSubmitJobDto {

    private JSONObject env;

    @NotEmpty(message = "数据源配置不能为空")
    private List<JSONObject> source;

    private List<JSONObject> transform;

    @NotEmpty(message = "数据去向配置不能为空")
    private List<JSONObject> sink;

    private String jobName;

    private String description;
}
