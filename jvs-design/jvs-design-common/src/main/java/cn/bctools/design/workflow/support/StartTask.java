package cn.bctools.design.workflow.support;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 启动流程入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class StartTask extends BaseTask {

}
