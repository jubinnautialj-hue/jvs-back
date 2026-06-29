package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentCard;
import cn.bctools.index.design.render.ComponentCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * 卡片
 *
 * @author jvs
 */
public interface ComponentCardService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentCard, ComponentCardRender, S> {

}
