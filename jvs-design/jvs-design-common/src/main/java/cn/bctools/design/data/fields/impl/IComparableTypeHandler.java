package cn.bctools.design.data.fields.impl;

import cn.bctools.design.data.fields.enums.DataQueryType;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件获取: 可比较的组件
 *
 * @Author: GuoZi
 */
public class IComparableTypeHandler {

    /**
     * 获取支持的查询类型
     *
     * @param fieldJson 字段设计数据
     * @return 查询类型集合
     */
    public List<DataQueryType> getEnabledQueryTypes() {
        List<DataQueryType> types = new ArrayList<>();
        types.add(DataQueryType.eq);
        types.add(DataQueryType.gt);
        types.add(DataQueryType.ge);
        types.add(DataQueryType.lt);
        types.add(DataQueryType.le);
        return types;
    }

}
