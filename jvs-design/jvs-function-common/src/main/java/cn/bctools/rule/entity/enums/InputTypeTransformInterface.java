package cn.bctools.rule.entity.enums;

import java.util.Map;

/**
 * @author guojing
 * 类型数据值获取
 */
public interface InputTypeTransformInterface {

    /**
     * 获取次组件名称
     *
     * @return
     */
    InputType name();

    /**
     * 转换
     *
     * @param key             字段
     * @param body            原始数据
     * @param data            这个字段的值
     * @param useCase         公式执行场景，偷穿即可  整体测试才会有
     * @param stringObjectMap 执行参数透传  只有正式执行才会有，
     */
    void transform(String key, Map<String, Object> body, Object data, Map<String, Object> stringObjectMap, String useCase);

}
