package cn.bctools.rule.tools.qr;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class QrDto {

    @NotNull(message = "二维码参数不能为空")
    @ParameterValue(info = "二维码参数", type = InputType.longtext, defaultValue = "自动化测试二维码")
    public String text;

//    @NotNull(message = "宽高不能为空")
//    @ParameterValue(info = "宽高", type = InputType.number, defaultValue = "300")
//    public Integer widthAndHeight;

//    @NotNull(message = "边距不能为空")
//    @ParameterValue(info = "边距", type = InputType.number, defaultValue = "2")
//    public Integer margin;

//    @ParameterValue(info = "是否上传到文件服务器", type = InputType.onOff, defaultValue = "true")
//    public Boolean onOff = false;

}
