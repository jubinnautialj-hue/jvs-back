package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentDay;
import cn.bctools.index.design.render.ComponentDayRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * The interface Component day service.
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentDayService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentDay, ComponentDayRender, S> {
}
