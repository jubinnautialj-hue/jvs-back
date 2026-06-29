package cn.bctools.index.design.render.Bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs The type Step card item.
 */
@Data
@Accessors(chain = true)
public class StepCardItem {

    /**
     * 步骤条名称
     */
    String stepName;
    /**
     * 步骤条内容
     */
    List<StepCardContent> stepContentList;
}
