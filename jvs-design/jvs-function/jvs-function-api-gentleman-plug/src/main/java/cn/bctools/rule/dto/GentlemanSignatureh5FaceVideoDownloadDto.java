package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signatureh 5 face video download dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureh5FaceVideoDownloadDto extends JunZiQianBaseDto {
    /**
     * The Order no.
     */
    @ParameterValue(info = "人脸订单号", type = InputType.input)
    public String orderNo;


}
