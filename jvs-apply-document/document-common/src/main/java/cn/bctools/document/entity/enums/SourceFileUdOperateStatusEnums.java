package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum SourceFileUdOperateStatusEnums {
    await("await", "等待或队列中"),
    success("success", "成功"),
    fail("fail", "失败");

    @EnumValue
    String value;
    String desc;
}
