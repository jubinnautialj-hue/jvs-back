package cn.bctools.index.design.render.Bo;

import cn.bctools.index.design.enums.ExpandDirectionEnum;
import cn.bctools.index.dto.JumpSettings;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Step card content.
 */
@Data
@Accessors(chain = true)
public class StepCardContent {

    /**
     * 图片
     */
    String image;

    /**
     * 标题
     */
    String title;

    /**
     * 描述
     */
    String description;

    /**
     * 跳转方式和跳转地址
     */
    JumpSettings jumpSettings;

    /**
     * 展示格式
     */
    ExpandDirectionEnum expandDirection;
}
