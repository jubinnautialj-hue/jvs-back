package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentBanner;
import cn.bctools.index.design.render.ComponentBannerRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * banner
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentBannerService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentBanner, ComponentBannerRender, S> {
}
