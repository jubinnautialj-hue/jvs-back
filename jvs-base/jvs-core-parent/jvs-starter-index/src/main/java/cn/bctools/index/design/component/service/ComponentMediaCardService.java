package cn.bctools.index.design.component.service;

import cn.bctools.index.design.component.ComponentMediaCard;
import cn.bctools.index.design.render.ComponentMediaCardRender;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.FormQueryParamsDto;

import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.Map;

/**
 * 媒体
 *
 * @param <S> the type parameter
 * @author jvs
 */
public interface ComponentMediaCardService<S extends FormQueryParamsBase> extends ComponentBaseService<ComponentMediaCard, ComponentMediaCardRender, S> {

}
