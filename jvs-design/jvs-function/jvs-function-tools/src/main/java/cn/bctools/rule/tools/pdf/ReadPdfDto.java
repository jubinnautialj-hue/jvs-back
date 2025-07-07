package cn.bctools.rule.tools.pdf;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Accessors(chain = true)
@Data
public class ReadPdfDto {

    @ParameterValue(info = "URL地址", necessity = false, type = InputType.text)
    public String url;

}
