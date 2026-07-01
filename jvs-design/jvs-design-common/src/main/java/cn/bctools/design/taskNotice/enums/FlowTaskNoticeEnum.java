package cn.bctools.design.taskNotice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wayne
 * @since 2025.09.10
 */
@Getter
@AllArgsConstructor
public enum FlowTaskNoticeEnum {
    /**
     * 待处理
     * 待办提醒初始状态
     */
    CREATE,
    /**
     * 已关闭
     * 处理完成
     */
    CLOSE,
    /**
     * 撤回
     * 撤回流程
     */
    RECALL,
    /**
     * 更新
     * 更新流程
     */
    UPDATE,
}
