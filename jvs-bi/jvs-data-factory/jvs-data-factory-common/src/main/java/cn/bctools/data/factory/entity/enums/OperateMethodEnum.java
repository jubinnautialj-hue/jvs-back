package cn.bctools.data.factory.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperateMethodEnum {

    AUTOMATIC("AUTOMATIC","自动"),
    MANUALLY("MANUALLY","手动"),
    ;

    @EnumValue
    private final String code;
    private final String desc;
}
