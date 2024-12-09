package cn.bctools.design.data.fields.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.enums.DataQueryType;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件获取: 允许多选的组件
 * <p>
 * 单个: eq
 * 多个: in
 *
 * @Author: GuoZi
 */
public class IMultipleTypeHandler {

    /**
     * 获取支持的查询类型
     *
     * @param fieldJson 字段设计数据
     * @return 查询类型集合
     */
    public List<DataQueryType> getEnabledQueryTypes(MultipleHtml fieldJson) {
        List<DataQueryType> enabledQueryTypes = new ArrayList<>();
        enabledQueryTypes.add(DataQueryType.in);
        enabledQueryTypes.add(DataQueryType.allin);
        enabledQueryTypes.add(DataQueryType.notIn);
        enabledQueryTypes.add(DataQueryType.isNull);
        return enabledQueryTypes;
    }


    /**
     * 根据value获取label 的值，使用于逻辑引擎返回的数据，需要进行全量匹配
     *
     * @param value
     * @param list
     * @param showPath
     * @return
     */
    public static String getLabel(String value, List<FormValueHtml> list, boolean showPath) {
        for (FormValueHtml tree : list) {
            if (tree.getValue().equals(value)) {
                return tree.getLabel();
            }
            if (ObjectNull.isNotNull(tree.getChildren())) {
                List<FormValueHtml> copys = BeanCopyUtil.copys(tree.getChildren(), FormValueHtml.class);
                if (showPath) {
                    String label = getLabel(value, copys, showPath);
                    //如果为空的时候，处理下一次匹配关系
                    if(ObjectNull.isNull(label)){
                        continue;
                    }
                    return tree.getLabel() + "/" + label;
                } else {
                    return getLabel(value, copys, showPath);
                }
            }
        }
        return "";
    }

    public static String getValue(String label, List<FormValueHtml> list) {
        for (FormValueHtml tree : list) {
            if (tree.getLabel().equals(label)) {
                return tree.getLabel();
            }
            if (ObjectNull.isNotNull(tree.getChildren())) {
                List<FormValueHtml> copys = BeanCopyUtil.copys(tree.getChildren(), FormValueHtml.class);
                return getValue(label, copys);
            }
        }
        return "";
    }
}
