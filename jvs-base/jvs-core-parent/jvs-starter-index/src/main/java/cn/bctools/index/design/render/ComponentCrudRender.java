package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Component crud render.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentCrudRender extends OptionsBase {
    Object data;
    List<Object> pages;
}
