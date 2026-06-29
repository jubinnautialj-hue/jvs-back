package cn.bctools.index.design.render;


import cn.bctools.index.dto.JumpSettings;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Component search nav render.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentSearchNavRender extends OptionsBase {

    /**
     * 跳转设置
     */
    private JumpSettings jumpSettings;
}
