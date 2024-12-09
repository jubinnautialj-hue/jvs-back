package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentCrud;
import cn.bctools.index.design.render.ComponentCrudRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * The interface Component crud service.
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentCrudService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentCrud, ComponentCrudRender, S> {

}
