package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具类操作类型
 *
 * @author admin
 */
@Getter
@AllArgsConstructor
public enum DcUtilOperationTypeEnum {
    /**
     * ocr
     */
    ocr("ocr"),
    /**
     * 文件合并
     **/
    merge("merge"),
    /**
     * 文件转换
     **/
    transition("transition");
    @EnumValue
    public final String value;
}
