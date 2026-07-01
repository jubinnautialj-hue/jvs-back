package cn.bctools.design.workflow.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 权限成员类型
 */
@Getter
@AllArgsConstructor
public enum PurviewPersonTypeEnum {

    /**
     * 权限成员类型
     */
    all("all", "全部成员"),
    custom("custom", "自定义"),
    ;

    @EnumValue
    @JsonValue
    private final String value;
    private String desc;
}
