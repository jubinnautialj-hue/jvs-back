package com.seatunnel.demo.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.seatunnel.demo.dto.SeaTunnelJobInfoDto;
import com.seatunnel.demo.dto.SeaTunnelSubmitJobDto;
import com.seatunnel.demo.dto.enums.JobStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MockSeaTunnelApiService implements SeaTunnelApiService {

    private final Map<String, SeaTunnelJobInfoDto> jobStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Mock SeaTunnel API Service initialized");
        createSampleJobs();
    }

    private void createSampleJobs() {
        SeaTunnelJobInfoDto job1 = new SeaTunnelJobInfoDto()
                .setJobId(IdUtil.simpleUUID())
                .setJobName("MySQL_to_Doris_Sync")
                .setJobStatus(JobStatusEnum.FINISHED)
                .setCreateTime(LocalDateTime.now().minusHours(2))
                .setStartTime(LocalDateTime.now().minusHours(2))
                .setFinishTime(LocalDateTime.now().minusMinutes(30))
                .setReadCount(10000L)
                .setWriteCount(10000L)
                .setSourceConfig(createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/test"))
                .setSinkConfig(createSinkConfig("Doris", "jdbc:mysql://localhost:9030/demo"))
                .setDescription("MySQL 数据同步到 Doris");

        SeaTunnelJobInfoDto job2 = new SeaTunnelJobInfoDto()
                .setJobId(IdUtil.simpleUUID())
                .setJobName("PostgreSQL_to_ClickHouse")
                .setJobStatus(JobStatusEnum.RUNNING)
                .setCreateTime(LocalDateTime.now().minusMinutes(10))
                .setStartTime(LocalDateTime.now().minusMinutes(10))
                .setReadCount(5000L)
                .setWriteCount(4800L)
                .setSourceConfig(createSourceConfig("PostgreSQL", "jdbc:postgresql://localhost:5432/test"))
                .setSinkConfig(createSinkConfig("ClickHouse", "jdbc:clickhouse://localhost:8123/default"))
                .setDescription("PostgreSQL 实时同步到 ClickHouse");

        jobStore.put(job1.getJobId(), job1);
        jobStore.put(job2.getJobId(), job2);
    }

    private JSONObject createSourceConfig(String type, String url) {
        JSONObject config = new JSONObject();
        config.put("plugin_name", type);
        config.put("url", url);
        config.put("table", "sample_table");
        return config;
    }

    private JSONObject createSinkConfig(String type, String url) {
        JSONObject config = new JSONObject();
        config.put("plugin_name", type);
        config.put("url", url);
        config.put("table", "target_table");
        return config;
    }

    @Override
    public String submitJob(SeaTunnelSubmitJobDto jobDto) {
        String jobId = IdUtil.simpleUUID();
        log.info("Submitting job: {} with config: {}", jobId, jobDto.getJobName());

        SeaTunnelJobInfoDto jobInfo = new SeaTunnelJobInfoDto()
                .setJobId(jobId)
                .setJobName(jobDto.getJobName() != null ? jobDto.getJobName() : "Job_" + jobId.substring(0, 8))
                .setJobStatus(JobStatusEnum.PENDING)
                .setCreateTime(LocalDateTime.now())
                .setReadCount(0L)
                .setWriteCount(0L)
                .setSourceConfig(jobDto.getSource() != null && !jobDto.getSource().isEmpty() ? jobDto.getSource().get(0) : new JSONObject())
                .setSinkConfig(jobDto.getSink() != null && !jobDto.getSink().isEmpty() ? jobDto.getSink().get(0) : new JSONObject())
                .setDescription(jobDto.getDescription());

        jobStore.put(jobId, jobInfo);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                jobInfo.setJobStatus(JobStatusEnum.RUNNING);
                jobInfo.setStartTime(LocalDateTime.now());

                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    if (jobInfo.getJobStatus() == JobStatusEnum.CANCELLED) {
                        return;
                    }
                    jobInfo.setReadCount(jobInfo.getReadCount() + 1000);
                    jobInfo.setWriteCount(jobInfo.getWriteCount() + 980);
                }

                jobInfo.setJobStatus(JobStatusEnum.FINISHED);
                jobInfo.setFinishTime(LocalDateTime.now());
                log.info("Job {} finished", jobId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                jobInfo.setJobStatus(JobStatusEnum.FAILED);
                jobInfo.setErrorMsg(e.getMessage());
            }
        }).start();

        return jobId;
    }

    @Override
    public SeaTunnelJobInfoDto getJobInfo(String jobId) {
        return jobStore.get(jobId);
    }

    @Override
    public void stopJob(String jobId) {
        SeaTunnelJobInfoDto jobInfo = jobStore.get(jobId);
        if (jobInfo != null) {
            jobInfo.setJobStatus(JobStatusEnum.CANCELLED);
            jobInfo.setFinishTime(LocalDateTime.now());
            log.info("Job {} stopped", jobId);
        }
    }

    @Override
    public List<SeaTunnelJobInfoDto> listAllJobs() {
        return new ArrayList<>(jobStore.values());
    }

    public void clearAllJobs() {
        jobStore.clear();
        createSampleJobs();
    }
}
