package cn.bctools.design.taskNotice.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Accessors(chain = true)
public class FlowNoticeResponseDto {
    private int code;
    private boolean success;
    private String msg;
    private Object data;
}
