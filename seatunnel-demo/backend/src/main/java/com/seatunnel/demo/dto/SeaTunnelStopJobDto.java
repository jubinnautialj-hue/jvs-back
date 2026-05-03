package com.seatunnel.demo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class SeaTunnelStopJobDto {

    @NotBlank(message = "任务ID不能为空")
    private String jobId;

    private Boolean isStopWithSavePoint = false;
}
