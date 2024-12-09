package cn.bctools.design.notice.handler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 消息通知-接收人支持的系统字段
 */
@Getter
@AllArgsConstructor
public enum ReceiverSystemFieldEnum {
    /**
     * 发起人
     */
    CREATE_BY_ID("createById", "发起人"),
    /**
     * 已审批人
     */
    APPROVED("approved", "已审批人"),
    /**
     * 待审批人
     */
    PENDING_APPROVAL("pendingApproval", "待审批人"),
    ;

    @JsonValue
    private final String value;
    private final String desc;

    public static List<Map<String, String>> getMap() {
        return Arrays.stream(ReceiverSystemFieldEnum.values()).map(v -> {
            Map<String, String> map = new HashMap<>(2);
            map.put("id", v.getValue());
            map.put("name", v.getDesc());
            return map;
        }).collect(Collectors.toList());
    }
}
