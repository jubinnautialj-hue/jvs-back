package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturecloud certi per info dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturecloudCertiPerInfoDto extends JunZiQianBaseDto {
    /**
     * The Full name.
     */
    @ParameterValue(info = "用户名称", type = InputType.input)
    public String fullName;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "用户证件号", type = InputType.input)
    public String identityCard;

    /**
     * The Iden front.
     */
    @ParameterValue(info = "身份证人像面,图片大小控制在1M以内", type = InputType.file)
    public RuleFile idenFront;

    /**
     * The Iden reverse.
     */
    @ParameterValue(info = "身份证国徽面，图片大小控制在1M以内", type = InputType.file)
    public RuleFile idenReverse;

    /**
     * The Apply table.
     */
    @ParameterValue(info = "申请表可以不用传", necessity = false, type = InputType.file)
    public RuleFile applyTable;

    /**
     * The Cert sn.
     */
    @ParameterValue(info = "证书sn", necessity = false, type = InputType.input)
    public String certSn;

    /**
     * The Bus id.
     */
    @ParameterValue(info = "第三方核验流水号", necessity = false, type = InputType.input)
    public String busId;

    /**
     * The Request param.
     */
    @ParameterValue(info = "第三方核验请求参数(json字符串格式)，参考示例：{\"idCard\":\"51xxxxxxxxxxxxxxxx\",\"mobile\":\"152xxxxxxxx\",\"name\":\"袁xx\",\"type\":21}", necessity = false, type = InputType.input)
    public String requestParam;

    /**
     * The Result param.
     */
    @ParameterValue(info = "第三方核验响应参数(json字符串格式)，参考示例：{\"code\":\"10000\",\"match\":1,\"message\":\"验证成功\",\"transitionId\":\"lo1y2p0ij32e855OO1y2ozy0akhfup\"}", necessity = false, type = InputType.input)
    public String resultParam;

    /**
     * The Verify time.
     */
    @ParameterValue(info = "核验时间精确到时分秒建议格式(yyyy-MM-dd HH:mm:ss)或(yyyyMMddHHmmss)", necessity = false, type = InputType.input)
    public String verifyTime;


}
