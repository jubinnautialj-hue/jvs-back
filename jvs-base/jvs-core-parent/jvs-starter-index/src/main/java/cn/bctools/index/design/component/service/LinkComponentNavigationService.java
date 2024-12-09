package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.LinkComponentNavigation;
import cn.bctools.index.design.render.LinkComponentNavigationRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs 链接导航
 *
 * @param <S> the type parameter
 */
public interface LinkComponentNavigationService<S extends FormQueryParamsBase> extends ComponentBaseService<LinkComponentNavigation, LinkComponentNavigationRender,S> {

}
