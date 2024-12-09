package cn.bctools.rule.tools.qr;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class QrDecodeDto {

    @NotNull(message = "二维码地址不能为空")
    @ParameterValue(info = "二维码图片地址", type = InputType.longtext)
    public String url;

}
