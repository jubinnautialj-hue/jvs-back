package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturelink dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturelinkDto extends JunZiQianBaseDto {
    /**
     * The Apply no.
     */
    @ParameterValue(info = "合同编号（合同发起接口中生成的APL开头的编号）", type = InputType.input)
    public String applyNo;

    /**
     * The Full name.
     */
    @ParameterValue(info = "签约人名称（合同发起接口中签署人姓名）", type = InputType.input)
    public String fullName;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "签约人证件号（合同发起接口中签署人证件号）", type = InputType.input)
    public String identityCard;

    /**
     * The Identity type.
     */
    @ParameterValue(info = "证件类型", explain = " 1身份证, 2护照, 3台胞证, 4港澳居民来往内地通行证, 11营业执照, 12统一社会信用代码, 20子账号, 99其他", type = InputType.number)
    public Integer identityType;

    /**
     * The View mode.
     */
    @ParameterValue(info = "1,PC端合同签署后跳转到查看详情页隐藏两边信息", necessity = false, type = InputType.number)
    public Integer viewMode;


}
