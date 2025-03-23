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
public enum TaskTypeEnum {

    ordinary("ordinary", "普通任务"),
    mqtt("mqtt", "mqtt任务"),
    api("api", "api任务"),
    sql("sql", "sql数据集");

    @EnumValue
    private final String code;
    private final String desc;
}
