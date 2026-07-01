package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturexiaochengxurenlian dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturexiaochengxurenlianDto extends JunZiQianBaseDto {
    /**
     * The Order no.
     */
    @ParameterValue(info = "订单号,最大支持32个字符", type = InputType.input)
    public String orderNo;

    /**
     * The Name.
     */
    @ParameterValue(info = "姓名", type = InputType.input)
    public String name;

    /**
     * The Identity card.
     */
    @ParameterValue(info = "身份证号", type = InputType.input)
    public String identityCard;

    /**
     * The Back url.
     */
    @ParameterValue(info = "同步回调地址", type = InputType.input)
    public String backUrl;

    /**
     * The Motions.
     */
    @ParameterValue(info = "MOUTH 动作序列,BLINK(眨眼),MOUTH(张嘴),NOD(点头),YAW(摇头).多个动作用空格隔开，如\"BLINK NOD\" 注：最大仅支持2个动作。", type = InputType.input)
    public String motions;

    /**
     * The Complexity.
     */
    @ParameterValue(info = "人脸难易度 , 1简单模式（不推荐使用）, 2正常模式, 3困难模式，默认不传，使用2正常模式", necessity = false, defaultValue = "2", type = InputType.input)
    public String complexity;


}
