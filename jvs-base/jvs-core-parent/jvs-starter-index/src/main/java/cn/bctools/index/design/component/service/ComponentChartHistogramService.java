package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentChartHistogram;
import cn.bctools.index.design.render.ComponentChartHistogramRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * 柱状图
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentChartHistogramService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentChartHistogram, ComponentChartHistogramRender, S> {

}
