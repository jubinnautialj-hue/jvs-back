package cn.bctools.common.enums;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ErrorMessageSendDingConfig extends SysConfigBase<ErrorMessageSendDingConfig> {
}
