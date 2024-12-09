package cn.bctools.index.design.component.service;


import cn.bctools.index.design.component.ComponentOaTask;
import cn.bctools.index.design.render.ComponentOaTaskRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs The interface Component oa task service.
 *
 * @param <S> the type parameter
 */
public interface ComponentOaTaskService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentOaTask, ComponentOaTaskRender,S> {

}
