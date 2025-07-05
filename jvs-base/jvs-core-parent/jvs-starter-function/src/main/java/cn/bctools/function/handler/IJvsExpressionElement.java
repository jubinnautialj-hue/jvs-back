package cn.bctools.function.handler;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.function.entity.vo.ElementVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Jvs表达式元素
 *
 * @author guojing
 */
public interface IJvsExpressionElement<T extends ElementVo> {

    String KEY_DESIGN_ID = "function_design_id";
    String KEY_DESIGN_EXTEND_JSON = "function_design_extend_json";
    String KEY_DESIGN_NODE_ID = "function_design_node_id";
    ThreadLocal<Map<String, List<ElementVo>>> local = new ThreadLocal<>();

    /**
     * 获取所有变量,函数
     *
     * @return 函数集合
     */
    List<T> getAllElements();

    default List<ElementVo> localElementVo() {
        String designId = getDesignId();
        if (ObjectNull.isNotNull(designId)) {
            return local.get().getOrDefault(designId, new ArrayList<>());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取表达式计算时的开发套件设计id
     *
     * @return 设计id
     */
    default String getDesignId() {
        return SystemThreadLocal.get(KEY_DESIGN_ID);
    }

    /**
     * 查询节点的id值
     *
     * @return
     */
    default String getNodeId() {
        return SystemThreadLocal.get(KEY_DESIGN_NODE_ID);
    }

    /**
     * 查询扩展信息
     *
     * @return
     */
    default String getExtendJson() {
        return SystemThreadLocal.get(KEY_DESIGN_EXTEND_JSON);
    }

}
