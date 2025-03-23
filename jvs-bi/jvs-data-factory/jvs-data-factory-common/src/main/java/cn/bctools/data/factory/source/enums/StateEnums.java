package cn.bctools.data.factory.source.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum StateEnums {
    succeed("succeed", "成功"),
    fail("fail", "失败"),
    ;

    private final String code;
    private final String desc;
}
