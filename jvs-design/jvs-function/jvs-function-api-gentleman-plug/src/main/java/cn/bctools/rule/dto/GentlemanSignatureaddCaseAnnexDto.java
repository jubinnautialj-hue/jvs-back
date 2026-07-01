package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureadd case annex dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureaddCaseAnnexDto extends JunZiQianBaseDto {

    /**
     * The Order no.
     */
    @ParameterValue(info = "仲裁订单编号", type = InputType.input)
    public String orderNo;

    /**
     * The Status.
     */
    @ParameterValue(info = "仲裁案件状态,1:未提交 2:待接受 3:预审中 4:已立案 5:答辩期 6:审理期 7:已驳回 8:已拒绝 9:已结案", type = InputType.number, cls = JunZiQianSelected.class)
    public Integer status;

}
