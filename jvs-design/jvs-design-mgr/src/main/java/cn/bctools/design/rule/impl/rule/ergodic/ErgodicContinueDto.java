package cn.bctools.design.rule.impl.rule.ergodic;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author wl
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ErgodicContinueDto extends BindDto {

    @ParameterValue(info = "跳出循环容器", type = InputType.onOff, explain = "当启用后才会生效")
    public Boolean body;

}
