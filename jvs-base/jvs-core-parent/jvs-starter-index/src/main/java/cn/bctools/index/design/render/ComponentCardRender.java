package cn.bctools.index.design.render;

import cn.bctools.index.dto.JumpSettings;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 通用卡片渲染数据
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentCardRender extends OptionsBase {

    /**
     * 显示名称
     */
    private String label;

    /**
     * 跳转设置
     */
    private JumpSettings jumpSettings;
}
