package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentChartLine;
import cn.bctools.index.design.render.ComponentChartLineRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import java.util.List;
import java.util.Map;

/**
 * 折线图
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentChartLineService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentChartLine, ComponentChartLineRender, S> {

}
