package cn.bctools.rule.hmacsha256;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author jvs
 * The type Hmac sha 256 dto.
 */
@Accessors(chain = true)
@Data
public class HmacSha256Dto {

    /**
     * The Text.
     */
    @ParameterValue(info = "需要加解密的原始文本", explain = "只支持字符串", type = InputType.longtext)
    public String text;

    /**
     * The Secret key.
     */
    @ParameterValue(info = "secretKey", type = InputType.text)
    @NotNull(message = "secretKey 不能为空")
    public String secretKey;

}
