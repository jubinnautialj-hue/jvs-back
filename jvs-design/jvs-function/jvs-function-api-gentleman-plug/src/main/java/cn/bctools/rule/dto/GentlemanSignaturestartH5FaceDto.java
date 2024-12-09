package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturestart h 5 face dto.
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturestartH5FaceDto extends JunZiQianBaseDto {
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
     * The Start from.
     */
    @ParameterValue(info = "启动方式1=browser ：表示在浏览器启动刷脸,2=app ：表示在 app 里启动刷脸,默认值为browser", type = InputType.number)
    public Integer startFrom;

    /**
     * The Notify url.
     */
    @ParameterValue(info = "人脸结果异步通知地址", necessity = false, type = InputType.input)
    public String notifyUrl;

    /**
     * The Complexity.
     */
    @ParameterValue(info = "人脸难易度 , 1简单模式（不推荐使用）, 2正常模式, 3困难模式，默认不传，使用2正常模式", necessity = false, defaultValue = "2", type = InputType.input)
    public String complexity;



}
