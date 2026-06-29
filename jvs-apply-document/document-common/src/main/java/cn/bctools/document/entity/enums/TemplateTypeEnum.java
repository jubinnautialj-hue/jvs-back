package cn.bctools.document.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 模板类型
 */
@Getter
public enum TemplateTypeEnum {
    text("text", "纯文本类型"),
    variable("variable", "变量类型");

    @EnumValue
    public final String value;
    public final String desc;

    TemplateTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
