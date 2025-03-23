package cn.bctools.data.factory.notice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TriggerTypeEnum {

    execSuccess("execSuccess","执行成功"),
    execFailures("execFailures","执行失败"),
    ;

    @EnumValue
    private final String value;
    private final String desc;

}
