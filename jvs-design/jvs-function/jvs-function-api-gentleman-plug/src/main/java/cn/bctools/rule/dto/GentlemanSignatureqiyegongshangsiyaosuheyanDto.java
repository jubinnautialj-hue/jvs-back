package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureqiyegongshangsiyaosuheyan dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureqiyegongshangsiyaosuheyanDto extends JunZiQianBaseDto {
    /**
     * The Ent name.
     */
    @ParameterValue(info = "企业名称", type = InputType.input)
    public String entName;

    /**
     * The Biz lic.
     */
    @ParameterValue(info = "证件号", type = InputType.input)
    public String bizLic;

    /**
     * The Name.
     */
    @ParameterValue(info = "法人姓名", type = InputType.input)
    public String name;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "法人身份证号", type = InputType.input)
    public String identityCard;


}
