package cn.bctools.index.design.render;

import cn.bctools.index.design.enums.JumpMethod;
import cn.bctools.index.dto.JumpSettings;
import cn.bctools.index.dto.OptionsBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Component quick card render.
 *
 * @author jvs 快捷卡片渲染数据
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentQuickCardRender extends OptionsBase {

    /**
     * 显示名称
     */
    private String label;

    /**
     * 显示项
     */
    private List<Item> items;


    /**
     * The type Item.
     */
    @Data
    @Accessors(chain = true)
    public static class Item{

        /**
         * 显示名称
         */
        private String label;

        /**
         * 跳转方式
         */
        private JumpSettings jumpSettings;
    }
}
