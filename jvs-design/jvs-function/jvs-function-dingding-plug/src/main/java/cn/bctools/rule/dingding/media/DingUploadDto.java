package cn.bctools.rule.dingding.media;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author jvs
 * The type Ding upload dto.
 */
@Data
@Accessors(chain = true)
public class DingUploadDto {


    /**
     * The Type.
     */
    @ParameterValue(info = "资源类型", type = InputType.selected, cls = DingMediaSelected.class)
    public String type;

    /**
     * The File name.
     */
    @ParameterValue(info = "文件名", type = InputType.text)
    public String fileName;
    /**
     * The Media.
     */
    @ParameterValue(info = "要上传的媒体文件", type = InputType.text)
    public String media;

}
