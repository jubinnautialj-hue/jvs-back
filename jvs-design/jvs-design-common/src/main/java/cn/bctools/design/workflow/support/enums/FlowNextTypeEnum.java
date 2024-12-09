package cn.bctools.design.workflow.support.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 流转类型。控制后续节点的流转类型
 */
@Getter
@AllArgsConstructor
public enum FlowNextTypeEnum {

    /**
     * 终止，流程结束
     */
    END("end", "终止"),
    /**
     * 当前节点处理完成，任务流转到下一节点
     */
    NEXT("next", "流转下一节点"),
    BACK("back", "回退"),
    /**
     * 多人审批，未结束审批
     */
    PENDING("pending", "审批中"),

    /**
     * 流转进度暂停在当前节点。（不继续流转、不结束任务）
     */
    HANG("hang", "挂起"),
    ;

    @JsonValue
    private String value;
    private String desc;
}
