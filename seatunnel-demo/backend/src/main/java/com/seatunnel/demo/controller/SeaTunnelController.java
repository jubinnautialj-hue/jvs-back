package com.seatunnel.demo.controller;

import com.seatunnel.demo.dto.Result;
import com.seatunnel.demo.dto.SeaTunnelJobInfoDto;
import com.seatunnel.demo.dto.SeaTunnelSubmitJobDto;
import com.seatunnel.demo.dto.SeaTunnelStopJobDto;
import com.seatunnel.demo.service.SeaTunnelApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/seatunnel")
@AllArgsConstructor
public class SeaTunnelController {

    private final SeaTunnelApiService seaTunnelApiService;

    @PostMapping("/job/submit")
    public Result<String> submitJob(@Valid @RequestBody SeaTunnelSubmitJobDto jobDto) {
        log.info("Submitting SeaTunnel job: {}", jobDto.getJobName());
        String jobId = seaTunnelApiService.submitJob(jobDto);
        return Result.success(jobId);
    }

    @GetMapping("/job/{jobId}")
    public Result<SeaTunnelJobInfoDto> getJobInfo(@PathVariable String jobId) {
        log.info("Getting job info for: {}", jobId);
        SeaTunnelJobInfoDto jobInfo = seaTunnelApiService.getJobInfo(jobId);
        if (jobInfo == null) {
            return Result.error("Job not found");
        }
        return Result.success(jobInfo);
    }

    @PostMapping("/job/stop")
    public Result<Void> stopJob(@Valid @RequestBody SeaTunnelStopJobDto stopDto) {
        log.info("Stopping job: {}", stopDto.getJobId());
        seaTunnelApiService.stopJob(stopDto.getJobId());
        return Result.success();
    }

    @GetMapping("/jobs")
    public Result<List<SeaTunnelJobInfoDto>> listJobs() {
        log.info("Listing all jobs");
        List<SeaTunnelJobInfoDto> jobs = seaTunnelApiService.listAllJobs();
        return Result.success(jobs);
    }
}
