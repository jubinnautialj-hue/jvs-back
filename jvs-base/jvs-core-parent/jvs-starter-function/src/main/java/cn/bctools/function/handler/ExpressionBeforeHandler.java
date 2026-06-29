package cn.bctools.function.handler;

import cn.bctools.function.entity.dto.ExecDto;

/**
 * @author jvs
 */
public interface ExpressionBeforeHandler {

    /**
     * 处理公式之前的转换
     * 如果存在表格的操作，将表格的返回数据进行优化，较少前端数据渲染的压力
     *
     * @param designId 设计的 id
     * @param useCase  场景
     * @param init     是否是用户第一次打开数据
     * @param body     触发的条件，表格，路径，数据等
     * @return the map
     */
    ExecDto handler(String designId, String useCase, Boolean init, ExecDto body);

}
