package cn.bctools.design.workflow.support.function.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.TransferDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 任务转交运行时参数
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class TransferRuntimeDto {
    /**
     * 操作人
     */
    private UserDto userDto;
    /**
     * 任务
     */
    private FlowTask flowTask;
    /**
     * transfer与userIds二选一
     * 优先按transfer配置转交任务，
     * 若transfer无值，则为userIds集合中的用户按代理配置自动转交
     */
    private TransferDto transfer;
    /**
     * 待自动选择代理人的被代理人id集合
     */
    private List<String> userIds;
}
