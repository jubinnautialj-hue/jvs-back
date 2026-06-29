package cn.bctools.index.design.render;

import cn.bctools.index.design.enums.NavigationType;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 * 导航栏渲染数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentNavigationRender extends OptionsBase {

    private NavigationType navigationType;
}
