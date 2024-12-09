package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturehuoquhetongbianhao dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturehuoquhetongbianhaoDto extends JunZiQianBaseDto {
    /**
     * The File key.
     */
    @ParameterValue(info = "文件key（SAAS文件发起接口中返回的fileKey）", necessity = false, type = InputType.input)
    public String fileKey;


}
