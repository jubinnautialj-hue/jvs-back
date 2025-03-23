package cn.bctools.data.factory.html.node.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ConnectionField implements Serializable {
    /**
     * 左侧字段key
     */
    private String leftFieldKey;
    /**
     * 右侧字段key
     */
    private String rightFieldKey;
}
