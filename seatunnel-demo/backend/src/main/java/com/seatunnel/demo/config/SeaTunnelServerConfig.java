package com.seatunnel.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "seatunnel.server")
@Data
public class SeaTunnelServerConfig {

    private String url = "http://localhost:8080";

    private Boolean enabled = false;
}
