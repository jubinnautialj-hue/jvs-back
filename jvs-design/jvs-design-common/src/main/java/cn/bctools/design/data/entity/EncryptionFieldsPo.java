package cn.bctools.design.data.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
public class EncryptionFieldsPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 加密类型
     */
    String encryptionExpress;
    /**
     * 字段属性
     */
    String fieldKey;
}
