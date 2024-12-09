package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 审批人为空时处理
 */
@Getter
@AllArgsConstructor
public enum NodePropertiesUserEmptyEnum {

    /**
     * 审批人为空时处理
     */
    TO_ADMIN("TO_ADMIN", "自动转交管理员"),
    /**
     * @deprecated 已弃用，为了兼容旧数据的设计暂时不能删除
     */
    TO_PASS("TO_PASS", "自动通过"),
    ;

    private String value;
    private String desc;

    public static NodePropertiesUserEmptyEnum getByValue(String value) {
        for (NodePropertiesUserEmptyEnum currentEnum : NodePropertiesUserEmptyEnum.values()) {
            if (currentEnum.value.equals(value)) {
                return currentEnum;
            }
        }
        return null;
    }
}
