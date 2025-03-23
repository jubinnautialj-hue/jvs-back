package cn.bctools.remote.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CredentialTypeEnum {
    none("none","无限制"),
    secret("secret","凭证"),
    ;

    @EnumValue
    private final String code;
    private final String desc;
}
