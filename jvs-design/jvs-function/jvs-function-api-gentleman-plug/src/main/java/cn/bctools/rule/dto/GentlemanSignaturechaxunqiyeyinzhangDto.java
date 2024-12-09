package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturechaxunqiyeyinzhang dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturechaxunqiyeyinzhangDto extends JunZiQianBaseDto {
    /**
     * The Email.
     */
    @ParameterValue(info = "邮箱，可不传，不传就查询当前主体印章", necessity = false, type = InputType.input)
    public String email;


}
