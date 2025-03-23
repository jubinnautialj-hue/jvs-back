package cn.bctools.bi.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum TaskStatusEnums {
    succeed("succeed", "成功"),
    fail("fail","失败"),
    all_set("all_set","准备好了"),;

    @EnumValue
    String value;
    String desc;
}
