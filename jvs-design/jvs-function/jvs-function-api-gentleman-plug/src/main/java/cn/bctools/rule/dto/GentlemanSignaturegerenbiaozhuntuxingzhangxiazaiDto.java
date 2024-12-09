package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturegerenbiaozhuntuxingzhangxiazai dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturegerenbiaozhuntuxingzhangxiazaiDto extends JunZiQianBaseDto {
    /**
     * The Name.
     */
    @ParameterValue(info = "姓名", type = InputType.input)
    public String name;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "身份证号", type = InputType.input)
    public String identityCard;

    /**
     * The Mobile.
     */
    @ParameterValue(info = "手机号", type = InputType.input)
    public String mobile;

    /**
     * The Sign id.
     */
    @ParameterValue(info = "自定义章ID", necessity = false, type = InputType.number)
    public Long signId;


}
