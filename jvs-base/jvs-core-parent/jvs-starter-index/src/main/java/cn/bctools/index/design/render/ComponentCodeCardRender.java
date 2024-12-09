package cn.bctools.index.design.render;

import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * The type Component code card render.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentCodeCardRender extends OptionsBase {

    /**
     * 电话号码
     */
    String phone;

    /**
     * 文件地址 链接
     */
    String filePath;
}
