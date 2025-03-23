package org.jim.core.exception;

/**
 * @ClassName ImDecodeException
 * @Description TODO
 * @Author WChao
* @Version 1.0
 **/
public class ImDecodeException extends ImException {

    public ImDecodeException(Throwable e) {
        super(e);
    }

    public ImDecodeException(String message) {
        super(message);

    }
}
