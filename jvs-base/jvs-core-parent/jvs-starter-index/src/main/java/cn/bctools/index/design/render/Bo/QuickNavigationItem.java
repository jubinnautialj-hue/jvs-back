package cn.bctools.index.design.render.Bo;

import cn.bctools.index.dto.JumpSettings;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Quick navigation item.
 */
@Data
@Accessors(chain = true)
public class QuickNavigationItem {

    /**
     * 图片
     */
    String image;

    /**
     * 描述
     */
    String description;

    /**
     * 跳转方式
     */
    JumpSettings jumpSettings;
}
