package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 数据卡片渲染数据
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentDataCardRender extends OptionsBase {

    /**
     * 选项名称
     */
    private String label;

    /**
     * 展示数据
     */
    private BigDecimal data;
}
