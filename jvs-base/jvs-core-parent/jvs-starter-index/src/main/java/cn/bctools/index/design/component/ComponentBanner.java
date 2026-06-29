package cn.bctools.index.design.component;

import cn.bctools.index.design.ComponentBaseInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Base64;
import java.util.List;

/**
 * banner
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentBanner extends ComponentBaseInfo {

    /**
     * 默认图片
     */
    List<DefaultImage> defaultImages;

    /**
     * The type Default image.
     */
    @Data
    @Accessors(chain = true)
    public static class DefaultImage {

        private String name;

        private String image;
    }
}
