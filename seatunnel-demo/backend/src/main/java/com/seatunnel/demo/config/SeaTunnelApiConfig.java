package com.seatunnel.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "seatunnel.api")
@Data
public class SeaTunnelApiConfig {

    private String submitJobUrl = "/hazelcast/rest/maps/submit-job";

    private String getJobInfoUrl = "/hazelcast/rest/maps/job-info/%s";

    private String stopJobUrl = "/hazelcast/rest/maps/stop-job";
}
