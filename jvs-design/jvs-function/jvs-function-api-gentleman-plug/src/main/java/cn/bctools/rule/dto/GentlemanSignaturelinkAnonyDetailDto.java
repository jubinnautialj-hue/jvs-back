package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturelink anony detail dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturelinkAnonyDetailDto extends JunZiQianBaseDto {
    /**
     * The Apply no.
     */
    @ParameterValue(info = "合同编号", type = InputType.input)
    public String applyNo;

    /**
     * The View mode.
     */
    @ParameterValue(info = "PC端查看合同模式 1查看合同页面隐藏两边信息", necessity = false, type = InputType.number)
    public Integer viewMode;


}
