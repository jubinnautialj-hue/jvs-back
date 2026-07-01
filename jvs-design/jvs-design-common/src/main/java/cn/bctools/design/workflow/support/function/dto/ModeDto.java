package cn.bctools.design.workflow.support.function.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 审批
 */
@Data
@Accessors(chain = true)
public class ModeDto {

    /**
     * 结束审批。 TRUE-是，FALSE-否
     */
    private Boolean end;

    /**
     * 审批通过。 TRUE-通过，FALSE-不通过
     */
    private Boolean pass;

    /**
     * 通过率
     */
    private Long passRate = 0L;
}
