package cn.bctools.rule.business.job;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author st
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class JobDto {

    @ParameterValue(info = "用户对象", necessity = false, type = InputType.job, explain = "选择岗位")
    public String job;

}
