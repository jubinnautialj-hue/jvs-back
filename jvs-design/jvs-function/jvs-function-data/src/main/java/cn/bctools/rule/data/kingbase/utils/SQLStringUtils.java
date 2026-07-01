package cn.bctools.rule.data.kingbase.utils;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;

import java.util.Map;

/**
 * 用于处理KingBase语句相关操作
 *
 * @author chenzy
 */
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
                s = StrUtil.replace(s, "${" + o + "}", "'" + param.get(o).toString() + "'");
            }
        }
        return s;
    }


}
