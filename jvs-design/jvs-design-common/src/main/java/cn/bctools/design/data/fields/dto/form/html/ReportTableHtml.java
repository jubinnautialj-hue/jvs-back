package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表格
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("静态表单")
@Accessors(chain = true)
public class ReportTableHtml extends BaseItemHtml {

    @ApiModelProperty(value = "表格字段")
    private List<BaseItemHtml> tableColumn;


}
