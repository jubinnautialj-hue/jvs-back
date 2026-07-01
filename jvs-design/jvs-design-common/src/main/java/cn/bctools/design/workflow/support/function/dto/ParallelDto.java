package cn.bctools.design.workflow.support.function.dto;

import cn.bctools.design.workflow.support.FlowResult;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class ParallelDto {

    private FlowResult flowResult;
    private BackResubmitDto backResubmitDto;
}
