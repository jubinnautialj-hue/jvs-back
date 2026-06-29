package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentQuickNavigation;
import cn.bctools.index.design.render.ComponentQuickNavigationRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * @author jvs 快捷卡片
 *
 * @param <S> the type parameter
 */
public interface ComponentQuickNavigationService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentQuickNavigation, ComponentQuickNavigationRender,S> {

}
