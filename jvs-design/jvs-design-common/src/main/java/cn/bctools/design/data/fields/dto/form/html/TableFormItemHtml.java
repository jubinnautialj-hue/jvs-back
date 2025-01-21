package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 表单组件: 表格
 */

@Data
@Accessors(chain = true)
@ApiModel(value = "表格")
@EqualsAndHashCode(callSuper = true)
public class TableFormItemHtml extends FieldBasicsHtml {

    @ApiModelProperty(value = "表格字段")
    private List<FieldBasicsHtml> tableColumn;

}
