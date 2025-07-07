package cn.bctools.rule.tools.similarity;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author jvs
 */
@Accessors(chain = true)
@Data
public class SimilarityDto {
    @NotNull
    @ParameterValue(info = "对比文本1", necessity = false, type = InputType.text)
    public String text1;

    @NotNull
    @ParameterValue(info = "对比文本2", necessity = false, type = InputType.text)
    public String text2;

}
