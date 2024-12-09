package cn.bctools.design.data.fields.dto.form.item;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 业务关联规则
 */
@Data
@Accessors(chain = true)
public class AssociationItemHtml {

    @ApiModelProperty("模型字段key")
    private String fieldKey;

    @ApiModelProperty(value = "表单字段key", notes = "AssociationTypeEnum为prop时使用此属性")
    private String prop;

    @ApiModelProperty(value = "赋值类型")
    private AssociationTypeEnum type;

    @ApiModelProperty(value = "公式id", notes = "AssociationTypeEnum为formula时使用此属性")
    private String formula;

    @ApiModelProperty(value = "公式类容", notes = "AssociationTypeEnum为formula时使用此属性")
    private String formulaContent;

    @ApiModelProperty(value = "值", notes = "AssociationTypeEnum为value时使用此属性")
    private Object value;

}
