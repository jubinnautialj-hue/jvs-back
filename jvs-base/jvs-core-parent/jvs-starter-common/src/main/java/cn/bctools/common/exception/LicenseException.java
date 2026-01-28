package cn.bctools.common.exception;

import lombok.Data;

/**
 * @author guojing
 */
@Data
public class LicenseException extends BusinessException {

    /**
     * 内部使用的异常处理
     *
     * @param message 错误信息
     */
    public LicenseException(String message) {
        super(message, 20000);
    }

}
