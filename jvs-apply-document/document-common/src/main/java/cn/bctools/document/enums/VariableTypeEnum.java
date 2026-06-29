package cn.bctools.document.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: admin
 * @Description: 模板替换时 变量类型
 */
@Getter
@AllArgsConstructor
public enum VariableTypeEnum {

    /**
     * 字符串
     */
    STRING("STRING", "字符串"),
    /**
     * 表格
     */
    TABLE("TABLE", "表格"),
    IMAGE("IMAGE", "图片");

    @EnumValue
    public final String value;
    public final String desc;

}
