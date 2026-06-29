package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentDynamic;
import cn.bctools.index.design.render.ComponentDynamicRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * 步骤条
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentDynamicService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentDynamic, ComponentDynamicRender, S> {

}
