package cn.bctools.rule.utils.html;


import cn.bctools.rule.entity.enums.NodeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class NodeHtml {

    private String id;
    /**
     * 画布,用于后端计算处理的逻辑
     */
    private String ergodicCanvas;
    private String no;
    private String ico;
    private String top;
    private HtmlData data;
    private String left;
    private String name;
    private String group;
    /**
     * 节点描述
     */
    private String desc;
    private NodeType type;
    @JsonProperty("testData")
    private String testdata;
    /**
     * 所在画布Id
     */
    private String canvasId;
    /**
     * 分组的位置信息
     */
    private List endpoints;

}
