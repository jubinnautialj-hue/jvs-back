package cn.bctools.document.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaohui
 */

@Getter
@AllArgsConstructor
public enum AuthTypeEnums {
    /**
     * 不需要权限认证
     * */
    none("none","不需要权限认证"),
    /**
     * 文库
     * */
    library("library","文库接口权限"),
    /**
     * 文档
     * */
    document("document","文档权限接口");
    @EnumValue
    public final String value;
    public final String desc;
}
