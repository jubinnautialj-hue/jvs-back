package com.xxl.job.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import groovy.transform.ToString;
import groovy.transform.builder.Builder;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标准的结果返回对象
 *
 * @author admin
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code = 0;

    private String msg = "success";

    private T data;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss SSS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss SSS")
    private LocalDateTime timestamp = LocalDateTime.now();

}
