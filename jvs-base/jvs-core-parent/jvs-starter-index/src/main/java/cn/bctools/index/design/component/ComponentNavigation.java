package cn.bctools.index.design.component;

import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.design.enums.NavigationType;
import cn.bctools.index.dto.JumpSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 * 导航栏
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentNavigation extends ComponentBaseInfo {

    /**
     * 导航栏类型
     */
    NavigationType navigationType;

    /**
     * 跳转方式 和 跳转地址
     */
    JumpSettings jumpSettings;
}
