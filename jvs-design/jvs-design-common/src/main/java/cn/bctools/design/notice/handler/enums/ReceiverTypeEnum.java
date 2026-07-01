package cn.bctools.design.notice.handler.enums;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 接收人——人员选择类型
 */
@Getter
@AllArgsConstructor
public enum ReceiverTypeEnum {

    /**
     * 用户
     */
    user(PersonnelTypeEnum.user.getValue()),
    /**
     * 角色
     */
    role(PersonnelTypeEnum.role.getValue()),
    /**
     * 系统字段
     */
    system_field("system_field"),
    /**
     * 模型字段
     */
    model_field("model_field"),
    ;
    private final String value;
}
