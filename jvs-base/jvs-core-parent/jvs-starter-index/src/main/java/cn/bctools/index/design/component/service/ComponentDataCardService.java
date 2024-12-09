package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentDataCard;
import cn.bctools.index.design.render.ComponentDataCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;


/**
 * 卡片
 *
 * @author jvs
 */
public interface ComponentDataCardService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentDataCard, ComponentDataCardRender, S> {

}
