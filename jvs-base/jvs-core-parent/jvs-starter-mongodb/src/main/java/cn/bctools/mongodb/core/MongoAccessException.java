package cn.bctools.mongodb.core;

import lombok.Data;

/**
 * @author xc
 */
@Data
public class MongoAccessException extends RuntimeException {
    public MongoAccessException() {
        super();
    }

    public MongoAccessException(String s) {
        super(s);
    }

    public MongoAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
