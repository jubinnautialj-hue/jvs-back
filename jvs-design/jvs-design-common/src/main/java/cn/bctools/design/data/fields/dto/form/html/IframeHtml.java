package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * 网页链接
 *
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class IframeHtml extends FieldBasicsHtml {

    String iframeurl;

}
