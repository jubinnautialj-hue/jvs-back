package cn.bctools.rule.tools.img;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author jvs
 */
@Accessors(chain = true)
@Data
public class ImgDto {
    @ParameterValue(info = "图片链接地址", type = InputType.input)
    public String fileUrl;
    @ParameterValue(info = "图片名称", type = InputType.input)
    public String fileName;

    @ParameterValue(info = "比例缩放", type = InputType.number, necessity = false, explain = "请输入数字，1~100,为缩小，大于100放大，填写比缩放比例后图片宽度高度将失效")
    public Integer scale;

    @ParameterValue(info = "图片的宽度", type = InputType.number, defaultValue = "200")
    public Integer width;
    @ParameterValue(info = "图片高度", type = InputType.number, defaultValue = "200")
    public Integer height;

}
