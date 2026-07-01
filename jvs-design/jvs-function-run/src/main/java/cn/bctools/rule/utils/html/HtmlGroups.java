package cn.bctools.rule.utils.html;

import lombok.Data;

import java.util.List;

/**
 * 节点信息是否正常
 */
@Data
public class HtmlGroups {

    /**
     * 描述信息
     */
    private String id;
    private String top;
    private String desc;
    private String left;
    private String label;
    private int height;
    private int width;
    /**
     * 这个组的节点信息
     */
    private List<String> nodes;

}
