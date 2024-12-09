package cn.bctools.index.dto;

import cn.bctools.index.design.enums.JumpMethod;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Jump settings.
 */
@Data
@Accessors(chain = true)
public class JumpSettings {

    /**
     * 跳转名称 key
     */
    private String name;

    /**
     * 跳转地址
     */
    private String targetUrl;

    /**
     * 跳转方式
     */
    private JumpMethod jumpMethod;
}
