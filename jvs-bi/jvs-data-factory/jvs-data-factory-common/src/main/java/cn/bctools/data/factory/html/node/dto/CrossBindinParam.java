package cn.bctools.data.factory.html.node.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class CrossBindinParam  implements Serializable {

    /**
     * 连接方式 join内连接 left左连接 right右连接
     */
    private String connection;
    /**
     * 字段
     */
    private List<Object> connectionFields;
    /**
     * 是否合并字段
     */
    private String isMerge;
}
