package cn.bctools.data.factory.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 队列类型 前置 本身 后置
 *
 * @author xiaohui
 */

@Getter
@AllArgsConstructor
public enum QueueTaskTypeEnum {

    PREFIX_TASK("PREFIX_TASK", "前置"),
    REAR_TASK("REAR_TASK", "后置"),
    ITSELF("ITSELF", "本身");

    @EnumValue
    private final String code;
    private final String desc;
}
