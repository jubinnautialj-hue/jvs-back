package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "图片上传")
public class ImageUploadHtml extends BaseItemHtml {
}
