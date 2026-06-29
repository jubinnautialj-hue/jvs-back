package cn.bctools.index.design.render;

import cn.bctools.index.design.render.Bo.StepCardItem;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs 步骤条渲染数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentStepCardRender extends OptionsBase {

    /**
     * 步骤
     */
    List<StepCardItem> stepList;

}
