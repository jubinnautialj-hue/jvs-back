package cn.bctools.rule.aes;

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
public class AesDto {

    @ParameterValue(info = "数据", explain = "只支持字符串", necessity = false, type = InputType.longtext)
    public String body;

    @ParameterValue(info = "类型", explain = "加密或解密", type = InputType.selected, cls = EncryptDecryptSelected.class)
    public boolean type;

    @ParameterValue(info = "AES16位_KEY", explain = "AES 首先使用初始密钥,为空则随机生成", type = InputType.input, necessity = false)
    public String aesKey;

}
