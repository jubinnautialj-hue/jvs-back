package cn.bctools.common.utils;

import com.alibaba.fastjson2.JSONPath;
import com.jayway.jsonpath.JsonPath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jvs 不直接使用 fastjson2的 jsonpath 中文导致失效，也不直接使用jayway,需要异常捕获返回默认值 null
 */
public class JvsJsonPath {

    /**
     * Read object.
     *
     * @param jsonObj the json obj
     * @param path    the path
     * @return the object
     */
    public static Object read(Object jsonObj, String path) {
        try {
            boolean b = containsChinese(path);
            if (b) {
                //判断 Path是否有中文
                Object read = JsonPath.read(jsonObj, path);
                return read;
            } else {
                if(jsonObj instanceof String){
                    return JSONPath.eval(jsonObj.toString(), path);
                }
                return JSONPath.eval(jsonObj, path);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Contains chinese boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean containsChinese(String str) {
        String regex = "[\u4e00-\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * Set.
     *
     * @param rootObject the root object
     * @param path       the path
     * @param value      the value
     */
    public static void set(Object rootObject, String path, Object value) {
        JSONPath.set(rootObject, path, value);
    }

}
