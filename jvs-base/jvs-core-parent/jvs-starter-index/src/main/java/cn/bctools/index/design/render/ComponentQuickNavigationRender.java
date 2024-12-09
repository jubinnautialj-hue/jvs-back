package cn.bctools.index.design.render;

import cn.bctools.index.design.render.Bo.QuickNavigationItem;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs 快捷导航渲染数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentQuickNavigationRender extends OptionsBase {

    /**
     * 快捷导航 - 项
     */
    List<QuickNavigationItem> itemList;

}
