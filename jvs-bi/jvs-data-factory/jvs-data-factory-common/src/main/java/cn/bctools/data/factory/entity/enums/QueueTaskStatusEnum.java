package cn.bctools.data.factory.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaohui
 */

@Getter
@AllArgsConstructor
public enum QueueTaskStatusEnum {
    QUEUE("QUEUE", "队列中"),
    EXECUTE("EXECUTE", "执行中"),
    STOP("STOP", "停止"),
    FAIL("FAIL", "失败"),
    EXECUTION_GRAPH("EXECUTION_GRAPH", "执行图生成中"),
    ACCOMPLISH("ACCOMPLISH", "执行完成");

    @EnumValue
    private final String code;
    private final String desc;
}
