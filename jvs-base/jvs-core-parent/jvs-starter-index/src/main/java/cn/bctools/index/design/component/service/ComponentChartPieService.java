package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentChartPie;
import cn.bctools.index.design.render.ComponentChartPieRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * 饼状图
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentChartPieService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentChartPie, ComponentChartPieRender, S> {

}
