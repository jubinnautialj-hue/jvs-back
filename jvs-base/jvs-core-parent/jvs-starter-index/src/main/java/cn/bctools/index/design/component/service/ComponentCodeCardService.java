package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentCodeCard;
import cn.bctools.index.design.render.ComponentCodeCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * 二维码
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentCodeCardService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentCodeCard, ComponentCodeCardRender, S> {

}
