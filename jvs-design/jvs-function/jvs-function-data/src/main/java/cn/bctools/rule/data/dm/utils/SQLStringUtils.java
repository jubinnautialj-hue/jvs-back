package cn.bctools.rule.data.dm.utils;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * 用于处理DM语句相关操作
 *
 * @author chenzy
 */
@Slf4j
public class SQLStringUtils {


    /**
     * 替换sql语句站位符
     *
     * @param
     * @return
     */
    public static String replace(String sql, Map param) {
        if(CollectionUtil.isEmpty(param)){
            return sql;
        }
        String s = sql;

        for (Object o : param.keySet()) {

            //判断如果是数组
            if (param.get(o) instanceof JSONArray) {
                String s1 = param.get(o).toString();
                s1 = StrUtil.replaceChars(s1, "[", "(");
                s1 = StrUtil.replaceChars(s1, "]", ")");
                s1 = StrUtil.replaceChars(s1, "\"", "'");
                s = StrUtil.replace(s, "${" + o + "}", s1);
            } else {
                String s1 = Optional.ofNullable(param.get(o)).map(Object::toString).orElseGet(() -> {
                    log.error("参数：{} 为null,转为空字符串",o);
                    return StringPool.EMPTY;
                });
                s = StrUtil.replace(s, "${" + o + "}", "'" + s1 + "'");
            }
        }
        return s;
    }


}
