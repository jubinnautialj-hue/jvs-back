package cn.bctools.index.design.render;

import cn.bctools.index.design.render.Bo.MediaItem;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Component media card render.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentMediaCardRender extends OptionsBase {

    /**
     * 媒体类
     */
    List<MediaItem> mediaItemList;

}
