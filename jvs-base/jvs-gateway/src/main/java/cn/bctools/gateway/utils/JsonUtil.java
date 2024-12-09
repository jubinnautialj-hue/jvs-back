package cn.bctools.gateway.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * JSON处理工具类
 *
 * @author lieber
 */
public enum JsonUtil {

    /**
     * 实例
     */
    INSTANCE;

    /**
     * json对象字符串开始标记
     */
    private final static String JSON_OBJECT_START = "{";

    /**
     * json对象字符串结束标记
     */
    private final static String JSON_OBJECT_END = "}";

    /**
     * json数组字符串开始标记
     */
    private final static String JSON_ARRAY_START = "[";

    /**
     * json数组字符串结束标记
     */
    private final static String JSON_ARRAY_END = "]";


    /**
     * 判断字符串是否json对象字符串
     *
     * @param val 字符串
     * @return true/false
     */
    public boolean isJsonObj(String val) {
        if (val.isEmpty()) {
            return false;
        }
        val = val.trim();
        if (val.startsWith(JSON_OBJECT_START) && val.endsWith(JSON_OBJECT_END)) {
            try {
                JSONObject.parseObject(val);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否json数组字符串
     *
     * @param val 字符串
     * @return true/false
     */
    public boolean isJsonArr(String val) {
        if (val.isEmpty()) {
            return false;
        }
        val = val.trim();
        if (val.isEmpty()) {
            return false;
        }
        val = val.trim();
        if (val.startsWith(JSON_ARRAY_START) && val.endsWith(JSON_ARRAY_END)) {
            try {
                JSONArray.parseArray(val);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
