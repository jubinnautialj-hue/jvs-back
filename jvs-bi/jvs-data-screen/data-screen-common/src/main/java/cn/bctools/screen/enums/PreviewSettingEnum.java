package cn.bctools.screen.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreviewSettingEnum {

    adaptation("adaptation","按屏幕比例适配"),
    spreadOut("spreadOut","全屏铺满"),
    original("original","原始大小"),
    ;
    @EnumValue
    private final String code;
    private final String desc;
}
