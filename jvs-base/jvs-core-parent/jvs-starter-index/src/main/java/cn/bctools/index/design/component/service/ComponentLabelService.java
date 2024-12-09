package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentLabel;
import cn.bctools.index.design.render.ComponentLabelRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * The interface Component label service.
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentLabelService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentLabel, ComponentLabelRender, S> {

}
