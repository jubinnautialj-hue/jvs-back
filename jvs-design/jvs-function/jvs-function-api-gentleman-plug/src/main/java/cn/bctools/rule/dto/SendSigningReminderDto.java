package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureapply sign dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SendSigningReminderDto extends JunZiQianBaseDto {
    
    /**
     * The Apply no.
     */
    @ParameterValue(info = "合同编号-单个合同签约时使用", type = InputType.input)
    public String applyNo;

    /**
     * The Business no.
     */
    @ParameterValue(info = "业务编号-批量签时通知专用", type = InputType.input)
    public String businessNo;

    /**
     * The Full name.
     */
    @ParameterValue(info = "签约人名称", type = InputType.input)
    public String fullName;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "签约人证件号", type = InputType.input)
    public String identityCard;

    /**
     * The Identity type.
     */
    @ParameterValue(info = "证件类型", explain = "1身份证, 2护照, 3台胞证, 4港澳居民来往内地通行证, 11营业执照, 12统一社会信用代码, 20子账号, 99其他", type = InputType.number)
    public Integer identityType;

    /**
     * The Sign notify type.
     */
    @ParameterValue(info = "提醒类型", explain = "1签字提醒,2到期前提醒,3到期后提醒,4合同到期前提醒,5合同到期后提醒", type = InputType.number)
    public Integer signNotifyType;

    /**
     * The Customize name.
     */
    @ParameterValue(info = "自定义接收短信中的商户名", type = InputType.number)
    public String customizeName;


}
