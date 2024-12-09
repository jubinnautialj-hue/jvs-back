package cn.bctools.rule.utils.html;

import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
@Data
public class HtmlData {

    private int x;
    private int y;
    private String top;
    private String functionId;
    private Map<String, Object> body;
    private String icon;
    private String left;
    private String name;
    private String size;
    private boolean test;
    @ApiModelProperty("节点类型")
    private String type;
    private String color;
    private String group;
    private String image;
    private String label;
    private String lineList;
    /**
     * 公式
     */
    private String formula;
    /**
     * 公式值
     */
    private String formulaContent;
    private int order;
    private String shape;
    private boolean status;
    private String explain;
    private boolean inspect;
    @ApiModelProperty("返回类型")
    ClassType returnType = ClassType.未识别;
    private List<List> inPoints;
    private boolean draggable;
    private List<List> outPoints;
    private List<HtmlParameters> parameters;
    private String stateImage;
    private String functionName;
    @ApiModelProperty("是否支持自定义结构")
    private Boolean customStructure;
    @ApiModelProperty("自定义结构")
    private List<RuleElementVo> customStructureBody;

}
