package cn.bctools.design.data.source.impl.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 用以构建更新字段
 */
public class DynamicUpdate {
    private Map<String, Object> updateMap = new HashMap<>();

    public DynamicUpdate set(String key, Object value) {
        updateMap.put(key, value);
        return this;
    }

    public Map<String, Object> getUpdateData() {
        return this.updateMap;
    }


}
