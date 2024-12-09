package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "蓝牙信标")
public class BluetoothBeaconHtml extends FieldBasicsHtml {
}
