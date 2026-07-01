package cn.bctools.rule.tools.qr;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 * 条码参数
 */
@Data
@Accessors(chain = true)
public class Code128Dto {
    @NotNull(message = "条码参数不能为空")
    @Length(max = 80, message = "条码参数最多80个字符")
    @ParameterValue(info = "条码参数", type = InputType.input, defaultValue = "0000000000")
    public String text;

    @NotNull(message = "宽不能为空")
    @ParameterValue(info = "宽", type = InputType.number, defaultValue = "300")
    public Integer width;

    @NotNull(message = "高不能为空")
    @ParameterValue(info = "高", type = InputType.number, defaultValue = "150")
    public Integer height;

    @ParameterValue(info = "顺时针旋转", necessity = false, type = InputType.number, defaultValue = "0")
    public Integer rotate;

}
