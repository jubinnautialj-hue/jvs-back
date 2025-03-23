package cn.bctools.data.factory.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaohui
 */
@Getter
@AllArgsConstructor
public enum JvsDataAuthTypeEnum {
    /**
     * 行权限
     */
    row("row", "行"),
    /**
     * 列权限
     */
    column("column", "列");
    @EnumValue
    private final String code;
    private final String desc;
}
