package cn.bctools.design.crud.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 模板类型
 */

@Getter
@AllArgsConstructor
public enum DesignTypeEnum {
/**自定义模板*/
    CUSTOMIZE(0, "自定义模板"),
    /**word模板*/
    WORD(1, "word模板"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;
    private String desc;
}
