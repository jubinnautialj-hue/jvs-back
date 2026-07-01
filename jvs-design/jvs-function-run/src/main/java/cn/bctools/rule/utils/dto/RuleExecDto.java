package cn.bctools.rule.utils.dto;


import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 运行中的节点
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class RuleExecDto {
    /**
     * The Graph.
     */
    HtmlGraph graph;
    /**
     * The Secret.
     */
    String secret;

    /**
     * 是否开启日志记录
     * The Open log recording.
     */
    Boolean openLogRecording;
    /**
     * The Tid.
     */
    String tid;
    /**
     * The Path.
     */
    String path = "";
    /**
     * The Type.
     */
    RunType type;
    /**
     * The Execute dto.
     */
    RuleExecuteDto executeDto;
}
