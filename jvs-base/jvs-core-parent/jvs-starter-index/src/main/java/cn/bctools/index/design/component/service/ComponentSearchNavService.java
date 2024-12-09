package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentSearchNav;
import cn.bctools.index.design.render.ComponentSearchNavRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * @author jvs The interface Component search nav service.
 *
 * @param <S> the type parameter
 */
public interface ComponentSearchNavService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentSearchNav, ComponentSearchNavRender,S> {

}
