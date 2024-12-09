package cn.bctools.rule.utils.html;

import cn.bctools.rule.exception.RuleException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("测试结果")
public class RuleExecuteDto implements Serializable {

    @ApiModelProperty("变量执行之后的值")
    Map<String, Object> variableMap = new LinkedHashMap<>();
    Map<String, Object> reqVariableMap = new LinkedHashMap<>();
    @ApiModelProperty("节点的结果,以节点名为key,结果为value,Value为字符串即可")
    Map<String, ResultDto> nodeResult = new LinkedHashMap<>();
    @ApiModelProperty("链接的线的执行状态")
    Set<String> lines = new HashSet<>();
    @ApiModelProperty("最后的结果")
    ResultDto endResult;
    @ApiModelProperty("最后的节点的结果")
    Object messageResult;
    /**
     * 异常信息
     */
    @ApiModelProperty("异常信息")
    RuleException exception;
    @ApiModelProperty("执行错误日志")
    String errorMessage;
    @ApiModelProperty("执行错误节点")
    Set<String> errorNodeId = new HashSet<>();

    public void addErrorNodeId(String errorNodeId) {
        this.errorNodeId.add(errorNodeId);
    }

    @ApiModelProperty("当前正在执行的ID")
    String execNodeId;
    @ApiModelProperty("当前执行的画布")
    String execCanvasId;
    @ApiModelProperty("同步状态下信息状态返回提示")
    String syncMessageTips = "";
    @ApiModelProperty("是成功消息，还是错误消息,默认是成功")
    Boolean stats = true;
    @ApiModelProperty("返回执行日志id")
    String tid;
    HtmlGraph graph;
    @ApiModelProperty("是否结束")
    Boolean isEnd = false;

    public RuleExecuteDto(Map<String, Object> variableMap) {
        this.variableMap = variableMap;
    }

}
