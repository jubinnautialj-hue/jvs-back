package cn.bctools.index.design.component.service;


import cn.bctools.index.design.component.ComponentNotice;
import cn.bctools.index.design.render.ComponentNoticeRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs The interface Component notice service.
 *
 * @param <S> the type parameter
 */
public interface ComponentNoticeService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentNotice, ComponentNoticeRender,S> {

}
