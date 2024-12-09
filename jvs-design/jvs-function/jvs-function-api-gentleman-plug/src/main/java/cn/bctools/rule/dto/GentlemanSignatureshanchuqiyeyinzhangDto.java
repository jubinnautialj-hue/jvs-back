package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureshanchuqiyeyinzhang dto.
 *
 * @author jvs
 */

@EqualsAndHashCode(callSuper = true)@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureshanchuqiyeyinzhangDto extends JunZiQianBaseDto {
    /**
     * The Sign id.
     */
    @ParameterValue(info = "印章ID", type = InputType.input)
    public String signId;

    /**
     * The Email.
     */
    @ParameterValue(info = "可不传，不传就删除当前主体印章", necessity = false, type = InputType.input)
    public String email;


}
