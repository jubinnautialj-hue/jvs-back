package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 表单组件: 单选框
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "单选框")
@EqualsAndHashCode(callSuper = true)
public class RadioItemHtml extends MultipleHtml {

}
