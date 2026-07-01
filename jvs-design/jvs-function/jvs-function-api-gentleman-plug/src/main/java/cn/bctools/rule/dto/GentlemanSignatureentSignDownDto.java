package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureent sign down dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureentSignDownDto extends JunZiQianBaseDto {
    /**
     * The Email.
     */
    @ParameterValue(info = "企业用户邮箱,不传入将公章附与API商户", necessity = false, type = InputType.input)
    public String email;

    /**
     * The Sign id.
     */
    @ParameterValue(info = "公章ID（signId和sysSign 二选一）", necessity = false, type = InputType.input)
    public String signId;

    /**
     * The Sys sign.
     */
    @ParameterValue(info = "1,下载系统生成的标准图形章 （signId和sysSign 二选一）", necessity = false, type = InputType.number)
    public Integer sysSign;


}
