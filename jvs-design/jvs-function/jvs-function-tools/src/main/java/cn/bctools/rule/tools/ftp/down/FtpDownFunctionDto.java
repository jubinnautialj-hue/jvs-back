package cn.bctools.rule.tools.ftp.down;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.tools.ftp.FtpSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@Accessors(chain = true)
@Data
public class FtpDownFunctionDto {

    @NotNull(message = "服务器不能为空")
    @ParameterValue(info = "FTP服务器", type = InputType.selected,  cls = FtpSelected.class)
    public String ftp;

    @NotNull(message = "服务器目录地址")
    @ParameterValue(info = "服务器目录地址", type = InputType.input,defaultValue = "/")
    public String destPath;

    @ParameterValue(info = "是否上传到文件服务器", type = InputType.onOff, defaultValue = "true")
    public Boolean onOff = false;

    @NotNull(message = "文件名不能为空")
    @ParameterValue(info = "文件名", type = InputType.input)
    public String fileName;

}
