package cn.bctools.rule.tools.base64;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.EncryptDecryptSelected;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Accessors(chain = true)
@Data
public class Base64Dto {

    @ParameterValue(info = "数据", type = InputType.longtext)
    public Object body;
    @ParameterValue(info = "加解密类型", type = InputType.selected, cls = EncryptDecryptSelected.class)
    public boolean type;
    @ParameterValue(info = "文件名,如果转文件才输入为空默认为转字符，只有解密才有效", type = InputType.input, necessity = false)
    public String fileName;

}
