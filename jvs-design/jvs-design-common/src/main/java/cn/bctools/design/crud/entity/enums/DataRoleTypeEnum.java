package cn.bctools.design.crud.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 数据权限类型
 */
@Getter
@AllArgsConstructor
public enum DataRoleTypeEnum {
    /**
     * 使用数据模型的数据权限
     */
    data_model("datamodel", "使用数据模型的数据权限"),
    /**
     * 使用自定义的数据权限
     */
    custom("custom", "使用自定义的数据权限"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;
}
