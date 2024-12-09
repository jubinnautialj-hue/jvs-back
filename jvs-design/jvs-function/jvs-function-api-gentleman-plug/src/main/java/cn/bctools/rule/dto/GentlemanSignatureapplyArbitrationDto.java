package cn.bctools.rule.dto;


import lombok.Data;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureapply arbitration dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureapplyArbitrationDto extends JunZiQianBaseDto {
    /**
     * The Apply no.
     */
    @ParameterValue(info = "文件编号(合同发起接口中返回的APL开头的编号)", type = InputType.input)
    public String applyNo;

    /**
     * The Full name.
     */
    @ParameterValue(info = "签约方名称（合同发起接口中签署人姓名）", type = InputType.input)
    public String fullName;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "签约方证件号（合同发起接口中签署人证件号）", type = InputType.input)
    public String identityCard;

    /**
     * The Identity type.
     */
    @ParameterValue(info = "证件类型 1 个人 12企业", type = InputType.number)
    public Integer identityType;

    /**
     * The Deal type.
     */
    @ParameterValue(info = "文件类型 0合同文件,1证据包文件", type = InputType.number)
    public Integer dealType;

    /**
     * The Facts and reasons.
     */
    @ParameterValue(info = "申请仲裁事实与理由", type = InputType.input)
    public String factsAndReasons;

    /**
     * The Proposers.
     */
    @ParameterValue(info = "申请人,参考后面proposers字段说明", type = InputType.listMap)
    public JSONArray proposers;

    /**
     * The Agents.
     */
    @ParameterValue(info = "代理人,参考后面agents字段说明", type = InputType.listMap)
    public JSONArray agents;

    /**
     * The Respondents.
     */
    @ParameterValue(info = "被申请人，参考后面respondents字段说明", type = InputType.listMap)
    public JSONArray respondents;

    /**
     * The Arbitration asks.
     */
    @ParameterValue(info = "仲裁请求,参考后面arbitrationAsks字段说明", type = InputType.listMap)
    public JSONArray arbitrationAsks;

    /**
     * The Arbitration petitions.
     */
    @ParameterValue(info = "仲裁申请书,参考后面arbitrationPetitions字段说明", type = InputType.listMap)
    public JSONArray arbitrationPetitions;

    /**
     * The Evidences.
     */
    @ParameterValue(info = "证据材料，参考后面evidences字段说明", type = InputType.listMap)
    public JSONArray evidences;

    /**
     * The Evidence dirs.
     */
    @ParameterValue(info = "证据目录，参考后面evidenceDirs字段说明", type = InputType.listMap)
    public JSONArray evidenceDirs;


}
