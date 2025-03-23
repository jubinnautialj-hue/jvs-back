package cn.bctools.data.factory.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * excel操作类型-子类
 */
@Getter
@AllArgsConstructor
public enum ExcelClassifyType {

    新增("add"),
    覆盖("cover"),
    数据片段("data"),
    提交记录("record"),
    编辑基本信息("base"),
    ;

    @EnumValue
    private final String code;
}
