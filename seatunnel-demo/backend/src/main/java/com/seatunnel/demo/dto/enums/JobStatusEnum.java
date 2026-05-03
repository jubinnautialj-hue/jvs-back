package com.seatunnel.demo.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobStatusEnum {

    RUNNING("RUNNING", "运行中"),
    FINISHED("FINISHED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    FAILED("FAILED", "失败"),
    PENDING("PENDING", "等待中");

    private final String code;
    private final String desc;

    public static JobStatusEnum fromCode(String code) {
        for (JobStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
