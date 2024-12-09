package cn.bctools.index.design.component.service;


import cn.bctools.index.design.component.ComponentMessage;
import cn.bctools.index.design.render.ComponentMessageRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @param <S> the type parameter
 * @author jvs
 * The interface Component message service.
 */
public interface ComponentMessageService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentMessage, ComponentMessageRender, S> {

}
