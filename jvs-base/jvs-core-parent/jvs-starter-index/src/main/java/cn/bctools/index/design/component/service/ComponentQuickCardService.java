package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentQuickCard;
import cn.bctools.index.design.render.ComponentQuickCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * @author jvs 卡片
 *
 * @param <S> the type parameter
 */
public interface ComponentQuickCardService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentQuickCard,ComponentQuickCardRender,S> {

}
