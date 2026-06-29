package cn.bctools.index.design.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs  The enum Jump method.
 */
@Getter
@AllArgsConstructor
public enum JumpMethod {
    /**
     * Blank jump method.
     */
    _blank("新窗口打开"),
    /**
     * Self jump method.
     */
    _self("当前页跳转");

    private final String desc;
}
