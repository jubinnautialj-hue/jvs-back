package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentProcessManagement;
import cn.bctools.index.design.render.ComponentProcessManagementRender;
import cn.bctools.index.dto.FormQueryParamsBase;

/**
 * @author jvs 流程管理
 *
 * @param <S> the type parameter
 */
public interface ComponentProcessManagementService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentProcessManagement, ComponentProcessManagementRender,S> {

}
