package cn.bctools.data.factory.source.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum OperationStateEnums {
    ADD("add", "新增"),
    UPDATE("update", "修改"),
    UNIFORMITY("uniformity", "一致"),
    DELETE("delete", "删除");

    private final String code;
    private final String desc;
}
