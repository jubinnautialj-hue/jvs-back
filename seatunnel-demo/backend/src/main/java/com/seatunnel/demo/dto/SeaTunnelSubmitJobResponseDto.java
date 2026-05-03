package com.seatunnel.demo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SeaTunnelSubmitJobResponseDto {

    private String status;

    private String message;

    private String jobId;
}
