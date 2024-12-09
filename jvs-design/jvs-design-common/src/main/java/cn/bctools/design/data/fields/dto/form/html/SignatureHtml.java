package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "手写签名")
public class SignatureHtml extends BaseItemHtml {
}
