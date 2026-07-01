package cn.bctools.rule.face;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.idcard.AliCodeSelected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FaceDto {

    @ParameterValue(info = "阿里APPcode", explain = "阿里APPcode", type = InputType.selected, cls = AliCodeSelected.class)
    public String appcode;

    @ParameterValue(info = "base64身份证", explain = "base64转换成功的图片", type = InputType.input)
    public String idCard;


}
