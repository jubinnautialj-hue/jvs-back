package cn.bctools.index.design.render.Bo;

import cn.bctools.index.design.enums.MediaTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Media item.
 */
@Data
@Accessors(chain = true)
public class MediaItem {

    /**
     * 标题
     */
    String title;

    /**
     * 媒体类型
     */
    MediaTypeEnum mediaType;

    /**
     * 文件地址
     */
    String filePath;

}
