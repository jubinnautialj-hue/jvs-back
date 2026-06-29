package cn.bctools.index.design.component;

import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.dto.JumpSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 数据卡片
 * 左侧展示图标
 * 右侧展示数据
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ComponentDataCard extends ComponentBaseInfo {

}
