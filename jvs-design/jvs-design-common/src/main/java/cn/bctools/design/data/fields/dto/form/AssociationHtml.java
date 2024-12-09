package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.data.fields.dto.form.item.AssociationItemHtml;
import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 规则
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class AssociationHtml {

    @ApiModelProperty("是否启用")
    private Boolean active;
    @ApiModelProperty("目标数据模型")
    private String dataModelId;
    @ApiModelProperty("操作类型")
    private RuleTypeEnum ruleType;
    @ApiModelProperty("字段类型")
    private List<AssociationItemHtml> fieldList;
    @ApiModelProperty("条件数据集")
    private List<BaseItemHtml> conditions;

}
