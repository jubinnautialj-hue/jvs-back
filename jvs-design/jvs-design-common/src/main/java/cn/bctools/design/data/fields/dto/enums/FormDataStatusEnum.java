package cn.bctools.design.data.fields.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单数据状态
 *
 * @author auto
 */
@Getter
@AllArgsConstructor
public enum FormDataStatusEnum {

    /**
     * 只读
     */
    read_only("只读"),
    /**
     * 普通
     */
    general("普通"),
    ;
    private final String description;
}
