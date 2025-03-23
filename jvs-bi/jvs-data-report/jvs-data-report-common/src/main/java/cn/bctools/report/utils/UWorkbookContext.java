package cn.bctools.report.utils;

import cn.bctools.report.model.univer.style.UStyle;
import cn.hutool.core.map.MapUtil;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

public class UWorkbookContext {

    /**
     * 样式
     */
    private static final ThreadLocal<Map<String, UStyle>> STYLE = TransmittableThreadLocal.withInitial(HashMap::new);

    public static Map<String, UStyle> getStyles(){
        return STYLE.get();
    }

    public static UStyle getStyle(String key){
        return STYLE.get().get(key);
    }

    public static void setStyles(Map<String,UStyle> styles){
        if(MapUtil.isEmpty(styles)){
            return;
        }
        STYLE.set(styles);
    }

    public static void setStyle(String key,UStyle style){
        if(key==null || style==null){
            return;
        }
        STYLE.get().put(key, style);
    }
}
