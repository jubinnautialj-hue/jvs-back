package cn.bctools.document.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: admin
 * @Description: 知识库-文件状态。
 */
@Getter
@AllArgsConstructor
public enum OfficeFIleTypeEnum {

    /**
     * 正常的
     */
    NORMAL("NORMAL", "正常的"),
    /**
     * 进行中
     */
    UNDERWAY("UNDERWAY", "进行中"),
    FAILURE("FAILURE", "失败");

    @EnumValue
    public final String value;
    public final String desc;

}
