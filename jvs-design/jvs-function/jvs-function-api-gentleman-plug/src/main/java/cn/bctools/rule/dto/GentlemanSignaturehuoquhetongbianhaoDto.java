package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturehuoquhetongbianhao dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
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
