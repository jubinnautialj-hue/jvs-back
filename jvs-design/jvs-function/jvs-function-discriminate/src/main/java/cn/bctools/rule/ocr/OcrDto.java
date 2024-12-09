package cn.bctools.rule.ocr;

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
public class OcrDto {

    @ParameterValue(info = "图片", type = InputType.imageSelect)
    @NotNull(message = "图片不能为空")
    public String url;

}
