package cn.bctools.rule;

import cn.bctools.common.exception.BusinessException;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author gx
 * 展示逻辑运行时的错误信息
 */
@Getter
public class RunParameterException extends BusinessException implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务错误code
     */
    private int code = -1;
    private String message;
    private Map data;

    public RunParameterException(String message, Map data) {
        super(message);
        this.message = message;
        this.data = data;
    }

}
