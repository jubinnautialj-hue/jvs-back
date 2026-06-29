package cn.bctools.index.design.component.service;


import cn.bctools.index.design.component.ComponentProjectNavigation;
import cn.bctools.index.design.render.ComponentProjectNavigationRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs The interface Component project navigation service.
 *
 * @param <S> the type parameter
 */
public interface ComponentProjectNavigationService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentProjectNavigation, ComponentProjectNavigationRender,S> {

}
