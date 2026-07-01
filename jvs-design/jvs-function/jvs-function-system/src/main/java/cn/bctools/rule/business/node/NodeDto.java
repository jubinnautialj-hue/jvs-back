package cn.bctools.rule.business.node;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NodeDto extends BindDto {

    @ParameterValue(info = "变量值", necessity = false, type = InputType.input)
    public Object body;

}
