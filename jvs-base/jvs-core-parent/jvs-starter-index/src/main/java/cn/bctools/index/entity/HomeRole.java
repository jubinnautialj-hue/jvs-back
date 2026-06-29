package cn.bctools.index.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs The type Home role.
 */
@Data
@Accessors(chain = true)
public class HomeRole implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    String id;
    /**
     * 名称
     */
    String name;
    /**
     * 类型
     */
    String type;
}
