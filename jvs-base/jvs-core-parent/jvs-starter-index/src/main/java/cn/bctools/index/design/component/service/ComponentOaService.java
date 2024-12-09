package cn.bctools.index.design.component.service;


import cn.bctools.index.design.component.ComponentOa;
import cn.bctools.index.design.component.ComponentOaTask;
import cn.bctools.index.design.render.ComponentOaRender;
import cn.bctools.index.design.render.ComponentOaTaskRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs The interface Component oa service.
 *
 * @param <S> the type parameter
 */
public interface ComponentOaService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentOa, ComponentOaRender,S> {

}
