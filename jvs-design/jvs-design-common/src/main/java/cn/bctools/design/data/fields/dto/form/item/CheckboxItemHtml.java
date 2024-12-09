package cn.bctools.design.data.fields.dto.form.item;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 表单组件: 多选框
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "多选框")
@EqualsAndHashCode(callSuper = true)
public class CheckboxItemHtml extends SelectItemHtml {

}
