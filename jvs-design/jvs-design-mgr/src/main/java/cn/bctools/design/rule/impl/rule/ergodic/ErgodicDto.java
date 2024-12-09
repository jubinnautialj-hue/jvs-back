package cn.bctools.design.rule.impl.rule.ergodic;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author wl
 */
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErgodicDto {

    @ParameterValue(info = "遍历数据", explain = "需要循环的数据，可用公式赋值", type = InputType.listMap, necessity = false)
    public Object body;

    @ParameterValue(info = "执行方式", explain = "同步：一步一步向下执行。异步阻塞：等待其完成后才能继续下一步", type = InputType.selected, necessity = false, cls = ErgodicAsyncSelected.class)
    public ErgodicAsyncEnum async;

}
