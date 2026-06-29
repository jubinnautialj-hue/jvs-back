package cn.bctools.index.dto;

import cn.bctools.index.design.enums.JumpMethod;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs 组件渲染基础数据
 */
@Data
@Accessors(chain = true)
public class OptionsBase {
    /**
     * 名称
     */
    String name;

    /**
     * 描述
     */
    String description;

    /**
     * 跳转地址
     */
    String targetUrl;

    /**
     * 跳转方式
     */
    JumpMethod jumpMethod;
}
