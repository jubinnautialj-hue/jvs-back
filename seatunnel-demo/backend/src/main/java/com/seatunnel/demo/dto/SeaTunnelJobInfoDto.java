package com.seatunnel.demo.dto;

import com.alibaba.fastjson2.JSONObject;
import com.seatunnel.demo.dto.enums.JobStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SeaTunnelJobInfoDto {

    private String jobId;

    private String jobName;

    private JobStatusEnum jobStatus;

    private LocalDateTime createTime;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private String errorMsg;

    private Long readCount;

    private Long writeCount;

    private JSONObject sourceConfig;

    private JSONObject sinkConfig;

    private String description;
}
