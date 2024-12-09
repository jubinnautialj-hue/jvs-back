package cn.bctools.rule.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.common.RuleElementVo;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wl
 */
public class RuleElementUtils {

    public static List<RuleElementVo> get(Map<String, Object> map) {
        if (ObjectNull.isNull(map)) {
            return null;
        }
        return map.keySet().stream()
                .map(e -> {
                    JvsParamType jvsParamType = JvsParamType.any;
                    RuleElementVo ruleElementVo = new RuleElementVo();
                    Object o = map.get(e);
                    if (o instanceof String) {
                        jvsParamType = JvsParamType.text;
                    }
                    if (o instanceof Number) {
                        jvsParamType = JvsParamType.number;
                    }
                    if (o instanceof Date) {
                        jvsParamType = JvsParamType.date;
                    }
                    if (o instanceof Boolean) {
                        jvsParamType = JvsParamType.bool;
                    }
                    if (o instanceof List) {
                        jvsParamType = JvsParamType.array;
                        if (ObjectNull.isNotNull(o)) {
                            //解析下级
                            Object o1 = ((List<?>) o).get(0);
                            //如果下级是对象才解析如果不是不解析
                            if (o1 instanceof Map) {
                                ruleElementVo.setChildren(get((Map<String, Object>) o1));
                            }
                        }
                    }
                    if (o instanceof Map) {
                        jvsParamType = JvsParamType.object;
                        //解析下级
                        ruleElementVo.setChildren(get((Map<String, Object>) o));
                    }
                    ruleElementVo.setName(e).setInfo(e).setJvsParamType(jvsParamType).setJvsParamTypeName(jvsParamType.getDesc());
                    return ruleElementVo;
                })
                .collect(Collectors.toList());
    }

    public static List<RuleElementVo> get(String map) {
        Map hashMap;
        if (JSONUtil.isTypeJSONArray(map)) {
            JSONArray objects = JSONArray.parseArray(map);
            if (ObjectNull.isNull(objects)) {
                return new ArrayList<>();
            }
            Object obj = objects.get(0);
            if (obj instanceof Map) {
                hashMap = (Map) obj;
            } else {
                return new ArrayList<>();
            }
        } else if (JSONUtil.isTypeJSONObject(map)) {
            hashMap = JSONUtil.parseObj(map);
        } else {
            return new ArrayList<>();
        }
        return RuleElementUtils.get(hashMap);
    }

}
