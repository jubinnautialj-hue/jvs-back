package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturecomm send sms dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturecommSendSmsDto extends JunZiQianBaseDto {
    /**
     * The Target.
     */
    @ParameterValue(info = "手机号", type = InputType.input)
    public String target;

    /**
     * The Template code.
     */
    @ParameterValue(info = "模板编号 模板申请时的用户给出模板id", type = InputType.input)
    public String templateCode;

    /**
     * The Param.
     */
    @ParameterValue(info = "模板内容中的预设键值对json字符串", necessity = false, type = InputType.input)
    public String param;


}
