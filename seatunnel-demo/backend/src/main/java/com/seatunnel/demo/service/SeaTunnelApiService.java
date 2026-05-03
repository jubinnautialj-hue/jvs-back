package com.seatunnel.demo.service;

import com.seatunnel.demo.dto.SeaTunnelJobInfoDto;
import com.seatunnel.demo.dto.SeaTunnelSubmitJobDto;

import java.util.List;

public interface SeaTunnelApiService {

    String submitJob(SeaTunnelSubmitJobDto jobDto);

    SeaTunnelJobInfoDto getJobInfo(String jobId);

    void stopJob(String jobId);

    List<SeaTunnelJobInfoDto> listAllJobs();
}
