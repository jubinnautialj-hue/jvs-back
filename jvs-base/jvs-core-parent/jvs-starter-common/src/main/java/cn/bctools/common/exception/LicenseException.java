package cn.bctools.common.exception;


import lombok.EqualsAndHashCode;

/**
 * @author guojing
 */
@EqualsAndHashCode(callSuper = true)
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
