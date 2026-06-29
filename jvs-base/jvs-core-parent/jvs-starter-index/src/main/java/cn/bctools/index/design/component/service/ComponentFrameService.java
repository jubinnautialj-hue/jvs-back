package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentFrame;
import cn.bctools.index.design.render.ComponentFrameRender;
import cn.bctools.index.dto.FormQueryParamsBase;


/**
 * The interface Component frame service.
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentFrameService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentFrame, ComponentFrameRender, S> {
}
