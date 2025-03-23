package cn.bctools.data.factory.html;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LineHtml {

    /**
     * 节点ID
     */
    private String id;
    /**
     *
     * 原节点
     */
    private String sourceNode;
    /**
     * 目标节点
     */
    private String targetNode;

}
