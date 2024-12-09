package cn.bctools.function.handler;

import cn.bctools.function.entity.dto.ExecDto;

import java.util.Map;

/**
 * 公式执行之后的操作
 *
 * @author bctools.cn
 */
public interface ExpressionAfterHandler {

    /**
     * 处理公式之后的转换
     * 如果存在表格的操作，将表格的返回数据进行优化，较少前端数据渲染的压力
     *
     * @param designId 设计的 id
     * @param init     是否是用户第一次打开数据
     * @param body     触发的条件，表格，路径，数据等
     * @return the map
     */
    Map<String, Object> handler(String designId, Boolean init, ExecDto body);
}
