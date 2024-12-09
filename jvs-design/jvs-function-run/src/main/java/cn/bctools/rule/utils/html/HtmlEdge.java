package cn.bctools.rule.utils.html;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 线上的操作
 *
 * @author administer
 */
@Data
@SuppressWarnings("unused")
public class HtmlEdge {

    /**
     * 节点ID
     */
    private String id;
    /**
     * 目标节点
     */
    private String to;
    /**
     * 原节点
     */
    private String from;
    /**
     * 表达式
     */
    private String label;
    /**
     * 公式ID
     */
    private String lineList;
    /**
     * 公式
     */
    private String formula;
    /**
     * 公式值
     */
    private String formulaContent;
    /**
     * 公式 给默认值
     */
    private Integer sort = 1;
    /**
     * 线的分类为三种分为 普通线、异步线、异常线
     * 普通线：可以设置执行顺序，支持在线上设置公式，如果公式执行返回为真时才继续向下执行，可切换为异步线和异常线
     * 异常线：当前节点执行出错后，将会执行异常线下的节点，异常线上不执行公式。异常线后续链接线也会依次执行。
     * 异步线：
     * 情况1.同一个节点下多条线需要同时执行时使用异步线，多条异步线不分前后顺序，同时执行后，不需要获取所有异步执行的结果，根据连线规则向后执行，直到寻找到最后一个节点停止执行。
     *    如： 根据用户手机号、邮寄、内站信。分别发送消息通知。不关心是否发送成功。直接将发送信息分别入库。
     * 情况2.同一个节点下多条线需要同时执行时使用异步线，多条异步线不分前后顺序，同时执行后，需要获取所有异步执行的结果。配合[同步线聚合]节点进行汇聚所有异步线的后续节点。再继续向下执行。直到寻找到最后一个节点停止执行。
     *    如： 获取某一企业天眼查数据信息，获取同盾数据信息、获取规则数据信息，对所有获取的信息进行数据聚合进行入库
     */
    private String state;
    /**
     * 连线的断点
     */
    private List breakPoints = new ArrayList();
    /**
     * 描述
     */
    private String desc;

}
