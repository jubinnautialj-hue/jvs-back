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
 * The type Gentleman signatureshujuqianfaqi dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureshujuqianfaqiDto extends JunZiQianBaseDto {
    /**
     * The Hash.
     */
    @ParameterValue(info = "对原始数据进行sha512运算得到的hash", type = InputType.input)
    public String hash;

    /**
     * The Signatories.
     */
    @ParameterValue(info = "签约方，参考后面签约方说明", type = InputType.listMap)
    public JSONArray signatories;


}
